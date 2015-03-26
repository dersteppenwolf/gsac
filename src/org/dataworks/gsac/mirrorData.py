#!/usr/bin/python
'''
 |===========================================|
 filename              : mirrorData.py
 author                : Stuart Wier
 created               : 2014-09-03
 latest update(version): 2014-12-17; ...;  2015-01-22; 2015-02-20

 exit code(s)          : 0, success; 1 for early exit (see log message why)

 description           : To populate or update a  UNAVCO Dataworks  database which has the UNAVCO Dataworks schema with GNSS datafiles from UNAVCO.
                       : Populates the data files metadata (table datafile) and also copies the complete GNSS data files to this computer.
                       : This process is run once a day by the ops crontab file; which see (do crontab -l).

 usage                 : Initial setup (one time): revise these Python code lines, each line is flagged with the word CHANGE,  to configure your use of this script:

                       : CHANGE the nominal example "dataworksserver.umx.edu" in this line below to your top level path directory name for your FTP server.
                       local_domain = "ftp://dataworksserver.umx.edu/rinex"  

		               : CHANGE: once, set the value of logflag the code line below  near line number 575, to set if log output goes to the screen as well as to the log file. 
                       logflag= 1  # Note: use 1 for operations.    use =2 for debugging runs, to see output on screen as well as in logFile

                       : This mirrorData.py process is run once a day by the ops account crontab file; which see (do crontab -l).

                       : Must already have in the database all the correct 'equip_config' table entries, the information about the stations' equipment sessions.
                       : That is achieved by running mirrorStations.py just before you run this script.

                       : or you can run this program by hand with commands like this, for example, to get datafiles for the one-month date range shown: 

                         ./mirrorData.py localhost dbacct dbacctpw dbname 2014-04-01 2014-04-30  networkname

                       : dbhost is like 'localhost', dbacct and dbacctpw are the MySQL account names and password to write to the database dbname such as "Dataworks".
                       : stationgroup is a UNAVCO archive name for a network, like COCONet or TLALOCNet

                        The command run by crontab is like: 
                        
                        /dataworks/mirror_gps_data_from_unavco/mirrorData.py localhost dataworks tlalocnetdataworks Dataworks 4daysback today TLALOCNet

                        The words "4daysback today" are special input arguments to cover the most recent days.  see code below dealing with '4daysback'.

                        Or, for a station name list in place of a network name (TLALOCNet), use, for separate stations use:  
                           Inside a single pair of quotation marks "", have:
                              a. a single station's four char ID  like "POAL"
                              b. semi-colon separated list of four char IDs, separated with ";"   like    "p123;p456"  
                                 The semi-colon separates the items in station group list.
                              c. wildcards using * like "TN*"
                        So for the station group, put any or all of a. thru c. in one string with no spaces, like  "POAL;P12*;TN*"
                        Upper case and lower case in station four char IDs are the same in GSAC use.
                        So a purely hypothetical command is like

                        /dataworks/mirror_gps_data_from_unavco/mirrorData.py localhost dataworks tlalocnetdataworks Dataworks 4daysback today "PALX;PHJX;PJZX;PLPX;PLTX;PTAX;PTEX"

                       If you do wish to mirror those stations' data files routinely, simply add this one command line to your crontab file for daily operation.

                       This takes about 2 to 6 minutes to check for and update GNSS data files from UNAVCO for 100 stations for the past 4 days.

 tested on             : Python 2.6.5 on Linux (on Ubuntu 10)
                       : "Python 2.6.6 (r266:84292, Jan 22 2014, 09:42:36) [GCC 4.4.7 20120313 (Red Hat 4.4.7-4)] on linux2" (on CentOS)
|===========================================|
'''

import os
import sys
import math
import string
import datetime
from   datetime import timedelta
from   time     import gmtime, strftime
import MySQLdb

def parseOneSiteMetadata ():
        global thissitecode
        global station_id 
        global equip_config_id  
        global datafile_name  
        global datafile_type_id 
        global sample_interval 
        global datafile_start_time
        global datafile_stop_time
        global published_time   
        global year            
        global day_of_year    
        global data_year
        global data_day_of_year
        global size_bytes   
        global MD5           
        global obsfilestotalsize
        global navfilestotalsize
        global metfilestotalsize
        global totalsizes
        global obsfilecount
        global navfilecount
        global metfilecount
        global wgetfilecount
        global numbstawithdata 
        global countinserts
        global toinserts
        global failinserts
        global countskips
        global nogetcount
        global countobs
        global countnav
        global countmet
        global timefixcount

        # data_file_info.csv is a GSAC output file in csv, about data files at one station,
        #    made by a GSAC API query done previously in this program in the main method (below).
        dataListFile = open ('data_file_info.csv');
        # read and count how many lines in file
        allLines = dataListFile.readlines()
        linecount = len(allLines)
        dataListFile.seek(0) # rewind to beginning
        if linecount>0 : 
            logWrite ( "    Count of gps datafiles for this station from UNAVCO GSAC in this time interval: "+`linecount-2` )
        else:
            logWrite ( "    NO gps data files to download for this station in this time interval. ")

        allstationcount=0
        stationcount=0
        station_counter=0
        countqc=0
        obssizeMB = 0.0
        navsizeMB = 0.0
        metsizeMB = 0.0
        qcsizeMB = 0.0
        totalMB = 0.0
        lastfilestacode= " "
        # station 4-char id list to help manage the same
        sta_ID_list=[]

        # read each line in data_file_info.csv, i.e. get the metadata about each gps data file from this station:
        for i in range(linecount) :
          line = dataListFile.readline()

          # skip first 2 lines in the .csv file, the header info: 
          if (i> 2 ) :   # add 'and i < 11' for test limit 

             # split out values from line, at commas
             strlist= string.split( line, "," )

             # example
             # ACP1,GNSS RINEX Observation (Unix Compressed),aa4e56eaffbda3e71b14f5a50ba95f9e,1877673,2014-10-04 00:00:00,ftp://data-out.unavco.org/pub/rinex/obs/2014/276/acp12760.14o.Z,
             # 2014-10-03 00:00:00,2014-10-03 23:59:45,0.0,ACP1
             # all these values are strings, not numbers
             staid              = (strlist[0])      # 4 char station ID code
             ftype              = (strlist[1])      # char string name of the file type like RINEX nav file ; see more below about ftype
             file_MD5           = (strlist[2])      # MD5 check sum value of the data file
             fsize              = (strlist[3])      # in bytes
             published_time     = (strlist[4])      # published date of the data file in its native archive
             file_url           = (strlist[5])      # the URL to download this gps file from UNAVCO
             datafile_start_time= (strlist[6])      # data start time, ISO 8601 format
             datafile_stop_time = (strlist[7])      # data end time

             sample_interval    = float( (strlist[8]) ) # data sample interval, seconds

             logWrite( "\n   station "+staid+": this data file's metadata from UNAVCO GSAC is \n    " +line[0:-1] )  # :-1] means do not print the line's terminal line return \n 
             #logWrite(   " id code, filetype, MD5, published time, file URL, datafile_start_time, datafile_stop_time")

             filext =file_url[-3:]

             # from file type name strings from the input file, select the file type id number for the  UNAVCO Dataworks  standard db schema:
             doc='''
		mysql> select * from datafile_type;
		+------------------+-------------------------------+-----------------------+----------------------------------------------------+
		| datafile_type_id | datafile_type_name            | datafile_type_version | datafile_type_description                          |
		+------------------+-------------------------------+-----------------------+----------------------------------------------------+
		|                1 | instrument data file          |                       | Any type or format of native, raw, or binary file  |
		|                2 | RINEX observation file        |                       | a RINEX 'o' obs file; may be compressed            |
		|                3 | RINEX GPS navigation file     |                       | a RINEX 'n' nav file; may be compressed            |
		|                4 | RINEX Galileo navigation file |                       | a RINEX 'e' nav file; may be compressed            |
		|                5 | RINEX GLONASS navigation file |                       | a RINEX 'g' nav file; may be compressed            |
		|                6 | RINEX meteorology file        |                       | a RINEX 'm' met file; may be compressed            |
		|                7 | RINEX QZSS navigation file    |                       | a RINEX 'j' nav file; may be compressed            |
		|                8 | RINEX Beidou navigation file  |                       | a RINEX 'c' nav file; may be compressed            |
		+------------------+-------------------------------+-----------------------+----------------------------------------------------+
		8 rows in set (0.00 sec)
	     '''
             filetypeid=0    # for the file type id number database.
             file_type="nav" # used in the ftp url for the local ftp service.

             # for logs only:
             fsizebytes = long(fsize)                  # convert the string to long integer for computations
             fsizeMB    = (fsizebytes*1.0) / (1048576) # convert integer bytes to megabytes as a float

             # Set file type id for this file, as used in dataworks schema datafile_type_id.  
             # Skip files of types (such as o.Z) not wanted to store in mirrors.
             # this of course uses the file type names from the UNAVCO GSAC
             usethisfile= True
             if ('highrate' in  file_url ) : 
                  usethisfile= False 
                  countskips+=1
                  logWrite( "    Skip this highrate file "+file_url )
             elif ("d.Z" == filext and 'bservation' in ftype):  # Hatanaka RINEX Observation 
                  filetypeid=2
                  countobs += 1;
                  obssizeMB += fsizeMB 
                  file_type="obs"
             elif ("n.Z" == filext and 'avigation' in ftype): # GPS nav file
                  filetypeid=3
                  countnav += 1;
                  navsizeMB += fsizeMB
             elif ("g.Z" == filext and 'avigation' in ftype): # GLONASS, "g" nav file
                  filetypeid=5
                  countnav += 1;
                  navsizeMB += fsizeMB
             elif ("e.Z" == filext and 'avigation' in ftype): # Galileo, "e" nav file
                  filetypeid=4
                  countnav += 1;
                  navsizeMB += fsizeMB
             elif ("m.Z" == filext and 'eteorolog' in ftype): # met file
                  filetypeid=6
                  countmet += 1;
                  metsizeMB += fsizeMB
                  file_type="met"
             else:
                  usethisfile= False 
                  countskips+=1
                  logWrite( "    Skip this file: its file type, "+file_url + ", is not wanted.")

             #print "\n    one datafile's values for station "+staid+": \n    ftype="+ftype+"_  file_MD5="+file_MD5+"_   fsize="+fsize+"_bytes  file_url="
             #  +file_url+"_  datafile_start_time="+datafile_start_time+"_  datafile_stop_time="+datafile_stop_time+"_  si="+`si`+"_"

             if usethisfile :

                 totalMB += fsizeMB 

                 # wrangle some more data values for the db from file_url such as ftp://data-out.unavco.org/pub/rinex/obs/1996/054/bogt0540.96d.Z
     
                 ind = file_url.find(":");
                 file_url_protocol = file_url[:ind]
                 #print "          file_url_protocol = _"+file_url_protocol  +"_"

                 ind = file_url.rfind("/") + 1;  # find index after last /
                 datafile_name = file_url[ind:]

                 logWrite( "   For the data file "+datafile_name+ ", find its equipment sessions' id number, load database datafile table with the file's metadata, and download the file from UNAVCO")

                 filestacode= datafile_name[:4]
                 filestacode = filestacode.upper()
                 #if 3==logflag : print "      the data file "+datafile_name +" has station code _"+ filestacode +"_"

                 if staid != filestacode:
                     logWrite("   PROBLEM: data file "+file_url+" is not for station(id) associated in GSAC file info list, id="+staid +".")
                     sys.stdout.flush()
                     filestacode="Bad_site_for_file"
                     # a kludge to force this file not to be loaded, and skip to next file
                     # or could do, to halt processing:
                     # sys.exit(1)

                 i1 = file_url.rfind("//") + 2;  # find index after the first //
                 tmp=file_url[i1:]
                 i2 = tmp.find("/") 
                 file_url_ip_domain = tmp[:i2]
                 #print "          file_url_ip_domain = _"+ file_url_ip_domain +"_"

                 ind = tmp.rfind("/") +1 ;  # find index after last /
                 file_url_folders = tmp[i2:ind]

                 # get year and day from normal case like
                 data_day_of_year = file_url_folders[-4:-1]
                 data_year = file_url_folders[-9:-5]
                 # use data_year=  long(data_year) # to convert the string to long integer for computations

                 # OR, get year and day from special case like ftp://data-out.unavco.org/pub/highrate/1-Hz/rinex/2013/223/lmmf/lmmf2230.13d.Z

                 #if 3==logflag : print "      year ="+ data_year + ",  day of year ="+ data_day_of_year+"."

                 # if no start or end times provided... make a time from thei gps file name, the day of year, using the sinex time format which uses doy.
                 if ""==datafile_start_time  :
                    # want datafile_start_time like 2013-11-22 12:00:00 since have no idea when in the day it stated!
                    starttimestr = data_year[2:] +  ":" + data_day_of_year +  ":43200"  # sinex format 
                    datafile_start_time =  generateISO8601fromSINEXtime(starttimestr,"")
                    logWrite("    PROBLEM: CORRECTED: datafile_start_time  was missing;  new value = "+datafile_start_time  +" for file "+file_url)
                    timefixcount+=1

                 if ""==datafile_stop_time :
                    endtimestr = data_year[2:] +  ":" + data_day_of_year +  ":43260"              #  end at 12:01 in day since have no idea...
                    datafile_stop_time =  generateISO8601fromSINEXtime(endtimestr,"")
                    logWrite("    PROBLEM:   CORRECTED: datafile_stop_time  was missing;  new value =  "+datafile_stop_time  +" for file "+file_url)
                    timefixcount+=1

                 # LOOK possible bug if one one of the two days above not defined...

                 # field names from database, table datafile:
                 station_id        = 1  # to be determined below 
                 equip_config_id   = 1  # to be determined below
                 URL_path          = "" # to be determined below
                 # already known:
                 original_datafile_name = datafile_name
                 datafile_type_id  = filetypeid
                 year          = data_year
                 day_of_year   = data_day_of_year
                 size_bytes    = fsizebytes
                 MD5           = file_MD5

                 # find station_id number in the database:
                 if (lastfilestacode == filestacode) :
                       pass # still working on same station as the preceeding line's station
                 else :
                     try:
                        station_id  = getTableRowID ("station_id", "station", "four_char_name", filestacode)
                        lastfilestacode == filestacode
                     except:
                          #if logflag >= 2 and filestacode!="Bad_site_for_file" : 
                          logWrite("      >>>>>>>>>>>>   PROBLEM: Did not find this station in the db: "+filestacode+".  Please first run mirrorStation.py. \n")
                          #print "    Except: Skip this file, "+file_url
                          #if filestacode!="Bad_site_for_file" : 
	                  listFile.close()
	                  logFile.close()
	                  gsacdb.close()
                          sys.stdout.flush()
                          sys.exit(1)

                 #if 3==logflag : print "      Load datafile metadata for station "+filestacode+" (id "+`station_id`+")"

                 descdatfiletable = '''
                   find in db:
                    | station_id          | int(6) unsigned | NO   | MUL | NULL    |                |
                    | equip_config_id     | int(6) unsigned | YES  | MUL | NULL    |                |
                   already read from metadata:
                    | datafile_name       | varchar(120)    | NO   |     | NULL    |                |
                    | original_datafile_name       | varchar(100)    | NO   |     | NULL    |                |
                    | datafile_type_id    | int(3) unsigned | NO   | MUL | NULL    |                |
                    | sample_interval     | float           | NO   |     | NULL    |                |
                    | datafile_start_time | datetime        | NO   |     | NULL    |                |
                    | datafile_stop_time  | datetime        | NO   |     | NULL    |                |
                    | year                | year(4)         | NO   |     | NULL    |                |
                    | day_of_year         | int(3)          | YES  |     | NULL    |                |
                    | published_time      | datetime        | NO   |     | NULL    |                |
                    | size_bytes          | int(10)         | NO   |     | NULL    |                |
                    | MD5                 | char(32)        | NO   |     | NULL    |                |
                   construct for this  UNAVCO Dataworks  repository:
                    | URL_path            | varchar(120)    | NO   |     | NULL    |                |
                    '''

                 # CHANGE the next line of code to define part of your path to files in your ftp server:
                 # change dataworksserver.umx.edu to your top level path directory name for your FTP server:
                 # example local_domain = "ftp://dataworksserver.umx.edu/rinex"  
                 local_domain           = "ftp://tlalocnet1/rinex"  

                 # make the ftp  users' downloads of datafiles from your Dataworks system (not from UNAVCO):
                 URL_path =             local_domain +"/"+ file_type +"/"+ year +"/"+ day_of_year + "/" + datafile_name

                 # check if this  datafile's metadata is already in the db table 'datafile':
                 haveitinDB=False
                 dfid=None
                 esid=None
                 isPriorLoad=False
                 statement=""
                 try:
                       statement=("SELECT datafile_id from datafile where station_id=%s and datafile_name='%s' and datafile_start_time='%s' and datafile_stop_time='%s' " % (station_id, datafile_name, datafile_start_time, datafile_stop_time ))
                       #if logflag>=2: print "      look for datafile_name in the db with sql: "+ statement
                       cursor.execute(statement)
                       row= cursor.fetchone()
                       dfid=  row[0];
                       dfid=  int(dfid); # fix the "L" value returned
                       haveitinDB=True
                       isPriorLoad=True
                       logWrite( "     Already have metadata for this datafile, "+datafile_name+", in the db datafile table, for station_id "+`station_id`+", at datafile_id ="+`dfid` )   
                       # BYPASS when true
                 except:
                     # for ftp file access, such as ftp://data-out.unavco.org/pub/rinex/nav/2014/207/airs2070.14n.Z

                     logWrite("      data file to load in db, and to download is  "+datafile_name)
                     # duplicates url shown in metadata line printed above.   logWrite("      ftp URL of file is    "+URL_path )

                     # LOOK MAJOR logic change: no longer demand that a data file have an equipment session corresponding to in time to this gps data file's times,  at this station.
                     # LOOK now the equip_config_id CAN be NULL in the database datafile table: equip_config_id        | int(6) unsigned | YES
                     # as per Fran Boler request Nov 18 2014.
                     # so, see if there happens to be a valid equip_config_id for an equipment session corresponding to this gps data file at this station :
                     try:
                             statement=("SELECT equip_config_id from equip_config  where  station_id= %s and equip_config_start_time<='%s' and (equip_config_stop_time>='%s' or equip_config_stop_time = '0000-00-00 00:00:00') " % (station_id, datafile_start_time, datafile_stop_time) )
                             logWrite ("      find equip id with sql: \n     "+ statement)
                             cursor.execute(statement)
                             row= cursor.fetchone()
                             esid=  row[0];
                             esid=  int(esid); # fix the "L" value returned
                             logWrite( "      this data file belongs to equip session # "+`esid`+".")
                     except:
                             # no longer matters logWrite("  PROBLEM:  FAILED to get equip session id with SQL \n     "+statement+". \n      Find fault and redo this process. \n")
                             logWrite( "      no equip session for this data file's time  is in the database yet; will try again later after the sessions are updated. ")
                             pass 
                              
                     # LOOK now always insert the data file metadata in the db even if esid is None
                     insertstmt=""
                     try:
                                    if esid == None :
                                       pass # print " >>>>>>>>>>>>>>>>  esid is none " # eisid = "NULL"
                                    else :
					    logWrite( "     Insert the gps data file's metadata into the db:")
					    toinserts = toinserts +1 
					    insertstmt=("INSERT into datafile (station_id,  equip_config_id, datafile_name, original_datafile_name, datafile_type_id, sample_interval, datafile_start_time, datafile_stop_time, published_time, year, day_of_year, size_bytes, MD5, URL_path) values (%s, %s, '%s', '%s', %s, %s, '%s', '%s', '%s', %s, %s, %s,  '%s', '%s')"  %  (`station_id`, `esid`, datafile_name, original_datafile_name, datafile_type_id, sample_interval, datafile_start_time, datafile_stop_time, published_time, year, day_of_year, size_bytes, MD5, URL_path ))
					    # if logflag==2 : print "        SQL is "+insertstmt 
					    cursor.execute(insertstmt)
					    gsacdb.commit()
					    countinserts = countinserts +1 
					    logWrite(  "     inserted data file metadata into db." )
					    haveitinDB=True
                     except:
                                    logWrite(  "    PROBLEM: failed to insert this data file's ("+file_url+") metadata into the db table datafile  for equip session "+esid)
                                    logWrite(  "    with SQL "+insertstmt)
                                    failinserts = failinserts +1 
                                    gsacdb.rollback()

                     sys.stdout.flush()

                     # overall total count; size for this station only; print "        obs files count and total size(MB): "+`countobs`+"   "+`obssizeMB`
                     #print "        nav files count and total size(MB): "+`countnav`+"   "+`navsizeMB`
                     #print "        met files count and total size(MB): "+`countmet`
                     #print "        running values:        size total =     "+`totalMB` +" MB"

                 if haveitinDB :
                        # Now download the datafile itself from url file_url 
                        cmd4 = "wget -N -nv -x -nH -P /data "+file_url
			# wget manuals: http://www.gnu.org/software/wget/manual/wget.html;   http://www.gnu.org/software/wget/manual/
			#  -N means wget will ask the server for the last-modified date. If the local file has the same timestamp as the server, or a newer one, the remote file 
			#     will not be re-fetched. However, if the remote file is more recent, Wget will proceed to fetch it.
                        # -nv means not verbose; restrict the screen output. -v is very verbose logging.
                        #  -x means make dirs, when needed.  
                        # -nH means do not use the remote domain hostname, the 'data-out.unavco.org' part in ftp://data-out.unavco.org/pub/rinex/obs/2013/334/acp13340.13d.Z 
                        # '-P /data' means put file (and its directories) under /data.  
                        # So with '-x -nH -P /data' you make a new file (and directories) like /data/pub/rinex/obs/2013/334/acp13340.13d.Z
                        # (stored in the exact same file path as at UNAVCO)
                        # options not used:
                        # --mirror is for recurvise downloads.
                        #  -c means 'continues if interrupted', and SKIP downloading if file is there already. But -c checks for EXACT same size and same name,
                        #  so the wget with -c downlaod FAILS if the file size has changed as does happen at UNAVCO, and gives status 2048 error.  Don't use -c!
                        # -N is right for our use: get a newer version, regardless of size changes, and don't download if not newer.

                        if isPriorLoad:
                            logWrite( "     Verify already having this data file with command "+cmd4)  #  check for OK name and size if already here.
                        else :
                            logWrite( "     Download this data file with command "+cmd4)  #  get the file if not already here.
 
                        cstatus4 = os.system(cmd4)
                        # for wget return status values, see http://www.gnu.org/software/wget/manual/html_node/Exit-Status.html
                        # Note that "success" can mean 'file was previously downloaded'.
                        if cstatus4 == 0 :
                            logWrite( "     No problems occurred verifying or downloading file with wget command "+cmd4) 
                            wgetfilecount += 1
                        else :
                            logWrite("  PROBLEM: command "+cmd4+" returned status="+`cstatus4`+".  Repeat that wget command by hand, with -v verbose (not -nv) to see error.")
                            nogetcount +=1
                 else :
                        pass

                 # end of this one gps file handling
                 sys.stdout.flush()

          # last line in for loop reading lines in dataListFile, each line for one data file at UNAVCO for this station in this date range.
                 

        # close the GSAC CSV file with this station's datafile information:
        dataListFile.close()

        # do the accounting sums for this one station:
        obsfilestotalsize += obssizeMB
        navfilestotalsize += navsizeMB
        metfilestotalsize += metsizeMB
        totalsizes += totalMB
        numbstawithdata +=1

        #print   " for this station, the total of data file sizes added = "+`totalMB` +" MB or "+ `(totalMB/1024)`+" GB"

        #end of parseOneSiteMetadata 


def logWrite (text):
     global logFile
     logFile.write(text + "\n")
      # for debugging runs to see output on screen:
     if logflag>=2: print (text)


        
def getTableRowID (idname, tablename,  rowname, rowvalue) :
       global logFile
       " find the id for the value rowvalue in the db table tablename."
       the_id=1 # unavco dataworks "not specified" meaning.
       try:
           SQLstatement=("SELECT  %s from %s  where  %s = '%s' " % ( idname,tablename,rowname,rowvalue ) )
           #if logflag>=2: print "            getTableRowID(): SQL statement ="+SQLstatement
           cursor.execute(SQLstatement)
           row= cursor.fetchone()
           the_id=  row[0];
           the_id=  int(the_id); # fix the "L" value returned
           #if logflag>=2: print "                got id="+`the_id` +"  for row value "+rowvalue+" in table "+tablename
       except:
           logWrite("    no row named "+rowname+" is yet in the database table "+tablename +"  for holding a value "+rowvalue )
           sys.stdout.flush()
           defunct='''
           # add this new value to that table; and get its id
           SQLstatement=("INSERT into %s (%s) value ('%s')"  %  ( tablename,rowname,rowvalue ) )
           if logflag>=2: print "            SQL statement ="+SQLstatement
           try:
               cursor.execute(SQLstatement)
               gsacdb.commit()
               if logflag>=2: print  "           Inserted new value "+rowvalue
           except:
               gsacdb.rollback()
               if logflag>=2: print  "           failed to insert new value "+rowvalue
           #  get the new id value
           SQLstatement=("SELECT %s from %s  where  %s = '%s' " % ( idname,tablename,rowname,rowvalue ) )
           if logflag>=2: print "            SQL statement ="+SQLstatement
           cursor.execute(SQLstatement)
           row= cursor.fetchone()
           the_id=  row[0];
           the_id=  int(the_id); # fix the "L" value returned
           if logflag>=2: print "                set id="+`the_id` +"  for row value "+rowvalue+" in table "+tablename
           '''
       return the_id
 #  END OF function  getTableRowID () 

def generateISO8601fromSINEXtime(sinextime, timezonechars):
        # convert strings of date-time from sinex such as  12:217:00000 12:225:86370  to ISO8601 strings
        #*SITE PT SOLN T DATA_START__ DATA_END____ DESCRIPTION_________ S/N__ FIRMWARE___
        # ABMF  A    1 P 12:217:00000 12:225:86370 TRIMBLE NETR9        ----- -----------
        # and 07:320:00000 11:099:63000
        #
        # time stamps in the following format: YY:DOY:SECOD YY-year; DOY- day of year; SECOD -sec of day;
        # E.g. the epoch 95:120:86399 denotes April 30, 1995 (23:59:59UT).

        # ISO8601 is like 2012-10-27T07:30:38-02:00  -2:00 being the time zone offset from UT
        # "Without any further additions, a date and time ... is assumed to be in some local time zone."
        # In order to indicate that a time is measured in Universal Time (UTC), you can append a capital letter Z to a time
        # time zone NOT INDICATED in a SINEX file value;  timezonechars such as "Z" or "-09:00" may be provided

        # for GSAC, a sinex "no value" becomes an undefined value:
        if (sinextime =="00:000:00000"):
            return "                   "

        days = string.atoi(sinextime[3:6])
        yr = "yyyy"
        syr=sinextime[0:2] # 2 chars 
        if (string.atoi(syr)<70):
             yr="20"+syr
        else:
             yr="19"+syr
        # unravel DYO part; what a mess : depends on which year:
        #     datetime.datetime(year, 1, 1) + datetime.timedelta(days - 1) 
        dt = datetime.datetime(string.atoi(yr), 1, 1) + datetime.timedelta(days - 1)
        ddMMyy=dt.strftime("%d/%m/%y") # gives dd/MM/yy like 25/03/06
        day= ddMMyy[0:2]
        mon= ddMMyy[3:5]
        time = string.atoi(sinextime[7:]) # integer seconds in day
        ihr=time/3600
        imin = (time - (ihr*3600))/60
        isec = (time -  (ihr*3600) - (imin*60))
        hr=`ihr`
        if (len(hr)==1) : hr = "0"+hr
        minu=`imin`
        if (len(minu)==1) : minu = "0"+minu
        sec=`isec`
        if (len(sec)==1) : sec= "0"+sec

        isotime = yr+"-" + mon + "-" + day+ "T"+hr+":"+minu+":"+sec+timezonechars
        return isotime
        # end function




# Main program: 

global logFile 
global thissitecode
global station_id 
#global equip_config_id  
global datafile_name  
global datafile_type_id 
global sample_interval 
global datafile_start_time
global datafile_stop_time
global published_time   
global year            
global day_of_year    
global data_year
global data_day_of_year
global size_bytes   
global MD5           
global obsfilestotalsize
global navfilestotalsize
global totalsizes
global obsfilecount
global navfilecount
global metfilecount
global wgetfilecount
global numbstawithdata 
global countinserts
global countskips
global countobs
global countnav
global countmet
global nogetcount
global timefixcount

# CHANGE
logflag= 1  # Note: use 1 for operations.    use =2 for debugging runs, to see output on screen as well as in logFile

dbhost=""
dbacct=""
dbacctpw=""
dbname=""
datadatefrom=""
datadateto=""

# get command line argument values
args = sys.argv
dbhost   = args[1]
dbacct   = args[2]
dbacctpw = args[3]
dbname   = args[4]
datadatefrom=args[5] # must be formattted like 2013-09-12
datadateto  =args[6]   # like 2014-12-31
stationgroup=args[7]

# set up handling routine daily processing with magic words startdate and enddate, for the past 4 or 7 days:
if "today"==datadateto :
  timestamp=strftime("%Y-%m-%d_%H%M%S", gmtime())  # this is a string,  like 2014-11-15_192724
  datadateto = timestamp[:10]
if "Ndaysback"==datadatefrom :
  # does past week:
  startdate = datetime.date.today () - datetime.timedelta (days=6)
  datadatefrom = startdate.strftime ("%Y-%m-%d")
if "4daysback"==datadatefrom :
  startdate = datetime.date.today () - datetime.timedelta (days=3) 
  datadatefrom = startdate.strftime ("%Y-%m-%d")

countobs=0
countnav=0
countmet=0
sitecount=0
nogetcount=0
countinserts=0
toinserts=0
failinserts=0
countskips=0
obsfilestotalsize =0.0
navfilestotalsize=0.0
metfilestotalsize=0.0
totalsizes=0.0
obsfilecount=0
navfilecount=0
metfilecount=0
wgetfilecount=0
numbstawithdata = 0
timefixcount=0

# connect to the database to write to, uses import MySQLdb
# MySQLdb.connect (host, acct, password, Mysql database name)
gsacdb = MySQLdb.connect(dbhost, dbacct, dbacctpw, dbname)
cursor = gsacdb.cursor()




# open log file; also describing processing results which need later attention:
dom =strftime("%d", gmtime())  # day of month, such as "16", to use in log file name
logfilename = "/dataworks/logs/mirrorData.py.log."+dom
timestamp   =strftime("%Y-%m-%d_%H:%M:%S", gmtime())

# compose the UNAVCO GSAC's API query string.  stationgroup can be a network name like TLALOCNet
httppart=' "http://www.unavco.org/data/web-services/gsacws/gsacapi/site/search/sites.csv?output=site.csv&site.interval=interval.normal&site.status=active&site.group=' + stationgroup+'"'

# or in case of a list of separate station IDs:
if ";" in stationgroup or len(stationgroup)== 4:
   # search for site by ID, and cut off trailing final ";" 
   httppart=' "http://www.unavco.org/data/web-services/gsacws/gsacapi/site/search/sites.csv?output=site.csv&site.interval=interval.normal&site.code=' + stationgroup+'"'
   logfilename = logfilename + ".extras"

cmd1 = "/usr/bin/curl -L "+ httppart + " > dataworks_sites_short.csv"

logFile     = open (logfilename, 'w')  # NOTE this creates a NEW file of the same log file name, destroying any previous log file of this name.
logWrite( "\n    Run mirrorData.py  ")
logWrite(   "    Process to search for and download GPS data files at UNAVCO, from _"+datadatefrom+"_ through _"+datadateto+"_")
logWrite(   "    Log file describing processing by mirrorData.py")
logWrite( "\n    mirrorData.py run started at "+timestamp+" ")
logWrite( "\n    ***** *****  Look at the log file after each run.  Look for errors in lines noted with the word PROBLEM. ***** ***** ")
if ";" in stationgroup or len(stationgroup)<5:
   logWrite("\n    Update these individual sites: "+stationgroup );
logWrite( "\n    Get list of sites from the UNAVCO GSAC server.  The Linux command is \n    "+cmd1 )
sys.stdout.flush()


# Do the query to the UNAVCO GSAC to make a list of stations in the file dataworks_sites_short.csv:
# with resulting metadata in the "GSAC short csv file format"
cstatus1 = os.system(cmd1)
# note that this Python process pauses until the cmd1 process completes. Typical time elaspsed is about 30 seconds.

# For each  station in the list, get metadata about its data files, and download the data files>
if cstatus1 == 0 :
    listFile = open ("dataworks_sites_short.csv");
    allLines = listFile.readlines()
    linecount = len(allLines)
    logWrite( "\n    UNAVCO GSAC site search query succeeded. Have site list dataworks_sites_short.csv with "+`linecount-1`+" stations. ")
    listFile.seek(0) # rewind to beginning

    # Main loop on all stations
    # read the lines in dataworks_sites_short.csv, the station file lines, one line per station
    for i in range(linecount) :
      siteline = listFile.readline() 

      # skip first 1 header lines:
      if (i>=1):  # and i<7):  for test limit 

        linelist= string.split( siteline, "," )
        thissitecode = (linelist[0])

        # for debugging, to limit to either of two sites use code like this if ( ("USMX"== thissitecode or "POAL"==thissitecode) and  4== len(thissitecode)):

        # Normally, do all stations:
        if ( 4== len(thissitecode)):
            # query the UNAVCO GSAC server for this one station's data file info, with results in a csv file:
            httppart = ' "http://www.unavco.org/data/web-services/gsacws/gsacapi/file/search?file.sortorder=ascending&site.code='+thissitecode+'&file.datadate.from='+datadatefrom+'&output=file.csv&site.name.searchtype=exact&site.code.searchtype=exact&limit=5000&file.datadate.to='+datadateto+'&site.interval=interval.normal" '
            cmd2= "/usr/bin/curl -L "+ httppart + " > data_file_info.csv"
            sitecount += 1
            logWrite("\n   "+`sitecount`+"  Next Station "+thissitecode+":" )

            testing = ''' to only do 3 stations around station count near 72 such as lcsb
            if ( sitecount < 70 ):
               continue
            if ( sitecount >= 75 ):
               continue
            '''

            logWrite(  "   Get metadata about all gps data files from station "+thissitecode+" in the given date range,  with linux command \n    "+cmd2)
            cstatus2 = os.system(cmd2)
            if cstatus2 == 0 :
                parseOneSiteMetadata()
            else :
                logWrite("  PROBLEM: Failed the Linux comand to get 1 station's data file info with the curl command just listed above." )

        else:
            logWrite("  PROBLEM: "+thissitecode+" station ID is invalid (not 4 characters)")

else :
    logWrite( "  PROBLEM: Linux comand to get the complete list of sites failed: "+ cmd1 +"\n")


logWrite( "\n  \n        Summary of mirroring data files from UNAVCO, during "+datadatefrom+" to "+datadateto+":")
logWrite( "\n          number of "+stationgroup+" stations in the UNAVCO archive checked for GNSS data files in this time interval: "+`numbstawithdata`)
# LOOK format float values:
logWrite( "\n          obs files totalsize=   %9.3f  MB" % obsfilestotalsize)
logWrite(   "          nav files totalsize=   %9.3f  MB" % navfilestotalsize)
logWrite(   "          met files totalsize=   %9.3f  MB" % metfilestotalsize)
logWrite(   "          total size all files=  %9.3f  MB  or %9.3f GB" % (totalsizes, (totalsizes/1024) ) )
logWrite( "\n          obs file count=   "+`countobs`)
logWrite(   "          nav file count=   "+`countnav`)
logWrite(   "          met file count=   "+`countmet`)
all = countobs + countnav  + countmet
logWrite( "          count of all required data files found in this time interval "+`all` +" (sum of 3 items above)")
# logWrite( "          count of not required data files is "+`countskips` + " (types not wanted for a UNAVCO Dataworks mirror)")
logWrite("\n          About new files to download:")
logWrite(  "          db: count of data files' info TO insert in the db                 "+`toinserts`)
logWrite(  "          db: count of data files' info Inserted in the db                  "+`countinserts`)
logWrite(  "          db: count of data files' info FAILED inserts in the db            "+`failinserts`)
logWrite("\n          files: success count of data files to download from UNAVCO, or already downloaded:   "+`wgetfilecount`)
logWrite(  "          files: count of wget problems encountered                                             "+`nogetcount`)
timestamp=  strftime("%a, %d %b %Y %H:%M:%S +0000", gmtime())  #  strftime("%d-%M-%Y %H:%M:%S", gmtime)
logWrite("\n                 Completed mirrorData.py at " +timestamp+ " UTC\n")

# close the GSAC  CSV file with the site list information:
listFile.close()
# disconnect from db server
gsacdb.close()
logFile.close()

sys.exit (0) # return success

# ALL DONE
