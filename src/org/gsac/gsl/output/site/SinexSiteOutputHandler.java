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

import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.net.URL;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;

import java.util.Hashtable;
import java.util.List;

import javax.servlet.*;
import javax.servlet.http.*;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;



/**
 * Output handler for results for users' site queries, formatted in SINEX.
 * See SINEX specs from http://www.iers.org/nn_324882/IERS/EN/Organization/AnalysisCoordinator/SinexFormat/sinex.html?__nnn=true
 * especially sinex_v201_appendix1.pdf and sinex_v201_introduction.pdf or more recent version of those files.
 * and get recent examples of SINEX files from http://sopac.ucsd.edu/processing/sinex/.
 *
 * @version     initial Nov 21, 2012; revised Nov 30 - Dec 4, 2012  
 * @author      SKW UNAVCO
 */
public class SinexSiteOutputHandler extends GsacOutputHandler {

    String starttime ="------------";
    String stoptime = "------------";
    String prevAntStartTime = "------------";
    String prevAntStopTime = "------------";
    String prevRecStartTime = "------------";
    String prevRecStopTime = "------------";

    /** output id */
    public static final String OUTPUT_SITE_SINEX = "site.snx";

    /** date formatter */
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /** date formatter */
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z");

    /** _more_          */
    private DecimalFormat latLonFormat = new DecimalFormat("####0.####");

    /**  to format ellipsoidal height values sometimes called elevation in GSAC code.  */
    private DecimalFormat elevationFormat = new DecimalFormat("####0.##");

    /** for antenna offset values from instrument reference point.  */
    private DecimalFormat offsetFormat = new DecimalFormat("####0.####");

    Calendar calendar = Calendar.getInstance();  // default is "GMT" 


    /**
     * ctor
     *
     * @param gsacRepository the repository
     * @param resourceClass _more_
     */
    public SinexSiteOutputHandler(GsacRepository gsacRepository,
                                   ResourceClass resourceClass) {
        super(gsacRepository, resourceClass);
        getRepository().addOutput(getResourceClass(), new GsacOutput(this, OUTPUT_SITE_SINEX, "SINEX", "/sites.snx", true));
        // "SINEX" is a label for the Results choice box in site search form on web page.
        // the sites.snx is used for names of files of results.  
    }



    /**
     * handle the request: format sites' information in SINEX
     *
     * @param request the request
     * @param response the response to write to
     *
     *
     * @throws Exception on badness
     */
    public void handleResult(GsacRequest request, GsacResponse response)
            throws Exception {
        // set mime type for browser's display actions:
        response.startResponse("text");
        PrintWriter pw = response.getPrintWriter();
        addHeader(pw);

        //We can have any number of sites here. 
        List<GsacSite> sites = response.getSites();

        // do SITE/ID block with all the sites:
        pw.append("+SITE/ID\n");
        pw.append("*CODE PT __DOMES__ T _STATION DESCRIPTION__ APPROX_LON_ APPROX_LAT_ _APP_H_\n");
        for (GsacSite site : sites) {
            //Call this to ensure that all of the metadata is added to the site
            getRepository().doGetFullMetadata(-1, site);
            addSiteIdentification(pw, site);
            //addSiteLocation(pw, site);
            //addSiteStream(pw, site);
        }
        pw.append("-SITE/ID\n");

/*
        // do SITE/antenna with all the sites:
        pw.append("*-------------------------------------------------------------------------------\n");
        pw.append("+SITE/ANTENNA\n");
        pw.append("*SITE PT SOLN T DATA_START__ DATA_END____ DESCRIPTION_________ S/N__\n");
        for (GsacSite site : sites) {
            addSiteEquipmentAntenna(pw, site);
        }
        pw.append("-SITE/ANTENNA\n");
*/

        // do SITE/RECEIVER with all the sites:
        pw.append("*-------------------------------------------------------------------------------\n");
        pw.append("+SITE/RECEIVER\n");
        pw.append("*SITE PT SOLN T DATA_START__ DATA_END____ DESCRIPTION_________ S/N__ FIRMWARE___\n");
        for (GsacSite site : sites) {
            // when testing only  
            //pw.append("  CALL addSiteEquipmentAntenna -------------------------------------------------------------------------------------------- for site \n");
            addSiteEquipmentReceiver(pw, site);
        }
        pw.append("-SITE/RECEIVER\n");

/*
        pw.append("*-------------------------------------------------------------------------------\n");
        pw.append("+SITE/ECCENTRICITY\n");
        pw.append("*                                             UP______ NORTH___ EAST____\n");
        pw.append("*SITE PT SOLN T DATA_START__ DATA_END____ AXE ARP->BENCHMARK(M)_________\n");
        for (GsacSite site : sites) {
            //Call this to ensure that all of the metadata is added to the site
            //getRepository().doGetFullMetadata(-1, site);
            addSiteEquipmentAntSinexEccentricity(pw, site);
            //addSiteStream(pw, site);
        }
        pw.append("-SITE/ECCENTRICITY\n");
*/

        //pw.append("*-------------------------------------------------------------------------------\n");
        //pw.append("+SITE/GPS_PHASE_CENTER\n");
        //pw.append("-SITE/GPS_PHASE_CENTER\n");

        pw.append("%ENDSNX\n");

        response.endResponse();
    }


    /**
     * label at the top of the file of results; a header.
     *
     * @param pw _more_
     */
    private void addHeader (PrintWriter pw) {
        String now;
        now = getNonNullString(myFormatDateTime( new Date()));
        now = getSinexTimeFormat(now, new Date());
        pw.append(  "%=SNX 2.01 " + getRepository().getRepositoryName() + " "+ now + "\n"); 
        pw.append(  "*-------------------------------------------------------------------------------\n");
    }

    /**
     * print results of site id details for this format style
     * example:
+SITE/ID
*CODE PT __DOMES__ T _STATION DESCRIPTION__ APPROX_LON_ APPROX_LAT_ _APP_H_
 ABMF  A 97103M001 P Les Abymes, FR         298 28 20.9  16 15 44.3   -25.6
 ABPO  A 33302M001 P Antananarivo, MG        47 13 45.2 -19  1  5.9  1553.0
 PALV  A 66005M002 P Palmer Station, AQ     295 56 56.0 -64 46 30.3    31.1
 PARK  A 50108M001 P Parkes, AU             148 15 52.6 -32 59 55.5   397.3
     *
     * @param pw _more_
     * @param site _more_
     *
     * @throws Exception _more_
     */
    private void addSiteIdentification(PrintWriter pw, GsacSite site)
            throws Exception {
        pw.append(" "+ setStringLength(site.getShortName(),4) +"  A");
        pw.append(" "+ setStringLength( (getProperty(site, GsacExtArgs.SITE_METADATA_IERDOMES, "") ),9) +" P");
        pw.append(" "+ setStringLengthRight(site.getLongName(),22) +" ");

        EarthLocation el = site.getEarthLocation();
        String latitude =formatLocation(el.getLatitude())  ;
        String longitude =formatLocation(el.getLongitude()) ;
        String ellipsoidalheight =elevationFormat.format(el.getElevation()) ;
        pw.append( setStringLength( longitude ,11) + " " );
        pw.append( setStringLength( latitude ,11) + " " );
        pw.append( setStringLength( ellipsoidalheight ,7) );
        pw.append("\n");  //  end of line
    }


    /**
     *  from the ipnput GSAC 'site' object, extract the value of the named field or API argument.
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
     * print results of site location for this format style
     *
     * @param pw _more_
     * @param site _more_
     *
     * @throws Exception _more_
     */
    private void addSiteLocation(PrintWriter pw, GsacSite site)
            throws Exception {
        List<GsacMetadata> politicalMetadata =
            site.findMetadata(
                new GsacMetadata.ClassMetadataFinder(
                    PoliticalLocationMetadata.class));
        PoliticalLocationMetadata plm = null;
        //Just use the first one
        if (politicalMetadata.size() > 0) {
            plm = (PoliticalLocationMetadata) politicalMetadata.get(0);
        }
        if (plm == null) {
            plm = new PoliticalLocationMetadata();
        }
        pw.append(    " site country:                 "+ getNonNullString(plm.getCountry())  + "\n");
        pw.append(    " site state or province:       "+ getNonNullString(plm.getState()  ) + "\n");
        pw.append(    " site city:                    "+ getNonNullString(plm.getCity()) + "\n");
        EarthLocation el = site.getEarthLocation();
        pw.append(    " site latitude:                "+ formatLocation(el.getLatitude())  + "" + "\n");
        pw.append(    " site longitude:               "+ formatLocation(el.getLongitude()) + "" + "\n");
        pw.append(    " site ellipsoidal height:      "+ elevationFormat.format(el.getElevation()) + "" + "\n");
        if (el.hasXYZ()) {
            pw.append(" site X coordinate:            "+ el.getX() + "" + "\n");
            pw.append(" site Y coordinate:            "+ el.getY() + "" + "\n");
            pw.append(" site Z coordinate:            "+ el.getZ() + "" + "\n");
        } else {
            pw.append(" site X coordinate:            " + "\n");
            pw.append(" site Y coordinate:            " + "\n");
            pw.append(" site Z coordinate:            " + "\n");
        }
    }

    /**
     * format a double value (such as lat and long) to 4 sig figs. 0.0001 lat res is about 1/100 of a km or 10 meters.
     *
     * @param v  a double number
     *
     * @return a String 
     */
    private String formatLocation(double v) {
        v = (double) Math.round(v * 10000) / 10000;
        String s = latLonFormat.format(v);
        return s;
    }



    /**
OLD version
     * print site receiver block  for all sites
*SITE PT SOLN T DATA_START__ DATA_END____ DESCRIPTION_________ S/N__ FIRMWARE___
 ABMF  A    1 P 12:217:00000 12:225:86370 TRIMBLE NETR9        ----- -----------
 ABPO  A    1 P 12:217:00000 12:225:86370 ASHTECH UZ-12        ----- -----------
 ADIS  A    1 P 12:217:00000 12:225:86370 JPS LEGACY           ----- -----------
     *
     * @param pw _more_
     * @param site _more_
     *
     * @throws Exception _more_
    private void addSiteEquipment OLD Receiver(PrintWriter pw, GsacSite site)
            throws Exception {
        List<GsacMetadata> equipmentMetadata =
            site.findMetadata(
                new GsacMetadata.ClassMetadataFinder(GnssEquipment.class));
        int sescount=0;

        for (GsacMetadata metadata : equipmentMetadata) {
            GnssEquipment equipment = (GnssEquipment) metadata;
            if (equipment.hasReceiver()) {
                sescount +=1;
                // for testing ONLY: pw.append("         REC SESSION "+sescount+  "\n ");
                pw.append(" "+ setStringLength(site.getShortName(),4) +"  A    1 P ");
                starttime= getNonNullString(myFormatDateTime( equipment.getFromDate()));
                starttime = getSinexTimeFormat(starttime, equipment.getFromDate());
                stoptime= getNonNullString(myFormatDateTime( equipment.getToDate()));
                stoptime = getSinexTimeFormat(stoptime, equipment.getToDate());
                pw.append( starttime+ " ");
                pw.append( stoptime+ " ");
                pw.append( setStringLengthRight( equipment.getReceiver(),20) +" ");
                pw.append( setStringLength( equipment.getReceiverSerial(),5) +" ");
                pw.append( setStringLengthRight( equipment.getReceiverFirmware(),11));
                pw.append("\n");
            } 
        }
    }
     */


    /**
     * print site receiver block  for all sites
     *
     * @param pw _more_
     * @param site _more_
     *
     * @throws Exception _more_
     */
    private void addSiteEquipmentReceiver(PrintWriter pw, GsacSite site)
            throws Exception {
        List<GsacMetadata> equipmentMetadata =
            site.findMetadata(
                new GsacMetadata.ClassMetadataFinder(GnssEquipment.class));
        int sescount=0;
        for (GsacMetadata metadata : equipmentMetadata) {
            GnssEquipment equipment = (GnssEquipment) metadata;
            sescount +=1;

            if (equipment.hasReceiver()) {
                // for testing ONLY: 
                //pw.append("         REC SESSION "+sescount+  "\n ");
                pw.append(" "+ setStringLength(site.getShortName(),4) +"  A    1 P ");
                starttime= getNonNullString(myFormatDateTime( equipment.getFromDate()));
                starttime = getSinexTimeFormat(starttime, equipment.getFromDate());
                stoptime= getNonNullString(myFormatDateTime( equipment.getToDate()));
                stoptime = getSinexTimeFormat(stoptime, equipment.getToDate());
                pw.append( starttime+ " ");
                pw.append( stoptime+ " ");
                pw.append( setStringLengthRight( equipment.getReceiver(),20) +" ");
                pw.append( setStringLength( equipment.getReceiverSerial(),5) +" ");
                pw.append( setStringLengthRight( equipment.getReceiverFirmware(),11));
                pw.append("\n");
            }
/*
            else if (equipment.hasAntenna()) {
                starttime= getNonNullString(myFormatDateTime( equipment.getFromDate()));
                starttime = getSinexTimeFormat(starttime, equipment.getFromDate());
                stoptime= getNonNullString(myFormatDateTime( equipment.getToDate()));
                stoptime = getSinexTimeFormat(stoptime, equipment.getToDate());
                if (starttime.equals(prevAntStartTime) ) {
                    ; //  why two antenna sessions with same times?
                    // don't reprint the same line
                }
                else {
                    prevAntStartTime = starttime;
                    prevAntStopTime = stoptime;
                    // for testing ONLY: 
                    //pw.append("         ANT SESSION "+sescount+  "\n ");
                    pw.append(" "+ setStringLength(site.getShortName(),4) +"  A    1 P ");
                    pw.append( starttime+ " ");
                    pw.append( stoptime+ " ");
                    pw.append( setStringLengthRight( equipment.getAntenna(),20) +" ");
                    pw.append( setStringLength( equipment.getAntennaSerial(),5) );
                    pw.append("\n");
                }
            }
*/
        }
    }

    /**
     * print site antenna ECCENTRICITY block  for all sites
*SITE PT SOLN T DATA_START__ DATA_END____ DESCRIPTION_________ S/N__
*-------------------------------------------------------------------------------
+SITE/ECCENTRICITY
*                                             UP______ NORTH___ EAST____
*SITE PT SOLN T DATA_START__ DATA_END____ AXE ARP->BENCHMARK(M)_________
 ABMF  A    1 P 12:217:00000 12:225:86370 UNE   0.0000   0.0000   0.0000
 ABPO  A    1 P 12:217:00000 12:225:86370 UNE   0.0083   0.0000   0.0000
 ADIS  A    1 P 12:217:00000 12:225:86370 UNE   0.0010   0.0000   0.0000
     *
     * @param pw _more_
     * @param site _more_
     *
     * @throws Exception _more_
     */
    private void addSiteEquipmentAntSinexEccentricity(PrintWriter pw, GsacSite site)
            throws Exception {
        List<GsacMetadata> equipmentMetadata =
            site.findMetadata(
                new GsacMetadata.ClassMetadataFinder(GnssEquipment.class));
        for (GsacMetadata metadata : equipmentMetadata) {
            GnssEquipment equipment = (GnssEquipment) metadata;
            if (equipment.hasAntenna()) {
                starttime= getNonNullString(myFormatDateTime( equipment.getFromDate()));
                starttime = getSinexTimeFormat(starttime, equipment.getFromDate());
                stoptime= getNonNullString(myFormatDateTime( equipment.getToDate()));
                stoptime = getSinexTimeFormat(stoptime, equipment.getToDate());

                if (starttime.equals(prevAntStartTime) ) {
                    ; //  why two antenna sessions with same times?
                    // don't reprint the same line
                }
                else {
                prevAntStartTime = starttime;
                prevAntStopTime = stoptime;
                pw.append(" "+ setStringLength(site.getShortName(),4) +"  A    1 P ");
                pw.append( starttime+ " ");
                pw.append( stoptime+ " ");
                pw.append( "UNE ");  // the axes order in coord offsets following is up(z or vertical), north , east 
                // the offsets in the GSAC equipment object is the next xyz double array in array order east, norht, up.
                // sinex likes these values in 6 characters in an 8 character field

                double[] xyz = equipment.getXyzOffset();
                String zo = offsetFormat.format(xyz[2]);
                if (zo.equals("0")) { zo= "0.0000"; }
                pw.append( "  "+ setStringLengthZeros(zo,6) + " ");

                String yo = offsetFormat.format(xyz[1]);
                if (yo.equals("0")) { yo= "0.0000"; }
                pw.append( "  "+ setStringLengthZeros(yo,6) + " ");

                String xo = offsetFormat.format(xyz[0]);
                if (xo.equals("0")) { xo= "0.0000"; }
                pw.append( "  "+ setStringLengthZeros(xo,6) );

                pw.append("\n");
                }
            }
            /* keep for future use
                //pw.append( _ALIGNMENTFROMTRUENORTH, "", ""));
                //pw.append(EQUIP_ANTENNACABLETYPE, "",
                //pw.append(EQUIP_ANTENNACABLELENGTH,
                pw.append("      Dome type:              "+ getNonNullString(equipment.getDome()) + "\n");
                pw.append("      Dome SN:                "+ getNonNullString(equipment.getDomeSerial()) + "\n");
            */
        }
    }


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
     * _more_
     *
     * @param date _more_
     *
     * @return _more_
     */
    private String myFormatDate(Date date) {
        if (date == null) { return ""; }
        synchronized (dateFormat) {
            return dateFormat.format(date);
        }
    }

    /**
     * _more_
     *
     * @param pw _more_
     * @param site _more_
     *
     * @throws Exception _more_
    private void addSiteStream(PrintWriter pw, GsacSite site)
            throws Exception {
        GsacMetadata.debug = true;
        //System.err.println("  SinexSiteOutputHandler.addSiteStream ():  Finding metadata");
        List<GsacMetadata> streamMetadata =
            site.findMetadata(
                new GsacMetadata.ClassMetadataFinder(StreamMetadata.class));
        GsacMetadata.debug = false;
        int cnt = 0;
        for (GsacMetadata metadata : streamMetadata) {
            StreamMetadata stream = (StreamMetadata) metadata;
            if (cnt == 0) {
                ; //pw.append( XmlUtil.openTag(XmlSiteLog.TAG_REALTIME_DATASTREAMS));
            }
            cnt++;
            stream.encode(pw, this, "plainsitelog");
        }
        if (cnt > 0) {
            ; //pw.append(XmlUtil.closeTag(XmlSiteLog.TAG_REALTIME_DATASTREAMS));
        }
    }
     */


  /**
   *  make string of desired length by padding LEFT end with " " if 's' is short, or truncate if 's' is too long.
   *
   * @param s               String input to fix 
   * @param desiredLength   ending length
   * @return                String of desiredLength
   */
  private static String setStringLength(String s, int desiredLength) {
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
   *  make string of desired length by padding RIGHT with "0" if 's' is short, or truncate if 's' is too long.
   *
   * @param s               String input to fix 
   * @param desiredLength   ending length
   * @return                String of desiredLength
   */
  public static String setStringLengthZeros(String s, int desiredLength) {
    String padString = "0";
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
   *  make string of  input date and time, in the  SINEX  time format:
| Time                                 | YY:DDD:SSSSS. "UTC" | I2.2, |
| | YY = last 2 digits of the year,    | 1H:,I3.3, |
| | if YY <= 50 implies 21-st century, | 1H:,I5.5 |
| | if YY > 50 implies 20-th century,  | |
| | DDD = 3-digit day in year,         | |
| | SSSSS = 5-digit seconds in day.    | |
   *
   * @param  input String starttime; time to be formatted
   * @param 
   * @return                String  of exactly 12 chars
   */
  public String  getSinexTimeFormat(String starttime, java.util.Date gd) {
      if (starttime.equals("") || starttime.equals("------------") ) {
          starttime="-----------";
          // the n data format
      } else {
          calendar.setTime( gd );
          String yy = calendar.get(calendar.YEAR) +"";  // like 1999
          yy =   yy.substring(2,4); // like 99
          String ddd  = "" + calendar.get(calendar.DAY_OF_YEAR);
          if (ddd.length() == 1) { ddd="0"+ddd; }
          if (ddd.length() == 2) { ddd="0"+ddd; }
          String time =myFormatDateTime( gd ); // such as 2009-03-30T00:00:00 -0600
          time=time.substring(11,19); 
          // for HHMMSS 
          time = time.replaceAll(":","");
          // next get  seconds in day
          int hh =  Integer.parseInt(   time.substring(0,2));
          int mm =  Integer.parseInt(   time.substring(2,4));
          int ss =  Integer.parseInt(   time.substring(4,6));
          int secs= hh*3600 + mm*60 +ss;
          String sssss = "" + secs; 
          // LOOK replace next and ddd business above with new method to pad with 0s on left to get a givne length
          if (sssss.length() == 1) { sssss="0"+sssss; }
          if (sssss.length() == 2) { sssss="0"+sssss; }
          if (sssss.length() == 3) { sssss="0"+sssss; }
          if (sssss.length() == 4) { sssss="0"+sssss; }
          starttime= yy+":"+ddd+":"+sssss;
          }
    starttime= setStringLengthRight(starttime,12);// should make no change!
    return starttime;
    }

    /**
     *  if 's' is null return "---------------" the 'nothing' time value; else return 's'.
     *
     * @param s  input String object
     *
     * @return  a string
     */
    private String getNonNullString(String s) {
        if (s == null) {
            return "------------";
        }
        return s;
    }




}
