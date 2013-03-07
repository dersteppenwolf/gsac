/*
 * Copyright 2012 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
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
 *      To conform with GAMIT,  and with other GSAC repositories, we ask you not to revise this format.  You can make a new similar but altered 
 *      handler .java class for your use.  Add its call to the class file SiteManager.java and rebuild GSAC.  Do not commit your core 
 *      GSAC code changes in this case into GSAC without consulting UNAVCO.
 *      For bug reports and suggested improvments please contact UNAVCO.
 *
 *   version SKW Nov. 29, 2012. mods Dec. 3; Dec. 5; 
*/

/*  Notes
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
*SITE  Station Name      Session Start      Session Stop       Ant Ht   HtCod  Ant N    Ant E    Receiver Type         Vers                  SwVer  Receiver SN           Antenna Type     Dome   Antenna SN          
 0001  GEONET0001        2011 060 00 00 00  9999 999 00 00 00   0.0000  DHBGP   0.0000   0.0000  TRIMBLE NETR9         Nav 4.17 Sig 0.00      4.17  --------------------  TRM29659.00      GSI    --------------------
 PALA  Palau Tide Gauge  2001 044 00 00 00  9999 999 00 00 00   0.0000  DHPAB   0.0000   0.0000  LEICA RS500           2.47                   2.47  70021                 LEIAT504         LEIS   --------------------
 PALK  Pallekele         2009 239 00 00 00  9999 999 00 00 00   0.0000  DHPAB   0.0000   0.0000  TRIMBLE NETRS         1.1-2                  1.10  4850161900            TRM41249.00      NONE   60261190            
 PALM  Palmer Station,   1997 113 00 00 00  1998 189 00 00 00   0.0794  DHPAB   0.0000   0.0000  --------------------  --------------------  -----  --------------------  ASH700936D_M     SCIS   CR14107             
 PALM  Palmer Station,   1998 189 00 00 00  2008 068 00 00 00   0.0794  DHPAB   0.0000   0.0000  ASHTECH Z-XII3        1E95                   8.25  RS00178               ASH700936D_M     SCIS   CR14107             
 PALM  Palmer Station,   2008 068 00 00 00  2009 090 16 30 00   0.0794  DHPAB   0.0000   0.0000  ASHTECH UZ-12         CQ00                  -----  ZR520021801           ASH700936D_M     SCIS   CR14107             
 PALM  Palmer Station,   2009 090 16 30 00  9999 999 00 00 00   0.0794  DHPAB   0.0000   0.0000  ASHTECH UZ-12         CQ00                  -----  UC2200436003          ASH700936D_M     SCIS   CR14107     
number chars in fields:
  4       16             17                    17               7       5         7        7        20                     20                 5        20                   16             5(not 4)  20

    " HtCod – Defines geometry of AntHt measurement – DHPAB is RINEX standard is vertical height to antenna reference point (ARP)"

    "the receiver type and firmware/software version (SwVer)"

     Note use of "9999 999 00 00 00" for "time unknown".

 *  Note, use of "GAMIT station.info", "/site.station.info.txt", true)); below, so that it can show up in a browser window.
 *  May wish to use only site.station.info for file naming properly.
 */

public class StationInfoSiteOutputHandler extends GsacOutputHandler {

    String id ="----";
    String name ="--------------------";
    String starttime ="9999 999 00 00 00";
    String stoptime = "9999 999 00 00 00";

    String rectype ="--------------------";
    String recsn ="--------------------";
    String firmvers ="--------------------";

    String swvers ="-----";

    String anttype ="--------------------";
    String antsn ="--------------------";
    String antn ="0";
    String ante ="0";
    String antht ="-------";
    String htcod ="-----";

    String dome ="-----";

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
        response.startResponse("text");
        PrintWriter pw = response.getPrintWriter();
        addHeader(pw);
        //Get all the sites in the results from the GSAC site query by the user: 
        List<GsacSite> sites = response.getSites();
        //For each site:
        for (GsacSite site : sites) {
            //Call this to ensure that all of the metadata is added to the site
            getRepository().doGetFullMetadata(-1, site);
            addSiteIdentification(pw, site);
            addSiteEquipment(pw, site);
        }
        response.endResponse();
    }


    /**
     * a GAMIT station.info file header for this case.
     *
     * @param pw _more_
     */
    private void addHeader (PrintWriter pw) {
        pw.append("*          Gamit station.info\n");
        pw.append("*          \n");
        pw.append("*          Generated by the "+ getRepository().getRepositoryName()  + " on "+ myFormatDateTime(new Date()) + " \n");
        pw.append("*          \n");
        pw.append("*SITE  Station Name      Session Start      Session Stop       Ant Ht   HtCod  Ant N    Ant E    Receiver Type         Vers                  SwVer  Receiver SN           Antenna Type     Dome   Antenna SN\n");
    }


    /**
     * get site id details for GAMI station.info format style
     *
     * @param pw _more_
     * @param site _more_
     *
     * @throws Exception _more_
     */
    private void addSiteIdentification(PrintWriter pw, GsacSite site)
            throws Exception {
        id = site.getShortName();
        name = site.getLongName();
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
    private String getProperty(GsacResource site, String propertyId,
                               String dflt) {
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
     * get  site equipment ('sessions') for this format style
     *
     * @param pw _more_
     * @param site _more_
     *
     * @throws Exception _more_
     */
    private void addSiteEquipment(PrintWriter pw, GsacSite site)
            throws Exception {
        List<GsacMetadata> equipmentMetadata =
            site.findMetadata(
                new GsacMetadata.ClassMetadataFinder(GnssEquipment.class));

        for (GsacMetadata metadata : equipmentMetadata) {
            GnssEquipment equipment = (GnssEquipment) metadata;

            if (equipment.hasReceiver()) {
                rectype=equipment.getReceiver() ;
                recsn=equipment.getReceiverSerial();
                firmvers=equipment.getReceiverFirmware();
                starttime= getNonNullGamitString(myFormatDateTime( equipment.getFromDate()));
                starttime = getGamitTimeFormat(starttime, equipment.getFromDate());
                stoptime= getNonNullGamitString(myFormatDateTime( equipment.getToDate()));
                stoptime = getGamitTimeFormat(stoptime, equipment.getToDate());
            }
            else if (equipment.hasAntenna()) {
                anttype=getNonNullGamitString(equipment.getAntenna());
                antsn  =getNonNullGamitString(equipment.getAntennaSerial());
                dome = getNonNullGamitString(equipment.getDome());
                double[] xyz = equipment.getXyzOffset();
                antht = offsetFormat.format(xyz[2]);
                if (antht.equals("0")) { antht = "0.0000"; }
                antn = offsetFormat.format(xyz[1]);
                if (antn.equals("0")) { antn = "0.0000"; }
                ante = offsetFormat.format(xyz[0]);
                if (ante.equals("0")) { ante = "0.0000"; }
                starttime= getNonNullGamitString(myFormatDateTime( equipment.getFromDate()));
                starttime = getGamitTimeFormat(starttime, equipment.getFromDate());
                stoptime= getNonNullGamitString(myFormatDateTime( equipment.getToDate()));
                stoptime = getGamitTimeFormat(stoptime, equipment.getToDate());
            }

            // construct the gamit station.info file line for this session at a site:
            pw.append(" " +setStringLength(id,4)+"  " +setStringLengthRight(name,16)+"  " +setStringLength(starttime,17)+"  " +setStringLength(stoptime,17)+"  "
                +setStringLength(antht,7)+"  "
                +setStringLength(htcod,5)+"  "
                +setStringLength(antn,7)+"  "
                +setStringLength(ante,7)+"  "
                +setStringLengthRight(rectype,20)+"  "
                +setStringLengthRight(firmvers,20)+"  "
                +setStringLength(swvers,5)+"  "
                +setStringLengthRight(recsn,20)+"  "
                +setStringLengthRight(anttype,15)+"  "
                +setStringLengthRight(dome,5)+"  "
                +setStringLengthRight(antsn,20)                      +"\n");

        } // end for loop on equipment items = sessions
    }     // end addSiteEquipment


//number chars in fields:
//*SITE  Station Name      Session Start      Session Stop       Ant Ht   HtCod  Ant N    Ant E    Receiver Type         Vers                  SwVer  Receiver SN           Antenna Type     Dome   Antenna SN          
//  4       16             17                    17               7       5         7        7        20                     20                 5        20                   16              4          20


    /**
     * _more_
     *
     * @param date _more_
     *
     * @return _more_
     */
    private String myFormatDateTime(Date date) {
        if (date == null) { return ""; }
        synchronized (dateTimeFormat) {
            return dateTimeFormat.format(date);
        }
    }


    /**
     *  if 's' is null return "--------------------" the GAMIT 'nothing' value; else return 's'.
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
          starttime="9999 999 00 00 00";
          // the GAMIT station.info 'no data' format
      } else {
          calendar.setTime( gd );
          String yyyy = calendar.get(calendar.YEAR) +""; 
          String ddd  = "" + calendar.get(calendar.DAY_OF_YEAR); 
          if (ddd.length() == 1) { ddd=" "+ddd; }
          if (ddd.length() == 2) { ddd=" "+ddd; }
          String time =myFormatDateTime( gd ); // such as 2009-03-30T00:00:00 -0600
          time=time.substring(11,19); // next remove the ":"
          time = time.replaceAll(":"," ");
          starttime= yyyy+" "+ddd+" "+time;
          }
    starttime= setStringLength(starttime,17);// should make no change!
    return starttime;
    }

}
