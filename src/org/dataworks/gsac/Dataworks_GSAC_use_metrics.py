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

import os
import sys
import string
import datetime
from datetime import datetime
from   time import gmtime, strftime
#import math 
import socket # to get host name from an IP 
import smtplib
from   email.mime.text import MIMEText

def scanlogfile ():
    msgbuf=""
    # extract GSAC log lines from the Apache Tomcat catalina.out file; put the lines in the new local file GSAC_log_file:
    cmd1 = "grep GSAC /var/log/tomcat6/catalina.out > GSAC_in_catalina_log_file"  
    cstatus1 = os.system(cmd1)
    # note that this Python process pauses until the cmd1 process completes. Typical time elaspsed is 
    if cstatus1 != 0:
        #print "\n    FAILED to make log file in this directory, GSAC_in_catalina_log_file. See line 45 in script Dataworks_GSAC_use_metrics.py.  \n"
        msgbuf +=  "\n    FAILED to make log file in this directory, GSAC_in_catalina_log_file. See line 45 in script Dataworks_GSAC_use_metrics.py.  \n"
        sys.exit(1)
    elif cstatus1 == 0 :
        file1 = open ('GSAC_in_catalina_log_file')

        linecount=0 
        reqcount=0 
        fedcount=0 
        sopaccount=0 
        ipslist=[]
        opslist=[]
        opslist=[]
        opsdict={}
        ipsdict={} # key is IP strings, value is how many occured in request for that IP
        siteformcount=0 
        ssearchcount=0
        sviewscount=0 
        fileformcount=0 
        filesearchscount=0
        fviewscount=0 
        startdate = ""

        # read and count how many lines in file 
        allLines = file1.readlines()
        linecount = len(allLines)
        file1.seek(0) # rewind to beginning
        #print "  count of lines in log file= "+`linecount`

        # read each line in the input file
        for i in range(linecount-1) :
          line = file1.readline()
          if " CDDIS " in line:
            pass
          else:
            #if (50<i) : break # if testing , can skip later lines

            if startdate=="" and " 201" in line[0:-1] :
               dateindex = string.find( line[0:-1], " 201")
               i2=dateindex + 11
               startdate = line[dateindex+1:i2]

               thishost=(socket.gethostname())

               #print "\n    GSAC Use at "+thishost+", from "+startdate +" to "+ strftime("%Y %b %d  %H:%M:%S UTC", gmtime())
               msgbuf +=   "\n    GSAC Use at "+thishost+", from "+startdate +" to "+ strftime("%Y %b %d  %H:%M:%S UTC", gmtime()) +"\n"
              
            if "new reque" in line[0:-1] or "INCOMING REQUEST" in line:
                # print "      new request: "+line[0:-1]  # to show all the kinds of request
                reqcount +=1 

                #check = "site"  # +"\"+"form"

            if "site/form" in line :
                    #print " site form request:  "+line[0:-1]  # to show all the kinds of request getting files or file info
                    siteformcount+=1

            if "site/search" in line[0:-1] :
                    ssearchcount+=1
                    #print " site search request:  "+line[0:-1]  # to show all the kinds of request getting files or file info

            if "file/form" in line[0:-1] :
                    fileformcount+=1
                    #print " file/form request:  "+line[0:-1]  # to show all the kinds of request getting files or file info
                
            if "file/search" in line[0:-1] :
                    filesearchscount+=1
                    #print " file/search request:  "+line[0:-1]  # to show all the kinds of request getting files or file info

            if "site/view" in line[0:-1] :
                    sviewscount+=1

            if "federated" in line[0:-1] :
                    fedcount +=1 

            ipin = line.find ("from IP ")
            IP=""
            if ipin>10 :
                    IP = line [ (ipin+8):-1]
                    # check for how many sopac xml requests:
                    #if 'output=site.xmllog' in line:
                    #    #print "\t \t \t   sopac xml request from : "+IP
                    #    pass

                    if IP in ipslist:
                       ipsdict[IP] += 1
                       #pass
                    else:
                       ipslist.append(IP)
                       #print "  new IP encountered first time = "+IP
                       ipsdict[IP] = 1

            # output cases without '&output=' in API request:
            if "file.download=Download" in line[0:-1] :
                OP='file.download (Webstart)'
                if OP in opslist:
                    opsdict[OP] += 1
                else:
                      opslist.append(OP)
                      opsdict[OP] = 1
                      #print "      output type requested: "+OP
            if 'file.wget' in line[0:-1]:
                OP='file.wget'
                if OP in opslist:
                    opsdict[OP] += 1
                else:
                      opslist.append(OP)
                      opsdict[OP] = 1
                      #print "      output type requested: "+OP

            # search API request for like &output=site.csv&bbox.
            if "output=" in line[0:-1] :
                opin = line.find ("output=")
                tmp = line [ (opin+7):-1]
                ti=tmp.find("&")
                OP = tmp[0:ti]
                #if "gsacxml" in OP:  plain output gsacxml form federated query only: ?
                #   print "   OP = "+OP + " from line "+line

                # handle types which do not end with &; or are incomplete some are api input errors whose intention is clear
                if 'site.json' in OP:
                   OP='site.json'
                if 'site.gsacxml' in OP:
                   OP='site.gsacxml'
                if 'site.csvfull' in OP:
                   OP='site.csvfull'
                if 'site.xmllog' in OP:
                   OP='site.xmllog'    #sopaccount+=1
                if 'site.cs' in OP:
                   OP='site.csv'
                if 'site.station.inf' in OP:
                   OP='site.station.info'
                if 'file.csv' in OP:
                   OP='file.csv'
                if 'file.url' in OP:
                   OP='file.url'
                if 'file.html' in OP:
                   OP='file.html'
                if 'file.gsacxml' in OP:
                   OP='file.gsacxml'
                if 'file.download' in OP:
                   OP='file.download (Webstart)'
                if 'file.json' in OP:
                   OP='file.json'
                if 'file.zip' in OP:
                   OP='file.zip'


                if OP in opslist:
                    opsdict[OP] += 1
                elif OP=="":
                    pass
                else:
                    if OP[0:2] != "xm" :   # odd bug fix
                      opslist.append(OP)
                      opsdict[OP] = 1
                      #print "      output type requested: "+OP


        #numdays = 27                                                    #  number of days in the log file NOT USED now

        #print "\n    Total count of GSAC requests   = "+`reqcount` + "  (includes page visits as well as actual GSAC search requests.)  "
        msgbuf +=   "\n    Total count of GSAC requests   = "+`reqcount` + "  (includes page visits as well as actual GSAC search requests.) \n"

        #v = (ssearchcount*1.0) /numdays
        #print "    Count of site searches         = "  +`ssearchcount`    # + " or about "+ ('%.1f' % v)+" per day, including API requests." 
        msgbuf +=   "    Count of site searches         = "  +`ssearchcount`  +" (has site/search in GSAC API request) \n"   # + " or about "+ ('%.1f' % v)+" per day, including API requests." 
        msgbuf +=   "    Count of site form searches    = "  +`siteformcount`  +" (has site/form in GSAC API request) \n"   # + " or about "+ ('%.1f' % v)+" per day, including API requests." 

        #v = (filesearchscount*1.0) /numdays
        #print "    Count of file searches         = "  +`filesearchscount` # + " or about "+('%.1f' % v)+" per day, including API requests." 
        msgbuf +=   "    Count of file searches         = "  +`filesearchscount` +" (has file/search in GSAC API request) \n" # + " or about "+('%.1f' % v)+" per day, including API requests." 
        msgbuf +=   "    Count of file form searches    = "  +`fileformcount` +" (has file/form in GSAC API request) \n" # + " or about "+('%.1f' % v)+" per day, including API requests." 


        # sorted listing of output type usage:
        #print "\n      Output types requested: "
        msgbuf +=   "\n    Output types requested: "+"\n" 
        for w in sorted(opsdict, key=opsdict.get, reverse=True):
          #if opsdict[w] > 10: to list only large counts
          #print "       "+ w + " \t "+ `opsdict[w]`
          msgbuf +=   "       "+ w + " \t "+ `opsdict[w]`+"\n" 

        # country counts
        frcount=0
        itcount=0
        ukcount=0
        iscount=0
        decount=0
        mxcount=0
        grcount=0
        escount=0
        bots=0
        botcount=0
        ipscount=0 

        #print "\n    "+`len(ipslist)` + " distinct sites (IPs) made GSAC requests."
        msgbuf +=   "\n    "+`len(ipslist)` + " distinct sites (IPs) made GSAC requests."+"\n" 
        msgbuf +=   "\n    The IPs making requests were: \n"
        msgbuf +=   "       IP          request count      host name \n"
        for w in sorted(ipsdict, key=ipsdict.get, reverse=True):

            # find host name for this IP 'w':
            try:
                hosttup= socket.gethostbyaddr(w) # or exception socket.herror
                hostname = hosttup[0]
                # results like ('stackoverflow.com', ['211.196.59.69.in-addr.arpa'], ['69.59.196.211'])
                #print "    "+ w +     "\t    "+ `ipsdict[w]` +  " requests    "+ hostname
                ipscount += 1
                msgbuf +=   "    "+ w +     "\t    "+ `ipsdict[w]` +  " requests    "+ hostname +"\n"
            except:  
                msgbuf +=   "    "+ w +     "\t    "+ `ipsdict[w]` +  " requests     hostname was not resolved by socket.gethostbyaddr(IP) in Python\n"
		noop_on_centos='''
		    # hostname was not resolved by socket.gethostbyaddr(IP) in Python, so use Linux whois utility:
		    # But, whois is missing on CentOS !
		    print "       "+ w + " \t "+ `ipsdict[w]` +  "   (no host name found)"
		    print "\n       IP "+ w + " \t # requests="+ `ipsdict[w]` +  "     from: "
		    cmd = "whois "+ w +" | grep 'descr:' "
		    cstatus1 = os.system(cmd)
		'''

        print msgbuf +"\n"; 
        utc_time = datetime.utcnow().strftime('%Y-%m-%d %H:%M:%S')
        # for testing
        sendEmail (thishost+" (dataworks) GSAC use report "+utc_time,"","wier@unavco.org",msgbuf)

        # end function scanlogfile

def sendEmail (subject, fromaddr, toaddr, text) :
        msg = MIMEText(text); msg['Subject'] = subject; msg['From'] = fromaddr; msg['To'] = toaddr 
        try :
           s = smtplib.SMTP('localhost')
           s.sendmail(fromaddr, [toaddr], msg.as_string())
           s.quit()
        except :
           pass 

scanlogfile()
sys.exit(0)
