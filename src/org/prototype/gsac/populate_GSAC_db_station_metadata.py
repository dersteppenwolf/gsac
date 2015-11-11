#!/usr/bin/python
'''
 filename              : populate_GSAC_db_station_metadata.py
 author                : Stuart Wier 
 created               : 2014-09-03
 updates               : 2014-09-04 to 2015-07-05.  2015-10-16 to 2015-11-11 ...

 exit Code(s)          : 0, success
                       : sys.exit (1) , curl failed, or no igs rcvr_ant.tab file found.

 description           The use is:
                       1. to make the initial population of a GSAC Prototype_15 style database with all stations in the network, and all the equipment sessions at each station.
                       2. to find and add newly-added stations in the network (and the new equipment sessions at that station).
                       3. to update existing equipment sessions (db table equip_config records), when the metadata was changed. Usually session stop_time.

                       The metadata comes from an ascii csv file in "GSAC site full csv format" like those output by GSACs on site searches, BUT NO header lines.

 configuration:        First, one time only, for your network and operations, revise lines with CHANGE :

                       satellite_system       ="GPS" # default; or could be for example "GPS,GLONASS"   CHANGE

                       And, optionally, you may change the value of logflag to choose if you want to see output on the terminal screen:
                       logflag =1   # controls screen output.  CHANGE: use =1 for routine operations, or use =2 to print log lines to screen, for testing

                       
 usage:                Run this script to update station metadata in the GSAC.
                  
                       First, make a csv file, the inputfilename, named like stations_instruments.csv, with station metadata you want in the database. 
                       The file format is that of a GSAC full sites.csv file, lacking the 4 top header lines. 

                       This Python is run with a command like this:

                              ./populate_GSAC_db_station_metadata.py   dbhost   dbaccount   dbaccountpw   dbname  inputfilename

                       Use your names for the database account name, account password, and database name such as  "localhost root pw2db Prototype_15_GSAC"

                       Running the process takes about 1 second per site; and makes a log file, populate_GSAC_db_station_metadata.log

                       Look at the log file after each run.  Look for errors noted in lines with "PROBLEM" and fix any problems.

                       You may need to insert a new agency in the db agency table, first.

 tested on             Python 2.6.5 on Linux (Ubuntu) ; CentOS Python 2.7.6 (default, Sep 16 2014, 12:23:18) [GCC 4.4.7 20120313 (Red Hat 4.4.7-4)] 
 

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
        
def load_db () :
    global logFile 
    global logfilename 
    global inputfilename
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

    logFile     = open (logfilename, 'w')  # NOTE this creates a NEW file of the same log file name, destroying any previous log file of this name.

    logWrite("\n  Log of populate_GSAC_db_station_metadata.py "+timestamp + " (log file is "+logfilename+")" )

    logWrite("\n  Look at the log files after each run.  Look for errors noted in lines with PROBLEM or LOOK and fix those issues. ***** *****\n")

    logWrite("    populate_GSAC_db_station_metadata.py loads or updates a GSAC Prototype_15 database with station and equipment session data from a full csv file."  )
    logWrite(  "    First, make a csv inputfile with station metadata you want in the database. "  )
    logWrite(  "    The use is:"  )
    logWrite(  "       1. To make the initial (first time) population of the GSAC database with all stations in the network, and all the equipment sessions at each station."  )
    logWrite(  "       2. Thereafter, for updates, to find and add any newly-added stations in the network (and add the equipment sessions at that station)."  )
    logWrite(  "          To update the equip session end times at the still-active sessions (which should have end time of 'end of today') "  )

    sys.stdout.flush()
    donecount=0

    cstatus1 = 0 # relic of old test
    if cstatus1 == 0 :
        logWrite ("    Open the input file "+ inputfilename );
        station_metadata_file = open (inputfilename) # ("stations_instruments.csv");
        # read and count how many lines in file
        allLines = station_metadata_file.readlines()
        linecount = len(allLines)
        station_metadata_file.seek(0) # rewind to beginning

        # line 1 is header only

        if linecount > 1 : logWrite(    "    There are "+`(linecount-1)`+" station - equipment sessions in this file."  )
        sys.stdout.flush()

        # full csv files have one or more lines for every station, one line per equipment session.  Use the station 4char ID ("four_char_ID") to keep track which one is in use.
        four_char_ID="" # the 4 char id of a station such as ABMF
        this_station_four_char_ID="" # working station now
        previous_station_four_char_ID=""  # the previous station in use
        prev_start_time =""
        prev_stop_time  =""
        donecount=0
        station_id = None  
        station_counter=0;
        # station 4-char id list to help manage the same
        sta_ID_list=[]
        prev_metadata = " "

        notes='''
        | To use the official IGS file rcvr_ant.tab
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
        logWrite("\n    Get complete IGS definitions of 'correct' Domes, Receivers and Antennae names with \n     "+igs_cmd)
        try:
           igs_status1 = os.system(igs_cmd)
           if igs_status1 != 0 :
               logWrite("  PROBLEM: command "+isg_cmd+" returned status="+`igs_status1`)
        except :
           pass # logWrite("    PROBLEM: try of command   "+isg_cmd+"    failed.")

        # Regardless of wget results, an old (or new) igs rcvr_ant.tab file; something should be here.
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
                igs_file = open ("rcvr_ant.tab"); # CHANGE
                igsfileok=True
            except :
                pass #logWrite("    igs file open fails,  for rcvr_ant.tab")
        if igsfileok :
            logWrite   ("    Opened a local copy of the IGS file rcvr_ant.tab")
        else :
            logWrite("    PROBLEM: could not find or open  local copy of the IGS rcvr_ant.tab file. Exit.  \n");
            sys.exit(1)

        # get contents of the rcvr_ant.tab file for later searches:
        igsmap = mmap.mmap(igs_file.fileno(), 0, access=mmap.ACCESS_READ)

        # read each line in the input file
        for i in range(linecount) :

           line = station_metadata_file.readline()
           metadata = line

           # line index from 0 on is data; can use for test also  and  i<30  to bypass lines after 30  if testing 
           if (i>=0 ):

             # one file line is one equipment session for one station. 
             #logWrite("\n     #"+ `(i-3)` + " metadata line in station-session file: "+line[:-1]  );

             # to skip a "PENDING" station: look for  the SPECIAL UNAVCO magic values:
             # as in CN45,CN_Toco_GPS_2013,10.837,-60.9383,33.2,building wall,,2050-01-01T00:00:00,1980-01-01T00:00:00,TRM59800.00,SCIT,5225354537,0.0083, ...
             if  "2050-01-01" in line and "1980-01-01" in line:
                # SKIP this station,  and go try the next line
                logWrite("      This is a 'pending' station a la' UNAVCO, with start time 2050-01-01 and end time in 1980; skip entry of this metadata."  );
                continue

             # split out values from line, at commas
             strlist= string.split( line, "," )

             example_line='''
             a full csv input file format example, for site's info:

P340,DashielCrkCN2008,39.4093,-123.0498,865.23,shallow-drilled braced,,2008-02-15T06:38:45,2009-03-12T05:59:45,TRM29659.00,SCIT,0220382762,0.0083,0.0000,0.0000,TRIMBLE NETRS,1.1-2 19 Apr 2005,4623116489,,,,,-       2691539.1186,-4136726.977,4028080.6731,,,,PBO;PBO Analysis  Complete;PBO Core Network;,1
P340,DashielCrkCN2008,39.4093,-123.0498,865.23,shallow-drilled braced,,2009-03-12T06:00:00,2010-10-22T05:59:45,TRM29659.00,SCIT,0220377400,0.0083,0.0000,0.0000,TRIMBLE NETRS,1.1-2 19 Apr 2005,4623116489,,,,,-       2691539.1186,-4136726.977,4028080.6731,,,,PBO;PBO Analysis  Complete;PBO Core Network;,1
P340,DashielCrkCN2008,39.4093,-123.0498,865.23,shallow-drilled braced,,2010-10-22T06:00:00,2015-10-16T05:59:45,TRM29659.00,SCIT,0220377400,0.0083,0.0000,0.0000,TRIMBLE NETRS,1.3-0,4623116489,,,,,-2691539.1186,-     4136726.977,4028080.6731,,,,PBO;PBO Analysis  Complete;PBO Core Network;,1
P341,WhiskytownCN2005,40.6507,-122.6069,406.85,deep-drilled braced,,2005-09-23T06:19:00,2010-07-18T05:59:45,TRM29659.00,SCIT,0220366152,0.0083,0.0000,0.0000,TRIMBLE NETRS,1.1-2 19 Apr 2005,4518249765,,,,,,,,,,,PBO; PBO Analysis  Complete;PBO Core Network;,2
P341,WhiskytownCN2005,40.6507,-122.6069,406.85,deep-drilled braced,,2010-07-18T06:00:00,2015-10-16T05:59:45,TRM29659.00,SCIT,0220366152,0.0083,0.0000,0.0000,TRIMBLE NETRS,1.3-0,4518249765,,,,,,,,,,,PBO;PBO          Analysis  Complete;PBO Core Network;2015-10-14 23:59:45,2

                Note that IERSDOMES may be correctly missing ",," for most stations

                the indices and values in a station row:
                0 4char ID  like ABMF
                1 station name    Aeroport du Raizet -LES ABYMES - Mitio France
                2 latitude          16.2623
                3 longitude         -61.5275
                4 ellipsoid height  -25.67
                5 monument style:  building roof
                6 iersdome 97103M001                               # MAY be not given in the file: optional 
                7 session start time   2011-03-24T00:00:00
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
                20 state/province Guadeloupe                   
                21 country France
                22, 23, 24, X,Y,Z,  the TRF position coordinates  
                25 agency name 
                26 metpack name
                27 metpack serial number
                28 "networks": a  ;-separated list of network names for this station and time interval
                29 latest data time 
                (last item in each line is a site count, not used by GSAC)
             '''

             SQLstatement=""

             four_char_ID= (strlist[0]) # the 4 char id of a station such as ABMF
             if (len(four_char_ID)>4) :  # should not occur; attempt to do something useful.
                 four_char_ID=four_char_ID[:4]
                 # logWrite("  BAD station 4 char id is > 4 chars: "+strlist[0] +"; will use just "+four_char_ID  );


             # when at a new station ; this happens first only after one station has been processed:
             if this_station_four_char_ID != four_char_ID :
                     if previous_station_four_char_ID != "" :
                         logWrite(   "*******   Station "+ previous_station_four_char_ID +" is up-to-date in the database (station count so far is "+`donecount`+") "  );
                     # if "" != this_station_four_char_ID : # not the first time
                     previous_station_four_char_ID = this_station_four_char_ID
                     logWrite  (   "\n*******  Check station " + four_char_ID );
                     sys.stdout.flush()
                     sys.stdout.flush()
                     donecount += 1

             this_station_four_char_ID = four_char_ID

             ## logWrite(metadata line; this to compare results of several run of this script, to see if the INPUT is the same
             logWrite("\n    Station and an equip session metadata line in csv file:\n      "+line[:70] ); # first part of line
             logWrite  ("      "+line[70:-1]  ); # second half of line
             sys.stdout.flush()

             staname = (strlist[1]) # station name

             # these next values are strings, not numbers. Which are what you want to load the db, to avoid Python rounding errors when converted to floats, which is needless.
             latstr =     (strlist[2])
             lonstr =     (strlist[3])
             ellphgtstr = (strlist[4])

             # native strings in all cases:
             monument_style_description   = (strlist[5])

             iers_domes =  (strlist[6])   # May be undefined, not given, null, just ",," in the input file .csv
             # most stations properly do not have this value 

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

             #logWrite("      This input equip_config sesssion for station "+four_char_ID +" has equip_config_start time= "+equipSessStartTime+ "  stop time=_"+equipSessStopTime )

             anttype  =  (strlist[9])
             item = anttype
             if igsmap.find(item) != -1:
                  #logWrite  (   "      Antenna name "+item+" is a valid IGS name, and in the IGS file rcvr_ant.tab")
                  pass
             else :
                  errormsg= "\n PROBLEM FIX: Antenna name name "+item+" is not an IGS name.  " \
                            +   "   has unexpected antenna name "+item+ ", not in http://igscb.jpl.nasa.gov/igscb/station/general/rcvr_ant.tab  \n "
                  logWrite ( errormsg)
                  #continue  # go ahead and use it anyway; its what the GSAC operator says is the antenna and we can't enforce some special name.

             radometype  =  (strlist[10])
             item = radometype
             if igsmap.find(item) != -1:
                  #logWrite  (   "      RADOME name "+item+" is a valid IGS name, and in the IGS file rcvr_ant.tab")
                  pass
             else :
                  errormsg= "\n PROBLEM: RADOME name "+item+" is not an IGS name.  " \
                            +   "   has unexpected RADOME "+item+ ", not in http://igscb.jpl.nasa.gov/igscb/station/general/rcvr_ant.tab  \n "
                  logWrite ( errormsg)
                  # continue  

             antsn  =  (strlist[11])
             adz  =  (strlist[12]) # char string of a number
             adn  =  (strlist[13])
             ade  =  (strlist[14])

             rcvtype  =  (strlist[15])
             item = rcvtype
             if ""!=  rcvtype :
                 if igsmap.find(item) != -1:
                      #logWrite  (   "      Receiver name "+item+" is a valid IGS name, and in the IGS file rcvr_ant.tab");
                      pass
                 else :
                      errormsg= "\n PROBLEM: Receiver name "+item+" is not an IGS name.  " \
                                +   "   has unexpected receiver name "+item+ ", not in http://igscb.jpl.nasa.gov/igscb/station/general/rcvr_ant.tab  \n "
                      logWrite ( errormsg)
                      # continue; 

             rcvfwvers     = (strlist[16])
             rcvsn         = (strlist[17])
             rcvsampInt    = (strlist[18]) # a string for a number
             #logWrite("      rcvsampInt  = _" + rcvsampInt   );

             locale_info   = (strlist[19])
             province_name = (strlist[20])
             nation_name   = (strlist[21])

             X             = (strlist[22]) # these 3 values are strings, not numbers. Which are what you want to load the db, to avoid Python rounding errors
             Y             = (strlist[23])
             Z             = (strlist[24])
             agencyname    = (strlist[25])
             #logWrite("      agency  =" + agencyname   ); # debug

             metpackname   = (strlist[26])
             metpack_serial_number=      (strlist[27])
             #    such as    metpackname = _WXT520_  metpack_serial_number =_K2950011_
             metpack_id=3 # look specail value for 'no metpack' in the GSAC Prototype_15 database, table metpack.
             if metpackname != "":
                 metpack_id = getOrSetTableRow ("metpack_id", "metpack", "metpack_name", metpackname) 
                 logWrite("      metpackname = _"+metpackname+"_  metpack_serial_number =_"+metpack_serial_number+"_   metpack_id="+`metpack_id`  );

             networks      = (strlist[28]) 
             if ""==networks :
                #logWrite("      networks is undefined; will use '' to create the value NULL in the db"  );
                pass
             else:
                #logWrite("      networks="+`networks`  );
                pass
             
             station_photo_URL          = "" # like http://www.unavco.org/data/gps-gnss/lib/images/station_images/PALX.jpg
             time_series_plot_image_URL = "http://geodesy.unr.edu/tsplots/IGS08/TimeSeries_cleaned/"+four_char_ID + ".png"  # like http://pboshared.unavco.org/timeseries/PALX_timeseries_cleaned.png 
                                             #  or  http://geodesy.unr.edu/tsplots/IGS08/TimeSeries_cleaned/ABMF.png 

             # Finally, insert the station metadata in the database -- if not already there. 

             #  find the station_id number in the db for this 4 char ID four_char_ID and the station name
             try:
                  #logWrite("      get station id number for  station with  4charID="+four_char_ID +"_ name="+staname+"_ ")
                  cursor.execute("""SELECT station_id from station where four_char_name = %s and station_name = %s """, (four_char_ID, staname ) )
                  #  not used for selects  :  gsacdb.commit()

                  # rows= cursor.fetchall() # gives array of array for each row, like ['Bob', '9123 4567'] for one row result of 2 values
                  # or for one row in rows, then use the one row
                  row= cursor.fetchone()
                  station_id=  row[0];
                  station_id=  int(station_id); # fix the "L" value returned
                  #  Already have row "+`station_id`+" in the db station table for "+four_char_ID+", so will not add new station data to the table.
                  logWrite(  "      Already have station "+four_char_ID +" in the database at  station_id= "+`station_id` );
                  sys.stdout.flush()
                  if four_char_ID not in sta_ID_list:
                      sta_ID_list.append(four_char_ID)
             except:
                   # the station is not in the database, so add it:
                   logWrite( "\n      Station is NEW; not in database, so insert this new station "+four_char_ID+" with name="+staname+" in the GSAC db station table:"  );
                   skip='''
                   logWrite( "      "  );
                   logWrite( "      ######################################################################################################################### "  );
                   logWrite( "      For this new station, you need to insert in the database some more information, not avaiable from the input file. "  );
                   logWrite( "      such as the station_photo_URL  in table station for each station, and ");
                   logWrite( "      in table equip_config, insert the radome serial number. "  );
                   logWrite( "      ######################################################################################################################### "  );
                   logWrite( "      "  );
                   '''

                   sys.stdout.flush()

                   #  use these initial values for tations, 
                   style_id       = 1    # GPS/GNSS Continuous
                   status_id      = 1    # Active 
                   access_id      = 2    # full public access

                   monument_style_id = 1 # id value 1 is "not specified"
                   nation_id=1           # id for name of nation; value 1 is "not specified"
                   locale_id=1           # id for name of city or place etc.   ; value 1 is "not specified"

                   the_id=1
                    # get or set monument_style_id based on monument_style_description value:
                   if ""==monument_style_description :
                        # default value of "not specified" at id=1 in db is used
                        logWrite("       no monument style in input file" );
                        pass
                   else:
                        # id= getOrSetTableRow (idname, tablename,  rowname, rowvalue)
                        monument_style_id  = getOrSetTableRow ("monument_style_id", "monument_style", "monument_style_description", monument_style_description) 
                        #print "   monument_style_description="+monument_style_description+";  id="+`monument_style_id`    # debug

                   info='''
                    mysql> desc locale;
                    +-------------+-----------------+------+-----+---------+----------------+
                    | Field       | Type            | Null | Key | Default | Extra          |
                    +-------------+-----------------+------+-----+---------+----------------+
                    | locale_id   | int(3) unsigned | NO   | PRI | NULL    | auto_increment |
                    | locale_name | varchar(70)     | NO   |     | NULL    |                |
                    +-------------+-----------------+------+-----+---------+----------------+
                    2 rows in set (0.00 sec)

                    mysql> select * from locale;
                    +-----------+------------------------+
                    | locale_id | locale_name            |
                    +-----------+------------------------+
                    |         1 | Boulder                |
                    |         2 | Felipe Carrillo Puerto |
                    |         3 | Weaverville            |
                    |         4 | Cerrillos              |
                    +-----------+------------------------+
                   '''
                   if ""==locale_info:
                        # default value of "not specified" at id=1 in db is used
                        logWrite("       no locale info input"  );
                        pass
                   else:
                        # id= getOrSetTableRow (idname, tablename,  rowname, rowvalue)
                        #print "  locale_info ="+locale_info  # debug
                        locale_id  = getOrSetTableRow ("locale_id", "locale", "locale_name", locale_info) 

                   if ""== nation_name:
                        # default value of "not specified" at id=1 in db is used
                        logWrite("       no country name in  input"  );
                        pass
                   else:
                        # id= getOrSetTableRow (idname, tablename,  rowname, rowvalue)
                        #print "  nation ="+nation_name # debug
                        nation_id  = getOrSetTableRow ("nation_id", "nation", "nation_name", nation_name) 

                   #logWrite ("        got values of ids monument_style_id, locale_id, nation_id = "+`monument_style_id`+"  "+`locale_id`+"   "+ `nation_id`)

                   skip ='''
                    LOOK how to create NULL in the db insert statement with '' for undefined numbers?
                   if ''==X: 
                        X = "" #None
                   if ''==Y: 
                        Y ="" # no workee None
                   if ''==Z:
                        Z ="" # None
                    '''

                   prov='''
                    mysql> desc province_state;
                    +---------------------+-----------------+------+-----+---------+----------------+
                    | Field               | Type            | Null | Key | Default | Extra          |
                    +---------------------+-----------------+------+-----+---------+----------------+
                    | province_state_id   | int(3) unsigned | NO   | PRI | NULL    | auto_increment |
                    | province_state_name | varchar(70)     | NO   |     | NULL    |                |
                    +---------------------+-----------------+------+-----+---------+----------------+
                   '''
                   province_state_id=1
                   if ""== province_name:
                        logWrite("       no province in  input"  );
                        pass
                   else:
                        # id= getOrSetTableRow (idname, tablename,  rowname, rowvalue)
                        province_state_id  = getOrSetTableRow ("province_state_id", "province_state", "province_state_name", province_name) 


                   agen='''mysql> desc agency;
    +--------------------------+-----------------+------+-----+---------+----------------+
    | Field                    | Type            | Null | Key | Default | Extra          |
    +--------------------------+-----------------+------+-----+---------+----------------+
    | agency_id                | int(4) unsigned | NO   | PRI | NULL    | auto_increment |
    | operating_agency_name    | varchar(100)    | NO   |     | NULL    |                |
    | operating_agency_address | varchar(150)    | YES  |     | NULL    |                |
    | operating_agency_email   | varchar(100)    | YES  |     | NULL    |                |
    | agency_individual_name   | varchar(100)    | YES  |     | NULL    |                |
    | other_contact            | varchar(150)    | YES  |     | NULL    |                |
    +--------------------------+-----------------+------+-----+---------+----------------+
                       '''
                   agency_id=1
                   if ""== agencyname:
                        logWrite("       no agency in  input"  );
                        pass
                   else:
                        # id= getOrSetTableRow (idname, tablename,  rowname, rowvalue)
                        agency_id  = getOrSetTableRow ("agency_id", "agency", "operating_agency_name", agencyname) 
                        logWrite("       set agency_id="+`agency_id`  );

                   agency_id=1 # debug

                   stat='''
                    mysql> desc station;
                    +----------------------------+-----------------+------+-----+---------+----------------+
                    | Field                      | Type            | Null | Key | Default | Extra          |
                    +----------------------------+-----------------+------+-----+---------+----------------+
                    | station_id                 | int(6) unsigned | NO   | PRI | NULL    | auto_increment |
                    | four_char_name             | char(4)         | NO   |     | NULL    |                |
                    | station_name               | varchar(50)     | NO   |     | NULL    |                |
                    | latitude_north             | double          | NO   |     | NULL    |                |
                    | longitude_east             | double          | NO   |     | NULL    |                |
                    | height_ellipsoid           | float           | YES  |     | NULL    |                |
                    | X                          | double          | YES  |     | NULL    |                |
                    | Y                          | double          | YES  |     | NULL    |                |
                    | Z                          | double          | YES  |     | NULL    |                |
                    | installed_date             | datetime        | YES  |     | NULL    |                |
                    | published_date             | datetime        | YES  |     | NULL    |                |
                    | retired_date               | datetime        | YES  |     | NULL    |                |
                    | earliest_data_date         | datetime        | YES  |     | NULL    |                |
                    | latest_data_date           | datetime        | YES  |     | NULL    |                |
                    | agency_id                  | int(3) unsigned | YES  | MUL | NULL    |                |
                    | access_id                  | int(3) unsigned | YES  | MUL | NULL    |                |
                    | style_id                   | int(3) unsigned | YES  | MUL | NULL    |                |
                    | status_id                  | int(3) unsigned | NO   | MUL | NULL    |                |
                    | monument_style_id          | int(3) unsigned | YES  | MUL | NULL    |                |
                    | nation_id                  | int(3) unsigned | YES  | MUL | NULL    |                |
                    | province_state_id          | int(3) unsigned | YES  |     | NULL    |                |
                    | locale_id                  | int(3) unsigned | YES  | MUL | NULL    |                |
                    | networks                   | varchar(2000)   | YES  |     | NULL    |                |   semi-colon separated list of network names, like    PBO;COCONut   Do not use commas in this string!
                    | originating_agency_URL     | varchar(220)    | YES  |     | NULL    |                |
                    | iers_domes                 | char(9)         | YES  |     | NULL    |                |
                    | station_photo_URL          | varchar(100)    | YES  |     | NULL    |                |
                    | time_series_plot_image_URL | varchar(100)    | YES  |     | NULL    |                |
                    | embargo_duration_hours     | int(6) unsigned | YES  |     | NULL    |                |
                    | embargo_after_date         | datetime        | YES  |     | NULL    |                |
                    +----------------------------+-----------------+------+-----+---------+----------------+
                                  '''
                   csvfilevalues='''
                #fields=ID[type='string'],station_name[type='string'],latitude,longitude,ellip_height[unit='m'],
monument_description[type='string'],      IERSDOMES[type='string'],

[ for equip session:]
session_start_time[type='date'            format='yyyy-MM-ddTHH:mm:ss zzzzz'],session_stop_time[type='date' format='yyyy-MM-ddTHH:mm:ss zzzzz'],
antenna_type[type='string'],dome_type[type='string'],antenna_SN[type='string'],Ant_dZ,Ant_dN,Ant_dE,             receiver_type[type='string'],firmware_version[type='string'],receiver_SN[type='string'],receiver_sample_interval,

  [for station :]
city_locale[type='string'],state_prov[type='string'],country[type='string'],X,Y,Z,                    agencyname[type='string'],

[ for equip session:]
metpackname[type='string'],metpack_serial_number[type='string']
                   '''

                   # to insert in db:
                   # char strings inserted are '%s'
                   # numbers represented with char strings are inserted with %s

                   # look problem, if the char string X is "" how do you enter it as null for the NUMERICAL X insert value in the db ?
                   # But if say X= "123.45" then  insert X with format %s works fine.

                   if ""==X:
                       SQLstatement=("INSERT INTO station (four_char_name,station_name,latitude_north,longitude_east,height_ellipsoid, installed_date, agency_id,access_id, style_id, status_id,monument_style_id, nation_id,province_state_id, locale_id, iers_domes, station_photo_URL, time_series_plot_image_URL,networks)   values  (    '%s', '%s',  %s,             %s,            %s,       '%s',              %s,       %s,       %s,      %s,       %s,            %s,        %s,                 %s,        '%s',       '%s',               '%s' , '%s' )"  %                                  ( four_char_ID, staname,              latstr,         lonstr,         ellphgtstr,   equipSessStartTime,agency_id,access_id,style_id,status_id,monument_style_id, nation_id,province_state_id, locale_id, iers_domes, station_photo_URL, time_series_plot_image_URL, networks) )
                   else:
                       SQLstatement=("INSERT INTO station (four_char_name,station_name,latitude_north,longitude_east,height_ellipsoid,X,Y,Z, installed_date, agency_id,access_id, style_id, status_id,monument_style_id, nation_id,province_state_id, locale_id, iers_domes, station_photo_URL, time_series_plot_image_URL,networks)   values  (    '%s', '%s',  %s,             %s,            %s,       %s,%s,%s, '%s',              %s,       %s,       %s,      %s,       %s,            %s,        %s,                 %s,        '%s',       '%s',               '%s' , '%s' )"  %                                  ( four_char_ID, staname,              latstr,         lonstr,         ellphgtstr,   X,Y,Z, equipSessStartTime,agency_id,access_id,style_id,status_id,monument_style_id, nation_id,province_state_id, locale_id, iers_domes, station_photo_URL, time_series_plot_image_URL, networks) )

                   logWrite("       Insert the new station into station table with SQL \n       "+SQLstatement   )

                   try:
                       # Add this new station to the database 'station' table.
                       #     rows in foreign keys' tables must be populated already in the database.
                       cursor.execute(SQLstatement)
                       gsacdb.commit()
                       newstacount += 1 
                       logWrite( " ***** *****  Inserted new STATION: "+four_char_name+",  "+station_name   )
                       sys.stdout.flush()

                   except:
                       # bogus fails logWrite(" PROBLEM FAILED  MySQL insert command to add new station four_char_ID=" +four_char_ID+" name="+staname+" "  );
                       #logWrite(               "      BUT sometimes actually succeeded: look in the database."  );
                       #logWrite(               "     (bogus insert failure message - why?)"  );
                       pass # skip always gets false failure of above insert; cf "INSERT into" below which does not

                   station_counter += 1;
                   if four_char_ID not in sta_ID_list:
                          sta_ID_list.append(four_char_ID)

                   stm=" "
                   try:
                       stm = ("SELECT station_id from station where four_char_name = '%s'  and station_name= '%s' " % (four_char_ID, staname    ) )
                       #cursor.execute("""SELECT station_id from station where four_char_name = '%s'  and station_name= '%s' """, (four_char_ID, staname ) )
                       # logWrite("   2nd try to find new station id with SQL   \n      "+stm  );
                       cursor.execute(stm)
                       gsacdb.commit()
                       row= cursor.fetchone()
                       station_id=  row[0];
                       station_id=  int(station_id); # fix the "L" value returned
                       logWrite( "      New station "+four_char_ID+" in database has station_id "+`station_id`  )
                   except:
                       #logWrite( " PROBLEM maybe FAILED to get the id for station four_char_ID="+four_char_ID + "\n       with SQL = "+stm + "\n      for case of metadata \n    "+metadata)
                       #logWrite( "      BUT sometimes actually succeeds: look in the database."  );
                       pass

             # end first big try; see if this station is already in the database station table. and add it when needed.
                 
  
             # Add any NEW equipment session rows for this station; ones not already recorded.

             equipinfo='''
                mysql> desc equip_config;
                +-------------------------+-----------------+------+-----+---------+----------------+
                | Field                   | Type            | Null | Key | Default | Extra          |
                +-------------------------+-----------------+------+-----+---------+----------------+
                | equip_config_id         | int(6) unsigned | NO   | PRI | NULL    | auto_increment |
                | station_id              | int(6) unsigned | NO   | MUL | NULL    |                |
                | equip_config_start_time | datetime        | NO   |     | NULL    |                |
                | equip_config_stop_time  | datetime        | YES  |     | NULL    |                |
                | data_latency_hours      | double          | YES  |     | NULL    |                |
                | data_latency_days       | int(11)         | YES  |     | NULL    |                |
                | data_completeness       | float           | YES  |     | NULL    |                |
                | db_update_time          | datetime        | YES  |     | NULL    |                |
                | antenna_id              | int(3) unsigned | NO   | MUL | NULL    |                |
                | antenna_serial_number   | varchar(20)     | YES  |     | NULL    |                |
                | antenna_height          | float           | YES  |     | NULL    |                |
                | metpack_id              | int(3) unsigned | YES  | MUL | NULL    |                |
                | metpack_serial_number   | varchar(20)     | YES  |     | NULL    |                |
                | radome_id               | int(3) unsigned | NO   | MUL | NULL    |                |
                | radome_serial_number    | varchar(20)     | YES  |     | NULL    |                |
                | receiver_firmware_id    | int(3) unsigned | NO   | MUL | NULL    |                |
                | receiver_serial_number  | varchar(20)     | NO   |     | NULL    |                |
                | satellite_system        | varchar(20)     | YES  |     | NULL    |                |
                | sample_interval         | float           | YES  |     | NULL    |                |
                +-------------------------+-----------------+------+-----+---------+----------------+

              the csv data file has:
                #fields=ID[type='string'],station_name[type='string'],latitude,longitude,ellip_height[unit='m'],monument_description[type='string'],IERSDOMES[type='string'],session_start_time[type='date'            format='yyyy-MM-ddTHH:mm:ss zzzzz'],session_stop_time[type='date' format='yyyy-MM-ddTHH:mm:ss zzzzz'],antenna_type[type='string'],dome_type[type='string'],antenna_SN[type='string'],Ant_dZ,Ant_dN,Ant_dE,             receiver_type[type='string'],firmware_version[type='string'],receiver_SN[type='string'],receiver_sample_interval,city_locale[type='string'],state_prov[type='string'],country[type='string'],X,Y,Z,                    agencyname[type='string'],metpackname[type='string'],metpack_serial_number[type='string'],networks[type='string']

                      so can add to an equip session:

                      | station_id              | int(6) unsigned | NO   | MUL | NULL    |                |
                | equip_config_start_time | datetime        | NO   |     | NULL    |                |
                | equip_config_stop_time  | datetime        | YES  |     | NULL    |                |
                      | db_update_time          | datetime        | YES  |     | NULL    |                |
                | antenna_id              | int(3) unsigned | NO   | MUL | NULL    |                |
                | antenna_serial_number   | varchar(20)     | YES  |     | NULL    |                |
                | antenna_height          | float           | YES  |     | NULL    |                |
                | metpack_id              | int(3) unsigned | YES  | MUL | NULL    |                |
                | metpack_serial_number   | varchar(20)     | YES  |     | NULL    |                |
                | radome_id               | int(3) unsigned | NO   | MUL | NULL    |                |

                      | receiver_firmware_id    | int(3) unsigned | NO   | MUL | NULL    |                |
                | receiver_serial_number  | varchar(20)     | NO   |     | NULL    |                |
                | sample_interval         | float           | YES  |     | NULL    |                |

             end equipinfo '''

             # all STRINGS :
             new_equip_config_start_time =equipSessStartTime
             new_equip_config_stop_time  =equipSessStopTime
             
             uptimestamp   =strftime("%Y-%m-%dT%H:%M:%S", gmtime())
             db_update_time   = uptimestamp # this is when added to db; nothing about the session really

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
                   # ONLY check for this station and this start time;  end time  is variable for the current active session.
                   SQLstatement=("SELECT equip_config_id,equip_config_start_time,equip_config_stop_time from equip_config where station_id= %s and equip_config_start_time= '%s'"
                                    % (station_id, new_equip_config_start_time))
                   #logWrite("      Look for this equip config record in the dq with sql  ... \n      "+ SQLstatement)
                   cursor.execute(SQLstatement)
                   #logWrite("      1") 
                   row     = cursor.fetchone()
                   #logWrite("      2") 
                   the_id=  row[0];
                   the_id=  int(the_id) # fix the "L" value returned
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
                   logWrite("      Already have this equipment session.") 
                   #logWrite("                    count of eq sessions "+`eqscount`) 
             except:
                   #logWrite("       there is no such equip config session, so add a new equip session.") 
                   #logWrite("     PROBLEM   FAILED to find this equip config session.") 
                   #logWrite("       but got The esid= "+`esid`) 
                   pass

             doAddSession=True
             #doUpdateStopTime=False

             # UPDATE existing equipment session  with new data end time
             if  haveit: 
                 # debug logWrite("      This equipment session # "+`esid`+" is already in the db. ") 
                 if newequipSessStartTimeDT == dbeqstart  and newequipSessStopTimeDT == dbeqstop :
                     # debug logWrite("      No new data for this station's equipment session  (the former and new session start and stop times match). ") 
                     #logWrite("      Done with this equip session data set line. Go try next input metadata line from the  GSAC .csv file.\n  ") 
                     doAddSession=False
                 elif newequipSessStopTimeDT > dbeqstop :
                     doAddSession=False
                     logWrite("      Need to update the equip_config_stop_time in that record, to the later new stop time from new input data.") 
                     logWrite("      (new equip_config_stop_time is " + str(newequipSessStopTimeDT) +"; > db stop time "+ str(dbeqstop) +") "  ) 
                     #dev='''
                     #doUpdateStopTime= True
                     statement=("UPDATE equip_config set equip_config_stop_time = '%s'  where equip_config_id= %s " % (new_equip_config_stop_time, esid))
                     #logWrite("      the SQL is "+statement)
                     cursor.execute(statement)
                     gsacdb.commit()
                     logWrite("      Updated the equip_config_stop_time in the db, for station "+four_char_ID+" and at equip_config_id "+`esid` +", "  )
                     stoptimeupdatecount += 1
                     # end of processing one metadata line from the GSAC .csv file
                     #'''


             sys.stdout.flush()

             #devel='''

             # OR, add New equipment session:  (don't have this equipment session in the db yet so add it)
             if ( haveit == False ) :
                 # debug logWrite("      adding a new equip session")
                 antenna_serial_number   =antsn # char string not a number
                 antenna_height          =adz # char string not a float
                 radome_serial_number   =" " 
                 receiver_serial_number =rcvsn
                 #satellite_system       ="GPS" # default; or could be for example "GPS,GLONASS"   CHANGE
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
                        logWrite("       no antenna name in input "  );
                 else:
                        antenna_id  = getOrSetTableRow ("antenna_id", "antenna", "antenna_name", antenna_name)
                        # debug logWrite("        antenna name and id " +antenna_name+"   "+`antenna_id` );

                 if ""== radome_name:
                        # default value of "not specified" at id=1 in db is used
                        radome_id  =  1 # not specified
                        # if logflag>=2: # logWrite("       no radome name in  full csv file results"  );
                 else:
                        radome_id  = getOrSetTableRow ("radome_id", "radome", "radome_name", radome_name)
                        #debug logWrite("        radome name and id " +radome_name+"   "+`radome_id` );

                 if ""== receiver_name:
                        # default value of "not specified" at id=1 in db is used
                        receiver_id=1 # not specified
                        # debuglogWrite("       no receiver name in full csv file results"  );
                 else:
                        # note the receiver_firmware table encapsulates two values into a unique combination,
                        # so to get the id number must do this
                        # elaboration of def getOrSetTableRow (idname, tablename,  rowname, rowvalue) 
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
                               # debug logWrite("        got receiver firmware id="+`receiver_id` +"  for type= "+ rcvtype+", firmware= "+rcvfwvers  );
                        except:
                               # if logflag>=2: # logWrite("       no row is yet in the receiver_firmware database for "+ rcvtype+", "+rcvfwvers  );
                               # add this new value to that table; and get its id  rrr
                               # one value SQLstatement=("INSERT into %s (%s) value ('%s')"  %  ( tablename,rowname,rowvalue ) )
                               SQLstatement=("INSERT into receiver_firmware (receiver_name,receiver_firmware) values ('%s','%s')"  %  (rcvtype,rcvfwvers ) )
                               # if logflag>=2: # logWrite("         insert receiver tbl  SQL statement ="+SQLstatement  );
                               try:
                                   cursor.execute(SQLstatement)
                                   gsacdb.commit()
                                   logWrite( "      Inserted new receiver values type= "+rcvtype+", firmware= "+rcvfwvers  );
                               except:
                                   gsacdb.rollback()
                                   # if logflag>=2: # logWrite( "           Failed to insert new values "+rcvtype+", "+rcvfwvers  );
                                   logWrite("       PROBLEM Failed to insert new values rcvtype "+rcvtype+", rcv vers "+rcvfwvers  + " for case of metadata = \n    "+metadata )
                               #  get the new id value
                               SQLstatement=("SELECT receiver_firmware_id from receiver_firmware where receiver_name='%s' and receiver_firmware='%s' " % (rcvtype,rcvfwvers) )
                               # if logflag>=2: # logWrite("            SQL statement ="+SQLstatement  );
                               cursor.execute(SQLstatement)
                               row= cursor.fetchone()
                               the_id=  row[0];
                               the_id=  int(the_id); # fix the "L" value returned
                               # if logflag>=2: # logWrite("                got receiver firmware id="+`the_id` +"  for "+ rcvtype+", "+rcvfwvers  );
                               receiver_id  = the_id

                 #   Insert this new equipment session into the db 
                 what=''' can add to an equip session:

                      | station_id              | int(6) unsigned | NO   | MUL | NULL    |                |
                | equip_config_start_time | datetime        | NO   |     | NULL    |                |
                | equip_config_stop_time  | datetime        | YES  |     | NULL    |                |
                      | db_update_time          | datetime        | YES  |     | NULL    |                |   new
                | antenna_id              | int(3) unsigned | NO   | MUL | NULL    |                |
                | antenna_serial_number   | varchar(20)     | YES  |     | NULL    |                |
                | antenna_height          | float           | YES  |     | NULL    |                |
                | metpack_id              | int(3) unsigned | YES  | MUL | NULL    |                |`   new int
                | metpack_serial_number   | varchar(20)     | YES  |     | NULL    |                |    new char str
                | radome_id               | int(3) unsigned | NO   | MUL | NULL    |                |

                      | receiver_firmware_id    | int(3) unsigned | NO   | MUL | NULL    |                |
                | receiver_serial_number  | varchar(20)     | NO   |     | NULL    |                |
                | sample_interval         | float           | YES  |     | NULL    |                |

                 # '''

                 try:
                       # orig  SQLstatement=("INSERT into equip_config (station_id, create_time, equip_config_start_time, equip_config_stop_time, antenna_id, antenna_serial_number, antenna_height, radome_id, radome_serial_number, receiver_firmware_id, receiver_serial_number, satellite_system,sample_interval) values (%s, '%s', '%s', '%s',  %s, '%s', %s,  %s, '%s', %s, '%s',  '%s', %s)" % ( station_id, create_time, new_equip_config_start_time, new_equip_config_stop_time, antenna_id, antenna_serial_number, antenna_height, radome_id, radome_serial_number, receiver_id, receiver_serial_number, satellite_system, rcvsampInt)) 
                       SQLstatement=("INSERT into equip_config (station_id, equip_config_start_time,           equip_config_stop_time, antenna_id, antenna_serial_number, antenna_height, radome_id,  receiver_firmware_id, receiver_serial_number, sample_interval, metpack_id, metpack_serial_number, db_update_time) values (%s,        '%s',                              '%s',                   %s,                 '%s',             %s,             %s,            %s,                 '%s',                          %s,                   %s,        '%s',                 '%s')" % ( station_id,  new_equip_config_start_time, new_equip_config_stop_time, antenna_id, antenna_serial_number, antenna_height, radome_id,  receiver_id,          receiver_serial_number, rcvsampInt,     metpack_id, metpack_serial_number, db_update_time)) 
                       #logWrite("      Insert this new equipment session into the db with SQL: \n      "+SQLstatement  )
                       logWrite("      "+SQLstatement  )
                       cursor.execute(SQLstatement)
                       gsacdb.commit()
                       newsessioncount += 1
                       #eqscount+=1
                       logWrite("      Inserted a new equipment session into the db." )

                 except:
                       logWrite("      PROBLEM FAILED to insert this equipment session into the db.")
                       #       BUT sometimes actually succeeds: look in the database."  );
                       failedcount +=1
                       pass

             # end if NOT haveit, so add it
             #'''  # end devel

           previous_station_four_char_ID = this_station_four_char_ID

           # end of adding station info and ALL equipment session info
           if this_station_four_char_ID != four_char_ID :
                 if "" != this_station_four_char_ID : # not the first time
                     previous_station_four_char_ID = this_station_four_char_ID
                     #if logflag>=1:  logWrite(  "  Station "+ previous_station_four_char_ID +" data in the database is up-to-date (station count "+`donecount`+")."

        # close the GSAC Full CSV input file 
        #station_metadata_file.close()

        logWrite(  "*******   Station "+ previous_station_four_char_ID +" is up-to-date in the database (station count "+`donecount`+") "  );

        logWrite("\n   Finished reading input station metadata .csv file ."  ); # station_metadata_file

        logWrite("\n   TOTAL station count handled was "+`donecount`  );

 #  END OF function load_db() 



def  logWrite (text) :
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



def makedatetime (dtstr) :
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
     #if (len(dtstr)<10)
     #   logWrite(" PROBLEM: date - time string input to makedatetime () is  too short: "+dtstr
     #   return dt
     if (""==dtstr):
         return None
     elif ('                   '==dtstr):
         return None
     if (len(dtstr) == 10):
         dt=datetime.datetime.strptime( dtstr, "%Y-%m-%d" )
     elif (len(dtstr) == 16) :
         dt=datetime.datetime.strptime( dtstr, "%Y-%m-%dT%H:%M" )
     elif (len(dtstr) == 19) :
         dt=datetime.datetime.strptime( dtstr, "%Y-%m-%dT%H:%M:%S" )
     return dt



# main program: 

global logFile 
global logfilename 
global inputfilename
global logflag 
global newstacount
global newsessioncount
global failedcount
global stationgroup
global eqscount
global donecount

logflag =2 # CHANGE USE =1 for routine operations.  use 2 for testing  and details

logfilename="populate_GSAC_db_station_metadata.log"

# open log file describing processing results 
timestamp   =strftime("%Y-%m-%d_%H:%M:%S", gmtime())
# or with '%a'  for day name   strftime("%a, %d %b %Y %H:%M:%S +0000", gmtime()) 

# get command line argument values
inputfile=""
dbhost="" 
dbacct="" 
dbacctpw="" 
dbname=""
stationgroup=""

# get input args from command line
args = sys.argv
#stationgroup = args[1]

# debug print "\n arg 1 ="+stationgroup+"= \n"

dbhost   = args[1]
dbacct   = args[2]
dbacctpw = args[3]
dbname   = args[4]
inputfilename = args[5]

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

logWrite("\n  -----------------  SUMMARY of populate_GSAC_db_station_metadata.py processing  ----------------- ")

logWrite("\n ***** *****  Inserted "+`newstacount`    +" new stations.  "  )

logWrite("\n ***** *****  Inserted "+`newsessioncount`+" new equipment sessions. "  )

#logWrite(    "\n ***** ***** Handled "+`donecount`+" stations in the database."  )
#logWrite(    "\n ***** ***** To update latest data end times in station - equipment sessions in the database:"  )
#logWrite(      " ***** *****   There are "+`eqscount`+" station - equipment sessions matches with existing equipment sessions in the database."  )
logWrite(      " ***** *****  Updated "+`stoptimeupdatecount`+" equip config table (equipment sessions) end times."  )

notused='''
if failedcount>0  :
    logWrite("\n ***** *****  Some attempted db actions FAILED.  Look in this log file for 'FAIL' and 'PROBLEM'.  "  )
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
