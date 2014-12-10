/*
 * Copyright 2013 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
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
 */

package org.gsac.gsl.output.site;


import org.gsac.gsl.*;
import org.gsac.gsl.metadata.*;
import org.gsac.gsl.metadata.gnss.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.output.*;
import org.gsac.gsl.util.*;

import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.TwoFacedObject;
//import ucar.unidata.util.StringUtil;

import java.io.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.GregorianCalendar;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import javax.servlet.*;
import javax.servlet.http.*;

/** 
 *  UNAVCO GSAC-WS output handler (formating of site query results) for the GAMIT station.info file format.
 *
 *  See notes below.
 * 
 *      To conform with GAMIT we ask you not to revise this format.  You can make a new similar but altered 
 *      handler .java class for your use.  Add its call to the class file SiteManager.java and rebuild GSAC.
 *      For bug reports and suggested improvments please contact UNAVCO.
 *
 *   @author SKWier Nov. 29, 2012. Dec. 3; Dec. 5 2012;  15 Nov 2013.
 *
 *  Notes
      For the Gamit station.info file format, see http://www-gpsg.mit.edu/~simon/gtgk/ and the docs on http://www-gpsg.mit.edu/~simon/gtgk/docs.htm, and 
      http://sopac.ucsd.edu/input/processing/gamit/setup/station.info, http://sopac.ucsd.edu/processing/gamit/,  http://geoapp02.ucsd.edu:8080/scignDataPortal/gpsProcXmlSummary.jsp,
      http://www.geologie.ens.fr/~vigny/site-desc/chili/station.info_cGPS,  http://geoweb.mit.edu/~simon/gtgk/tutorial/Lecture_3.pdf
      http://csrc.ucsd.edu/input/processing/gamit/setup/station.info
      This is a column counting format!    Desired results sample:
 *          Gamit station.info
 *
 *          Generated by SOPAC on 18-Nov-2012 @ 23:00:08 UTC
 *          Send questions, comments or concerns to devel@gpsmail.ucsd.edu
 *
 *   SITE  Station Name      Session Start      Session Stop       Ant Ht   HtCod  Ant N    Ant E    Receiver Type         Vers                  SwVer  Receiver SN           Antenna Type     Dome   Antenna SN          
     0001  GEONET0001        2011 060 00 00 00  9999 999 00 00 00   0.0000  DHBGP   0.0000   0.0000  TRIMBLE NETR9         Nav 4.17 Sig 0.00      4.17  --------------------  TRM29659.00      GSI    --------------------
     PALA  Palau Tide Gauge  2001 044 00 00 00  9999 999 00 00 00   0.0000  DHPAB   0.0000   0.0000  LEICA RS500           2.47                   2.47  70021                 LEIAT504         LEIS   --------------------
     PALK  Pallekele         2009 239 00 00 00  9999 999 00 00 00   0.0000  DHPAB   0.0000   0.0000  TRIMBLE NETRS         1.1-2                  1.10  4850161900            TRM41249.00      NONE   60261190            
     PALM  Palmer Station,   1997 113 00 00 00  1998 189 00 00 00   0.0794  DHPAB   0.0000   0.0000  --------------------  --------------------  -----  --------------------  ASH700936D_M     SCIS   CR14107             
     PALM  Palmer Station,   1998 189 00 00 00  2008 068 00 00 00   0.0794  DHPAB   0.0000   0.0000  ASHTECH Z-XII3        1E95                   8.25  RS00178               ASH700936D_M     SCIS   CR14107             
     PALM  Palmer Station,   2008 068 00 00 00  2009 090 16 30 00   0.0794  DHPAB   0.0000   0.0000  ASHTECH UZ-12         CQ00                  -----  ZR520021801           ASH700936D_M     SCIS   CR14107             
     PALM  Palmer Station,   2009 090 16 30 00  9999 999 00 00 00   0.0794  DHPAB   0.0000   0.0000  ASHTECH UZ-12         CQ00                  -----  UC2200436003          ASH700936D_M     SCIS   CR14107     
    number chars in fields:
      4       16             17                    17               7       5         7        7        20                     20                 5        20                   16             5(not 4)  20

    "HtCod – Defines geometry of AntHt measurement – DHPAB is RINEX standard is vertical height to antenna reference point (ARP)"
    SwVer is "the receiver type and firmware/software version "
    Note GAMIT's use of "9999 999 00 00 00" for "time unknown".
 */

public class StationInfoSiteOutputHandler extends GsacOutputHandler {
    Calendar calendar = Calendar.getInstance();  // default is "GMT" 

    /** output id */
    public static final String OUTPUT_SITE_STATIONINFO= "site.station.info";

    /** date formatter */
    //private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /** date formatter */
    private SimpleDateFormat dateTimeFormat =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z");

    /** formatter for GNSS antenna value of offset of phase center from instrument center */
    private DecimalFormat offsetFormat = new DecimalFormat("####0.####");


    /**
     * ctor
     *
     * @param gsacRepository the repository
     * @param resourceClass _more_
     */
    public StationInfoSiteOutputHandler (GsacRepository gsacRepository, ResourceClass resourceClass) {
        super(gsacRepository, resourceClass);
        getRepository().addOutput(getResourceClass(), new GsacOutput(this, OUTPUT_SITE_STATIONINFO,
            //  use  station.info for file naming properly
            "GAMIT station.info", "/site.station.info", true));
    }

    /**
     * handle the request: format the sites' information in GAMIT station.info files' format
     *
     * @param request the request
     * @param response the response to write to
     *
     * @throws Exception on badness
     */
    public void handleResult(GsacRequest request, GsacResponse response)
            throws Exception {
        //This sets output mime type (how browser handles it)
        response.startResponse(GsacResponse.MIME_TEXT);
        //response.  startResponse("text");
        PrintWriter pw = response.getPrintWriter();
        addHeader(pw);
        //Get all the sites in the results from the GSAC site query by the user: 
        List<GsacSite> sites = response.getSites();
        //For each site:
        for (GsacSite site : sites) {
            //Call this to ensure that all of the metadata is added to the site
            getRepository().doGetFullMetadata(-1, site);
            addSiteEquipment(pw, site);
        }
        response.endResponse();
    }


    /**
     *  make this date-time in UTC, in ISO 8601 format
     *
     * @param date _more_
     *
     * @return _more_
     */
    private String iso8601UTCDateTime(Date date) {
        if (date == null) { return ""; }
        /*synchronized (dateTimeFormatnoT) {
            return dateTimeFormatnoT.format(date); } */
        // make this date-time in UTC, in ISO 8601 format 
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String utcTime = sdf.format( date );
        return utcTime;
    }


    /**
     *  add the GAMIT station.info file header lines for this case (append to output object).
     *
     * @param pw _more_
     */
    private void addHeader (PrintWriter pw) {
        pw.append("*          Gamit station.info\n");
        pw.append("*          \n");
        pw.append("*          Generated by the "+ getRepository().getRepositoryName()  + " at "+ iso8601UTCDateTime(new Date()) + " \n");
        pw.append("*          \n");
        pw.append("*SITE  Station Name      Session Start      Session Stop       Ant Ht   HtCod  Ant N    Ant E    Receiver Type         Vers                  SwVer  Receiver SN           Antenna Type     Dome   Antenna SN\n");
    }


    /**
     * ? needed only to define class. get site id details for GAMI station.info format style
     *
     * @param pw _more_
     * @param site _more_
     *
     * @throws Exception _more_
     */
    private void addSiteIdentification(PrintWriter pw, GsacSite site)
            throws Exception {
    }


    /**
     *  from the input GSAC 'site' object, extract the value of the named field or API argument.
     *
     * @param site _more_
     * @param propertyId _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    private String getProperty(GsacResource site, String propertyId, String dflt) {
        List<GsacMetadata> propertyMetadata =
            (List<GsacMetadata>) site.findMetadata(
                new GsacMetadata.ClassMetadataFinder(PropertyMetadata.class));
        for (int i = 0; i < propertyMetadata.size(); i++) {
            PropertyMetadata metadata =
                (PropertyMetadata) propertyMetadata.get(i);
            if (metadata.getName().equals(propertyId)) {
                return metadata.getValue();
            }
        }
        return "";
    }



    /**
     * get  site equipment (gamit 'sessions') for this format style
     *
     * @param pw _more_
     * @param site _more_
     *
     * @throws Exception _more_
     */
    private void addSiteEquipment(PrintWriter pw, GsacSite site)
            throws Exception {

        // for this site (all sessions):
        String id = site.getShortName();
        String name = site.getLongName();
        // LOOK could test here for bad values of site id or name

        String starttime ="9999 999 00 00 00";
        String stoptime = "9999 999 00 00 00";

        String rectype ="--------------------";
        String recsn ="--------------------";
        String firmvers ="--------------------";
        String anttype ="--------------------";
        String antsn ="--------------------";
        String antn ="0";
        String ante ="0";
        String antht ="-------";
        String htcod ="-----";
        String dome ="-----";
        String swVer ="-----";

        String vrectype ="--------------------";
        String vrecsn ="--------------------";
        String vfirmvers="--------------------";
        String vswVer="-----";
        String vanttype="--------------------";
        String vantsn="--------------------";
        String vantn="0";
        String vante="0";
        String vantht="-------";
        String vdome="-----";

        /* get a list of GsacMetadata objects, ie, the results from the GSAC query */
        List<GsacMetadata> equipmentMetadata = site.findMetadata( new GsacMetadata.ClassMetadataFinder(GnssEquipment.class));

        int goterror =0;
        int session =0;
        Date block_startDate=null;
        Date block_stopDate=null;
        Date laststartDate=null;
        Date laststopDate=null;
        int cnt=1;

        /* for each one, get out its 'GnssEquipment' object and do ... */
        for (GsacMetadata metadata : equipmentMetadata) {
            GnssEquipment equipment = (GnssEquipment) metadata;
            //if (equipment != null) {System.out.println(" site info: new equipment session from "+equipment.getFromDate()+" to "+equipment.getToDate()); }

            double[] xyz = equipment.getXyzOffset();

            // make string of antenna vertical offset double value, trapping bad values, and reformatting for gamit station.info format
            if (xyz == null ) { antht = "------"; }
            else  { 
               antht = offsetFormat.format(xyz[2]); 
               if (antht.equals("0")) { antht = "0.0000"; }
               // doing this busts some java compilers: if (antht.equals("�")) { antht = "0.0000"; }  // this 'character' can occur  in some db
               antn = offsetFormat.format(xyz[1]); 
               if (antn.equals("0")) { antn = "0.0000"; }
               //if (antn.equals("�")) { antn = "0.0000"; }
               ante = offsetFormat.format(xyz[0]);
               if (ante.equals("0")) { ante = "0.0000"; }
               //if (ante.equals("�")) { ante = "0.0000"; } 
            }
            goterror =0;
            Date startDate=null;
            Date stopDate=null;
            startDate = equipment.getFromDate();
            stopDate = equipment.getToDate();
            cnt=+1;
            if (1==cnt) {
               laststartDate  = startDate; 
               laststopDate = stopDate;

               }

            if (equipment.hasReceiver()) {
                rectype=equipment.getReceiver() ;
                recsn=equipment.getReceiverSerial();
                // for firmvers, handle case of value 'unknown' or 'not provided'
                    String answer = equipment.getReceiverFirmware();
                    //answer = answer.replaceAll(",", " ");
                    if ( answer.contains("unknown") || answer.contains("not provided")  || answer.equals("") || answer.equals(" ") ) {
                       answer= "--------------------";
                    }
                firmvers = answer;

                swVer=equipment.getSwVer();
                starttime= getNonNullGamitString(dateTimeFormat_ISO8601( equipment.getFromDate()));
                starttime = getGamitTimeFormat(starttime, equipment.getFromDate());

                stoptime= getNonNullGamitString(dateTimeFormat_ISO8601( equipment.getToDate()));
                stoptime = getGamitTimeFormat(stoptime, equipment.getToDate());
            } 

            if (equipment.hasAntenna()) {
                
                anttype=getNonNullGamitString(equipment.getAntenna());
                //antsn  =getNonNullGamitString(equipment.getAntennaSerial());
                // for antsn, handle case of value 'unknown' or 'not provided'
                    String answer = equipment.getAntennaSerial();
                    //answer = answer.replaceAll(",", " ");
                    if ( answer.contains("unknown") || answer.contains("not provided")  || answer.equals("") || answer.equals(" ") ) {
                       answer = "--------------------"; 
                    }
                antsn = answer;

                String dt = equipment.getDome();
                //     System.out.println(" site info: new dome =_"+dt+"_");
                if ( dt!=null && dt!="" && dt != " " && !dt.contains("   ") ) {
                   dome = getNonNullGamitString(dt); 
                }

                starttime= getNonNullGamitString(dateTimeFormat_ISO8601( equipment.getFromDate()));
                starttime = getGamitTimeFormat(starttime, equipment.getFromDate());

                stoptime= getNonNullGamitString(dateTimeFormat_ISO8601( equipment.getToDate()));
                stoptime = getGamitTimeFormat(stoptime, equipment.getToDate());
            }

            // construct the gamit station.info file line for this equipment session at a site:

            // int compareTo(Date date) Compares the value of the invoking object with that of date. Returns 0 if the values are equal. 
            // Returns a negative value if the invoking object is earlier than date. Returns a positive value if the invoking object is later than date.
            // MyVar == null ... null means that the object has a null pointer i.e. points to nothing.

            // trap and log bad cases, or print good line if possible:
            if ( startDate==null && stopDate==null) { 
              pw.append("*               Error in supplied information. In next line, representing one database record, the start time and stop time are both undefined. \n");
              pw.append("*");
              goterror =1;
            }
            else if ( startDate==null ) { 
              pw.append("*               Error in supplied information. In next line, representing one database record, the start time is undefined. \n");
              pw.append("*");
              goterror =1;
            }
            else if ( startDate!=null && stopDate!=null  && startDate.compareTo( laststartDate) > 0 && startDate.compareTo( laststopDate) < 0) {  // start  is inside previous site visitt
              pw.append("*               Error in supplied information. In next line, representing one equipment session, the start time is inside the previous session's time range. \n");
              pw.append("*");
              goterror =1;
            }
            else if ( startDate!=null && stopDate!=null  && startDate.compareTo( stopDate) == 0) {  // start time equals stop time
              pw.append("*               Error in supplied information. In next line, representing one database record, the start time equals stop time. Zero duration session. \n");
              pw.append("*");
              goterror =1;
            }
            else if ( startDate!=null && stopDate!=null  && startDate.compareTo( stopDate) > 0) {  // start time is later than stop time
              pw.append("*              Error in supplied information. In next line, representing one database record, the start time is after stop time \n");
              pw.append("*");
              goterror =1;
            }
            if (1==goterror) {
              pw.append(   setStringLength(id,4)+"  " +setStringLengthRight(name,16)+"  " +setStringLength(starttime,17)+"  " +setStringLength(stoptime,17)+"  "
                +setStringLength(antht,7)+"  " +setStringLength(htcod,5)+"  " +setStringLength(antn,7)+"  "
                +setStringLength(ante,7)+"  " +setStringLengthRight(rectype,20)+"  " +setStringLengthRight(firmvers,20)+"  "
                +setStringLength(swVer,5)+"  " +setStringLengthRight(recsn,20)+"  " +setStringLengthRight(anttype,15)+"  "
                +setStringLengthRight(dome,5)+"  " +setStringLengthRight(antsn,20)        +"\n");
                continue;  // bad times so jump to next equipment block
            }

            // all OK with dates,  note one date may be null
                // sort gsac equip groups into gamit sessions

                if (session == 0) {
                   block_startDate=startDate;
                   block_stopDate=stopDate;
                   session += 1;
                   vrectype = rectype ;
                   vswVer = swVer ;
                   vrecsn =recsn;
                   vfirmvers = firmvers;
                   vanttype=anttype;
                   vantsn = antsn;
                   vdome = dome;
                   vantn = antn ;
                   vante = ante ;
                   vantht = antht ;
                }

                if (0 ==startDate.compareTo( block_startDate))  // this equipment block same time as previous 
                   { // still in this session
                   // see if the new stop time is later
                   if (stopDate != null && stopDate.compareTo( block_stopDate)>0) {
                     block_stopDate=stopDate;
                   }
                   session += 1;
                   vrectype = rectype ;
                   vswVer = swVer;
                   vrecsn =recsn;
                   vfirmvers = firmvers;
                   vanttype=anttype;
                   vantsn = antsn;
                   vdome = dome;
                   vantn = antn ;
                   vante = ante ;
                   vantht = antht ;
                 }

                if (startDate.compareTo( block_startDate) > 0)  // this equipment block begins after the previous one; so beginning a new geodesy site 'visit'
                   { 
                   // starting a new  geodesy visit session

                   // first print out previous visit
                   starttime= getNonNullGamitString(dateTimeFormat_ISO8601(block_startDate) );
                   starttime = getGamitTimeFormat(starttime,block_startDate );
                   stoptime= getNonNullGamitString(dateTimeFormat_ISO8601(block_stopDate) );
                   stoptime = getGamitTimeFormat(stoptime,block_stopDate );
                   pw.append(" "+  setStringLength(id,4)+"  " +setStringLengthRight(name,16)+"  " +setStringLength(starttime,17)+"  " +setStringLength(stoptime,17)+"  "
                      +setStringLength(vantht,7)+"  " +setStringLength(htcod,5)+"  " +setStringLength(vantn,7)+"  " +setStringLength(vante,7)+"  "
                      +setStringLengthRight(vrectype,20)+"  " +setStringLengthRight(vfirmvers,20)+"  " +setStringLength(vswVer,5)+"  " +setStringLengthRight(vrecsn,20)+"  "
                      +setStringLengthRight(vanttype,15)+"  " +setStringLengthRight(vdome,5)+"  " +setStringLengthRight(vantsn,20)        +"\n");
                   /* test logging System.out.println(" site info normal line "+  setStringLength(id,4)+"  " +setStringLengthRight(name,16)+"  " +setStringLength(starttime,17)+"  " +setStringLength(stoptime,17)+"  "
                      +setStringLength(vantht,7)+"  " +setStringLength(htcod,5)+"  " +setStringLength(vantn,7)+"  " +setStringLength(vante,7)+"  "
                      +setStringLengthRight(vrectype,20)+"  " +setStringLengthRight(vfirmvers,20)+"  " +setStringLength(vswVer,5)+"  " +setStringLengthRight(vrecsn,20)+"  "
                      +setStringLengthRight(vanttype,15)+"  " +setStringLengthRight(vdome,5)+"  " +setStringLengthRight(vantsn,20)        +"\n");
                   */

                   // start values for new visit
                   block_startDate=startDate;
                   block_stopDate=stopDate;
                   session += 1;
                   vrectype = rectype ;
                   vswVer = swVer;
                   vrecsn =recsn;
                   vfirmvers = firmvers;
                   vanttype=anttype;
                   vantsn = antsn;
                   vdome = dome;
                   vantn = antn ;
                   vante = ante ;
                   vantht = antht ;
                   }
            laststartDate=startDate;
            laststopDate=stopDate;

        } // end for loop on GSAC equipment items , NOT gamit station.info sessions

       // print out final session 
       starttime= getNonNullGamitString(dateTimeFormat_ISO8601(block_startDate) );
       starttime = getGamitTimeFormat(starttime,block_startDate );
       stoptime= getNonNullGamitString(dateTimeFormat_ISO8601(block_stopDate) );
       stoptime = getGamitTimeFormat(stoptime,block_stopDate );
       // for testing:
       // System.out.println("  final session; starttime ="+starttime); 
       // System.out.println("  final session; stoptime ="+stoptime); 
       pw.append(" "+  setStringLength(id,4)+"  " +setStringLengthRight(name,16)+"  " +setStringLength(starttime,17)+"  " +setStringLength(stoptime,17)+"  "
          +setStringLength(vantht,7)+"  " +setStringLength(htcod,5)+"  " +setStringLength(vantn,7)+"  " +setStringLength(vante,7)+"  "
          +setStringLengthRight(vrectype,20)+"  " +setStringLengthRight(vfirmvers,20)+"  " +setStringLength(vswVer,5)+"  " +setStringLengthRight(vrecsn,20)+"  "
          +setStringLengthRight(vanttype,15)+"  " +setStringLengthRight(vdome,5)+"  " +setStringLengthRight(vantsn,20)        +"\n");
       /* test logging System.out.println(" site info normal line "+  setStringLength(id,4)+"  " +setStringLengthRight(name,16)+"  " +setStringLength(starttime,17)+"  " +setStringLength(stoptime,17)+"  "
          +setStringLength(vantht,7)+"  " +setStringLength(htcod,5)+"  " +setStringLength(vantn,7)+"  " +setStringLength(vante,7)+"  "
          +setStringLengthRight(vrectype,20)+"  " +setStringLengthRight(vfirmvers,20)+"  " +setStringLength(vswVer,5)+"  " +setStringLengthRight(vrecsn,20)+"  "
          +setStringLengthRight(vanttype,15)+"  " +setStringLengthRight(vdome,5)+"  " +setStringLengthRight(vantsn,20)        +"\n");
       */

}     // end addSiteEquipment


/**
 *  format a Date object into a String in ISO 8601 format
 *
 * @param date  a Date object
 *
 * @return date and time formatted like 2013-04-05T15:17:02 +0000 
 */
private String dateTimeFormat_ISO8601(Date date) {
    if (date == null) { return ""; }
    synchronized (dateTimeFormat) {
        return dateTimeFormat.format(date);
    }
}


/**
 *  if 's' is null return "--------------------" the GAMIT 'nothing' value; else return  the input String's'.
 *
 * @param s  input String object
 *
 * @return  a string
 */
private String getNonNullGamitString(String s) {
    if (s == null) {
        return "--------------------";
    }
    return s;
}

/**
*  make string of desired length by padding left with " " if 's' is short, or truncate if 's' is too long.
*
* @param s               String input to fix 
* @param desiredLength   ending length
* @return                String of desiredLength
*/
public static String setStringLength(String s, int desiredLength) {
String padString = " ";
if (s==null) {                              // new 13 Mar
   s="-------------------------";           // new 13 Mar
}                                           // new 13 Mar
if (s.length() > desiredLength) {
    s =  s.substring(0,desiredLength);
}
else if (s.length() == desiredLength) {
    return s;
}
else {
    while (s.length() < desiredLength) {
        s = padString + s;
    }
}
return s;
}

/**
*  make string of desired length by padding RIGHT with " " if 's' is short, or truncate if 's' is too long.
*
* @param s               String input to fix 
* @param desiredLength   ending length
* @return                String of desiredLength
*/
public static String setStringLengthRight(String s, int desiredLength) {
String padString = " ";
// null input trap
if ( s == null) {
   s = "";
}
if (desiredLength<0) {
     desiredLength=0;
}
if (s.length() > desiredLength) {
    s =  s.substring(0,desiredLength);
}
else if (s.length() == desiredLength) {
    return s;
}
else {
    while (s.length() < desiredLength) {
        s = s + padString;
    }
}
return s;
}

/**
*  make string of  input date and time, in the odd GAMIT format YYYY day-of-year HH MM SS
* day of year must be exactly 3 chars.
*
* @param 
* @param 
* @return                String  of exactly 17 chars
*/
public String  getGamitTimeFormat(String starttime, java.util.Date gd) {
  if (starttime.equals("") || starttime.equals("--------------------") ) {
      starttime="9999 999 00 00 00"; // the GAMIT station.info 'no date' format
  } else {
      calendar.setTime( gd );
      String yyyy = calendar.get(calendar.YEAR) +""; 
      String ddd  = "" + calendar.get(calendar.DAY_OF_YEAR); 
      if (ddd.length() == 1) { ddd=" "+ddd; }
      if (ddd.length() == 2) { ddd=" "+ddd; }
      String time =dateTimeFormat_ISO8601( gd ); // such as 2009-03-30T00:00:00 -0600
      time=time.substring(11,19); // next remove the ":"
      time = time.replaceAll(":"," ");
      starttime= yyyy+" "+ddd+" "+time;
      }
starttime= setStringLength(starttime,17);// should make no change!
return starttime;
}

}
