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
 * Output handler for results for users' site queries, formatted in SINEX.  Conforms to SINEX specifications.
 *
 * See SINEX v 2.01 specs from http://www.iers.org/nn_10910/IERS/EN/Organization/AnalysisCoordinator/SinexFormat/sinex__cont.html?__nnn=true
 * especially sinex_v201_appendix1.pdf and sinex_v201_introduction.pdf or more recent version of those files.
 * You can get recent examples of SINEX files from http://sopac.ucsd.edu/processing/sinex/.
 *
 * Note unknown fields in SINEX are filled with - characters. No field is left blank.  Any missing IERS DOMES values should have an M or S (only).
 * Firmware version characters are left justified.  So are all "DESCRIPTION" items.
 * 
 *      To conform with SINEX and with other GSAC repositories we ask you not to revise this Java file.  You are very welcome to make a new similar but altered 
 *      handler .java class for your use.  Add its call to the class file SiteManager.java, replacing this class. and rebuild GSAC.  
 *      Do not commit changes in GSAC code into GSAC without consulting UNAVCO.
 *      For bug reports and suggested improvments please contact UNAVCO.
 *
 * @version     initial Nov 21, 2012; revised Nov 30-Dec 4, 2012; June 10, 2013.  
 * @author      SKW UNAVCO
 */
public class SinexSiteOutputHandler extends GsacOutputHandler {

    String starttime ="------------";
    String stoptime = "------------";
    String prevAntStartTime = "------------";
    String prevAntStopTime =  "------------";
    String prevRecStartTime = "------------";
    String prevRecStopTime =  "------------";

    /** output id */
    public static final String OUTPUT_SITE_SINEX = "site.snx";

    /** date formatter */
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /** date formatter */
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z");

    /**  lati or longi with 4 decimal places*/
    private DecimalFormat latLonFormat = new DecimalFormat("####0.####");

    /**  for lati or longi seconds part (float) to string  with only one decimal place*/
    private DecimalFormat secFormat = new DecimalFormat("##.#");

    /**  to format ellipsoidal height values, called "elevation" in older GSAC code.  */
    private DecimalFormat heightFormat = new DecimalFormat("####0.##");

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
        }
        pw.append("-SITE/ID\n");


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


        // do SITE/antenna with all the sites:
        pw.append("*-------------------------------------------------------------------------------\n");
        pw.append("+SITE/ANTENNA\n");
        pw.append("*SITE PT SOLN T DATA_START__ DATA_END____ DESCRIPTION_________ S/N__\n");
        for (GsacSite site : sites) {
            addSiteEquipmentAntenna(pw, site);
        }
        pw.append("-SITE/ANTENNA\n");


        pw.append("*-------------------------------------------------------------------------------\n");
        pw.append("+SITE/ECCENTRICITY\n");
        pw.append("*                                             UP______ NORTH___ EAST____\n");
        pw.append("*SITE PT SOLN T DATA_START__ DATA_END____ AXE ARP->BENCHMARK(M)_________\n");
        for (GsacSite site : sites) {
            //Call this to ensure that all of the metadata is added to the site
            //getRepository().doGetFullMetadata(-1, site);
            addSiteEquipmentAntSinexEccentricity(pw, site);
        }
        pw.append("-SITE/ECCENTRICITY\n");

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
        // need this at top of page <meta charset='utf-8'>
        pw.append(  "%=SNX 2.01 " + getRepository().getRepositoryName() + " "+ now + "\n"); 
        pw.append(  "*-------------------------------------------------------------------------------\n");
    }

    /**
     * print results of site location, as per SINEX specs: 
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
        // from GsacSite site
        String id = site.getShortName();
        String name = site.getLongName();
        

        pw.append(" "+ setStringLength(site.getShortName(),4) +"  A");
        pw.append(" "+ setStringLength( (getProperty(site, GsacExtArgs.SITE_METADATA_IERDOMES, "") ),9) +" P");

        //System.out.println("  for site id="+id+"    sinex staname =" +name +";  iers domes="+(getProperty(site, GsacExtArgs.SITE_METADATA_IERDOMES, "") )+"_"   );
        // shows correct Icelandic characters

        pw.append(" "+ setStringLengthRight(name,22));

        EarthLocation el = site.getEarthLocation();
        String latitude = formatLatitudeDMS (el.getLatitude() ) ;
        String longitude =formatLongitudeDMS (el.getLongitude()) ; 
        pw.append( longitude );
        pw.append( latitude );

        // About ellipsoidal height 
        // could force value "0.0" here if your metadata has elevation above a geoid not ellipsoid height:
        // but usually geodesy positions from GNSS receivers have ellipsoidal height
        String ellipsoidalheight =heightFormat.format(el.getElevation()) ;  // one decimal point; but if decimal fraction is zero as in 5.0 then this gives just "5" not 5.0; no good.
        // fix it:
        if (ellipsoidalheight.contains(".") ) { ; }
        else { ellipsoidalheight=ellipsoidalheight+".0"; }
        //  add " " as needed for SINEX column-count format
        String add="";
        if (ellipsoidalheight.length() ==3 )      { add="     "; }
        else if (ellipsoidalheight.length() ==4 ) { add="    "; }
        else if (ellipsoidalheight.length() ==5 ) { add="   "; }
        else if (ellipsoidalheight.length() ==6 ) { add="  "; }
        else if (ellipsoidalheight.length() ==7 ) { add=" "; }
        ellipsoidalheight = add+ellipsoidalheight;
        pw.append( ellipsoidalheight );
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
        
        // About ellipsoidal height:
        // NOTE if your site metadata has elevation above a geoid model, use this no-value line:
        //pw.append(    " site ellipsoidal height:      \n");
        // ELSE if your site metadata has true ellipsoidal height, use this line:
        pw.append(    " site ellipsoidal height:      "+ heightFormat.format(el.getElevation()) + "" + "\n");

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
     * format a position value double value of latitude LATITUDE input, to degrees(int) and minutes (integer) and DECIMAL seconds as per sinex format.
     * of the form exactly:   LAT -36 36 10.2, in range -90.0 to + 90.0; 
     * LOOK NOTE: show zero for latitude when given  bad values outside that range.
     * example as per SINEX specs: 
        +//SITE/ID
        *CODE PT __DOMES__ T _STATION DESCRIPTION__ APPROX_LON_ APPROX_LAT_ _APP_H_
         ABMF  A 97103M001 P Les Abymes, FR         298 28 20.9  16 15 44.3   -25.6
         ABPO  A 33302M001 P Antananarivo, MG        47 13 45.2 -19  1  5.9  1553.0
         PALV  A 66005M002 P Palmer Station, AQ     295 56 56.0 -64 46 30.3    31.1
         PARK  A 50108M001 P Parkes, AU             148 15 52.6 -32 59 55.5   397.3
     *
     * @param double decimalDegrees 
     *
     * @return a String 
     */
    private String formatLatitudeDMS(double decimalDegrees) {
        // return zero for latitude when given  bad values outside that range.
        //                                                            " -64 46 30.3"
        if ( decimalDegrees<-90.0 || decimalDegrees > 90.0) {  return "   0  0  0.0"; }

        String sdlat=""+decimalDegrees;
        //System.out.println("\n  formatLatitudeDMS.  input value   ="+sdlat);

        // example of converting -15.1014 
        int    degrees = (int) decimalDegrees;  // truncates as desired, to a + or - integer ; -15 in this case

        // make minutes part as fraction of degree; always positive:
        double minsdegfrac = decimalDegrees - degrees;  // like -15.1014 - (-15) = -01014.
        // drop the minux part of the fraction, if any
        if ( minsdegfrac < 0.0) { minsdegfrac *= -1.0; }

        // "minutes part" in units minutes: 
        int minutes = (int) (minsdegfrac * 60);  // like  (int)(0.1014*60) = int(6.084)= 6 minutes 

        String sdeg=""+degrees;
        String smin=""+minutes;
        String add="";
        
        // sdeg should have 4 characters
        if (sdeg.length() ==1 )      { add="   "; }
        else if (sdeg.length() ==2 ) { add="  "; }
        else if (sdeg.length() ==3 ) { add=" "; }
        sdeg=add + sdeg;
        //System.out.println("  formatLatitudeDMS sdeg =_"+sdeg+"__" );
        // smin should have 3 characters
        if (smin.length() ==1 )      { add="  "; }
        else if (smin.length() ==2 ) { add=" "; }
        smin=add+ smin;
        //System.out.println("  formatLatitudeDMS smin =_"+smin+"__" );
        //System.out.println("  formatLatitudeDMS model   _ -64 46 30.3__");
        //System.out.println("  formatLatitudeDMS  dg mn =_"+sdeg+smin+"__" );

        // decimal seconds: compute by (minutes fraction in unit seconds) minus integer minutes in units seconds  
        double seconds = (minsdegfrac*3600 - minutes*60) ;  

        String ssec = secFormat.format(seconds); // one decimal point; but if decimal fraction is zero as in 5.0 then this gives just "5" not 5.0; no good.
        // fix it:
        if (ssec.contains(".") ) { ; }
        else { ssec=ssec+".0"; }
        //  add " " as needed for SINEX column-count format
        if (ssec.length() ==1 )      { add="    "; }
        else if (ssec.length() ==2 ) { add="   "; }
        else if (ssec.length() ==3 ) { add="  "; }
        else if (ssec.length() ==4 ) { add=" "; }
        ssec=add+ssec;
        // if sec is '0'
        //if (sec.length() <=2 ) { sec = sec+".0"; }
        // need an extra leading space if only have a single-digitn integer part of seconds value like 9.9,  using 3 spaces not 4
        //if (seconds<10.0 ) { sec = "  "+sec; }

        String latStr = sdeg + smin + ssec;
        //System.out.println("  formatLatitudeDMS  dg mn =_"+latStr+"__" );
        //return setStringLength(latStr, 11);
        return latStr;
    }

    /**
     * format a position value LONGITUDE to degrees and minutes (integers) and DECIMAL minutes as per sinex format.
     * of the form exactly: LON 274 50  3.8 
     * longitude in range 0.0 to +360.0; NO negative results.
     *
     * @param double decimalDegrees 
     *
     * @return a String 
     */
    private String formatLongitudeDMS(double decimalDegrees) {
        // return zero for latitude when given  bad values outside that range.
        //                                                            " -64 46 30.3"
        if ( decimalDegrees<-360.0 || decimalDegrees >360.00) {  return "   0  0  0.0"; }

        String sdlat=""+decimalDegrees;
        //System.out.println("\n  formatLongitudeDMS.  input value   ="+sdlat);

        // example of converting -15.1014 
        int    degrees = (int) decimalDegrees;  // truncates as desired, to a + or - integer ; -15 in this case

        // make minutes part as fraction of degree; always positive:
        double minsdegfrac = decimalDegrees - degrees;  // like -15.1014 - (-15) = -01014.
        // drop the minux part of the fraction, if any
        if ( minsdegfrac < 0.0) { minsdegfrac *= -1.0; }

        // "minutes part" in units minutes: 
        int minutes = (int) (minsdegfrac * 60);  // like  (int)(0.1014*60) = int(6.084)= 6 minutes 

        String sdeg=""+degrees;
        String smin=""+minutes;
        String add="";
        
        // sdeg should have 4 characters
        if (sdeg.length() ==1 )      { add="   "; }
        else if (sdeg.length() ==2 ) { add="  "; }
        else if (sdeg.length() ==3 ) { add=" "; }
        sdeg=add + sdeg;
        //System.out.println("  formatLongitudeDMS sdeg =_"+sdeg+"__" );
        // smin should have 3 characters
        if (smin.length() ==1 )      { add="  "; }
        else if (smin.length() ==2 ) { add=" "; }
        smin=add+ smin;
        //System.out.println("  formatLongitudeDMS smin =_"+smin+"__" );
        //System.out.println("  formatLongitudeDMS model   _ -164 46 30.3__");
        //System.out.println("  formatLongitudeDMS  dg mn =_"+sdeg+smin+"__" );

        // decimal seconds: compute by (minutes fraction in unit seconds) minus integer minutes in units seconds  
        double seconds = (minsdegfrac*3600 - minutes*60) ;  

        String ssec = secFormat.format(seconds); // one decimal point; but if decimal fraction is zero as in 5.0 then this gives just "5" not 5.0; no good.
        // fix it:
        if (ssec.contains(".") ) { ; }
        else { ssec=ssec+".0"; }
        //  add " " as needed for SINEX column-count format
        if (ssec.length() ==1 )      { add="    "; }
        else if (ssec.length() ==2 ) { add="   "; }
        else if (ssec.length() ==3 ) { add="  "; }
        else if (ssec.length() ==4 ) { add=" "; }
        ssec=add+ssec;
        // if sec is '0'
        //if (sec.length() <=2 ) { sec = sec+".0"; }
        // need an extra leading space if only have a single-digitn integer part of seconds value like 9.9,  using 3 spaces not 4
        //if (seconds<10.0 ) { sec = "  "+sec; }

        String lonStr = sdeg + smin + ssec;
        return lonStr;

        /* orig
        if (decimalDegrees<0.0) { decimalDegrees = decimalDegrees + 360.0; }

        int    degrees = (int) decimalDegrees;  // truncates as desired, to a + or - integer ; -15 in this case
        // minutes part as fraction of degree; always positive:
        double minsdegfrac = decimalDegrees - degrees;  // like |-15.353 - (-15)| = |-0.353| = .353.
        if ( minsdegfrac < 0.0) { minsdegfrac *= -1.0; }
        // "minutes part" in units integer minutes: lll
        int minutes = (int) (minsdegfrac * 60);  // like  (int)(0.647*60) = int(38.82)= 38 minutes 
        // decimal seconds: comp by minutes fraction in unit seconds minus integer minutes in units seconds  
        double seconds = (minsdegfrac*3600 - minutes*60) ;  
        //int isec = int(seconds);
        //int frac = int ( (seconds - isec) *10);  // integer number of tenths of seconds
        String deg = ""+degrees;
        String min = ""+minutes;
        // note minutes is integer; can be '0'; need an extra leading space if only have a minute value like 9.9 using 3 spaces not 4
        if (10>minutes) { min = "  "+min; }

        String sec = secFormat.format(seconds); // one decimal point;  but look it rounds to one decimal , so 15.97 becomes 16 not '16.0'
        // if sec is '0'
        if (sec.length() <=2 ) { sec = sec+".0"; }
        // need an extra leading space if only have a single-digitn integer part of seconds value like 9.9,  using 3 spaces not 4
        if (seconds<10.0 ) { sec = "  "+sec; }
        String s = deg + " " + min + " " + sec;
        // tests s = s + "(from "+decimalDegrees+" minsdegfrac="+minsdegfrac+" minutes="+minutes+"  seconds="+seconds+" deg="+deg+" min="+min +" sec="+sec+")" ;
        return setStringLength(s, 11);
        */
    }



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


                // was pw.append( setStringLength( equipment.getReceiverSerial(),5) +" ");
                    // handle case of value 'unknown' or 'not provided'
                    String answer = equipment.getReceiverSerial();
                    answer = answer.replaceAll(",", " ");
                    if ( answer.contains("unknown") || answer.contains("not provided") || answer.equals("") || answer.equals(" ")) {
                       answer="-----------";
                    }
                    pw.append( setStringLength(answer,5)                            +" " );

                // was pw.append( setStringLengthRight( equipment.getReceiverFirmware(),11));
                    // handle case of value 'unknown' or 'not provided'
                    answer = equipment.getReceiverFirmware();
                    answer = answer.replaceAll(",", " ");
                    if ( answer.contains("unknown") || answer.contains("not provided")  || answer.equals("") || answer.equals(" ") ) {
                       answer="-----------";
                    }
                    pw.append( setStringLength(answer,11)                                 );


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
     * print site antenna block  for all sites
     *
     * @param pw _more_
     * @param site _more_
     *
     * @throws Exception _more_
     */
    private void addSiteEquipmentAntenna(PrintWriter pw, GsacSite site)
            throws Exception {
        List<GsacMetadata> equipmentMetadata =
            site.findMetadata(
                new GsacMetadata.ClassMetadataFinder(GnssEquipment.class));

        int sescount=0;

        for (GsacMetadata metadata : equipmentMetadata) {
            GnssEquipment equipment = (GnssEquipment) metadata;
            sescount +=1;

/*
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
*/

            if (equipment.hasAntenna()) {
                starttime= getNonNullString(myFormatDateTime( equipment.getFromDate()));
                starttime = getSinexTimeFormat(starttime, equipment.getFromDate());
                stoptime= getNonNullString(myFormatDateTime( equipment.getToDate()));
                stoptime = getSinexTimeFormat(stoptime, equipment.getToDate());

                
                String dt = equipment.getDome();
                System.out.println(" addSiteEquipmentAntenna(): initial dome value=_"+dt+"_");
                if (dt.length()<4 || dt==null || dt=="" || dt== " " || dt== "  " || dt== "   " ) {
                   dt="    "; 
                }
                else if ( dt!=null && dt!="" && dt != " " && !dt.contains("   ") ) {
                   dt = getNonNullDomeString(dt);
                }
                

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

                    // for DESCRIPTION use antenna type name plus dome type name(4 char only)
                    // orig pw.append( setStringLengthRight( equipment.getAntenna(),20) +" ");
                    pw.append( setStringLengthRight( equipment.getAntenna(),16) + dt +" ");
                    // FIX above: handle case of values from UNAVCO db of 'unknown' or 'not provided'

                    String answer = equipment.getAntennaSerial();
                    answer = answer.replaceAll(",", " ");
                    if ( answer.contains("unknown") || answer.contains("not provided")  || answer.equals("") || answer.equals(" ") ) {
                       answer="-----------";
                    }
                    pw.append( setStringLength(answer,5) );
                    // was pw.append( setStringLength( equipment.getAntennaSerial(),5) );
                    pw.append("\n");
                }
            }

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
                    ; //  why two antenna sessions with same times?  // don't reprint the same line
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
        /*synchronized (dateTimeFormat) {
            return dateTimeFormat.format(date);
        }*/
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
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
        /*synchronized (dateFormat) {
            return dateFormat.format(date);
        } 
          */
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }


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
          starttime="------------"; // the no data available format
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

    /**
     *  if 's' is null return "    "  value; else return  the input String's', but 4 chars only.
     *
     * @param s  input String object
     *
     * @return  a string
     */
    private String getNonNullDomeString(String s) {
        if (s == null) {
            return "    ";
        }
        String s2         = s; 
        if (s2.length() >5) {
         s2 = s.substring(0,4);        
        }
        return s2;
    }




}
