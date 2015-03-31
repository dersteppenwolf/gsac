

To get this UNAVCO Dataworks file email Fran Bolder, boler at unavco.org


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
