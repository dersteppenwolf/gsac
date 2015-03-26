#!/usr/bin/python
'''
 filename              : mirrorStations.py
 author                : Stuart Wier 
 created               : 2014-09-03
 updates               : 2014-09-04 17:35 UTC; 2014-09-11 20:05 UTC; 2014-09-23 14:38 UTC; 2014-09-25 15:30; 6 Oct; 17 Oct; 20 Oct; 29 Oct; 
                         2014 4 Nov; 7 Nov., 10 to 13 Nov., 14 Nov, 15 Nov. and 16 Nov. yes Saturday and Sunday; 17 Nov. through  21 Nov., 2 Dec. 2014 
                         2104 19 Dec, 14 Jan 2015, 20 Jan, 21 Jan.; 22 Jan; 

 exit code(s)          : 0, success

 description           The use is:
		       1. to make the initial population of a Dataworks database with all stations in the network, and all the equipment sessions at each station.
		       2. to find and add newly-added stations in the network (and the new equipment sessions at that station).
		       3. to update existing equipment sessions (db equip_config records), when the metadata was changed at UNAVCO. Usually session stop_time.

                       The metadata comes from the UNAVCO (http://www.unavco.org) archive of GPS information.
                       The metadata is obtained using the GSAC service at UNAVCO (http://www.unavco.org/software/data-management/gsac/gsac.html). (see [1])
                       The metadata from GSAC is provided in a "GSAC full csv file", named in this case dataworks_stations.csv (see [1])
                       The UNAVCO GSAC is supposedly at http://www.unavco.org/data/web-services/gsacws/gsacapi/, but calling curl to that without -L fails.
                       At the moment the UNAVCO GSAC is actually at http://facility.unavco.org/, but I was told to use www.unavco.org so have to add -L to get to facility.unavco.org/.

                       NOTE You MUST run this script everytime you run mirrorData.py, before you run mirrorData.py, since UNAVCO changes equipment session end times
                         everytime a new file comes in for a station.  If you try to download a new file for a session when the db equip_config_stop_time is older, the download fails.

 configuration:        : First, one time only, for your network and operations, revise 

                          satellite_system ="GPS"    # default  could be for example "GPS,GLONASS"   CHANGE    near line 487

                       : and set the value of logflag to choose if you want to see output to the terminal:

                          logflag =1                 # controls screen output.  CHANGE: USE =1 for routine operations. OR use =2 to print log lines to screen, for testing, near line 674
                       
 usage:                : 1.  Run this script daily with the Dataworks 'ops' account's  Linux crontab job, to look for new or changed station info:
                  
                           This Python is run with a command like this:

                              /dataworks/mirror_station_metadata_from_unavco/mirrorStations.py   stationgroup   dbhost   dbaccount   dbaccountpw   dbname

                           Use your names for the database account name, account password, and database name such as Dataworks

                           For the "stationgroup" command line argument or value, you use the network name as found in 
                           the UNAVCO archive (COCONet or TLALOCNet) which you mirror. 

                            so the nominal command 
                              /dataworks/mirror_station_metadata_from_unavco/mirrorStations.py   stationgroup  dbhost    dbaccount   dbaccountpw          dbname

                            becomes something like
                             /dataworks/mirror_station_metadata_from_unavco/mirrorStations.py    TLALOCNet     localhost  dataworks  tlalocnetdataworks   Dataworks

                           Or, for stationgroup use, for separate stations, not a network name,  
                           inside "", have:
                              a. a single station's four char ID  like "POAL"
                              b. semi-colon separated list of four char IDs, separated with ";"   like    "p123;p456"  
                              c. wildcards like "TN*"

                           So for the stationgroup argument, put all of a. thru c. in one string with no spaces, like  "POAL;P12*;TN*"
                            The semi-colon separates the items in stationgroup argument.
                           Upper case and lower case in station four char IDs are the same in GSAC use.

                            so a command is like

                          /dataworks/mirror_station_metadata_from_unavco/mirrorStations.py  "POAL;TNAM"  localhost  dataworks  dbpassword  Dataworks

                          Use this option with caution; you can add a lot of stations not in your network to your system with one simple command.



                          Running the process takes about 1 second per site; and makes a log file, /dataworks/logs/mirrorStations.py.log

                        2. Look at the log file after each run.  Look for errors noted in lines with "PROBLEM" and LOOK  and fix any problems.

                        3. Update these database tables by hand when a new station is added (no such data is available from the from UNAVCO GSAC): 
                           radome_serial_number in equip_config; 
                           and in table station, update field values for  
                                  operator_agency_id, 
                                  and data_publisher_agency_id. You may need to insert a new agency in the db agency table, first.

 tested on             : Python 2.6.5 on Linux (Ubuntu) ; CentOS Python 2.6 
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
# To install Python's MySQLdb on ubuntu/debian, just do command   sudo apt-get install python-mysqldb
# and see this http://stackoverflow.com/questions/372885/how-do-i-connect-to-a-mysql-database-in-python
        
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
    # Do the query to the UNAVCO GSAC's API, to make a list of all stations' info in your network, in the file dataworks_stations.csv.
    # The metadata comes from the UNAVCO (http://www.unavco.org) archive of GPS information.
    # The metadata is obtained using the GSAC service at UNAVCO (http://www.unavco.org/software/data-management/gsac/gsac.html). (see [1])
    # The metadata from GSAC is provided in a "GSAC full csv file", named in this case dataworks_stations.csv (see [1])

    dom =strftime("%d", gmtime())  # day of month, such as "16", to use in log file name
    logfilename = "/dataworks/logs/mirrorStations.py.log."+dom
    timestamp   =strftime("%Y-%m-%d_%H:%M:%S", gmtime())

    # compose the UNAVCO GSAC's API query string.
    httppart=' "http://www.unavco.org/data/web-services/gsacws/gsacapi/site/search?output=sitefull.csv&site.group='+stationgroup+'&site.status=active" '

    # in case of separate station IDs:
    if ";" in stationgroup or len(stationgroup)<5:
        # search for site by ID, and cut off trailing final ";" 
        httppart=' "http://www.unavco.org/data/web-services/gsacws/gsacapi/site/search?output=sitefull.csv&site.code='+stationgroup+'"'
        logfilename = logfilename + ".extras"

    # compose the command to make the query using the Linux 'curl' command line utility:
    cmd1 = "/usr/bin/curl -L "+ httppart + " > dataworks_stations.csv"

    logFile     = open (logfilename, 'w')  # NOTE this creates a NEW file of the same log file name, destroying any previous log file of this name.

    logWrite("\n    Log of mirrorStations.py "+timestamp + " (log file is "+logfilename+")" )

    logWrite("\n ***** *****  Look at the log files after each run.  Look for errors noted in lines with PROBLEM or LOOK and fix those issues. ***** *****\n \n")

    logWrite("\n    mirrorStations.py loads or updates a GSAC dataworks database with station and equipment session data from a 'GSAC full csv file' made by the UNAVCO GSAC server."  )
    logWrite(  "    The use is:"  )
    logWrite(  "       1. To make the initial (first time) population of your Dataworks database with all stations in the network, and all the equipment sessions at each station."  )
    logWrite(  "       2. To find and add any newly-added stations in the network (and add the equipment sessions at that station)."  )
    logWrite(  "       4. To update the equip session end times at the still-active sessions (which should have end time of 'end of today') "  )

    if ";" in stationgroup or len(stationgroup)<5:
      logWrite("\n    Update these individual sites: "+stationgroup );
    else:
      logWrite("\n    Update all sites in the station network "+stationgroup );

    logWrite("\n    First, get site and equipment metadata at those sites from the UNAVCO GSAC. The GSAC API Linux command is \n    "+cmd1 )
    # -L handles HTML address redirect on remote server end.
    sys.stdout.flush()

    # execute the command to make the query using the Linux 'curl' command line utility:
    cstatus1 = os.system(cmd1)
    # note that this Python process pauses until the cmd1 process completes. Typical time elaspsed is 1 second per station, or 2 mins for 120 stations.

    # handle failed connection: if cstatus1 != 0 :

    # For each  station in the list, get metadata about the station and equipment sessions there.

    if cstatus1 == 0 :
	station_metadata_file = open ("dataworks_stations.csv");
        # logWrite("    Opened 'GSAC full csv file' of station and equipment session data, dataworks_stations.csv, made with the GSAC query to UNAVCO."  );
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
        igs_cmd= "wget -v -N http://igscb.jpl.nasa.gov/igscb/station/general/rcvr_ant.tab"
        logWrite("\n    Get complete IGS definitions of 'correct' Domes, Receivers and Antennae names with \n     "+igs_cmd)
        try:
           igs_status1 = os.system(igs_cmd)
           if igs_status1 != 0 :
               logWrite("  PROBLEM: command "+isg_cmd+" returned status="+`igs_status1`)
        except :
           logWrite("    PROBLEM: try of command to run wget for rcvr_ant.tab failed. (NOT that the wget ram and failed to get a file)")

        try:  #  regardless of wget results, an igs rcvr table file should be here already.
	   igs_file = open ("rcvr_ant.tab");
           igsfileok=True
        except :
           logWrite("    igs file open fails, for local rcvr_ant.tab")
           try:
	      igs_file = open ("/dataworks/mirror_station_metadata_from_unavco/rcvr_ant.tab");
              logWrite("    igs file open OK, for /dataworks/mirror_station_metadata_from_unavco/rcvr_ant.tab")
              igsfileok=True
           except :
              logWrite("    igs file open fails,  for /dataworks/mirror_station_metadata_from_unavco/rcvr_ant.tab")

        logWrite   ("    Got and opened IGS file rcvr_ant.tab")

        # get contents of the rcvr_ant.tab file for later searches:
        igsmap = mmap.mmap(igs_file.fileno(), 0, access=mmap.ACCESS_READ)

        # read each line in the input Full CSV file, the results from the UNAVCO GSAC; i is 0 base
        for i in range(linecount) :

           line = station_metadata_file.readline()
           metadata = line

           # i>3 is to bypass 4 header lines ; and perhaps some others if testing is the i<35 say
           if (i>3):

             # one file line is one equipment session for one station. 

	     # to skip a "PENDING" station: trap the SPECIAL UNAVCO magic values:
	     # as in CN45,CN_Toco_GPS_2013,10.837,-60.9383,33.2,building wall,,2050-01-01T00:00:00,1980-01-01T00:00:00,TRM59800.00,SCIT,5225354537,0.0083, ...
	     if  "2050-01-01" in line and "1980-01-01" in line:
	        # SKIP this station,  and go try the next line
	        logWrite("      This is a 'pending' station an UNAVCO, with start time 2050-01-01 and end time in 1980; skip entry of this metadata."  );
	        continue

             # split out values from line, at commas
             strlist= string.split( line, "," )

             example_line='''
             from the unavco GSAC, a full sites csv file format example:
             There are four header lines, not used here. There are  fields: first at [0]

                #fields=ID[type='string'],station_name[type='string'],latitude,longitude,ellip_height[unit='m'],monument_description[type='string'],IERSDOMES[type='string'],session_start_time[type='date' format='yyyy-MM-ddTHH:mm:ss zzzzz'],session_stop_time[type='date' format='yyyy-MM-ddTHH:mm:ss zzzzz'],antenna_type[type='string'],dome_type[type='string'],antenna_SN[type='string'],Ant_dZ,Ant_dN,Ant_dE,receiver_type[type='string'],firmware_version[type='string'],receiver_SN[type='string'],receiver_sample_interval,city_locale[type='string'],state_prov[type='string'],country[type='string'],X,Y,Z,agencyname[type='string'],metpackname[type='string'],metpackSN[type='string'],site_count
                #   Generated by UNAVCO GSAC Repository on 2014-08-29T16:38:13 
                #   Missing times (no characters) may mean 'not removed' or 'no change.' 
                #   The CSV convention for point data is CF for CSV; see  UNAVCO GSAC docs page which is somewhere.
                BARA,Barahona,18.2086,-71.098,78,building wall,40801M001,2013-07-04T00:00:00,2013-08-22T23:59:45,TRM59800.00,SCIS,5220354476,0.0000,0.0000,0.0000,TRIMBLE NETR9,4.80,5115K74983,15,Barahona,,Dominican Republic,,,,,WXT520,G3920007,7
BARA,Barahona,18.2086,-71.098,78,building wall,40801M001,2013-08-23T00:00:00,2014-04-26T23:59:45,TRM59800.00,SCIS,5220354476,0.0000,0.0000,0.0000,TRIMBLE NETR9,4.81,5115K74983,15,Barahona,,Dominican Republic,,,,,WXT520,G3920007,7
BARA,Barahona,18.2086,-71.098,78,building wall,40801M001,2014-04-27T00:00:00,2014-05-07T23:59:45,TRM59800.00,SCIS,5220354476,0.0000,0.0000,0.0000,TRIMBLE NETR9,4.81,5115K74983,15,Barahona,,Dominican Republic,,,,,WXT520,G3920007,7
BARA,Barahona,18.2086,-71.098,78,building wall,40801M001,2014-05-08T00:00:00,2014-11-25T23:59:45,TRM59800.00,SCIS,5220354476,0.0000,0.0000,0.0000,TRIMBLE NETR9,4.85,5115K74983,15,Barahona,,Dominican Republic,,,,,WXT520,G3920007,7

                Note that IERSDOMES may be missing ",," for Dataworks.
                NOte that state name, x,y,z, and site_count are not used by Dataworks.

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
                28 site count                                       NOT used by Dataworks      
             '''

             SQLstatement=""

             code= (strlist[0]) # the 4 char id of a station such as ABMF
             if (len(code)>4) : # should not occur; attempt to do something useful.
                 code=code[:4]
                 # logWrite("  BAD station 4 char id is > 4 chars: "+strlist[0] +"; will use just "+code  );


             # when at a new station ; this happens first only after one station has been processed:
             if this_station_code != code :
                     if previous_station_code != "" :
                         logWrite(   "*******   Station "+ previous_station_code +" is up-to-date in the database (station count so far is "+`donecount`+") "  );
                     # if "" != this_station_code : # not the first time
                     previous_station_code = this_station_code
                     logWrite  (   "\n*******  Check station " + code );
                     sys.stdout.flush()
                     sys.stdout.flush()
                     donecount += 1

             this_station_code = code

             ## logWrite(metadata line; this to compare results of several run of this script, to see if the INPUT is the same
             logWrite("\n    Next equip session metadata line in csv file from UNAVCO: "+line[:70] ); # first part of line
             logWrite  ("      "+line[70:-1]  ); # second half of line
             sys.stdout.flush()

             staname = (strlist[1]) # station name

             # these next values are strings, not numbers. Which are what you want to load the db, to avoid Python rounding errors

             latstr =     (strlist[2])
             lonstr =     (strlist[3])
             ellphgtstr = (strlist[4])
             # native strings in all cases:
             domesiers =  (strlist[6])   # May be undefined, not given, null, just ",," in the input file .csv

             stastart  =  (strlist[7])  #a STRING not datatime object;   like stop time=_2009-03-13T23:59:45   NOTE has T 
             # cut time value to second value; if full ISO 8601 value with time zone offset value, like start time=_2006-08-23T00:00:00 +0000
             if (len(stastart)>19) :
                 stastart=stastart[:19]

             stastop   =  (strlist[8])
             # cut time value to second value; if full ISO 8601 value with time zone offset value, like start time=_2006-08-23T00:00:00 +0000
             if (len(stastop)>19) :
                 stastop=stastop[:19]

             anttype  =  (strlist[9])
             item = anttype
	     if igsmap.find(item) != -1:
                  logWrite  (   "      Antenna name "+item+" is a valid IGS name, and in the IGS file rcvr_ant.tab")
	     else :
                  errormsg= "\n PROBLEM FIX: Antenna name name "+item+" is NOT a valid IGS name. (got from the UNAVCO GSAC.) " \
                            + "\n  The UNAVCO data for station "+code+" beginning at "+stastart \
                            +  " has INVALID  Antenna name "+item+ ", not in http://igscb.jpl.nasa.gov/igscb/station/general/rcvr_ant.tab  \n "
                  logWrite ( errormsg)
                  #sendEmail (subject, fromaddr, toaddr, text)
                  #sendEmail ("Dataworks: about invalid IGS ant/rcvr/radome name from UNAVCO",  "", "dataworks@unavco.org", errormsg )
             	  continue  # try next station metadata set



             radometype  =  (strlist[10])
             item = radometype
	     if igsmap.find(item) != -1:
                  logWrite  (   "      RADOME name "+item+" is a valid IGS name, and in the IGS file rcvr_ant.tab")

	     else :
                  errormsg= "\n PROBLEM: RADOME name "+item+" is NOT a valid IGS name. (got from the UNAVCO GSAC.) " \
                            + "\n The UNAVCO data for station "+code+" beginning at "+stastart \
                            +  " has INVALID  RADOME "+item+ ", not in http://igscb.jpl.nasa.gov/igscb/station/general/rcvr_ant.tab  \n "
                  logWrite ( errormsg)
                  #sendEmail (subject, fromaddr, toaddr, text)
                  #sendEmail ("Dataworks: about invalid IGS ant/rcvr/radome name from UNAVCO",  "", "dataworks@unavco.org", errormsg )
             	  continue  # try next station metadata set


             antsn  =  (strlist[11])
             adz  =  (strlist[12])
             adn  =  (strlist[13])
             ade  =  (strlist[14])


             rcvtype  =  (strlist[15])
             item = rcvtype
	     if igsmap.find(item) != -1:
                  logWrite  (   "      Receiver name "+item+" is a valid IGS name, and in the IGS file rcvr_ant.tab");
	     else :
                  errormsg= "\n PROBLEM: Receiver name "+item+" is NOT a valid IGS name. (got from the UNAVCO GSAC.) " \
                            + "\n The UNAVCO data for station "+code+" beginning at "+stastart \
                            +  " has INVALID Receiver name "+item+ ", not in http://igscb.jpl.nasa.gov/igscb/station/general/rcvr_ant.tab  \n "
                  logWrite ( errormsg)
                  #sendEmail ("Dataworks: about invalid IGS ant/rcvr/radome name from UNAVCO",  "", "dataworks@unavco.org", errormsg )
             	  continue;  # try next station metadata set


             rcvfwvers  =  (strlist[16])
             rcvsn  =  (strlist[17])
             rcvsampInt  =  (strlist[18]) # a string for a number
             logWrite("      rcvsampInt  = _" + rcvsampInt   );
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
             logWrite("      metpackname = _"+metpackname+"_  metpackSN =_"+metpackSN+"_   metpack_id="+`metpack_id`  );

             #state         = (strlist[20])  # NOT used by dataworks, as per design specification announced in a meeting.
             # as per instructions of Fran Boler, Oct 29 2014, do NOT store state or province name anywhere in the Dataworks database; so skip these 4 lines:

             logWrite("      This input equip_config sesssion for station "+code +" has equip_config_start time= "+stastart+ "   _stop time=_"+stastop )

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
                   logWrite( "      For this new station, you need to insert in the database some more information, not avaiable from UNAVCO GSAC results: "  );
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
                   agency_id      = 30   # "not supplied by UNAVCO mirror"           (and not shown by GSAC, so no way to get the value from the db by remote user.)

                   # get  or set the id for the foreign keys, to be reset in in this program based in onfo in the gsac full csv input file:
                   monument_style_id = 1 # id value is "not specified"
                   country_id=1          # not specified
                   locale_id=1           # "not specified"
                   the_id=1
                    # get or set monument_style_id based on monument_style_description value:
                   if ""==monument_style_description :
                        # default value of "not specified" at id=1 in db is used
                        logWrite("       no monument style in UNAVCO GSAC full csv file results " );
                        pass
                   else:
                        # id= getOrSetTableRow (idname, tablename,  rowname, rowvalue)
                        monument_style_id  = getOrSetTableRow ("monument_style_id", "monument_style", "monument_style_description", monument_style_description) 

                   if ""==locale_info:
                        # default value of "not specified" at id=1 in db is used
                        logWrite("       no locale info in UNAVCO GSAC full csv file results"  );
                        pass
                   else:
                        # id= getOrSetTableRow (idname, tablename,  rowname, rowvalue)
                        locale_id  = getOrSetTableRow ("locale_id", "locale", "locale_info", locale_info) 

                   if ""== country_name:
                        # default value of "not specified" at id=1 in db is used
                        logWrite("       no country name in UNAVCO GSAC full csv file results"  );
                        pass
                   else:
                        # id= getOrSetTableRow (idname, tablename,  rowname, rowvalue)
                        country_id  = getOrSetTableRow ("country_id", "country", "country_name", country_name) 

                   logWrite ("        got values of ids monument_style_id, locale_id, country_id = "+`monument_style_id`+"  "+`locale_id`+"   "+ `country_id`)

                   station_photo_URL= "" 
                   time_series_plot_photo_URL="" 

                   SQLstatement=("INSERT into station (four_char_name,station_name,latitude_north,longitude_east,height_above_ellipsoid,installed_date, style_id, status_id, access_id,  ellipsoid_id ,  iers_domes, network_id,country_id,locale_id,monument_style_id ) values  ('%s', '%s', %s,  %s,  %s, '%s', %s, %s,  %s, %s, '%s', '%s', '%s', %s, %s)" % ( code, staname, latstr, lonstr, ellphgtstr, stastart,  style_id, status_id, access_id, ellipsoid_id, domesiers,  network_id, country_id,locale_id, monument_style_id))

                   try:
                       # add this new station to the Dataworks database 'station' table.
                       # rows in foreign keys' tables must be populated already in the database.
                       logWrite("       Insert the new station into station table with SQL \n       "+SQLstatement   )
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
                       logWrite( "      Station "+code+" has station_id "+`station_id`  )
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
             create_time                 = crtimestamp # this is when added to db; nothing about the session really
             new_equip_config_start_time =stastart
             new_equip_config_stop_time  =stastop

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
                 start2=datetime.datetime.strptime(stastart , "%Y-%m-%dT%H:%M:%S" )
                 stop2=datetime.datetime.strptime(stastop , "%Y-%m-%dT%H:%M:%S" )
                 #logWrite(  "      DEBUG: "+this_station_code+" previous equip session times: "+start1.strftime("%Y-%m-%d %H:%M:%S")+" - "+stop1.strftime("%Y-%m-%d %H:%M:%S"))
                 #                                                            2014-02-21 23:59:45 - 2014-02-21 23:59:45
                 #logWrite(  "      DEBUG: "+this_station_code+"    these equip session times:                       "+start2.strftime("%Y-%m-%d %H:%M:%S")+" - "+stop2.strftime("%Y-%m-%d %H:%M:%S"))
                 # if THIS interval STARTS before the PREVIOUS interval ENDS (all cases of "too early")
                 if stastart < prev_stop_time : 
                      logWrite(  " PROBLEM: in data from UNAVCO, bad equip session time spans: equip_config times overlap; start before previous end time"  );
                      logWrite(  "       metadata line = \n      "+line  );
                      #failedcount +=1

             # end skip - block  to look for problematical equip session time spans '''
             nn='''
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
             prev_start_time =stastart
             prev_stop_time  =stastop
             prev_metadata = line 

             # Check if this equipment session at this station already is in the database:
             haveit=False
             dbeqstart= "" # a STRING
             dbeqstop = ""
             # for NEW values in the input file: make datetime objects:
             newstastartDT=datetime.datetime.strptime(stastart, "%Y-%m-%dT%H:%M:%S" )
             newstastopDT =datetime.datetime.strptime(stastop,  "%Y-%m-%dT%H:%M:%S" )
             the_id=1
             esid=-1
             try:
                   #logWrite("      Look for this particular equipment session at station_id="+`station_id` ) # +"  in the db with SQL \n      "+statement
                   # ONLY check for this station and this start time;  end time given by UNAVCO GSAC is variable for the current active session.
                   SQLstatement=("SELECT equip_config_id,equip_config_start_time,equip_config_stop_time from equip_config where station_id= %s and equip_config_start_time= '%s'" % (station_id, new_equip_config_start_time))
                   logWrite("      Look for this equip config record in the dq with sql  ... \n      "+ SQLstatement)
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
                 logWrite("      This session # "+`esid`+" is already in the db. ") 
                 if newstastartDT == dbeqstart  and newstastopDT == dbeqstop :
                     logWrite("      No new GNSS data for this station from the UNAVCO archive today (the former and new session start and stop times match). ") 
                     logWrite("      Done with this equip session data set line. Go try next input metadata line from the UNAVCO GSAC .csv file.\n  ") 
                     doAddSession=False
                 elif newstastopDT > dbeqstop :
                     doAddSession=False
                     logWrite("      But, need to update the equip_config_stop_time in that record, to the later new stop time from new input data.") 
                     logWrite("      (new equip_config_stop_time is " + str(newstastopDT) +"; > db stop time "+ str(dbeqstop) +") "  ) 
                     #doUpdateStopTime= True
                     statement=("UPDATE equip_config set equip_config_stop_time = '%s'  where equip_config_id= %s " % (new_equip_config_stop_time, esid))
                     #logWrite("      the SQL is "+statement)
                     cursor.execute(statement)
                     gsacdb.commit()
                     logWrite("      Did update the equip_config_stop_time in the db, for station "+code+" and at equip_config_id "+`esid` +"\n"  )
                     stoptimeupdatecount += 1
                     # end of processing one metadata line from the UNAVCO GSAC .csv file

             skip='''
                 else:
                     logWrite("     PROBLEM SOME ODD equip session times here; compare start and stop times in the database and in this GCAC info file, for station "+code+". ") 
                     doAddSession=False
                     pass

	     # one could check the equip table values against the same parameter's values from the unavco gsac query results, in case of changes;             
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
		 logWrite("       add equip session")
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
                       logWrite("     Inserted a new equipment session into the db " )
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


def sendEmail (subject, fromaddr, toaddr, text) :
	# the text file contains only ASCII characters.
	# fromaddr is the sender's email address
	# toaddr is the recipient's email address, actually A list of strings, one for each recipient.
	# Import smtplib for the actual sending function
	#import smtplib
	# Import the email modules 
	#from email.mime.text import MIMEText

        # call like sendEmail (text, ' Dataworks: mirrorStation.py error detected',  '', 'wier@unavco.org' )

	# Open a plain text file for reading.  For this example, assume that
	# the text file contains only ASCII characters.
	#fp = open(textfile, 'rb')
	# Create a text/plain message
	#msg = MIMEText(fp.read())
	#fp.close()

	msg = MIMEText( text )

	msg['Subject'] = subject
	msg['From'] = fromaddr
	msg['To'] = toaddr 

	# Send the message via our own SMTP server, but don't include the envelope header.
        try :
	   s = smtplib.SMTP('localhost')
	   s.sendmail(fromaddr, [toaddr], msg.as_string())
	   s.quit()
	except SMTPException:
	   print "Error: unable to send email"




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

logflag =1 # CHANGE USE =1 for routine operations.  use 2 for testing  and details

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
    logWrite("\n ***** *****  Inserted "+`newstacount`    +" new stations. (look in this log file for 'Inserted.') "  )
    ## logWrite("\n ***** *****  Inserted "+`newstacount`    +" new stations, without problems.")  );
    pass
else :
    logWrite("\n ***** ***** No new stations added."  )
    pass

if newsessioncount>0  :
    logWrite("\n ***** *****  Inserted "+`newsessioncount`+" new equipment sessions. (look in this log file for 'Inserted.')"  )
    ## logWrite("\n ***** *****  Inserted "+`newsessioncount`+" new equipment sessions, without problems. \n \n")
    pass
else :
    logWrite("\n ***** ***** No new equipment sessions added."  )
    pass

logWrite(    "\n ***** ***** There are "+`donecount`+" stations in the database."  )

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
