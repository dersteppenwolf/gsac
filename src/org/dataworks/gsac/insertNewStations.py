

To get this UNAVCO Dataworks file email Fran Bolder, boler at unavco.org


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
