
To get this UNAVCO Dataworks file email Fran Bolder, boler at unavco.org


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
