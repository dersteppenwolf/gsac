#!/usr/bin/python
'''
 filename              : insertNewStations.py
 author(s)             : Stuart Wier
 created               : 2014-10-13
 latest update(version): 2014-12-10

 exit code(s)          : 0, success

                       : Loads station and instrument metadata into a Dataworks for GNSS database, using as input a "CSV" file made by the  user.
                       : This will add a new station, and / or it will add a new equipment session to an existing station.  This will add both, if both are new.
                       : Station metadata is in the 'station' table in the Dataworks for GNSS database and in related look-up tables.
                       : Instrument metadata is in the 'equip_config' table in the Dataworks for GNSS database and in related look-up tables.

 usage                 : 1. make a csv file about a station and one equipment session, named station_data.csv. There is one line in the file for each station + one equipment session. 
                         The format is 

                         4_char_ID[type=string],station_name[type=string],latitude[float],longitude[float],ellip_height[float,unit='m'],monument_description[type=string],IERSDOMES[type=string],session_start_time[type='date' format='yyyy-MM-ddTHH:mm:ss zzzzz'],session_stop_time[type='date' format='yyyy-MM-ddTHH:mm:ss zzzzz'],antenna_type[type=string],dome_type[type=string],antenna_SN[type=string],Ant_dZ[float],Ant_dN[float],Ant_dE[float],receiver_type[type=string],firmware_version[type=string],receiver_SN[type=string],receiver_sample_interval[float,unit='s'],locale_name[type=string],country[type=string],agency_name[type=string],network_name[type=string],metpack_name[type=string],metpackSN[type=string]

                         sample line:

                         ts01,test station 01,46.4623,-106.5275,-25.67,building roof,97103M001,2009-12-25T00:00:00,2010-08-26T23:59:30,TRM55971.00,NONE,1440911917,0.0083,0.0000,0.0000,TRIMBLE NETR5,4.03,4917K61764,30.00,Dry Gulch,U.S.,Institute of Geodesy,TLALOCNet,no metpack,,

                         Commas separate values.  NO commas are allowed in names or in any other values.  
                         NO quotation marks in values, or the quotation marks will become part of values in the database.
                         No spaces unless they are part of a name. 
                         Make sure of the order latitude,longitude. West longitudes are negative.  The ONLY permitted unspecified values are metpack_name and metpackSN. 
                         If there is no metpack, use the exact phrase "no metpack" in the .csv file, without quotation marks.

                         Run this Python script with a command like this example. 

                        ./insertNewStations.py station_data.csv localhostname dbacct dbacctpw database_name

                        : Use your names for the database account name, its account password, and database name, such as 

                        ./insertNewStations.py station_data.csv localhost     dbacct thedbpw  Dataworks

 report any bugs to    : dataworks@unavco.org
                   
 tested on             : Python 2.6 on Centos.
'''

import os
import sys
import math
import string
import datetime
from   datetime import timedelta
from   time     import gmtime, strftime

import MySQLdb
# To install Python's MySQLdb on Ubuntu or on Debian, just do command   sudo apt-get install python-mysqldb
# and see this http://stackoverflow.com/questions/372885/how-do-i-connect-to-a-mysql-database-in-python
        
def load_db ():
        global logFile 
	global newstacount
	global newsessioncount
        newstacount=0
        newsessioncount=0

        file1 = open (inputfile);
        print "    Opened csv file of station and equipment session data: "+inputfile

        # read and count how many lines in file
        allLines = file1.readlines()
        linecount = len(allLines)
        file1.seek(0) # rewind to beginning
        # full csv files have one or more lines for every station, one line per equipment session.  Use the station name to keep track which one is in use.

        code="" # the 4 char id of a station such as ABMF
        this_station_code="" # working station now
        previous_station_code=""  # the previous station in use
        donecount=1
        station_id = None 
        station_counter=0;

        # station 4-char id list to help manage the same
        sta_ID_list=[]

        # read each line in the input CSV file; i is 0 base
        for i in range(linecount) :

           line = file1.readline()
           metadata = line

           if (i>=0):   #if (i<11):   # < is for testing, skip all but a few

             # one file line is one equipment session for one station. 

             # split out values from line, at commas
             strlist= string.split( line, "," )

             SQLstatement=""

             code= (strlist[0]) # the 4 char id of a station such as ABMF
             if (len(code)>4) : # should not occur; attempt to do something useful.
                 code=code[:4]

             # print "\n    input file file line "+`i+1` + " for station "+ code+", "+staname
             if logflag>=2:   print "    station metadata in csv is: "+ line[:-1]

             # when at a new station (this simply for screen messages):
             if this_station_code != code :
                 if "" != this_station_code : # not the first time
                     previous_station_code = this_station_code
                     if logflag>=1: print   "    Station "+ previous_station_code +" is up-to-date in the database (station count "+`donecount`+")."
                     donecount += 1
                 this_station_code = code

             staname = (strlist[1]) # station name

             # these values are strings, not numbers. Which are what you want to load the db, to avoid Python rounding errors
             latstr =     (strlist[2])
             lonstr =     (strlist[3])
             ellphgtstr = (strlist[4])
             # native strings in all cases:
             monument_style_description   = (strlist[5])
             domesiers =  (strlist[6])

             stastart  =  (strlist[7])
             # cut value if full ISO 8601 value with time zone offset value, like start time=_2006-08-23T00:00:00 +0000, down to hh:mm:ss at end
             if (len(stastart)>19) :
                 stastart=stastart[:19]
             stastop   =  (strlist[8])
             if (len(stastop)>19) :
                 stastop=stastop[:19]

             anttype  =  (strlist[9])
             radometype  =  (strlist[10])
             antsn  =  (strlist[11])
             adz  =  (strlist[12])
             adn  =  (strlist[13])
             ade  =  (strlist[14])
             rcvtype  =  (strlist[15])
             rcvfwvers  =  (strlist[16])
             rcvsn  =  (strlist[17])
             rcvsampInt  =  (strlist[18])

             locale_info   = (strlist[19])
             country_name  = (strlist[20])
             # []  21,22,23 are X,Y,Z  - NOT used in the dataworks database so far
             agency_name =  (strlist[21])
             network_name=  (strlist[22]) 
             metpack_name=  (strlist[23])
             metpacksn =    (strlist[24])
             

             # state or province name is NOT stored in the "Dataworks" database schmea, as per UNAVCO decision of ---.
             #print "       metpack name="+metpack_name+"_ SN="+metpacksn
             # print "    have station and equipment session info. for station "+code +", full name "+staname+", at longi "+lonstr+", lati="+latstr
             #print "    Station  4charID="+code +"_ name="+staname+"_ long="+lonstr+"_ lat="+latstr+"_ ellip hgt="+ellphgtstr+"_ start time=_"+stastart+
             # "_ stop time=_"+stastop+"_ monument=_"+monument_style_description+"_ IERSdomes=_"+domesiers+"_ anttype=_"+anttype
             # +"_ radometype="+radometype+"_ antsn="+antsn+"_ site count="+sitecount[:-1]

             # Load the station info in the database, IF needed. 

             #   -------------- LOOK if the station is in the station table already.  The COMBINATION OF 4 char ID code AND the full name are only unique.  ------------------
             try:
                  SQLstatement=('SELECT station_id from station where four_char_name="%s" and station_name="%s" ' % ( code, staname) )
                  logFileWrite("    Look for station with sql \n      "+SQLstatement )
                  cursor.execute(SQLstatement)

                  # OK orig cursor.execute("""SELECT station_id from station where four_char_name= %s and station_name= %s """, (code,staname ) )
                  # rows= cursor.fetchall() # gives an array of  rows found
                  # or for one row
                  row= cursor.fetchone()
                  station_id=  row[0];
                  station_id=  int(station_id); # fix the "L" value returned
                  #  Already have row "+`station_id`+" in the db station table for "+code+", so will not add new station data to the table.
                  logFileWrite(  "    Already have station "+code +" in the GSAC database.")
                  if code not in sta_ID_list:
                      sta_ID_list.append(code)
             except:
                   # the station is not in the database, so add it:
                   #logFileWrite( "     To insert a station "+code+"  name="+staname+" in the db station table:")
                   # no pass! do next: 

                   # db station table schema:
                   station='''
                    CREATE TABLE `station` (
                      `station_id` int(6) unsigned NOT NULL AUTO_INCREMENT,
                      `four_char_name` char(4) NOT NULL,
                      `station_name` varchar(50) NOT NULL,
                      `latitude_north` double NOT NULL,
                      `longitude_east` double NOT NULL,
                      `height_above_ellipsoid` float NOT NULL,
                      `installed_date` datetime NOT NULL,
                      `retired_date` datetime DEFAULT NULL,
                      `style_id` int(3) unsigned NOT NULL,
                      `status_id` int(3) unsigned NOT NULL,
                      `access_id` int(3) unsigned NOT NULL,
                      `monument_style_id` int(3) unsigned NOT NULL,
                      `country_id` int(3) unsigned NOT NULL,
                      `locale_id` int(3) unsigned NOT NULL,
                      `ellipsoid_id` int(1) unsigned NOT NULL,
                      `iers_domes` char(9) DEFAULT NULL,
                      `operator_agency_id` int(3) unsigned NOT NULL,
                      `data_publisher_agency_id` int(3) unsigned NOT NULL,
                      `network_id` int(5) unsigned NOT NULL,
                      `station_image_URL` varchar(100) DEFAULT NULL,
                      `time_series_URL` varchar(100) DEFAULT NULL,
                      PRIMARY KEY (`station_id`),
                      KEY                 `style_id_idx` (`style_id`),
                      KEY                `status_id_idx` (`status_id`),
                      KEY                `access_id_idx` (`access_id`),
                      KEY        `monument_style_id_idx` (`monument_style_id`),
                      KEY               `country_id_idx` (`country_id`),
                      KEY                `locale_id_idx` (`locale_id`),
                      KEY             `ellipsoid_id_idx` (`ellipsoid_id`),
                      KEY       `operator_agency_id_idx` (`operator_agency_id`),
                      KEY `data_publisher_agency_id_idx` (`data_publisher_agency_id`),
                      KEY               `network_id_idx` (`network_id`),
                      CONSTRAINT `access_id` FOREIGN KEY (`access_id`) REFERENCES `access` (`access_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
                      CONSTRAINT `country_id` FOREIGN KEY (`country_id`) REFERENCES `country` (`country_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
                      CONSTRAINT `data_publisher_agency_id` FOREIGN KEY (`data_publisher_agency_id`) REFERENCES `agency` (`agency_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
                      CONSTRAINT `ellipsoid_id` FOREIGN KEY (`ellipsoid_id`) REFERENCES `ellipsoid` (`ellipsoid_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
                      CONSTRAINT `locale_id` FOREIGN KEY (`locale_id`) REFERENCES `locale` (`locale_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
                      CONSTRAINT `monument_style_id` FOREIGN KEY (`monument_style_id`) REFERENCES `monument_style` (`monument_style_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
                      CONSTRAINT `network_id` FOREIGN KEY (`network_id`) REFERENCES `network` (`network_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
                      CONSTRAINT `operator_agency_id` FOREIGN KEY (`operator_agency_id`) REFERENCES `agency` (`agency_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
                      CONSTRAINT `status_id` FOREIGN KEY (`status_id`) REFERENCES `station_status` (`station_status_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
                      CONSTRAINT `style_id` FOREIGN KEY (`style_id`) REFERENCES `station_style` (`station_style_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
                      ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
                   '''

                   #  use these initial values for COCONet stations, which values are NOT available to this program, since there is no such info in a gsac full csv input file:
                   style_id       = 1    # GPS/GNSS Continuous
                   status_id      = 1    # Active 
                   access_id      = 2    # full public access
                   ellipsoid_id   = 1    # WGS 84
                   network_id     = 1    # COCONet
                   agency_id      = 1    # unspecified

                   # get  or set the id for the foreign keys, to be reset in in this program based in onfo in the gsac full csv input file:
                   monument_style_id = 1 # id value is "not specified"
                   country_id=1          # not specified
                   locale_id=1           # "not specified"
                   the_id=1

                    # get or set monument_style_id based on monument_style_description value:
                   if ""==monument_style_description :
                        # default value of "not specified" at id=1 in db is used
                        logFileWrite("       no monument style info in input csv file")
                   else:
                        # id= getOrSetTableRow (idname, tablename,  rowname, rowvalue)
                        monument_style_id  = getOrSetTableRow ("monument_style_id", "monument_style", "monument_style_description", monument_style_description) 


                   if ""==agency_name:
                        logFileWrite("       no agency name in input csv file")
                   else:
                        agency_id  = getOrSetTableRow ("agency_id", "agency", "agency_name", agency_name) 

                   if ""==network_name:
                        logFileWrite("       no network name in input csv file")
                   else:
                        network_id  = getOrSetTableRow ("network_id", "network", "network_name", network_name) 


                   if ""==locale_info:
                        logFileWrite("       no locale name in input csv file")
                   else:
                        # id= getOrSetTableRow (idname, tablename,  rowname, rowvalue)
                        locale_id  = getOrSetTableRow ("locale_id", "locale", "locale_info", locale_info) 


                   if ""== country_name:
                        logFileWrite("       no country name in input csv file")
                   else:
                        # id= getOrSetTableRow (idname, tablename,  rowname, rowvalue)
                        country_id  = getOrSetTableRow ("country_id", "country", "country_name", country_name) 

		   station_photo_URL=     "http://www.unavco.org/data/gps-gnss/lib/images/station_images/"+code+".jpg"
		   time_series_plot_photo_URL="http://pboshared.unavco.org/timeseries/"+code+"_timeseries_cleaned.png"

                   try:
                       # add this 'new' station to the Dataworks database 'station' table.
                       # rows in  foreign keys' tables must be populated already  in the database.

                       SQLstatement=("INSERT into station (four_char_name,station_name,latitude_north,longitude_east,height_above_ellipsoid,installed_date, style_id, status_id, access_id,  ellipsoid_id ,  iers_domes ,station_image_URL,time_series_URL,  network_id,country_id,locale_id,operator_agency_id,data_publisher_agency_id,monument_style_id) values  ('%s', '%s', %s,  %s,  %s, '%s', %s, %s,  %s,  %s,  '%s','%s','%s', %s,%s,%s,%s,%s,  %s)" % ( code, staname, latstr, lonstr, ellphgtstr, stastart,  style_id, status_id, access_id, ellipsoid_id, domesiers, station_photo_URL,time_series_plot_photo_URL, network_id, country_id,locale_id, agency_id,agency_id, monument_style_id))
                       logFileWrite("      Insert new station into station table with SQL \n      "+SQLstatement )
                       cursor.execute(SQLstatement)
                       gsacdb.commit()
                       logFileWrite(  "            Inserted new Station: "+four_char_name+", name:  _"+station_name +"_ \n");
	               newstacount += 1

                   except:
                       # FIX may fail  even when inserts works
                       #logFileWrite( "   'FAILED'  to insert new station "+code+"_ name="+staname+"_")
                       #logFileWrite( "   BUT may actually have succeeded: look in the database.")
                       pass # skip always gets false failure of above insert; cf "INSERT into" below which does not

                   station_counter += 1;
                   if code not in sta_ID_list:
                          sta_ID_list.append(code)

                   try:
                       stm = ("SELECT station_id from station where four_char_name = '%s'  and station_name= '%s' " % (code, staname    ) )
                       logFileWrite("       find new station id with SQL:    \n       "+stm )
                       cursor.execute(stm)
                       #cursor.execute("""SELECT station_id from station where four_char_name = '%s'  and station_name= '%s' """, (code, staname ) )
                       row= cursor.fetchone()
                       station_id=  row[0];
                       station_id=  int(station_id); # fix the "L" value returned
                       logFileWrite( "       Have station in the database: station "+code+" is at table station:station_id "+`station_id`)
                   except:
                       logFileWrite( "  PROBLEM reported FAILED to get the station:station_id value for new station "+code )
                       logFileWrite( "   BUT may actually have succeeded: look in the database.")

             # end first big try; see if this station is already in the database station table. and add it when needed.
  

             # ---------------------------------       Add NEW equipment session row for this station; if not already in database.  -------------------------------------------------

             equiptableinfo='''
                mysql> desc equip_config;
		+-------------------------+-----------------+------+-----+---------+----------------+
		| Field                   | Type            | Null | Key | Default | Extra          |
		+-------------------------+-----------------+------+-----+---------+----------------+
		| equip_config_id         | int(6) unsigned | NO   | PRI | NULL    | auto_increment |
		| station_id              | int(6) unsigned | NO   | MUL | NULL    |                |
		| create_time             | datetime        | NO   |     | NULL    |                |
		| equip_config_start_time | datetime        | NO   |     | NULL    |                |
		| equip_config_stop_time  | datetime        | YES  |     | NULL    |                |
		| antenna_id              | int(3) unsigned | NO   | MUL | NULL    |                |
		| antenna_serial_number   | varchar(20)     | NO   |     | NULL    |                |
		| antenna_height          | float           | NO   |     | NULL    |                |
		| metpack_id              | int(3) unsigned | YES  | MUL | NULL    |                |
		| metpack_serial_number   | varchar(20)     | YES  |     | NULL    |                |
		| radome_id               | int(3) unsigned | NO   | MUL | NULL    |                |
		| radome_serial_number    | varchar(20)     | NO   |     | NULL    |                |
		| receiver_firmware_id    | int(3) unsigned | NO   | MUL | NULL    |                |
		| receiver_serial_number  | varchar(20)     | NO   |     | NULL    |                |
		| satellite_system        | varchar(20)     | YES  |     | NULL    |                |
		+-------------------------+-----------------+------+-----+---------+----------------+
             '''

             if ""== metpack_name:
                 logFileWrite("       no metpack name in input csv file")
             else:
                 # id= getOrSetTableRow (idname, tablename,  rowname, rowvalue)
                 metpack_id  = getOrSetTableRow ("metpack_id", "metpack", "metpack_name", metpack_name) 

             create_time             =stastart
             equip_config_start_time =stastart
             equip_config_stop_time  =stastop

             # for the database, as for UNAVCO archive, use null stop time for case where equipment session is not yet stopped, 
             # if you are looking at a just-made GSAC full csv file, the UNAVCO GSAC dates the stop time as the exact the end of yesterday.
             # such as 2014-09-03T23:59:30 in the csv file made anytime on the day 2014-09-04.
             #print "         equip_config_stop_time  = "+stastop
             if "T23:59:"==stastop[10:17] :
                 now    = datetime.datetime.utcnow()
                 estime = makedatetime (stastop)
                 deltat = now -estime
                 deltadays = deltat.days
                 #print    "     days ="+`deltadays`
                 if  deltadays < 1:
                     #equip_config_stop_time=None # this will appear in the mysql database as value "0000-00-00 00:00:00"
                     equip_config_stop_time = "0000-00-00 00:00:00"
                     #print ">>>> latest equip session for station "+staname

             # check if this equipment session at this station already is in the database:
             haveit=False
             try:
                   # logFileWrite("     Look for this particular equipment session at station_id="+`station_id` # +"  in the db with SQL \n      "+statement
                   # statement=("SELECT equip_config_id from equip_config  where  station_id= %s and equip_config_start_time= '%s' and  equip_config_stop_time= '%s'" % (station_id,            equip_config_start_time, equip_config_stop_time))
                   statement=("SELECT equip_config_id from equip_config  where  station_id= %s and equip_config_start_time= '%s'" % (station_id, equip_config_start_time))
                   logFileWrite("     Look for equip session with sql: \n        "+ statement )
                   cursor.execute(statement)
                   row= cursor.fetchone()
                   esid=  row[0];
                   esid=  int(esid); # fix the "L" value returned
                   haveit=True
                   logFileWrite("     Already have this equipment session in the GSAC database, for station "+ code +".")
             except:
                   if logflag>=1: print  "\n      Enter a new equipment session for station id="+`station_id`+"   "+code+", "+staname+", in the database"

             if (False==haveit) :
                 antenna_serial_number   =antsn
                 antenna_height          =adz
                 radome_serial_number   ="unspecified" 
                 #receiver_firmware_vers =rcvfwvers
                 receiver_serial_number =rcvsn
                 satellite_system       ="GPS" # default 
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
                        logFileWrite("       no antenna name in UNAVCO GSAC full csv file results")
                 else:
                        antenna_id  = getOrSetTableRow ("antenna_id", "antenna", "antenna_name", antenna_name)

                 if ""== radome_name:
                        # default value of "not specified" at id=1 in db is used
                        radome_id  =  1 # not specified
                        logFileWrite("       no radome name in UNAVCO GSAC full csv file results")
                 else:
                        radome_id  = getOrSetTableRow ("radome_id", "radome", "radome_name", radome_name)

                 if ""== receiver_name:
                        # default value of "not specified" at id=1 in db is used
                        receiver_id  =  1 # not specified
                        logFileWrite("       no receiver name in UNAVCO GSAC full csv file results")
                 else:
                        # note the receiver_firmware table encapsulates two values into a unique combination,
                        # so to get the id number must do this
                        # elaboration of def getOrSetTableRow (idname, tablename,  rowname, rowvalue) :
                        logFileWrite("           get or set receiver_firmware table row ")
                        the_id=1
                        receiver_id  =  1 # not specified
                        try:
                               #SQLstatement=("SELECT  %s from %s  where  %s = '%s' " % ( idname,tablename,rowname,rowvalue ) )
                               SQLstatement=("SELECT receiver_firmware_id from receiver_firmware where receiver_name='%s' and receiver_firmware='%s' " % (rcvtype,rcvfwvers) )
                               logFileWrite("            SQL statement ="+SQLstatement)
                               cursor.execute(SQLstatement)
                               row= cursor.fetchone()
                               the_id=  row[0];
                               the_id=  int(the_id); # fix the "L" value returned
                               receiver_id  = the_id
                               logFileWrite("                got receiver firmware id="+`receiver_id` +"  for "+ rcvtype+", "+rcvfwvers )
                        except:
                               logFileWrite("       no row is yet in the receiver_firmware database for "+ rcvtype+", "+rcvfwvers)
                               # add this new value to that table; and get its id  rrr
                               # one value SQLstatement=("INSERT into %s (%s) value ('%s')"  %  ( tablename,rowname,rowvalue ) )
                               SQLstatement=("INSERT into receiver_firmware (receiver_name,receiver_firmware) values ('%s','%s')"  %  (rcvtype,rcvfwvers ) )
                               logFileWrite("         insert receiver tbl  SQL statement ="+SQLstatement)
                               try:
                                   cursor.execute(SQLstatement)
                                   gsacdb.commit()
                                   print  "           Inserted new receiver values "+rcvtype+", "+rcvfwvers
                               except:
                                   gsacdb.rollback()
                                   logFileWrite( "           Failed to insert new values "+rcvtype+", "+rcvfwvers)
                                   #logFileWrite("\n     Failed to insert new values rcvtype "+rcvtype+", rcv vers "+rcvfwvers  + " for case of metadata = \n    "+metadata )
                               #  get the new id value
                               SQLstatement=("SELECT receiver_firmware_id from receiver_firmware where receiver_name='%s' and receiver_firmware='%s' " % (rcvtype,rcvfwvers) )
                               logFileWrite("            SQL statement ="+SQLstatement)
                               cursor.execute(SQLstatement)
                               row= cursor.fetchone()
                               the_id=  row[0];
                               the_id=  int(the_id); # fix the "L" value returned
                               logFileWrite("                got receiver firmware id="+`the_id` +"  for "+ rcvtype+", "+rcvfwvers )
                               receiver_id  = the_id

                 try:

                       SQLstatement=("INSERT into equip_config (station_id, create_time, equip_config_start_time, equip_config_stop_time, antenna_id, antenna_serial_number, antenna_height, radome_id, radome_serial_number, receiver_firmware_id, receiver_serial_number, satellite_system,metpack_id,metpack_serial_number) values (%s, '%s', '%s', '%s',  %s, '%s', %s,  %s, '%s', %s, '%s',  '%s', %s, '%s' )" % ( station_id, create_time, equip_config_start_time, equip_config_stop_time, antenna_id, antenna_serial_number, antenna_height, radome_id, radome_serial_number, receiver_id, receiver_serial_number, satellite_system, metpack_id,metpacksn))
                       logFileWrite( "       Insert an equipment session into the db with SQL: \n            "+ SQLstatement )
	               newssessioncount += 1
                       cursor.execute(SQLstatement)
                       gsacdb.commit()
                 except:
                       #logFileWrite("     FAILED to insert this equipment session into the db;") # the metadata is \n     "+metadata ) 
                       #logFileWrite("       BUT may actually have succeeded: look in the database.")
                       pass # FIX always SAYs fail when actually is OK
                 logFileWrite(  "       Inserted an equipment session" ) # LOOK never seen?

             # end if haveit

           # end of adding station info and equipment session info
           if this_station_code != code :
                 if "" != this_station_code : # not the first time
                     previous_station_code = this_station_code
                     if logflag>=1: print   "  Station "+ previous_station_code +" data in the database is up-to-date (station count "+`donecount`+")."

        # close the GSAC Full CSV input file 
        file1.close()

 #  END OF function load_db() 



def getOrSetTableRow (idname, tablename,  rowname, rowvalue) :
       " find the id for the value rowvalue in the db table tablename, or add a new row to the table and return that id."
       #logFileWrite("          called getOrSetTableRow")
       the_id=1
       try:
           SQLstatement=("SELECT  %s from %s  where  %s = '%s' " % ( idname,tablename,rowname,rowvalue ) )
           #logFileWrite("           query: "+SQLstatement
           cursor.execute(SQLstatement)
           row= cursor.fetchone()
           the_id=  row[0];
           the_id=  int(the_id); # fix the "L" value returned
           #logFileWrite("           got id="+`the_id` +"  for row value "+rowvalue+" in table "+tablename
       except:
           #logFileWrite("       no "+rowname+" is yet in the database for "+rowvalue
           # add this new value to that table; and get its id
           SQLstatement=("INSERT into %s (%s) value ('%s')"  %  ( tablename,rowname,rowvalue ) )
           #logFileWrite("            SQL statement ="+SQLstatement
           try:
               cursor.execute(SQLstatement)
               gsacdb.commit()
               #print  "          inserted new value "+rowvalue + " in table.field="+ tablename+"."+  rowname
           except:
               gsacdb.rollback()
               #logFileWrite( "           Failed to insert new value "+rowvalue )

           #  get the new id value
           SQLstatement=("SELECT %s from %s  where  %s = '%s' " % ( idname,tablename,rowname,rowvalue ) )
           #logFileWrite("            SQL statement ="+SQLstatement
           cursor.execute(SQLstatement)
           row= cursor.fetchone()
           the_id=  row[0];
           the_id=  int(the_id); # fix the "L" value returned
           #logFileWrite("                set id="+`the_id` +"  for row value "+rowvalue+" in table "+tablename
       return the_id
 #  END OF function  getOrSetTableRow () 



def makedatetime (dtstr):
     # creates a python datetime object (NOT a string) from individual parameter values. Note that the time fields are optional; if omitted the time value is 0:00:00, which is midnight.
     # uses datetime.datetime.strptime( "2007-03-04 21:08:12", "%Y-%m-%d %H:%M:%S" )
     # or   datetime.datetime( year , month , day,hour,minute,second,microsecond,tzinfo)  
     # for such as 2010-05-03 08:45:59
     # or such as 2010-05-03 08:45
     # or 2008-09-11
     # or empty string
     # or a too=short string
     dt=None
     #if (len(dtstr)<10):
     #    print " PROBLEM: date - time string input to makedatetime () is  too short: "+dtstr
     #    return dt
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



def logFileWrite (text):
     global logFile
     logFile.write(text + "\n")
      # for debugging runs to see output on screen:
     if logflag>=2 :
           print (text)


# main program: 

global logFile 
global newstacount
global newsessioncount
# get command line argument values
inputfile=""
dbhost="" 
dbacct="" 
dbacctpw="" 
dbname=""
args = sys.argv
#  input file name from 1st arg on command line; file must be in directory where this is run
inputfile= args[1]
dbhost   = args[2]
dbacct   = args[3]
dbacctpw = args[4]
dbname   = args[5]

logflag= 2  # CHANGE Note: use =1 for operational (limited) screen log lines; or use =2 for testing  and details

# open log file describing processing results, problems, which need more attention:
timestamp=strftime("%Y-%m-%d_%H%M%S", gmtime()) 
logfn = "/dataworks/logs/insertNewStations.py.log"  # IF you want the log file elsewhere
logFile = open (logfn, 'w')
logFileWrite("\n   insertNewStations.py  run at "+timestamp+" \n")
logFileWrite(  "   Load or update a GSAC dataworks database with station and equipment session data from a GSAC full csv file made by the UNAVCO GSAC server.")

# connect to the database to write to, uses import MySQLdb
# MySQLdb.connect (host, acct, password, Mysql database name)
gsacdb = MySQLdb.connect(dbhost, dbacct, dbacctpw, dbname)
cursor = gsacdb.cursor()
logFileWrite("\n   Connected to the database " +dbname+ " with account "+dbacct+", on "+dbhost +"\n")

# the processing method:
load_db()
        
# disconnect from db server
gsacdb.close()

if newstacount>0 :
    logFileWrite("\n ***** *****  Inserted "+`newstacount`    +" new stations, without problems.")
else :
    logFileWrite("\n ***** ***** No new stations added.")

out='''
if newsessioncount>0  :
    logFileWrite("\n ***** *****  Inserted "+`newsessioncount`+" new equipment sessions, without problems. \n \n")
else :
    logFileWrite( "\n ***** ***** No new equipment sessions added.")
''' 

logFileWrite("\n Complete at " + strftime("%a, %d %b %Y %H:%M:%S +0000", gmtime()) + "  UTC \n")
 
logFileWrite("\n ***** *****  Look at the log file "+logfn+" after each run.  Look for errors noted in lines with PROBLEM and fix any problems. ***** *****\n \n")

logFile.close()

sys.exit (0) # return success

# ALL DONE
