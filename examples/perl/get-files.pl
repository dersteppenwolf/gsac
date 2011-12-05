#!/usr/bin/perl

#
# GET-FILES.PL - Call GSAC REST services to compare/get files. It will ouput 
# two or three lines per file. E.g.:
#
# R:ftp://garner......   The remote file information.
# G:            ......   The retrieved file (through ftp get).
# L:            ......   The local file.
#
# It builds a query URL then uses the Perl LWP module to GET a CSV file of 
# the query results.  Each line of the CSV file represents a file on the 
# remote archive. If the file exists locally, in a directory structure that
# mirrors the remote, then the size, md5 checksum and modification time are
# printed.  If it does not exist locally, the file is retrieved then the
# comparison is made.
# 
# Currently, it does not report differences or attempt to retrieve a file
# whose size, etc., do not match.
#
# Copyright 2011, Reagents of the University of California
#
# $Id: get-files.pl 296 2011-11-15 00:00:16Z hankr $


use strict;
use LWP::UserAgent;
use Digest::MD5 qw(md5);
use Time::Local;

my $fdir = './gsacfiles';
my $host = 'http://swave.ucsd.edu:8080';
my $from = '2011-11-01';
my $to   = '2011-11-02';
my $net  = 'BARD';
my $type = 'gnss.data.rinex.observation';
my $surl = "$host/gsacws/gsacapi/file/search/files.csv?output=file.csv&file.datadate.from=$from&file.datadate.to=$to&site.group=$net&file.type=$type";

my $content;

print "GET-FILES.PL -- Call REST service and get files from GSAC archive\n" ;

# Make the directory if it does not exist.
unless (-e $fdir) {
  print "Creating directory, $fdir.\n";
  mkdir( $fdir );
}

# Die if a file exists with the the same name.
unless ( -d $fdir ) {
  die "Error: Cannot create directory, $fdir, file exists.\n";
}

# Die if we cannot write to the directory.
unless (-w $fdir ) {
  die "Error: Cannot write to directory, $fdir\n";
} 

# Note: We know the file structure of the archive.  It is organized
# by Julian day.  Other archives may require a different algorithm.

# We assume the Unix program is available.
my $year = `date -d $from +%Y`;
chomp( $year );
my $jfrom = `date -d $from +%j`;
chomp( $jfrom );
my $jto   = `date -d $to +%j`;
chomp( $jto );

unless ( -d "$fdir/$year" ) {
  #print( "$fdir/$year\n" );
  mkdir( "$fdir/$year" );
}

# Insure all the local directories exist.
for ( my $jday = $jfrom; $jday <= $jto; $jday++ ) {
  unless ( -d "$fdir/$year/$jday" ) {
    mkdir( "$fdir/$year/$jday" ); 
  }
}

# Create a UserAgent to make the service request.
my $ua = new LWP::UserAgent;
# $ua->timeout(3);        # 3 sec timeout is default
$ua->agent("Perl/LWP"); 

print "GET: $surl\n";

my $req = HTTP::Request->new( GET => $surl );
my $res = $ua->request($req);

if ($res->is_success) {
  $content= $res->content;
  #print( $content );
}
else {
  die "Error: No response from repository.\n";
} 

# Create an array of each line
my @lines = split( '\n', $content );

foreach ( @lines ) {
  #print( "$_\n" );
  # Ingore comment lines.
  if ( /^#/ ) { next; }
  my ($id, $type, $md5, $size, $date, $url)  = split( ',', $_ );
  #print( join( ':', ($date, $size, $md5, $url, "\n")) );

  # Note: some values may have leading/trailing witespace
  #$url =~ s/^\s+//; 
  $url =~ s/\s+$//; 

  printf( "R:$url  Size: %8u  Md5: $md5  Mod: $date\n", $size );

  # Extract the filename, assuming organized by year/jday/file
  my @url = split( '/', $url );
  my $fname = pop(@url);
  my $jday  = pop(@url);
  my $year  = pop(@url);
  my $lfile = join( '/', $fdir,$year,$jday,$fname);

  # Get the file if it does not exist locally.
  unless ( -f $lfile ) {
    # This script is meant to demonstrate GSAC functions, not best practices.
    GET:
    print( "G:$url  " );
    my $ftpq = HTTP::Request->new( GET => $url );
    $ftpq->authorization_basic('anonymous','gsacws\@ucsd.edu');
    my $fres = $ua->request($ftpq);
    if ($fres->is_success) {
       my $content = $fres->content;
       my $clen = length( $content );
       printf( "Size: %8u  ", $clen );
       print( "Md5: $md5  " );
       print( "Mod: $date\n" );
       open( LFILE, ">$lfile" ) or print( "Error: Unable to create $lfile \n");
       binmode( LFILE );
       print( LFILE $content );
       close( LFILE );
       # Set the modification time
       my ($yr,$mo,$dy) = split( '-', $date );
       my $mod_time = timelocal( 0, 0, 0, $dy, $mo-1, $yr );
       utime( $mod_time, $mod_time, $lfile );
    } else {
      # Note: Repeated failures will loop.
      print( "Failed.\n" );
    }
  }

  # Check the file
  printf( "L: %54s  ", $lfile );
  my $lsize = (stat($lfile))[7];
  my @lmod  = localtime( (stat($lfile))[9] );
  unless ( open( LFILE, "$lfile" ) ) { 
    print( "Unable to open ...\n\n" );  
    next;
  }
  my $lmd5 = Digest::MD5->new;
  while (<LFILE>) {
    $lmd5->add($_);
  }
  close(FILE);
  printf( "Size: %8u  ", $lsize );
  #my $cmd5 = md5($content);
  my $cmd5 = $lmd5->hexdigest;
  print( "Md5: $cmd5  " );
  printf( "Mod: %i-%02i-%02i %02i:%02i:%02i\n", @lmod[5]+1900, @lmod[4]+1, @lmod[3], @lmod[2], @lmod[1], @lmod[0] );
  close( LFILE );

  # If the digest is different, then the files are different.
  if ( $cmd5 != $md5 ) { # || ($lsize != $size)) {
    goto GET;
  }

  print( "\n" );

} # foreach

