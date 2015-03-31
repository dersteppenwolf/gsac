

To get this UNAVCO Dataworks file email Fran Bolder, boler at unavco.org


#!/usr/bin/python
'''
 filename              : Dataworks_GSAC_use_metrics.py
 author(s)             : Stuart Wier.
 created               : 2014-10-10
 latest update(version): 2015-01-15

 exit code(s)          : 0, success; 1 for early exit (see log message why)

 description           : To make a GSAC usage report from the catalina.out Tomcat log file

                       : reads the latest Tomcat log file, /var/log/tomcat6/catalina.out catalina.out 

 usage                 : run this script in account ops, with the command:

			 /dataworks/metrics/Dataworks_GSAC_use_metrics.py

                         Results, a text output report, goes to the terminal.

 report bugs to        : dataworks@unavco.org
                   
 tested on             : Python 2.6.5 on Linux (on Ubuntu 10)
                       : "Python 2.6.6 (r266:84292, Jan 22 2014, 09:42:36) [GCC 4.4.7 20120313 (Red Hat 4.4.7-4)] on linux2" (on CentOS)
'''
