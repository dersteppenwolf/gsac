#!/usr/bin/python
'''
 filename              : mirrorStations.py
 author                : Stuart Wier 
 created               : 2014-09-03
 latest update         : 2015-01-14 improve comments and log file wording.

 tested and verified   : 2016-03-16 tested with latest GSAC dataworks code in SourceForge.  Correctly loaded COCONet networks station and instrument data.

 exit code(s)          : 0, success
                       : sys.exit (1), curl failed

 description           UNAVCO GSAC, "Dataworks code" to populate the GSAC database station table and equip_config table information using a query to another GSAC for the information.

                       The use is:
                       1. to make the initial population of a Dataworks database with all stations in the network, and all the equipment sessions at each station.
                       2. to find and add newly-added stations in the network (and the new equipment sessions at that station).
                       3. to update existing equipment sessions (db equip_config records), when the metadata was changed at the remote GSAC. Usually session stop_time.

                       The metadata comes from a remote GSAC such as the UNAVCO GSAC at http://www.unavco.org/software/data-management/gsac/gsac.html. (see [1])

                       The metadata from GSAC is provided in a file with format "GSAC full csv file", named in this case dataworks_stations.csv (see [1])

                       NOTE You MUST run this script everytime you run mirrorData.py, before you run mirrorData.py, since the remote GSAC should change equipment session end times
                         everytime a new file comes in for a station.  If you try to download a new file for a session when the db equip_config_stop_time is older, the download fails.

 configuration:        : First, one time only, for your network and operations, revise satellite_system ="GPS"    # default  could be for example "GPS,GLONASS"   CHANGE    near line 487
 
                         and see to these CHANGE lines:

                       : CHANGE URL for a different domain and a similar GSAC API URL from other remote GSACs.
                       : CHANGE revise 'unknown' in this line to have your acronym in place of 'unknown':

		       : CHANGE # look to NOT get local stations such as "GeoRED" stations put back in your GSAC, where they originated:

			           : set the value of logflag to choose if you want to see output to the terminal:
                         logflag =1  # controls screen output.  CHANGE: USE =1 for routine operations. OR use =2 to print log lines to screen, for testing, near line 674
                       

 usage:                    Run this script (as with Linux crontab job), to look for new or changed station info.
                  
                           This Python is run with a command like this:

                              /dataworks/mirror_station_metadata/mirrorStations.py   stationgroup   dbhost   dbaccount   dbaccountpw   dbname

                           Use your names for the database account name, account password, and database name such as Dataworks

                           For the "stationgroup" command line argument or value, you use the network name (like COCONet) as found in 
                           the remote GSAC archive which you mirror. 

                            so the nominal command 
                              /dataworks/mirror_station_metadata/mirrorStations.py   stationgroup  dbhost    dbaccount   dbaccountpw          dbname

                            becomes something like
                             ./mirrorStations.py  COCONet  localhost  root  batenococ Dataworks_GSAC_database 

                           Or, for stationgroup use, for separate stations, not a network name,  
                           inside "", have:
                              a. a single station's four char ID  like "POAL"
                              b. semi-colon separated list of four char IDs, separated with ";"   like    "p123;p456"  
                              c. wildcards like "TN*"

                           So for the stationgroup argument, put all of a. thru c. in one string with no spaces, like  "POAL;P12*;TN*"
                            The semi-colon separates the items in stationgroup argument.
                           Upper case and lower case in station four char IDs are the same in GSAC use.

                            so a command is like

                          /dataworks/mirror_station_metadata/mirrorStations.py  "POAL;TNAM"  localhost  dataworks  dbpassword  Dataworks

                          Use this option with caution; you can add a lot of stations not in your network to your system with one simple command.

                          Running the process takes about 1 second per site; and makes a log file, /dataworks/logs/mirrorStations.py.log

                        2. Look at the log file after each run.  Look for errors noted in lines with "PROBLEM" and LOOK  and fix any problems.

                        3. Update these database tables by hand when a new station is added (no such data is available from the from GSAC): 
                           radome_serial_number in equip_config; and in table station, update field values for  operator_agency_id, 
                                  and data_publisher_agency_id. You may need to insert a new agency in the db agency table, first.

 tested on             : Python 2.6.5 on Linux (Ubuntu) ; CentOS Python 2.6 

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
import mmap
import smtplib
from   email.mime.text import MIMEText

import MySQLdb
# To install Python's MySQLdb on ubuntu/debian, just do command   
#     sudo apt-get install python-mysqldb
# Also; see this http://stackoverflow.com/questions/372885/how-do-i-connect-to-a-mysql-database-in-python
        
def load_db ():
    global logFile 
    global logfilename 
    global failedcount
    global newstacount
    global eqscount
    global stoptimeupdatecount
    global donecount
    global newsessioncount
    global stationgroup
    newstacount=0
    newsessioncount=0
    failedcount =0 
    eqscount=0
    stoptimeupdatecount=0

    # [1]
    # Do the query to the remote GSAC's API, to make a list of all stations' info in your network, in the file dataworks_stations.csv.
    # The metadata is obtained using a remote GSAC service, such as at UNAVCO (http://www.unavco.org/software/data-management/gsac/gsac.html). 
    # The metadata from GSAC is provided in a "GSAC full csv file", named in this case dataworks_stations.csv.

    dom =strftime("%d", gmtime())  # day of month, such as "16", to use in log file name

    # local log file:
    logfilename = "mirrorStations.py.log."+dom   # CHANGE use this for a local log file
    # or with full path like logfilename = "/dataworks/logs/mirrorStations.py.log."+dom
    logFile = open (logfilename, 'w')  # NOTE this creates a NEW file of the same log file name, destroying any previous log file of this name from previous month.

    timestamp   =strftime("%Y-%m-%d_%H:%M:%S", gmtime())
    logWrite("\n    Log of mirrorStations.py "+timestamp + ".  The log file is "+logfilename )
    logWrite(  "    Look at this log file after each run.  Look for errors noted in lines with PROBLEM or LOOK and fix those issues. ***** *****")

    # compose the remote GSAC's API query string.
    # like /usr/bin/curl -L "http://www.unavco.org/gsacws/gsacapi/site/search?site.group=COCONet&output=sitefull.csv&site.interval=interval.normal&site.status=active"                      > somefilename.csv

    # CHANGE URL for a different domain and a similar GSAC API URL from other remote GSACs.
    # CHANGE revise 'unknown' in this line to have your acronym in place of 'unknown':
    httppart=  ' "http://www.unavco.org/gsacws/gsacapi/site/search?output=sitefull.csv&site.group='+stationgroup+'&site.status=active&user=unavcotest&site.interval=interval.normal" '

    # in case of separate station IDs:
    if ";" in stationgroup or len(stationgroup)<5:
        # search for site by ID, and cut off trailing final ";" 
        # CHANGE URL for a different domain and a similar GSAC API URL from other remote GSACs.
        # CHANGE revise 'unknown' in this line to have your acronym in place of 'unknown':
        httppart=' "http://www.unavco.org/data/web-services/gsacws/gsacapi/site/search?output=sitefull.csv&site.code='+stationgroup+'&user=unavcotest"'
        logfilename = logfilename + ".extras"

    # compose the command to make the query using the Linux 'curl' command line utility:
    cmd1 = "/usr/bin/curl -L "+ httppart + " > dataworks_stations.csv"
    # -L handles any HTML address redirect on remote server end.

    skip='''
    logWrite(  "    mirrorStations.py loads or updates a GSAC dataworks database with station and equipment session data from a 'GSAC full csv file' made by the GSAC server."  )
    logWrite(  "    The use is:"  )
    logWrite(  "       1. To make the initial (first time) population of your Dataworks database with all stations in the network, and all the equipment sessions at each station."  )
    logWrite(  "       2. To find and add any newly-added stations in the network (and add the equipment sessions at that station)."  )
    logWrite(  "       4. To update the equip session end times at the still-active sessions (which should have end time of 'end of today') "  )
    '''

    if ";" in stationgroup or len(stationgroup)<5:
      logWrite("\n    Update these individual sites: "+stationgroup );
    else:
      logWrite("\n    Update all stations in the "+stationgroup+" network." );

    logWrite(    "    First, get site and equipment metadata at those sites from the remote GSAC. The GSAC API Linux command is \n    "+cmd1 )
    sys.stdout.flush()

    # execute the command to make the query using the Linux 'curl' command line utility:
    cstatus1 = os.system(cmd1)
    # note that this Python process pauses until the cmd1 process completes. Typical time elaspsed is 1 second per station, or 2 mins for 120 stations.

    donecount=0
    # handle failed connection: 
    if cstatus1 != 0 :
          logWrite("\n    PROBLEM: curl command to get sites info from remote GSAC failed." );
          logWrite(  "    curl command was "+cmd1 +"\n" );
          #print ("\n    PROBLEM: curl command to get sites info from remote GSAC failed." );
          #print (  "    curl command was "+cmd1 + "\n" );

          time.sleep(60) # approx. 60 seconds
          cstatus1 = os.system(cmd1)
          if cstatus1 != 0 :
              logWrite("\n    PROBLEM: 2nd try of the curl command, after 60 sec pause, to get sites info from remote GSAC failed. Exit." );
              sys.exit (1)



    # For each  station in the list, get metadata about the station and equipment sessions there.

    if cstatus1 != 0 :
        logWrite("\n    PROBLEM: non-zero status value=" +`cstatus1` + "  from "+cmd1 +" \n Exit. \n" );
        sys.exit (1)
    elif cstatus1 == 0 :
        logWrite("\n    Success running "+cmd1 );
        logWrite("\n    Open the file dataworks_stations.csv \n" );
        station_metadata_file = open ("dataworks_stations.csv");
        # logWrite("    Opened 'GSAC full csv file' of station and equipment session data, dataworks_stations.csv, made with the GSAC query."  );
        # read and count how many lines in file
        allLines = station_metadata_file.readlines()
        linecount = len(allLines)
        station_metadata_file.seek(0) # rewind to beginning
        if linecount > 4 : logWrite(    "    There are "+`(linecount-4)`+" station - equipment sessions in this file."  )
        sys.stdout.flush()
        # full csv files have one or more lines for every station, one line per equipment session.  Use the station 4char ID ("code") to keep track which one is in use.
        code="" # the 4 char id of a station such as ABMF
        this_station_code="" # working station now
        previous_station_code=""  # the previous station in use
        prev_start_time =""
        prev_stop_time  =""
        donecount=0
        station_id = None 
        station_counter=0;
        # station 4-char id list to help manage the same
        sta_ID_list=[]
        prev_metadata = " "

        # To use the official IGS file rcvr_ant.tab
        notes='''
    | This file details naming conventions for IGS equipment descriptions in       |
    | site logs, RINEX headers, and SINEX.  Additional and historical equipment    |
    | names also appear here for convenience.  Please refer to the document        |
    | "IGS Site Guidelines" @ http://www.igs.org/network/guidelines/guidelines.html|
    | for guidance in selecting equipment suitable for use in the IGS Network.     |
    | The IGS Central Bureau, with support from the IGS Infrastructure Committee   |
    | and IGS Antenna Working Group maintains this list and should be contacted    |
    | at igscb @ igscb.jpl.nasa.gov with any comments.                             |   
        The file lists IGS's names for Domes, Receivers and Antennae.                           '''
        igsfileok=False

        # get the IGS file to get the  list of IGS radomes, receivers, and antenna names in the official IGS file, at this URL:
        # print "\n    The IGS file to get is igscb.jpl.nasa.gov/igscb/station/general/rcvr_ant.tab: \n";
        igs_cmd= "wget -v -N --no-check-certificate http://igscb.jpl.nasa.gov/igscb/station/general/rcvr_ant.tab"
        logWrite("\n    Get IGS file rcvr_ant.tab, with definitions of 'correct' Domes, Receivers and Antennae names, using command \n     "+igs_cmd)
        print("\n    Get IGS file rcvr_ant.tab, with definitions of 'correct' Domes, Receivers and Antennae names, using command \n     "+igs_cmd)
        try:
           igs_status1 = os.system(igs_cmd)
           if igs_status1 != 0 :
               logWrite("  PROBLEM: command "+isg_cmd+" returned status="+`igs_status1`)
               print("  PROBLEM: command "+isg_cmd+" FAILED: returned status="+`igs_status1`)
        except :
           pass # logWrite("    PROBLEM: try of command   "+isg_cmd+"    failed.")

        # Regardless of wget results, an old (or brand new) igs rcvr_ant.tab file should be here.
        # try file open in current working directory.
        try:
           igs_file = open ("rcvr_ant.tab");
           #logWrite("    igs file open OK, for rcvr_ant.tab in current working directory.")
           igsfileok=True
        except :
           #logWrite("    no local copy of rcvr_ant.tab yet")
           pass
        if False==igsfileok :
            # or try file open here:
            try:
                igs_file = open ("/dataworks/mirror_station_metadata/rcvr_ant.tab");
                #logWrite("    igs file open OK, for /dataworks/mirror_station_metadata/rcvr_ant.tab")
                igsfileok=True
            except :
                pass #logWrite("    igs file open fails,  for /dataworks/mirror_station_metadata/rcvr_ant.tab")
        if igsfileok :
            logWrite   ("    Got and opened the most recent IGS file rcvr_ant.tab")
            print      ("    Got and opened the most recent IGS file rcvr_ant.tab")
        else :
            logWrite("    PROBLEM: could not find and open an IGS rcvr_ant.tab file. Exit.  \n");
            sys.exit(1)


        # get contents of the rcvr_ant.tab file for later searches:
        igsmap = mmap.mmap(igs_file.fileno(), 0, access=mmap.ACCESS_READ)

        # read each line in the input Full CSV file, the results from the remote GSAC; i is 0 base
        for i in range(linecount) :

           line = station_metadata_file.readline()
           metadata = line

           # i>3 is to bypass 4 header lines ; and perhaps some others if testing is the i<35 say
           if (i>3):


             # to skip a "PENDING" station: trap the SPECIAL UNAVCO magic values:
             # as in CN45,CN_Toco_GPS_2013,10.837,-60.9383,33.2,building wall,,2050-01-01T00:00:00,1980-01-01T00:00:00,TRM59800.00,SCIT,5225354537,0.0083, ...
             if  "2050-01-01" in line and "1980-01-01" in line:
                # one file line is one equipment session for one station. 
                logWrite("\n    "+ `(i-3)` + " Next line in stations' equipment session file: "+line  );
                # SKIP this station,  and go try the next line
                logWrite("      This is a 'pending' station an UNAVCO, with start time 2050-01-01 and end time in 1980; skip entry of this metadata."  );
                continue

             # split out values from line, at commas
             strlist= string.split( line, "," )

             example_line='''
             a full sites csv file format example:
             There are four header lines, not used here. There are  fields: first at [0]

                #fields=ID[type='string'],station_name[type='string'],latitude,longitude,ellip_height[unit='m'],monument_description[type='string'],IERSDOMES[type='string'],session_start_time[type='date' format='yyyy-MM-ddTHH:mm:ss zzzzz'],session_stop_time[type='date' format='yyyy-MM-ddTHH:mm:ss zzzzz'],antenna_type[type='string'],dome_type[type='string'],antenna_SN[type='string'],Ant_dZ,Ant_dN,Ant_dE,receiver_type[type='string'],firmware_version[type='string'],receiver_SN[type='string'],receiver_sample_interval,city_locale[type='string'],state_prov[type='string'],country[type='string'],X,Y,Z,agencyname[type='string'],metpackname[type='string'],metpackSN[type='string'],networks[type='string'],site_count
                #   Generated by UNAVCO GSAC Repository on 2014-08-29T16:38:13 
                #   Missing times (no characters) may mean 'not removed' or 'no change.' 
                #   The CSV convention for point data is CF for CSV; 
ABMF,Aeroport du Raizet -LES ABYMES - Mitio France,16.2623,-61.5275,-25.67,building roof,97103M001,2015-04-28T15:00:30,2015-12-07T23:59:30,TRM57971.00,NONE,1441112501,0.0000,0.0000,0.0000,LEICA GR25,3.11,1830399,30,LES ABYMES,Guadeloupe,France,,,,,,,COCONet;COCONetPartner;IGS;Low Latency;PBO Analysis Complete;,1
ABVI,Anegada,18.7297,-64.3325,-35.55,deep foundation pillar,,2011-02-04T15:12:15,2012-09-13T23:59:45,TRM29659.00,SCIT,02200669,0.0083,0.0000,0.0000,TRIMBLE NETRS,1.3-0  05 Apr 2010,4720132687,15,Anegada,,British Virgin Islands,,,,,,,COCONet;COCONetPartner;Met Sites;PBO Analysis Complete;Puerto Rico;,2
ABVI,Anegada,18.7297,-64.3325,-35.55,deep foundation pillar,,2012-09-14T00:00:00,2013-04-22T23:59:45,TRM29659.00,SCIT,02200669,0.0083,0.0000,0.0000,TRIMBLE NETRS,1.3-1 10/DEC/2010,4720132687,15,Anegada,,British Virgin Islands,,,,,,,COCONet;COCONetPartner;Met Sites;PBO Analysis Complete;Puerto Rico;,2
ABVI,Anegada,18.7297,-64.3325,-35.55,deep foundation pillar,,2013-04-23T00:00:00,2014-04-30T23:59:45,TRM29659.00,SCIT,02200669,0.0083,0.0000,0.0000,TRIMBLE NETRS,1.3-2,4720132687,15,Anegada,,British Virgin Islands,,,,,,,COCONet;COCONetPartner;Met Sites;PBO Analysis Complete;Puerto Rico;,2
ABVI,Anegada,18.7297,-64.3325,-35.55,deep foundation pillar,,2014-06-16T00:00:00,2015-10-19T23:59:45,TRM29659.00,SCIT,02200669,0.0083,0.0000,0.0000,TRIMBLE NETRS,1.3-2,4720132687,15,Anegada,,British Virgin Islands,,,,,WXT520,J0510007,COCONet;COCONetPartner;Met Sites;PBO Analysis Complete;Puerto Rico;,2
ACP1,ACP1,9.3714,-79.9499,12.76,deep-drilled braced,,2008-10-24T00:00:00,2011-05-19T23:59:45,TRM41249.00,NONE,60216597,0.0083,0.0000,0.0000,TRIMBLE NETRS,1.1-3,4720132691,15,Sherman,,Panama,,,,,,,COCONet;COCONetPartner;PBO Analysis Complete;,3
ACP1,ACP1,9.3714,-79.9499,12.76,deep-drilled braced,,2012-04-13T18:10:30,2015-11-18T23:59:45,TRM41249.00,NONE,60216597,0.0083,0.0000,0.0000,TRIMBLE NETRS,1.3-1,4720132691,15,Sherman,,Panama,,,,,,,COCONet;COCONetPartner;PBO Analysis Complete;,3
ACP6,ACP6,9.2385,-79.4078,943.56,deep-drilled braced,,2008-10-14T19:28:00,2015-11-18T23:59:45,TRM41249.00,NONE,60187024,0.0083,0.0000,0.0000,TRIMBLE NETRS,1.1-3,4702127016,15,Panama,,Panama,,,,,,,COCONet;COCONetPartner;PBO Analysis Complete;,4


                Note that IERSDOMES may be missing ",," for Dataworks.
                Note that state name, x,y,z, and site_count are not used by Dataworks.

                the indices and values in a station row:
                0 4char ID  like ABMF
                1 station name    Aeroport du Raizet -LES ABYMES - Mitio France
                2 latitude          16.2623
                3 longitude         -61.5275
                4 ellipsoid height  -25.67
                5 monument style:  building roof
                6 iersdome 97103M001                               # MAY be not given in the file, OPtional for dataworks.
                7 session start time   2011-03-24T00:00:00,
                8 session end time     2011-08-28T23:59:30
                9 anttype TRM55971.00
                10 radome type NONE
                11 ant SN 1440911917
                12,13,14 ant dz dnorht deast 0.0000,0.0000,0.0000,
                15 rcvr type TRIMBLE NETR5
                16 rcvr firmw vers 4.22
                17 rcvr SN 4917K61764
                18 rcvr sampling interval this session 30 [s]
                19 city/locale LES ABYMES
                20 state/province Guadeloupe                       NOT used by Dataworks
                21 country France
                22, 23, 24, X,Y,Z,  the TRF position coordinates   NOT used by Dataworks
                25 agency name 
                26 metpack name
                27 metpack serial number
                28 networks names string    ";" separated
                29 site count                                       NOT used by Dataworks      
             '''

             SQLstatement=""

             code= (strlist[0]) # the 4 char id of a station such as ABMF
             if (len(code)>4) : # should not occur; attempt to do something useful.
                 code=code[:4]
                 logWrite("  PROBLEM station 4 char id is > 4 chars: "+strlist[0] +"; will use just "+code  );

             # logWrite  (   "*******  Station " + code );

             # when at a new station ; this happens first only after one station has been processed:
             if this_station_code != code :
                     if previous_station_code != "" :
                         logWrite(   "*******   Station "+ previous_station_code +" is up-to-date in the database (station count so far is "+`donecount`+") "  );
                     # if "" != this_station_code : # not the first time
                     previous_station_code = this_station_code
                     logWrite  (   "\n*******  Check new station " + code );
                     sys.stdout.flush()
                     sys.stdout.flush()
                     donecount += 1

             this_station_code = code

             # one file line is one equipment session for one station. 
             logWrite("\n    "+ `(i-3)` + " line in station-session file: "+line [:-1] );

             ## logWrite(metadata line; this to compare results of several run of this script, to see if the INPUT is the same
             #logWrite("    Next equip session metadata line in csv file from remote GSAC: "+line[:70] ); # first part of line
             #logWrite  ("      "+line[70:-1]  ); # second half of line
             sys.stdout.flush()

             staname = (strlist[1]) # station name

             # these next values are strings, not numbers. Which are what you want to load the db, to avoid Python rounding errors

             latstr =     (strlist[2])
             lonstr =     (strlist[3])
             ellphgtstr = (strlist[4])
             # native strings in all cases:
             domesiers =  (strlist[6])   # May be undefined, not given, null, just ",," in the input file .csv

             equipSessStartTime  =  (strlist[7])  #a STRING not datatime object;   like 2009-03-13T23:59:45   NOTE has character T 
             # cut time value to seconds value; if full ISO 8601 value with time zone offset value, like 2006-08-23T00:00:00 +0000
             if (len(equipSessStartTime)>19) :
                 equipSessStartTime=equipSessStartTime[:19]

             # equipSessStartTime time value is used for the installed_date in the database for an all-new station when first inserted in the database.
             # This is perhaps after true installed date, but no other date is available.  You can of course update the installed date in the db by hand.

             equipSessStopTime   =  (strlist[8])
             # cut time value to seconds value; if full ISO 8601 value with time zone offset value, like 2006-08-23T00:00:00 +0000
             if (len(equipSessStopTime)>19) :
                 equipSessStopTime=equipSessStopTime[:19]

             anttype  =  (strlist[9])
             item = anttype

             if igsmap.find(item) != -1:
                  #logWrite  (   "      Antenna name "+item+" is a valid IGS name, and in the IGS file rcvr_ant.tab")
                  pass
             else :
                  errormsg= "\n PROBLEM FIX: Antenna name name "+item+" is NOT a valid IGS name. (got from the remote GSAC.) " \
                            + "\n  The GSAC data for station "+code+" beginning at "+equipSessStartTime \
                            +  " has INVALID  Antenna name "+item+ ", not in http://igscb.jpl.nasa.gov/igscb/station/general/rcvr_ant.tab  \n "
                  logWrite ( errormsg)
                  #continue  

             radometype  =  (strlist[10])
             item = radometype

             if igsmap.find(item) != -1:
                  #logWrite  (   "      RADOME name "+item+" is a valid IGS name, and in the IGS file rcvr_ant.tab")
                  pass
             else :
                  errormsg= "\n PROBLEM: RADOME name "+item+" is NOT a valid IGS name. (got from the remote GSAC.) " \
                            + "\n The remote GSAC data for station "+code+" beginning at "+equipSessStartTime \
                            +  " has INVALID  RADOME "+item+ ", not in http://igscb.jpl.nasa.gov/igscb/station/general/rcvr_ant.tab  \n "
                  logWrite ( errormsg)
                  # continue  


             antsn  =  (strlist[11])
             adz  =  (strlist[12])
             adn  =  (strlist[13])
             ade  =  (strlist[14])


             rcvtype  =  (strlist[15])
             item = rcvtype
             if igsmap.find(item) != -1:
                  #logWrite  (   "      Receiver name "+item+" is a valid IGS name, and in the IGS file rcvr_ant.tab");
                  pass
             else :
                  errormsg= "\n PROBLEM: Receiver name "+item+" is NOT a valid IGS name. (got from the remote GSAC.) " \
                            + "\n The remote GSAC data for station "+code+" beginning at "+equipSessStartTime \
                            +  " has INVALID Receiver name "+item+ ", not in http://igscb.jpl.nasa.gov/igscb/station/general/rcvr_ant.tab  \n "
                  logWrite ( errormsg)
                  # continue; 


             rcvfwvers  =  (strlist[16])
             rcvsn  =  (strlist[17])
             rcvsampInt  =  (strlist[18]) # a string for a number
             #logWrite("      rcvsampInt  = _" + rcvsampInt   );
             sitecount =  (strlist[25])
             monument_style_description   = (strlist[5])
             locale_info   = (strlist[19])
             country_name  = (strlist[21])
             agencyname    = (strlist[25])
             metpackname   = (strlist[26])
             metpackSN=      (strlist[27])
             # FIX 
             if metpackname == None :
                  metpackname=""
             if metpackSN == None :
                  metpackSN=""

             #      rcvsampInt  = _15  NEW
             #        metpackname = _WXT520_  metpackSN =_K2950011_
             metpack_id=0
             metpack_id = getOrSetTableRow ("metpack_id", "metpack", "metpack_name", metpackname) 
             #logWrite("      metpackname = _"+metpackname+"_  metpackSN =_"+metpackSN+"_   metpack_id="+`metpack_id`  );

             networks =    strlist[28]

             # CHANGE
             # look to NOT get local stations such as "GeoRED" stations put back in your GSAC, where they originated:
             if "GeoRED" in networks:
                logWrite("\n >>> SKIPPED GeoRED station _"+code +"  <<<< <<<< <<<< \n")
                continue

             logWrite("      networks names string = _" +networks + "_ " )

             #state         = (strlist[20])  # NOT used by dataworks, as per design specification announced in a meeting.
             # as per instructions of Fran Boler, Oct 29 2014, do NOT store state or province name anywhere in the Dataworks database; so skip these 4 lines:

             #logWrite("      This input equip_config sesssion for station "+code +" has equip_config_start time= "+equipSessStartTime+ "  stop time=_"+equipSessStopTime )

             # Finally, insert the station metadata in the database -- if not already there. 

             #  find the station_id number in the db for this 4 char ID code AND for the station name
             try:
                  #logWrite("      get station id number for  station with  4charID="+code +"_ name="+staname+"_ ")
                  cursor.execute("""SELECT station_id from station where four_char_name = %s """, (code, ) )
                  #  WARNING: NOT all station codes in the UNAVCO database are unique.  Coconet station SSIA has 5 sites and 5 different names, for example.
                  #  new code: add check of station name as well to SQL above
                  #  not used for selects    gsacdb.commit()
                  # rows= cursor.fetchall() # gives array of array for each row, like ['Bob', '9123 4567'] for one row result of 2 values
                  # or for one row in rows, then use the one row
                  row= cursor.fetchone()
                  station_id=  row[0];
                  station_id=  int(station_id); # fix the "L" value returned
                  #  Already have row "+`station_id`+" in the db station table for "+code+", so will not add new station data to the table.
                  logWrite(  "      Already have station "+code +" in the database." );
                  sys.stdout.flush()
                  if code not in sta_ID_list:
                      sta_ID_list.append(code)
             except:
                   # the station is not in the database, so add it:
                   logWrite( "      Station is NEW; not in db, so automatically insert this new station "+code+" with name="+staname+" in the db station table:"  );
                   logWrite( "      "  );
                   logWrite( "      ######################################################################################################################### "  );
                   logWrite( "      LOOK: "  );
                   logWrite( "      For this new station, you need to insert in the database some more information, not avaiable from remote GSAC results: "  );
                   logWrite( "      in the database agency table, insert a new row for the the operator_agency and a row for the data_publisher_agency "  );
                   logWrite( "      in station table for this station:  insert the new operator_agency_id "  );
                   logWrite( "      in station table for this station:  the data_publisher_agency_id"  );
                   logWrite( "      in station table for this station:  the station_image_URL  "  );
                   logWrite( "        and somewhere online provide an image file corresponding to the station_image_URL  "  );
                   logWrite( "      in station table for this station:  the time_series_URL "  );
                   logWrite( "        and somewhere online provide an image file corresponding to the picture of a time series, for the time_series_URL  "  );
                   logWrite( "      in table equip_config, insert the radome serial number and the metpack_serial_number.  "  );
                   logWrite( "      ######################################################################################################################### "  );
                   logWrite( "      "  );
                   sys.stdout.flush()

                   #  use these initial values for dataworks stations, which values are NOT available to this program, since there is no such info in a gsac full csv input file:
                   style_id       = 1    # GPS/GNSS Continuous
                   status_id      = 1    # Active 
                   access_id      = 2    # full public access
                   ellipsoid_id   = 1    # WGS 84
                   network_id     = 1    # COCONet or whatever your network is.
                   agency_id      = 30   # "not supplied by remote GSAC           (and not shown by GSAC, so no way to get the value from the db by remote user.)

                   # get  or set the id for the foreign keys, to be reset in in this program based in onfo in the gsac full csv input file:
                   monument_style_id = 1 # id value is "not specified"
                   country_id=1          # not specified
                   locale_id=1           # "not specified"
                   the_id=1
                    # get or set monument_style_id based on monument_style_description value:
                   if ""==monument_style_description :
                        # default value of "not specified" at id=1 in db is used
                        logWrite("       no monument style in GSAC full csv file results " );
                        pass
                   else:
                        # id= getOrSetTableRow (idname, tablename,  rowname, rowvalue)
                        monument_style_id  = getOrSetTableRow ("monument_style_id", "monument_style", "monument_style_description", monument_style_description) 

                   if ""==locale_info:
                        # default value of "not specified" at id=1 in db is used
                        logWrite("       no locale info in GSAC full csv file results"  );
                        pass
                   else:
                        # id= getOrSetTableRow (idname, tablename,  rowname, rowvalue)
                        locale_id  = getOrSetTableRow ("locale_id", "locale", "locale_info", locale_info) 

                   if ""== country_name:
                        # default value of "not specified" at id=1 in db is used
                        logWrite("       no country name in  GSAC full csv file results"  );
                        pass
                   else:
                        # id= getOrSetTableRow (idname, tablename,  rowname, rowvalue)
                        country_id  = getOrSetTableRow ("country_id", "country", "country_name", country_name) 

                   #logWrite ("        got values of ids monument_style_id, locale_id, country_id = "+`monument_style_id`+"  "+`locale_id`+"   "+ `country_id`)

                   station_photo_URL= "" 
                   time_series_plot_photo_URL="" 

                   # New May 26 2015
                   #SQLstatement=("INSERT INTO station (four_char_name,station_name,latitude_north,longitude_east,height_above_ellipsoid,installed_date, style_id, status_id, access_id,  ellipsoid_id ,  iers_domes, network_id,country_id,locale_id,monument_style_id, latest_data_time ) values  ('%s', '%s', %s,  %s,  %s, '%s', %s, %s,  %s, %s, '%s', '%s', '%s', %s, %s, '%s')" % ( code, staname, latstr, lonstr, ellphgtstr, equipSessStartTime,  style_id, status_id, access_id, ellipsoid_id, domesiers,  network_id, country_id,locale_id, monument_style_id, equipSessStopTime))
                   SQLstatement=("INSERT INTO station (four_char_name,station_name,latitude_north,longitude_east,height_above_ellipsoid,installed_date, style_id, status_id, access_id,  ellipsoid_id ,  iers_domes, network_id,country_id,locale_id,monument_style_id ) values  ('%s', '%s', %s,  %s,  %s, '%s', %s, %s,  %s, %s, '%s', '%s', '%s', %s, %s)" % ( code, staname, latstr, lonstr, ellphgtstr, equipSessStartTime,  style_id, status_id, access_id, ellipsoid_id, domesiers,  network_id, country_id,locale_id, monument_style_id))

                   try:
                       # add this new station to the Dataworks database 'station' table.
                       # rows in foreign keys' tables must be populated already in the database.
                       #logWrite("       Insert the new station into station table with SQL \n       "+SQLstatement   )
                       cursor.execute(SQLstatement)
                       gsacdb.commit()
                       logWrite( " ***** *****  Inserted new STATION: "+four_char_name+",  "+station_name   )
                       newstacount += 1 
                       sys.stdout.flush()

                   except:
                       # bogus fails logWrite(" PROBLEM FAILED  MySQL insert command to add new station code=" +code+" name="+staname+" "  );
                       #logWrite(               "      BUT sometimes actually succeeded: look in the database."  );
                       pass # skip always gets false failure of above insert; cf "INSERT into" below which does not

                   station_counter += 1;
                   if code not in sta_ID_list:
                          sta_ID_list.append(code)

                   stm=" "
                   try:
                       stm = ("SELECT station_id from station where four_char_name = '%s'  and station_name= '%s' " % (code, staname    ) )
                       #cursor.execute("""SELECT station_id from station where four_char_name = '%s'  and station_name= '%s' """, (code, staname ) )
                       # logWrite("   2nd try to find new station id with SQL   \n      "+stm  );
                       cursor.execute(stm)
                       gsacdb.commit()
                       row= cursor.fetchone()
                       station_id=  row[0];
                       station_id=  int(station_id); # fix the "L" value returned
                       #logWrite( "      Station "+code+" has station_id "+`station_id`  )
                   except:
                       #logWrite( " PROBLEM maybe FAILED to get the id for station code="+code + "\n       with SQL = "+stm + "\n      for case of metadata \n    "+metadata)
                       #logWrite( "      BUT sometimes actually succeeds: look in the database."  );
                       #failedcount +=1
                       ## logWrite("\n     Failed to get the id for station code="+code + "\n      with SQL = "+stm + "\n      for case of metadata = \n    "+metadata )
                       pass

             # end first big try; see if this station is already in the database station table. and add it when needed.
  
             # Add any NEW equipment session rows for this station; ones not already recorded.

             equipinfo='''
                desc equip_config;    # as of 15 Nov 2014
                +-------------------------+-----------------+------+-----+---------+----------------+
                | Field                   | Type            | Null | Key | Default | Extra          |
                +-------------------------+-----------------+------+-----+---------+----------------+
                | equip_config_id         | int(6) unsigned | NO   | PRI | NULL    | auto_increment |
                | station_id              | int(6) unsigned | NO   | MUL | NULL    |                |
                | create_time             | datetime        | NO   |     | NULL    |                |
                | equip_config_start_time | datetime        | NO   |     | NULL    |                |
                | equip_config_stop_time  | datetime        | NO   |     | NULL    |                |
                | antenna_id              | int(3) unsigned | NO   | MUL | NULL    |                |
                | antenna_serial_number   | varchar(20)     | NO   |     | NULL    |                |
                | antenna_height          | float           | NO   |     | NULL    |                |
                | metpack_id              | int(3) unsigned | YES  | MUL | NULL    |                |
                | metpack_serial_number   | varchar(20)     | YES  |     | NULL    |                |
                | radome_id               | int(3) unsigned | NO   | MUL | NULL    |                |
                | radom_serial_number     | varchar(20)     | NO   |     | NULL    |                |
                | receiver_firmware_id    | int(3) unsigned | NO   | MUL | NULL    |                |
                | receiver_serial_number  | varchar(20)     | NO   |     | NULL    |                |
                | satellite_system        | varchar(20)     | YES  |     | NULL    |                |
                +-------------------------+-----------------+------+-----+---------+----------------+
             end equipinfo '''

             # all STRINGS 
             crtimestamp   =strftime("%Y-%m-%dT%H:%M:%S", gmtime())
             create_time   = crtimestamp # this is when added to db; nothing about the session really
             new_equip_config_start_time =equipSessStartTime
             new_equip_config_stop_time  =equipSessStopTime

             skip=''' # look for hypothetical bad equip session time spans 
             # no such have been encountered in actual use.
             # look for overlapping equip session time spans sss  tttt
             if previous_station_code == this_station_code:
                 # string 2011-11-29T00:00:00 to time object. from datetime import datetime
                 # date_object = datetime.strptime('Jun 1 2005  1:33PM', '%b %d %Y %I:%M%p')
                 # previous time span at this station:
                 start1=datetime.datetime.strptime(prev_start_time , "%Y-%m-%dT%H:%M:%S" )
                 stop1=datetime.datetime.strptime(prev_stop_time, "%Y-%m-%dT%H:%M:%S" )
                 # this new time span at this station:
                 start2=datetime.datetime.strptime(equipSessStartTime , "%Y-%m-%dT%H:%M:%S" )
                 stop2=datetime.datetime.strptime(equipSessStopTime , "%Y-%m-%dT%H:%M:%S" )
                 #logWrite(  "      DEBUG: "+this_station_code+" previous equip session times: "+start1.strftime("%Y-%m-%d %H:%M:%S")+" - "+stop1.strftime("%Y-%m-%d %H:%M:%S"))
                 #                                                            2014-02-21 23:59:45 - 2014-02-21 23:59:45
                 #logWrite(  "      DEBUG: "+this_station_code+"    these equip session times:                       "+start2.strftime("%Y-%m-%d %H:%M:%S")+" - "+stop2.strftime("%Y-%m-%d %H:%M:%S"))
                 # if THIS interval STARTS before the PREVIOUS interval ENDS (all cases of "too early")
                 if equipSessStartTime < prev_stop_time : 
                      logWrite(  " PROBLEM: in data from remote GSAC, bad equip session time spans: equip_config times overlap; start before previous end time"  );
                      logWrite(  "       metadata line = \n      "+line  );
                      #failedcount +=1

             # end skip - block  to look for problematical equip session time spans 

                  cursor.execute("""SELECT station_id from station where four_char_name = %s """, (code, ) )
                  #  WARNING: NOT all station codes in the UNAVCO database are unique.  Coconet station SSIA has 5 sites and 5 different names, for example.
                  #  new code: add check of station name as well to SQL above
                  #  not used for selects    gsacdb.commit()
                  # rows= cursor.fetchall() # gives array of array for each row, like ['Bob', '9123 4567'] for one row result of 2 values
                  # or for one row in rows, then use the one row
                  row= cursor.fetchone()
                  station_id=  row[0];
                  station_id=  int(station_id); # fix the "L" value returned
             '''

             sys.stdout.flush()
             
             # ok move to this time interval
             prev_start_time =equipSessStartTime
             prev_stop_time  =equipSessStopTime
             prev_metadata = line 

             # Check if this equipment session at this station already is in the database:
             haveit=False
             dbeqstart= "" # a STRING
             dbeqstop = ""
             # for NEW values in the input file: make datetime objects:
             newequipSessStartTimeDT=datetime.datetime.strptime(equipSessStartTime, "%Y-%m-%dT%H:%M:%S" )
             newequipSessStopTimeDT =datetime.datetime.strptime(equipSessStopTime,  "%Y-%m-%dT%H:%M:%S" )
             the_id=1
             esid=-1
             try:
                   #logWrite("      Look for this particular equipment session at station_id="+`station_id` ) # +"  in the db with SQL \n      "+statement
                   # ONLY check for this station and this start time;  end time given by UNAVCO GSAC is variable for the current active session.
                   SQLstatement=("SELECT equip_config_id,equip_config_start_time,equip_config_stop_time from equip_config where station_id= %s and equip_config_start_time= '%s'" % (station_id, new_equip_config_start_time))
                   #logWrite("      Look for this equip config record in the dq with sql  ... \n      "+ SQLstatement)
                   cursor.execute(SQLstatement)
                   #logWrite("      1") 
                   row     = cursor.fetchone()
                   #logWrite("      2") 
                   the_id=  row[0];
                   the_id=  int(the_id) # fix the "L" value returned
                   #receiver_id  = the_id
                   #strid   =  row[0];
                   #logWrite("      3 string id= "+ strid) 
                   esid    = the_id
                   #logWrite("      4 int  esid= "+`esid`) 
                   dbeqstart=row[1] # a datetime.datetime
                   #logWrite("       start time ="+dbeqstart.strftime("%Y-%m-%d %H:%M:%S")) 
                   dbeqstop =row[2]
                   # FORMER logWrite("       stop  time ="+dbeqstop.strftime("%Y-%m-%d %H:%M:%S")) 
                   haveit    = True
                   eqscount+=1
                   #logWrite("                    count of eq sessions "+`eqscount`) 
             except:
                   #logWrite("     PROBLEM   FAILED to find this equip config session.") 
                   #logWrite("       but got The esid= "+`esid`) 
                   pass

             doAddSession=True
             #doUpdateStopTime=False
             # but when you got some kind of match session in the db:
             if  haveit: 
                 logWrite("      This equipment session at id "+`esid`+" is already in the db equip-config table.") 
                 if newequipSessStartTimeDT == dbeqstart  and newequipSessStopTimeDT == dbeqstop :
                     logWrite("      No new metadata for this station from the remote GSAC archive today (the former and new session start and stop times match). ") 
                     #logWrite("      Done with this equip session data set line. Go try next input metadata line from the  GSAC .csv file.\n  ") 
                     doAddSession=False
                 elif newequipSessStopTimeDT > dbeqstop :
                     doAddSession=False
                     logWrite("      But, need to update the equip_config_stop_time in that record, to the later new stop time from new input data.") 
                     logWrite("      (new equip_config_stop_time is " + str(newequipSessStopTimeDT) +"; > db stop time "+ str(dbeqstop) +") "  ) 
                     #doUpdateStopTime= True
                     statement=("UPDATE equip_config set equip_config_stop_time = '%s'  where equip_config_id= %s " % (new_equip_config_stop_time, esid))
                     #logWrite("      the SQL is "+statement)
                     cursor.execute(statement)
                     gsacdb.commit()
                     #logWrite("      Updated the equip_config_stop_time in the db, for station "+code+" and at equip_config_id "+`esid` +", "  )
                     stoptimeupdatecount += 1
                     # end of processing one metadata line from the GSAC .csv file

                     # New May 26 2015
                     # update latest_data_time values in the station table
                     #SQLstatement= ("UPDATE station set latest_data_time = '%s'  where station_id = %s " % ( new_equip_config_stop_time, station_id ))
                     #cursor.execute(SQLstatement)
                     #gsacdb.commit()
                     #logWrite("        and updated latest data time for station. " )


             skip='''
                 else:
                     logWrite("     PROBLEM SOME ODD equip session times here; compare start and stop times in the database and in this GCAC info file, for station "+code+". ") 
                     doAddSession=False
                     pass

         # one could check the equip table values against the same parameter's values from the gsac query results, in case of changes;             
         # It's much simpler just to reset values:
             if  haveit: 
           # update rcvsampInt   
           try:
            statement=("UPDATE equip_config set sample_interval  = %s    where equip_config_id= %s " % (rcvsampInt, esid))
            logWrite("      update rcvsampInt SQL is "+statement)
            cursor.execute(statement)
            gsacdb.commit()
            logWrite("      OK, updated rcvsampInt="+rcvsampInt);
               except:
                        pass
             # this part will be activated after the UNAVCO GSAC can provide the metpack metadata
           #        metpack data                   (idname,      tablename,  rowname,        rowvalue)  
           if len(metpackname)>0 and esid>0 :
            metpack_id  = getOrSetTableRow ("metpack_id", "metpack", "metpack_name", metpackname) 
            statement=("UPDATE equip_config set metpack_id             = %s    where equip_config_id= %s " % (metpack_id, esid))
            logWrite("      update metpackSN SQL is "+statement)
            cursor.execute(statement)
            gsacdb.commit()
            logWrite("      OK, updated metpack_id ="+`metpack_id` );
           if len(metpackSN)>0  and esid>0:
            # update metpack SN and metpack_id in the equip table
            statement=("UPDATE equip_config set metpack_serial_number  = '%s'  where equip_config_id= %s " % (metpackSN, esid))
            logWrite("      update metpackSN SQL is "+statement)
            cursor.execute(statement)
            gsacdb.commit()
            logWrite("      OK, updated metpackSN ="+metpackSN);
             '''

             sys.stdout.flush()

             # DON'T have this session in the db yet so add it:
             if ( haveit == False ) :
                 #logWrite("       add equip session")
                 antenna_serial_number   =antsn
                 antenna_height          =adz
                 radome_serial_number   =" " # not available from UNAVCO GSAC
                 receiver_serial_number =rcvsn
                 satellite_system       ="GPS" # default; or could be for example "GPS,GLONASS"   CHANGE
                 esid=0

                 #  for foreign key fields
                 antenna_name=anttype
                 radome_name=radometype
                 receiver_name=rcvtype
                 receiver_id =1
                 antenna_id  =1
                 radome_id   =1 

                 if ""== antenna_name:
                        # default value of "not specified" at id=1 in db is used
                        antenna_id  =  1 # not specified
                        # if logflag>=2: # logWrite("       no antenna name in UNAVCO GSAC full csv file results"  );
                 else:
                        antenna_id  = getOrSetTableRow ("antenna_id", "antenna", "antenna_name", antenna_name)

                 if ""== radome_name:
                        # default value of "not specified" at id=1 in db is used
                        radome_id  =  1 # not specified
                        # if logflag>=2: # logWrite("       no radome name in UNAVCO GSAC full csv file results"  );
                 else:
                        radome_id  = getOrSetTableRow ("radome_id", "radome", "radome_name", radome_name)

                 if ""== receiver_name:
                        # default value of "not specified" at id=1 in db is used
                        receiver_id  =  1 # not specified
                        # if logflag>=2: # logWrite("       no receiver name in UNAVCO GSAC full csv file results"  );
                 else:
                        # note the receiver_firmware table encapsulates two values into a unique combination,
                        # so to get the id number must do this
                        # elaboration of def getOrSetTableRow (idname, tablename,  rowname, rowvalue) :
                        # if logflag>=2: # logWrite("         getOrSet receiver_firmware_id "  );
                        the_id=1
                        receiver_id  =  1 # not specified
                        try:
                               #SQLstatement=("SELECT  %s from %s  where  %s = '%s' " % ( idname,tablename,rowname,rowvalue ) )
                               SQLstatement=("SELECT receiver_firmware_id from receiver_firmware where receiver_name='%s' and receiver_firmware='%s' " % (rcvtype,rcvfwvers) )
                               # if logflag>=2: # logWrite("            SQL statement ="+SQLstatement  );
                               cursor.execute(SQLstatement)
                               row= cursor.fetchone()
                               the_id=  row[0];
                               the_id=  int(the_id); # fix the "L" value returned
                               receiver_id  = the_id
                               # if logflag>=2: # logWrite("                got receiver firmware id="+`receiver_id` +"  for "+ rcvtype+", "+rcvfwvers  );
                        except:
                               # if logflag>=2: # logWrite("       no row is yet in the receiver_firmware database for "+ rcvtype+", "+rcvfwvers  );
                               # add this new value to that table; and get its id  rrr
                               # one value SQLstatement=("INSERT into %s (%s) value ('%s')"  %  ( tablename,rowname,rowvalue ) )
                               SQLstatement=("INSERT into receiver_firmware (receiver_name,receiver_firmware) values ('%s','%s')"  %  (rcvtype,rcvfwvers ) )
                               # if logflag>=2: # logWrite("         insert receiver tbl  SQL statement ="+SQLstatement  );
                               try:
                                   cursor.execute(SQLstatement)
                                   gsacdb.commit()
                                   logWrite( "            Inserted new receiver values "+rcvtype+", "+rcvfwvers  );
                               except:
                                   gsacdb.rollback()
                                   # if logflag>=2: # logWrite( "           Failed to insert new values "+rcvtype+", "+rcvfwvers  );
                                   ## logWrite("\n     Failed to insert new values rcvtype "+rcvtype+", rcv vers "+rcvfwvers  + " for case of metadata = \n    "+metadata )
                               #  get the new id value
                               SQLstatement=("SELECT receiver_firmware_id from receiver_firmware where receiver_name='%s' and receiver_firmware='%s' " % (rcvtype,rcvfwvers) )
                               # if logflag>=2: # logWrite("            SQL statement ="+SQLstatement  );
                               cursor.execute(SQLstatement)
                               row= cursor.fetchone()
                               the_id=  row[0];
                               the_id=  int(the_id); # fix the "L" value returned
                               # if logflag>=2: # logWrite("                got receiver firmware id="+`the_id` +"  for "+ rcvtype+", "+rcvfwvers  );
                               receiver_id  = the_id

                 logWrite("      Insert this new equipment session into the db ")
                 try:
                       SQLstatement=("INSERT into equip_config (station_id, create_time, equip_config_start_time, equip_config_stop_time, antenna_id, antenna_serial_number, antenna_height, radome_id, radome_serial_number, receiver_firmware_id, receiver_serial_number, satellite_system,sample_interval) values (%s, '%s', '%s', '%s',  %s, '%s', %s,  %s, '%s', %s, '%s',  '%s', %s)" % ( station_id, create_time, new_equip_config_start_time, new_equip_config_stop_time, antenna_id, antenna_serial_number, antenna_height, radome_id, radome_serial_number, receiver_id, receiver_serial_number, satellite_system, rcvsampInt)) 
                       logWrite("      Insert this new equipment session into the db with SQL: \n      "+SQLstatement  )
                       cursor.execute(SQLstatement)
                       gsacdb.commit()
                       newsessioncount += 1
                       #eqscount+=1
                       logWrite("     Inserted a new equipment session into the db," )

                       # New May 26 2015
                       # update latest_data_time values in the station table
                       #SQLstatement= ("UPDATE station set latest_data_time = '%s'  where station_id = %s " % ( new_equip_config_stop_time, station_id ))
                       #cursor.execute(SQLstatement)
                       #gsacdb.commit()
                       #logWrite("        and updated latest data time for station. " )

                 except:
                       #logWrite(" PROBLEM maybe FAILED to insert this equipment session into the db.")
                       #logWrite( "        BUT sometimes actually succeeds: look in the database."  );
                       #failedcount +=1
                       pass

             # end if NOT haveit, so add it

           previous_station_code = this_station_code

           # end of adding station info and ALL equipment session info
           if this_station_code != code :
                 if "" != this_station_code : # not the first time
                     previous_station_code = this_station_code
                     #if logflag>=1:  logWrite(  "  Station "+ previous_station_code +" data in the database is up-to-date (station count "+`donecount`+")."

        # close the GSAC Full CSV input file 
        #station_metadata_file.close()

        logWrite(  "*******   Station "+ previous_station_code +" is up-to-date in the database (station count "+`donecount`+") "  );

        logWrite("\n   Finished reading input station metadata file from the UNAVCO GSAC."  ); # station_metadata_file

        logWrite("\n   TOTAL station count is "+`donecount`  );

 #  END OF function load_db() 



def  logWrite (text):
     global logFile 
     global logfilename 
     global logflag 
     logFile.write(text + "\n")
     if logflag>=2 :
       print (text)


def getOrSetTableRow (idname, tablename,  rowname, rowvalue) :
       " find the id for the value rowvalue in the db table tablename, or add a new row to the table and return that id."
       # if logflag>=2: # logWrite("        call getOrSetTableRow"  );
       the_id=1
       try:
           SQLstatement=("SELECT  %s from %s  where  %s = '%s' " % ( idname,tablename,rowname,rowvalue ) )
           # if logflag>=2: # logWrite("            SQL statement ="+SQLstatement  );
           cursor.execute(SQLstatement)
           row= cursor.fetchone()
           the_id=  row[0];
           the_id=  int(the_id); # fix the "L" value returned
           # logWrite("                got id="+`the_id` +"  for row value "+rowvalue+" in table "+tablename  );
       except:
           # if logflag>=2: # logWrite("       no "+rowname+" is yet in the database for "+rowvalue  );
           # add this new value to that table; and get its id
           SQLstatement=("INSERT into %s (%s) value ('%s')"  %  ( tablename,rowname,rowvalue ) )
           # if logflag>=2: # logWrite("            SQL statement ="+SQLstatement  );
           try:
               cursor.execute(SQLstatement)
               gsacdb.commit()
               # logWrite( " ***** *****           Inserted new value "+rowvalue + " in table.field="+ tablename+" . "+  rowname  );
           except:
               gsacdb.rollback()
               # if logflag>=2: # logWrite( "           Failed to insert new value "+rowvalue  );
           #  get the new id value
           SQLstatement=("SELECT %s from %s  where  %s = '%s' " % ( idname,tablename,rowname,rowvalue ) )
           # if logflag>=2: # logWrite("            SQL statement ="+SQLstatement  );
           cursor.execute(SQLstatement)
           row= cursor.fetchone()
           the_id=  row[0];
           the_id=  int(the_id); # fix the "L" value returned
           #  logWrite("                set id="+`the_id` +"  for row value "+rowvalue+" in table "+tablename  );
       return the_id
 #  END OF function  getOrSetTableRow () 



def makedatetime (dtstr):
     # creates a python datetime object (NOT a string) from individual parameter values. 
     # input string is usually like 2010-05-03 08:45:59
     # or such as 2010-05-03 08:45
     # or 2008-09-11
     # or empty string
     # or a too=short string
     # Note that the time fields are optional; if omitted the time value is 0:00:00, which is midnight.
     # uses datetime.datetime.strptime( "2007-03-04 21:08:12", "%Y-%m-%d %H:%M:%S" )
     # or   datetime.datetime( year , month , day,hour,minute,second,microsecond,tzinfo)  
     dt=None
     #if (len(dtstr)<10):
     #   logWrite(" PROBLEM: date - time string input to makedatetime () is  too short: "+dtstr
     #   return dt
     if (""==dtstr):
         return None
     elif ('                   '==dtstr):
         return None
     if (len(dtstr) == 10):
         dt=datetime.datetime.strptime( dtstr, "%Y-%m-%d" )
     elif (len(dtstr) == 16):
         dt=datetime.datetime.strptime( dtstr, "%Y-%m-%dT%H:%M" )
     elif (len(dtstr) == 19):
         dt=datetime.datetime.strptime( dtstr, "%Y-%m-%dT%H:%M:%S" )
     return dt



# main program: 

global logFile 
global logfilename 
global logflag 
global newstacount
global newsessioncount
global failedcount
global stationgroup
global eqscount
global donecount

logflag =2 # CHANGE USE =1 for routine operations.  use 2 for testing  and details

# open log file describing processing results 
timestamp   =strftime("%Y-%m-%d_%H:%M:%S", gmtime())
# or with '%a'  for day name   strftime("%a, %d %b %Y %H:%M:%S +0000", gmtime()) 

#dom =strftime("%d", gmtime())  # day of month, such as "16", to use in log file name
#logfilename = "/dataworks/logs/mirrorStations.py.log."+dom # could add + timestamp  + ".txt"
#logFile     = open (logfilename, 'w')  # NOTE this creates a NEW file of the same log file name, destroying any previous log file of this name.
#logWrite("\n    Log of mirrorStations.py "+timestamp + " (this log file is "+logfilename+")" )

# get command line argument values
inputfile=""
dbhost="" 
dbacct="" 
dbacctpw="" 
dbname=""
stationgroup=""

# get input args from command line
args = sys.argv
stationgroup = args[1]

# debug print "\n arg 1 ="+stationgroup+"= \n"

dbhost   = args[2]
dbacct   = args[3]
dbacctpw = args[4]
dbname   = args[5]

# connect to the database to write to, uses import MySQLdb
gsacdb = MySQLdb.connect(dbhost, dbacct, dbacctpw, dbname)
cursor = gsacdb.cursor()
#  FIX check return status and report errors
#logWrite("    Connected to the database " +dbname   ) # + " with account "+dbacct+", on "+dbhost 
sys.stdout.flush()

# the processing method:
load_db()

sys.stdout.flush()
        
# disconnect from db server
gsacdb.close()

logWrite("\n   SUMMARY of mirrorStation.py processing: ")

if newstacount>0 :
    logWrite("\n ***** *****  Inserted "+`newstacount`    +" new stations. (look in the log file for 'Inserted.') "  )
    ## logWrite("\n ***** *****  Inserted "+`newstacount`    +" new stations, without problems.")  );
    pass
else :
    logWrite("\n ***** ***** No new stations added."  )
    pass

if newsessioncount>0  :
    logWrite("\n ***** *****  Inserted "+`newsessioncount`+" new equipment sessions. (look in the log file for 'Inserted.')"  )
    ## logWrite("\n ***** *****  Inserted "+`newsessioncount`+" new equipment sessions, without problems. \n \n")
    pass
else :
    logWrite("\n ***** ***** No new equipment sessions added."  )
    pass

logWrite(    "\n ***** ***** There were "+`donecount`+" stations checked in the database."  )

logWrite(    "\n ***** ***** There were "+`eqscount`+" station - equipment sessions matches in the database."  )

logWrite(    "\n ***** ***** Updated "+`stoptimeupdatecount`+" equip config table stop times."  )

notused='''
if failedcount>0  :
    logWrite("\n ***** ***** Some attempted db actions FAILED.  Look in this log file for 'FAIL' and 'PROBLEM'.  "  )
    BUT sometimes actually succeed: look in the database. must call commit right after execute
    pass
'''

timestamp=  strftime("%a, %d %b %Y %H:%M:%S +0000", gmtime())  #  strftime("%d-%M-%Y %H:%M:%S", gmtime)
logWrite("\n Complete at " + timestamp + " UTC  "   )

logWrite("\n ***** *****  Look at the log files after each run.  Look for errors noted in lines with PROBLEM or LOOK and fix those issues. ***** *****\n \n")

sys.stdout.flush()
logFile.close()

sys.exit (0)   # return success

# ALL DONE
