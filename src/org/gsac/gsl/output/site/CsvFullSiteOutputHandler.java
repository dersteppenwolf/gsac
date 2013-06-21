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

import java.io.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.ArrayList;
import java.util.Date;
import java.lang.Double;
import java.util.Hashtable;
import java.util.List;
import javax.servlet.*;
import javax.servlet.http.*;


/* 
 * Class description: formats GSAC query results to write a csv file format, with a long list of geodesy parameters.
 *
 * This GSAC csv format has:
 * id+"," +name+"," +latitude+","+longitude+","+ellipsoidalheight+","+mondesc+","+iersdomes+","+  
 * starttime+"," +stoptime+","+anttype+"," +dome+"," +antsn+"," +antht+"," +antn+"," +ante+"," +rectype+"," +firmvers+"," +recsn+","+sitecount+"\n");
 * The fields are separated with commas. Leading and trailing space-characters adjacent to comma field separators are ignored.
 * NO commas are allowed here inside a field value; this usually pertains to only site descriptions in geodesy data. Any commas in such a field are replaced with " ".
 * The first record in a CSV file may be a header record containing column (field) names; and is so in this case.
 * 
 *      To conform with other GSAC repositories we ask you not to revise this format.  You are very welcome to make a new similar but altered 
 *      handler .java class for your use.  Add its call to the class file SiteManager.java and rebuild GSAC.  Do not commit your core 
 *      GSAC code changes in thsi case into GSAC without consulting UNAVCO.
 *      For bug reports and suggested improvments please contact UNAVCO.
 *
 * version Nov 30, 2012. SKW
 * revised Dec 7, 2012 SKW.
 * revised, new name to avoid conflict with another similar class now reinstituted. Feb. 25 2013. SKW.
 */
public class CsvFullSiteOutputHandler extends GsacOutputHandler {

    String id ="";
    String name ="";
    String country= "";
    String state ="";
    String city   ="";
    String latitude ="";
    String longitude ="";
    String ellipsoidalheight ="";
    String Xcoordinate="";
    String Ycoordinate="";
    String Zcoordinate="";
    String mondesc ="";
    String iersdomes ="";
    String cdpnum ="";
    String indate ="";
    int sitecount=0;

    /** output id */
    public static final String OUTPUT_SITE_FULL_CSV = "site.csv.full";

    /** date formatter  note NO "T", and we do not know if it is UTC time or what.*/
    /* somehow the Z here results in a value like "2001-07-11 00:00:00 -0600" with no Z */
    private SimpleDateFormat dateTimeFormatnoT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

    /** formatter for GNSS antenna value of offset of phase center from instrument center */
    private DecimalFormat offsetFormat = new DecimalFormat("####0.####");

    /** _more_          */
    private DecimalFormat latLonFormat = new DecimalFormat("####0.####");

    /**  to format elevation or ellipsoidal height values sometimes called elevation in GSAC code.  */
    private DecimalFormat elevationFormat = new DecimalFormat("####0.##");

    /**
     * replace any commas in the input string 's' with " " to prevent contamination of the csv data line with non-separator commas. 
     *
     * @param s  string to remove commas from
     *
     * @return _more_
     */
    private String cleanString(String s) {
        s = s.replaceAll(",", " ");
        // or could replace with s = s.replaceAll(",", "_COMMA_");
        return s;
    }

    /**
     * ctor; and make file name for result file.
     *
     * @param gsacRepository the repository
     * @param resourceClass _more_
     */
    public CsvFullSiteOutputHandler (GsacRepository gsacRepository, ResourceClass resourceClass) {
        super(gsacRepository, resourceClass);
        getRepository().addOutput(getResourceClass(),
                                  new GsacOutput(this, OUTPUT_SITE_FULL_CSV,
                                      "GSAC station info, csv (full)", "/fullsites.csv", true)); 
                                      //  use .txt to allow seeing the result in the brower window.
                                      // if you use .csv some browswers want to shove the results in Excel which is very un-useful.
                                  /*  'name in browser menu" ,  result  "file name extension" */
    }

    /**
     * handle the request: format the sites' information in  csv lines per site and per equipment session
     *
     * @param request the request
     * @param response the response to write to
     *
     * @throws Exception on badness
     */
    public void handleResult(GsacRequest request, GsacResponse response)
            throws Exception {
        //This sets output mime type (how browser handles it; text lets user see query results in a browser, and can also get form the gsac client with file name sites.csv)
        // BUT  in this case where file extesion is ".csv"  the browser probably would ignore this 'text' value and try to load the file in some doc processor like Excel.
        response.startResponse("text");
        PrintWriter pw = response.getPrintWriter();
        addHeader(pw);
        //Get all the sites in the results (response) from the GSAC site query made by the user: 
        List<GsacSite> sites = response.getSites();
        sitecount=0;
        //For each site:
        for (GsacSite site : sites) {
            sitecount++;
            //Call this to ensure that all of the metadata is added to the site
            getRepository().doGetFullMetadata(-1, site);
            addSiteIdentification(pw, site);
            addSiteLocation(pw, site);
            addSiteEquipment(pw, site);
        }
        response.endResponse();
    }


    /**
     *  header for this case.
     *
     * @param pw _more_
     */
    private void addHeader (PrintWriter pw) {

        // About ellipsoidal height:
        // choose use of elevation or ellipsoid height
        // use this header line:
        //pw.append("ID,station name,latitude,longitude,ellipsoidal height,monument description,IERSDOMES,db record start date,db record stop date,antenna type,dome type,antenna SN,Ant dZ,Ant dN,Ant dE,receiver type, firmware version,receiver SN,site count\n");
        // OR this line, if you have eleation above a goid, not ellipsoid height:
        pw.append("ID,station name,latitude,longitude,elevation,monument description,IERSDOMES,db record start date,db record stop date,antenna type,dome type,antenna SN,Ant dZ,Ant dN,Ant dE,receiver type, firmware version,receiver SN,site count\n");
        //pw.append("#  missing times may mean 'equipment still in operation;' for other missing values see previous or next site's session. \n");
    }

    /**
     * get site id details for this format style
     *
     * @param pw _more_
     * @param site _more_
     *
     * @throws Exception _more_
     */
    private void addSiteIdentification(PrintWriter pw, GsacSite site)
            throws Exception {
        id = site.getShortName();
        name = cleanString( site.getLongName() ); // cleanString removes unwanted commas in the name which mess up the csv line
        mondesc =getProperty(site, GsacExtArgs.SITE_METADATA_MONUMENTDESCRIPTION, "");
        iersdomes =getProperty(site, GsacExtArgs.SITE_METADATA_IERDOMES, "");
        // these next two often missing and not printed out below
        cdpnum =getProperty(site, GsacExtArgs.SITE_METADATA_CDPNUM, "");
        Date date = site.getFromDate();
        if (date != null) { indate = myFormatDateTime(date); }
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
        country= getNonNullString(plm.getCountry())  ;
        state  = getNonNullString(plm.getState()  ) ;
        city   =getNonNullString(plm.getCity()) ;

        EarthLocation el = site.getEarthLocation();
        latitude =formatLocation(el.getLatitude())  ;
        longitude =formatLocation(el.getLongitude()) ;
        ellipsoidalheight =elevationFormat.format(el.getElevation()) ;
        if (el.hasXYZ()) {
            Xcoordinate= Double.toString(el.getX() );
            Ycoordinate= Double.toString(el.getY() );
            Zcoordinate= Double.toString(el.getZ() );
        } else {
            Xcoordinate="";
            Ycoordinate="";
            Zcoordinate="";
        }

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
    String starttime ="";
    String stoptime ="";
    String antht ="";
    String antn ="";
    String ante ="";
    String anttype ="";
    String dome ="";
    String antsn ="";
    String rectype ="";
    String firmvers ="";
    String recsn ="";

        List<GsacMetadata> equipmentMetadata =
            site.findMetadata(
                new GsacMetadata.ClassMetadataFinder(GnssEquipment.class));
        for (GsacMetadata metadata : equipmentMetadata) {
            GnssEquipment equipment = (GnssEquipment) metadata;

            if (equipment.hasReceiver()) {
                starttime= getNonNullString(myFormatDateTime( equipment.getFromDate()));
                stoptime= getNonNullString(myFormatDateTime( equipment.getToDate()));
                rectype=equipment.getReceiver() ;
                recsn=equipment.getReceiverSerial();
                firmvers=equipment.getReceiverFirmware();
            }

            if (equipment.hasAntenna()) {
                double[] xyz = equipment.getXyzOffset();
                antht = offsetFormat.format(xyz[2]);
                if (antht.equals("0")) { antht = "0.0000"; }
                if (antht == null) { antht = "0.0000"; }
                antn = offsetFormat.format(xyz[1]);
                if (antn.equals("0")) { antn = "0.0000"; }
                if (antn == null) { antn = "0.0000"; }
                ante = offsetFormat.format(xyz[0]);
                if (ante.equals("0")) { ante = "0.0000"; }
                if (ante == null) { ante = "0.0000"; }
                anttype=getNonNullString(equipment.getAntenna());
                antsn  =getNonNullString(equipment.getAntennaSerial());
                dome = getNonNullString(equipment.getDome());
                starttime= getNonNullString(myFormatDateTime( equipment.getFromDate()));
                stoptime= getNonNullString(myFormatDateTime( equipment.getToDate()));
                /* others possible
                pw.append( _ALIGNMENTFROMTRUENORTH, "", ""));
                pw.append(EQUIP_ANTENNACABLETYPE, "",
                pw.append(EQUIP_ANTENNACABLELENGTH,
                */
            }

            // construct the csv file line for this session at a site:
            // these often lacking: cdpnum+","+indate+","+ 
            pw.append(id+"," +name+"," +latitude+","+longitude+","+ellipsoidalheight+","+mondesc+","+iersdomes+","+   
                starttime+"," +stoptime+","+anttype+"," +dome+"," +antsn+"," +antht+"," +antn+"," +ante+"," +rectype+"," +firmvers+"," +recsn+","+sitecount+"\n");

        } // end for loop on sessions
    }     // end addSiteEquipment


    /**
     * _more_
     *
     * @param date _more_
     *
     * @return _more_
     */
    private String myFormatDateTime(Date date) {
        if (date == null) { return ""; }
        synchronized (dateTimeFormatnoT) {
            return dateTimeFormatnoT.format(date);
        }
    }

    /**
     *  if 's' is null return ""; else return 's'.
     *
     * @param s  input String object
     *
     * @return  a string
     */
    private String getNonNullString(String s) {
        if (s == null) {
            return "";
        }
        return s;
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

}
