#!/usr/bin/python
'''
 |===========================================|

 filename              : populate_GSAC_db_datafile_metadata.py
 author                : Stuart Wier
 created               : 2014-09-03
 latest update(version): 2015-10-30             Stuart Wier 

 exit code(s)          : 0, success; 
                       : sys.exit (1),  PROBLEM: curl command to get sites info from remote GSAC failed.

 description           : To populate or update a GSAC Prototype15 database, the datafile table, with data files' metadata.
                       : Populates the data files metadata (table datafile), and MAY also copy the complete GNSS data files to this computer.

 usage                 : Initial setup (one time): revise these Python code lines, each line is flagged with the word CHANGE,  to configure your use of this script:

                       : CHANGE: once, set the value of logflag the code line below  near line number 575, to set if log output goes to the screen as well as to the log file. 
                       logflag= 1  # Note: use 1 for operations.    use =2 for debugging runs, to see output on screen as well as in logFile

                       : Must already have in the database all the correct 'equip_config' table entries, the information about the stations' equipment sessions.
                       : That is achieved by running populate_GSAC_db_station_metadata.py.

                       : Run this program by hand with commands like this, 

                         ./populate_GSAC_db_datafile_metadata.py localhost dbacct dbacctpw dbname  data_file_info.csv

                       : dbhost is like 'localhost', dbacct and dbacctpw are the MySQL account name and password to write to the database called dbname. 


 tested on             : Python 2.6.5 on Linux (on Ubuntu 10)

 *
 * Copyright 2014, 2015 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
 * http://www.unavco.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 

'''

import os
import sys
import math
import string
import datetime
from   datetime import timedelta
from   time     import gmtime, strftime
import MySQLdb


def parseMetadata ():
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


        sample_file_lines = '''

#fields=site_4char_ID[type='string'],Data_Type[type='string'],MD5[type='string'],FileSize[unit='byte'],PublishDate[type='date' format='yyyy-MM-dd HH:mm:ss'],URL[type='string'],dataStartTime[type='date' format='yyyy-MM-dd HH:mm:ss'],dataStopTime[type='date' format='yyyy-MM-dd HH:mm:ss'],sampleInterval[unit='s']
P341,GNSS Navigation,74a71ed96a87df74d6f8ef12a9eec7a2,30258,2015-10-03 00:00:00,ftp://data-out.unavco.org/pub/rinex/nav/2015/275/p3412750.15n.Z,2015-10-02 00:00:00,2015-10-02 23:59:45,15.0,
P341,GNSS RINEX Observation QC Report,5669b4de1ce3402fae82154b42fed07f,20458,2015-10-03 00:00:00,ftp://data-out.unavco.org/pub/rinex/qc/2015/275/p3412750.15S,2015-10-02 00:00:00,2015-10-02 23:59:45,15.0,
P341,GNSS RINEX Observation (Unix Compressed),56e733d136f25cc15242110b50edd069,1739473,2015-10-03 00:00:00,ftp://data-out.unavco.org/pub/rinex/obs/2015/275/p3412750.15o.Z,2015-10-02 00:00:00,2015-10-02 23:59:45,15.0,
P341,GNSS RINEX Observation (Hatanaka  Unix Compressed),a45ebbf82d97892b7c37d1941809e435,611045,2015-10-03 00:00:00,ftp://data-out.unavco.org/pub/rinex/obs/2015/275/p3412750.15d.Z,2015-10-02 00:00:00,2015-10-02 23:59:45,15.0,
P341,GNSS RINEX Observation QC Report,629c7226a9a550d1913645a6cac873a3,20461,2015-10-07 00:00:00,ftp://data-out.unavco.org/pub/rinex/qc/2015/279/p3412790.15S,2015-10-06 00:00:00,2015-10-06 23:59:45,15.0,
P341,GNSS RINEX Observation (Unix Compressed),b9536575bf032ab58c33a85ad7d558a8,1738795,2015-10-07 00:00:00,ftp://data-out.unavco.org/pub/rinex/obs/2015/279/p3412790.15o.Z,2015-10-06 00:00:00,2015-10-06 23:59:45,15.0,
P341,GNSS RINEX Observation (Hatanaka  Unix Compressed),0e24fb6854ca4de38d796b40ed119c20,609781,2015-10-07 00:00:00,ftp://data-out.unavco.org/pub/rinex/obs/2015/279/p3412790.15d.Z,2015-10-06 00:00:00,2015-10-06 23:59:45,15.0,
        '''

        # data_file_info.csv is a  file in csv format, about data files 
        dataListFile = open ('data_file_info.csv');
        # read and count how many lines in file
        allLines = dataListFile.readlines()
        linecount = len(allLines)
        dataListFile.seek(0) # rewind to beginning
        if linecount>0 : 
            logWrite ( "    Count of gps datafiles for : "+`linecount-1` )
        else:
            logWrite ( "    NO gps data files to download in data_file_info.csv. ")
            sys.exit(1)

        allstationcount=0
        stationcount=0
        station_counter=0
        linesread=0
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

          # skip first  line in the .csv file, the header info: 
          if (i> 0 ) :   # add 'and i < 11' for test limit 
             linesread += 1

             samplefile_lines='''
data_file_info.csv 

#fields=site_4char_ID[type='string'],Data_Type[type='string'],MD5[type='string'],FileSize[unit='byte'],PublishDate[type='date' format='yyyy-MM-dd HH:mm:ss'],URL[type='string'],dataStartTime[type='date' format='yyyy-MM-dd HH:mm:ss'],dataStopTime[type='date' format='yyyy-MM-dd HH:mm:ss'],sampleInterval[unit='s']

P341,GNSS RINEX Observation QC Report,efebbba4b0d6d9be3c93c386bfda3096,20461,2015-10-12 00:00:00,ftp://data-out.unavco.org/pub/rinex/qc/2015/284/p3412840.15S,2015-10-11 00:00:00,2015-10-11 23:59:45,15.0,
P341,GNSS RINEX Observation (Unix Compressed),dd9e32ca9d96225ed3e511f9e3070c30,1737879,2015-10-12 00:00:00,ftp://data-out.unavco.org/pub/rinex/obs/2015/284/p3412840.15o.Z,2015-10-11 00:00:00,2015-10-11 23:59:45,15.0,
P341,GNSS RINEX Observation (Hatanaka  Unix Compressed),a056080ef860a9f04dcaef0e35858b22,612841,2015-10-12 00:00:00,ftp://data-out.unavco.org/pub/rinex/obs/2015/284/p3412840.15d.Z,2015-10-11 00:00:00,2015-10-11 23:59:45,15.0,
P341,GNSS Navigation,56967d7a5dcfa52f8d9dc60d2bf014cd,30716,2015-10-12 00:00:00,ftp://data-out.unavco.org/pub/rinex/nav/2015/284/p3412840.15n.Z,2015-10-11 00:00:00,2015-10-11 23:59:45,15.0,
P341,GNSS RINEX Observation QC Report,6670c91ff8ce9bb7f449bc19f60dd86e,20463,2015-10-09 00:00:00,ftp://data-out.unavco.org/pub/rinex/qc/2015/281/p3412810.15S,2015-10-08 00:00:00,2015-10-08 23:59:45,15.0,
P341,GNSS RINEX Observation (Unix Compressed),7903a3a764f3d93757cf290754491b93,1738779,2015-10-09 00:00:00,ftp://data-out.unavco.org/pub/rinex/obs/2015/281/p3412810.15o.Z,2015-10-08 00:00:00,2015-10-08 23:59:45,15.0,
P341,GNSS RINEX Observation (Hatanaka  Unix Compressed),191cfaf652c33ddc3bbfb957734d7d34,611229,2015-10-09 00:00:00,ftp://data-out.unavco.org/pub/rinex/obs/2015/281/p3412810.15d.Z,2015-10-08 00:00:00,2015-10-08 23:59:45,15.0,
P341,GNSS Navigation,4ccbd8dd55e0093aa4c82d63e85538cd,31453,2015-10-09 00:00:00,ftp://data-out.unavco.org/pub/rinex/nav/2015/281/p3412810.15n.Z,2015-10-08 00:00:00,2015-10-08 23:59:45,15.0,
P341,GNSS RINEX Observation QC Report,2
             '''
             # split out values from line, at commas
             strlist= string.split( line, "," )

             # all these values are strings, not numbers
             staid              = (strlist[0])      # 4 char station ID code
             ftype              = (strlist[1])      # char string name of the file type like RINEX nav file ; see more below about ftype
             file_MD5           = (strlist[2])      # MD5 check sum value of the data file
             fsize              = (strlist[3])      # in bytes
             published_time     = (strlist[4])      # published date of the data file in its native archive
             file_url           = (strlist[5])      # the URL to download this, like ftp://data-out.unavco.org/pub/rinex/obs/2015/284/p3412840.15d.Z
             datafile_start_time= (strlist[6])      # data start time, ISO 8601 format
             datafile_stop_time = (strlist[7])      # data end time
             # float: 
             sample_interval    = float( (strlist[8]) ) # data sample interval, seconds

             logWrite( "\n  site "+staid+" metadata file line = " +line[0:-1] )  
             print   ( "\n  site "+staid+"    metadata line= \n   " +line[0:75] +"\n   " +line[75:-1])  
             # :-1] means do not print the line's terminal line return \n 
             #logWrite(   " id code, filetype, MD5, published time, file URL, datafile_start_time, datafile_stop_time")

             filext =file_url[-3:]
             print( "      filext="+filext)

             doc='''
                select * from  data_type;
                +--------------+-----------------------------------------+---------------------------------------------------+
                | data_type_id | data_type_name                          | data_type_description                             |
                +--------------+-----------------------------------------+---------------------------------------------------+
                |            1 | instrumental data                       | any type instrumental native, raw, or binary data |
                |            2 | GNSS observation file                   | any GNSS obs file                                 |
                |            3 | GPS navigation file                     | a GPS          navigation file                    |
                |            4 | Galileo navigation file                 | a Galileo GNSS navigation file                    |
                |            5 | GLONASS navigation file                 | a GLONASS GNSS navigation file                    |
                |            6 | GNSS meteorology file                   | a GNSS meteorology       file                     |
                |            7 | QZSS navigation file                    | a QZSS GNSS navigation file                       |
                |            8 | Beidou navigation file                  | a Beidou GNSS navigation file                     |
                |            9 | Final Daily time series                 | Final Daily time series solution                  |
                |           10 | Rapid Daily time series                 | Rapid Daily time series solution                  |
                |           11 | Rapid 5 minute time series              | Rapid 5 min time series solution                  |
                |           12 | Ultra Rapid 5 minute time series        | Ultra Rapid 5 min time    series solution         |
                |           13 | Ultra Rapid 5 minute Combo time series  | Ultra Rapid 5 min Combo time series solution      |
                |           14 | Nights Ultra Rapid 5 minute time series | Night Ultra Rapid 5 min time series    solution   |
                |           15 | Time series plot                        | Time series static plot image                     |
                |           16 | Time series cleaned plot                | Time series cleaned static plot image             |
                |           17 | Time series Rapid 5 min plot            | Time series Rapid 5  minute plot image            |
                |           18 | SINEX product                           |                                                   |
                |           19 | GNSS sites velocities                   |                                                   |
                |           20 | GNSS sites positions                    |                                                   |
                |           21 | strainmeter observations                |                                                   |
                |           22 | tidegage observations                   |                                                   |
                |           23 | tiltmeter observations                  |                                                   |
                |           24 | DORIS                                   | DORIS                                             |
                |           25 | SLR                                     | SLR                                               |
                |           26 | VLBI                                    | VLBI                                              |
                |           27 | teqc qc S file                          | teqc QC summary S file                            |
                +--------------+-----------------------------------------+---------------------------------------------------+
	         '''

             # from file type name strings from the input file, select the file type id number  for the GSAC Prototype 15 database 
             filetypeid=0    # for the file type id number database.
             file_type="na" # used in the ftp url for the local ftp service.

             # for logs only:
             fsizebytes = long(fsize)                  # convert the string to long integer for computations
             fsizeMB    = (fsizebytes*1.0) / (1048576) # convert integer bytes to megabytes as a float

             # Set file type id for this file, as used in the GSAC Prototype 15 database
             usethisfile= True
             datafile_format_id=0
             # LOOK new an also set values of datafile_format_id here: ADD NEW CODE here below
             # `datafile_format` VALUES (1,'RINEX 2'),(2,'RINEX 3'),(3,'BINEX'),(4,'SINEX'),(5,'UNR tenv3 northings and eastings'),(6,'UNR txyz2 Cartesian xyz'),(7,'UNR tenv traditional NEU'),(8,'plot image'),(9,'UNR  station QC estimate .qa file'),(10,'UNR kenv 5 minute products'),(11,'UNR krms RMS products'),(12,'DORIS'),(13,'SLR'),(14,'VLBI'),(15,'BOTTLE'),(16,'SEED'),(17,'PBO GPS Velocity Field Format'),(18,'PBO GPS Station  Position Time Series, .pos');
             if ('highrate' in  file_url ) :  # special case from UNAVCO
                  usethisfile= False 
                  countskips+=1
                  usethisfile= False
                  logWrite( "    Skip this highrate file "+file_url )
             elif ("d.Z" == filext  ) :   #and 'bservation' in ftype):  # Hatanaka-compressed GNSS RINEX Observation 
                  filetypeid=2
                  countobs += 1;
                  obssizeMB += fsizeMB 
                  file_type="obs"
             elif ("o.Z" == filext  ) :   #and 'bservation' in ftype):  # compressed GNSS RINEX Observation 
                  filetypeid=2
                  countobs += 1;
                  obssizeMB += fsizeMB 
                  file_type="obs"
             elif ("n.Z" == filext  ) :   #and 'avigation' in ftype): # GPS nav file
                  filetypeid=3
                  countnav += 1;
                  navsizeMB += fsizeMB
             elif ("g.Z" == filext  ) :   #and 'avigation' in ftype): # GLONASS, "g" nav file
                  filetypeid=5
                  countnav += 1;
                  navsizeMB += fsizeMB
             elif ("e.Z" == filext  ) :   #and 'avigation' in ftype): # Galileo, "e" nav file
                  filetypeid=4
                  countnav += 1;
                  navsizeMB += fsizeMB
             elif ("m.Z" == filext  ) :   #and 'eteorolog' in ftype): # met file
                  filetypeid=6
                  countmet += 1;
                  metsizeMB += fsizeMB
                  file_type="met"
             elif ( "S" == filext[-1:]): # teqc QC summary S file
                  filetypeid=27
                  countmet += 1;
                  metsizeMB += fsizeMB
                  file_type="S"
             else:
                  usethisfile= False 
                  countskips+=1
                  print( "    Skip this file: its file type, "+file_url + ", is not wanted. \n")
                  logWrite( "    skip this file "+file_url + ", its file type is not wanted.")
                  continue

             print "      station "+staid+" \n      ftype="+ftype+"_\n      file_MD5="+file_MD5+"_\n      fsize="+fsize+" bytes_\n      file_url="  +file_url+"_ \n      datafile_start_time="+datafile_start_time+"_ \n      datafile_stop_time="+datafile_stop_time+"_ \n      si="+`sample_interval`+"_  \n      file type ID="+`filetypeid`

             if usethisfile :

                 totalMB += fsizeMB 

                 # wrangle some more data values for the db from file_url such as ftp://data-out.unavco.org/pub/rinex/obs/1996/054/bogt0540.96d.Z
     
                 ind = file_url.find(":");   # from ftp://data-out.unavco.org/pub/rinex/obs/2015/284/p3412840.15d.Z
                 file_url_protocol = file_url[:ind]
                 print "      file_url_protocol = _"+file_url_protocol  +"_"

                 ind = file_url.rfind("/") + 1;  # find index after last /
                 datafile_name = file_url[ind:]
                 datafile_path = file_url[:ind]
                 print "      filename ="+datafile_name+"   file path ="+datafile_path

                 # full log logWrite( "   For the data file "+datafile_name+ ", find its equipment sessions' id number, load database datafile table with the file's metadata, and download the file from remote GSAC")

                 filestacode= datafile_name[:4]
                 filestacode = filestacode.upper()
                 print "      data file name = "+datafile_name +" has station code _"+ filestacode +"_"

                 if staid != filestacode:
                     logWrite("   PROBLEM: data file "+file_url+" is not for station(id) associated in GSAC file info list, id="+staid +". \n")
                     sys.stdout.flush()
                     # to force this file not to be loaded, and skip to next file
                     filestacode="Bad_site_for_file"
                     continue
                     # or could do, to halt processing:
                     # sys.exit(1)

                 i1 = file_url.rfind("//") + 2;  # find index after the first //
                 tmp=file_url[i1:]
                 i2 = tmp.find("/") 
                 file_url_ip_domain = tmp[:i2]
                 print "      file_url_ip_domain = _"+ file_url_ip_domain +"_"

                 ind = tmp.rfind("/") +1 ;  # find index after last /
                 file_url_folders = tmp[i2:ind]
                 print "      file_url_folders ="+ file_url_folders +"_"

                 # get year and day from normal case like
                 data_day_of_year = file_url_folders[-4:-1]
                 data_year = file_url_folders[-9:-5]
                 # use data_year=  long(data_year) # to convert the string to long integer for computations

                 # OR, get year and day from special case like ftp://data-out.unavco.org/pub/highrate/1-Hz/rinex/2013/223/lmmf/lmmf2230.13d.Z

                 print "      year ="+ data_year + ",  day of year ="+ data_day_of_year+"."
                 print "      publish time ="+ published_time

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

                 # already known:
                 URL_path   = file_url_folders
                 #original_datafile_name = datafile_name
                 datafile_type_id  = filetypeid
                 year          = data_year
                 day_of_year   = data_day_of_year
                 size_bytes    = fsizebytes
                 MD5           = file_MD5

                 station_id        = 1  # to be determined below 
                 equip_config_id   = 1  # to be determined below

                 # find station_id number in the database:
                 if (lastfilestacode == filestacode) :
                       pass # still working on same station as the preceeding line's station
                 else :
                     try:
                        station_id  = getTableRowID ("station_id", "station", "four_char_name", filestacode)
                        lastfilestacode == filestacode
                        print "      station id="+`station_id`
                     except:
                          #if logflag >= 2 and filestacode!="Bad_site_for_file" : 
                          logWrite("      >>>>>>>>>>>>   PROBLEM: Did not find this station in the db: "+filestacode+".  Please populate station and equip_config tables. \n")
                          #print "    Except: Skip this file, "+file_url
                          #if filestacode!="Bad_site_for_file" : 
                          #listFile.close()
                          logFile.close()
                          gsacdb.close()
                          sys.stdout.flush()
                          continue
                          #sys.exit(1)


                 #continue # for testing skip next code

                 haveitinDB=False
                 dfid=None
                 esid=None
                 isPriorLoad=False

                 #print "  go do next line for test...\n"
                 #continue

                 # check if this  datafile's metadata is already in the db table 'datafile':
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
                     #logWrite("      data file to load in db, and to download is  "+datafile_name)
                     # see if is a valid equip_config_id for an equipment session corresponding to this  data file :
                     try:
                             print "      Load datafile metadata for station "+filestacode+" (id "+`station_id`+")"
                             statement=("SELECT equip_config_id from equip_config  where  station_id= %s and equip_config_start_time<='%s' and (equip_config_stop_time>='%s' or equip_config_stop_time = '0000-00-00 00:00:00') " % (station_id, datafile_start_time, datafile_stop_time) )
                             print ("      find equip id with sql: \n     "+ statement)
                             cursor.execute(statement)
                             row= cursor.fetchone()
                             esid=  row[0];
                             esid=  int(esid); # fix the "L" value returned
                             print ( "      this data file belongs to equip session # "+`esid`+".")

                             insertstmt=""
                             doc= '''
desc datafile;
+-------------------------+-----------------+------+-----+---------+----------------+
| Field                   | Type            | Null | Key | Default | Extra          |
+-------------------------+-----------------+------+-----+---------+----------------+
| datafile_id             | int(9) unsigned | NO   | PRI | NULL    | auto_increment |
| station_id              | int(6) unsigned | NO   | MUL | NULL    |                |
| equip_config_id         | int(6) unsigned | YES  | MUL | NULL    |                |
| datafile_name           | varchar(120)    | NO   |     | NULL    |                |
| URL_complete            | varchar(220)    | NO   |     | NULL    |                |
| URL_protocol            | varchar(7)      | YES  |     | NULL    |                |
| URL_domain              | varchar(50)     | YES  |     | NULL    |                |
| URL_path_dirs           | varchar(70)     | YES  |     | NULL    |                |
| data_type_id            | int(3) unsigned | NO   | MUL | NULL    |                |
| datafile_format_id      | int(3) unsigned | YES  | MUL | NULL    |                |
| data_reference_frame_id | int(3) unsigned | YES  | MUL | NULL    |                |
| datafile_start_time     | datetime        | NO   |     | NULL    |                |
| datafile_stop_time      | datetime        | NO   |     | NULL    |                |
| datafile_published_date | datetime        | YES  |     | NULL    |                |
| sample_interval         | float           | YES  |     | NULL    |                |
| latency_estimate        | float           | YES  |     | NULL    |                |
| year                    | year(4)         | YES  |     | NULL    |                |
| day_of_year             | int(3)          | YES  |     | NULL    |                |
| size_bytes              | int(10)         | YES  |     | NULL    |                |
| MD5                     | char(32)        | YES  |     | NULL    |                |
| originating_agency_URL  | varchar(220)    | YES  |     | NULL    |                |
+-------------------------+-----------------+------+-----+---------+----------------+
                             '''
                             try:
                                logWrite( "     Insert the gps data file's metadata into the db:")
                                print ( "     Insert the gps data file's metadata into the db:")
                                toinserts = toinserts +1 
                                #insertstmt=("INSERT into datafile (station_id,  equip_config_id, datafile_name,  data_type_id, sample_interval, datafile_start_time, datafile_stop_time, datafile_published_date, year, day_of_year, size_bytes, MD5, URL_path) values (%s, %s, '%s',  %s, %s, '%s', '%s', '%s', %s, %s, %s,  '%s', '%s')"  %  (`station_id`, `esid`, datafile_name,  datafile_type_id, sample_interval, datafile_start_time, datafile_stop_time, published_time, year, day_of_year, size_bytes, MD5, URL_path ))
                                insertstmt=("INSERT into datafile (station_id,  equip_config_id, datafile_name, URL_complete,URL_protocol,URL_domain,URL_path_dirs, data_type_id, sample_interval, datafile_start_time, datafile_stop_time, datafile_published_date, year, day_of_year, size_bytes, MD5) values (%s, %s, '%s', '%s', '%s','%s', '%s', %s, %s, '%s', '%s', '%s', %s, %s, %s,  '%s')"  %  (`station_id`, `esid`, datafile_name, file_url, file_url_protocol,file_url_ip_domain,file_url_folders, datafile_type_id, sample_interval, datafile_start_time, datafile_stop_time, published_time, year, day_of_year, size_bytes, MD5 ))
                                print "        SQL is "+insertstmt 
                                cursor.execute(insertstmt)
                                gsacdb.commit()
                                countinserts = countinserts +1 
                                logWrite(  "     Inserted data file's metadata into db." )
                                print   (  "     Inserted data file's metadata into db. \n" )
                                haveitinDB=True
                             except:
                                logWrite(  "    PROBLEM: failed to insert this data file's ("+file_url+") metadata into the db table datafile  for equip session "+esid)
                                logWrite(  "    with SQL "+insertstmt)
                                failinserts = failinserts +1 
                                gsacdb.rollback()
                     except:
                             print ( "      no equip session for this data file; or no insertion.   Try again later after the sessions are updated. ")
                             continue 
                              
                     skip='''

                     sys.stdout.flush()

                     # overall total count; size for this station only; print "        obs files count and total size(MB): "+`countobs`+"   "+`obssizeMB`
                     #print "        nav files count and total size(MB): "+`countnav`+"   "+`navsizeMB`
                     #print "        met files count and total size(MB): "+`countmet`
                     #print "        running values:        size total =     "+`totalMB` +" MB"

                 if haveitinDB :
                        # Now download the datafile itself from url file_url 
                        logWrite(  "     next, download data file from " + file_url)
                        cmd4 = "wget -N -nv -x -nH -P /data "+file_url
			# wget manuals: http://www.gnu.org/software/wget/manual/wget.html;   http://www.gnu.org/software/wget/manual/
			#  -N means wget will ask the server for the last-modified date. If the local file has the same timestamp as the server, or a newer one, the remote file 
			#     will not be re-fetched. However, if the remote file is more recent, Wget will proceed to fetch it.
                        # -nv means not verbose; restrict the screen output. -v is very verbose logging.
                        #  -x means make dirs, when needed.  
                        # -nH means do not use the remote domain hostname, the 'data-out.unavco.org' part in ftp://data-out.unavco.org/pub/rinex/obs/2013/334/acp13340.13d.Z 
                        # '-P /data' means put file (and its directories) under /data.  
                        # So with '-x -nH -P /data' you make a new file (and directories) like /data/pub/rinex/obs/2013/334/acp13340.13d.Z
                        # (stored in the exact same file path as at remote server)
                        # options not used:
                        # --mirror is for recurvise downloads.
                        #  -c means 'continues if interrupted', and SKIP downloading if file is there already. But -c checks for EXACT same size and same name,
                        #  so the wget with -c downlaod FAILS if the file size has changed as does happen at UNAVCO, and gives status 2048 error.  Don't use -c!
                        # -N is right for our use: get a newer version, regardless of size changes, and don't download if not newer.

                        if isPriorLoad:
                            logWrite( "     Verify already having this data file with command "+cmd4)  #  check for OK name and size if already here.
                        else :
                            # full log logWrite( "     Download this data file with command "+cmd4)  #  get the file if not already here.
                            pass
 
                        cstatus4 = 2
                        #cstatus4 = os.system(cmd4)
                        # for wget return status values, see http://www.gnu.org/software/wget/manual/html_node/Exit-Status.html
                        # Note that "success" can mean 'file was previously downloaded'.
                        if cstatus4 == 0 :
                            logWrite( "     No problems downloading or verifying file.") # with command "+cmd4) 
                            wgetfilecount += 1
                        else :
                            logWrite("     PROBLEM: command "+cmd4+"\n     returned status="+`cstatus4`+".  Repeat that wget command by hand, with -v verbose (not -nv) to see error.")
                            nogetcount +=1
                 else :
                        pass
                 '''

                 # end of this one data file handling
                 sys.stdout.flush()

          # last line in for loop reading lines in dataListFile, each line for one data file at the remote GSAC for this station in this date range.
                 
        print "\n     -----------------  Checked "+`linesread`+" lines."

        # close the GSAC CSV file with this station's datafile information:
        dataListFile.close()

        # do the accounting sums for this one station:
        obsfilestotalsize += obssizeMB
        navfilestotalsize += navsizeMB
        metfilestotalsize += metsizeMB
        totalsizes += totalMB
        numbstawithdata +=1

        #print   " for this station, the total of data file sizes added = "+`totalMB` +" MB or "+ `(totalMB/1024)`+" GB"

        #end of parseMetadata 


def logWrite (text):
     global logFile
     logFile.write(text + "\n")
      # for debugging runs to see output on screen:
     if logflag>=2: print (text)


        
def getTableRowID (idname, tablename,  rowname, rowvalue) :
       global logFile
       " find the id for the value rowvalue in the db table tablename."
       the_id=1 #   "not specified" meaning.
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
inputfilename =args[5]  # typically data_file_info.csv

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
logfilename = "populate_GSAC_db_datafile_metadata.log" # +dom  MAY add day of month to log file name 
logFile = open(logfilename, 'w')

timestamp   =strftime("%Y-%m-%d_%H:%M:%S", gmtime())


parseMetadata()  # for data_file_info.csv


logWrite( "\n  \n        Summary of populate_GSAC_db_datafile_metadata.py "+datadatefrom+" to "+datadateto+":")
# old logWrite( "\n          number of "+stationgroup+" stations in the remote GSAC archive checked for GNSS data files in this time interval: "+`numbstawithdata`)
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
# logWrite( "          count of not required data files is "+`countskips` + " (types not wanted for a Dataworks mirror)")
logWrite("\n          About new files to download:")
logWrite(  "          db: count of data files' info TO insert in the db                 "+`toinserts`)
logWrite(  "          db: count of data files' info Inserted in the db                  "+`countinserts`)
logWrite(  "          db: count of data files' info FAILED inserts in the db            "+`failinserts`)
logWrite("\n          files: success count of data files to download from the remote GSAC, or already downloaded:   "+`wgetfilecount`)
logWrite(  "          files: count of wget problems encountered                                             "+`nogetcount`)
timestamp=  strftime("%a, %d %b %Y %H:%M:%S +0000", gmtime())  #  strftime("%d-%M-%Y %H:%M:%S", gmtime)
logWrite("\n                 Completed populate_GSAC_db_datafile_metadata.py at " +timestamp+ " UTC\n")

# close the GSAC  CSV file with the site list information:
#listFile.close()
# disconnect from db server
gsacdb.close()
logFile.close()

sys.exit (0) # return success

# ALL DONE
