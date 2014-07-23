/*
 * Copyright 2014 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
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
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.List;
import javax.servlet.*;
import javax.servlet.http.*;


/**
 * Creates GSAC's OpsXmlSiteOutputHandler formatted site search results.
 * This is a new additional GSAC XML format, and more complete and more compact than the original 2010 GSAC XML, XmlSiteOutputHandler.java.  It is for support of fields ops.
 *
 * Designed for the UNAVCO GSAC, not especially for any other GSAC implementation. 
 * usually the call to OpsXmlSiteOutputHandler in gsl/output/Site.java is commented out for use outside of UNAVCO, though anyone may use it.
 *
 * When viewing a xml file made here in a browser you get the message at top:
 * "This XML file does not appear to have any style information associated with it."
 * This means that there's no stylesheet present on how to display the page. Stylesheets are only needed for when you want to view it in a browser.
 * Which is not the case for the intended or primary use of this XML file.
 *
 * @author S K Wier.
 * @version original version of OpsXmlSiteOutputHandler;  12 May 2014; Mods June, July 2014.
 */
public class OpsXmlSiteOutputHandler extends GsacOutputHandler {

    /** output id */
    public static final String OUTPUT_SITE_OPSXML = "siteops.xml";

    private DecimalFormat latLonFormat = new DecimalFormat("####0.####");

    /**  to format elevation or ellipsoidal height values sometimes called elevation in GSAC code.  */
    private DecimalFormat elevationFormat = new DecimalFormat("####0.##");

    private DecimalFormat offsetFormat = new DecimalFormat("####0.####");

    /**
     * ctor
     *
     * @param gsacRepository the repository
     * @param resourceClass _more_
     */
    public OpsXmlSiteOutputHandler(GsacRepository gsacRepository,
                                   ResourceClass resourceClass) {
        super(gsacRepository, resourceClass);
        getRepository().addOutput(getResourceClass(), new GsacOutput(this, OUTPUT_SITE_OPSXML, "GSAC Sites info, Ops XML", "/siteops.xml", true));
    }

    /**
     *  make  a string for the 'Date', in UTC, in ISO 8601 format
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
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String utcTime = sdf.format( date );
        return utcTime;
    }



    /**
     * handle the request
     *
     * @param request the request
     * @param response the response to write to
     *
     *
     * @throws Exception on badness
     */
    public void handleResult(GsacRequest request, GsacResponse response) throws Exception {
        response.startResponse(GsacResponse.MIME_XML);
        PrintWriter pw = response.getPrintWriter();
        pw.append(XmlUtil.XML_HEADER + "\n");

        //Add the first open tag and the 'equip' namespace
        pw.append(XmlUtil.openTag(XmlSiteLog.TAG_GSACOPSXML, XmlUtil.attrs(new String[] { XmlSiteLog.ATTR_XMLNS_EQUIP, XmlSiteLog.XMLNS_XMLNS_EQUIP, })));

        /* in general, about SCHEMALOCATION (not used here): The Java XML parser will read the schemaLocation values and try to load them from the internet, in order to validate the XML file. */
        /* But there is no unavco gsac xml or app xml schema document */

        //Add file contents:
        List<GsacSite> sites = response.getSites();
        for (GsacSite site : sites) {
            //  Call this to ensure that all of the metadata is added to the site
            getRepository().doGetFullMetadata(-1, site);
            //  Add the various content areas
            pw.append(XmlUtil.openTag (XmlSiteLog.TAG_SITEBLOCK)); // new request by Chad Pyatt July 18 2014, this tag.
            addFormInformation(pw);
            addSiteIdentification(pw, site);
            addSiteLocation(pw, site);
            addSiteEquipment(pw, site);
            addSiteStream(pw, site);
            pw.append(XmlUtil.closeTag(XmlSiteLog.TAG_SITEBLOCK));
        }

        pw.append(XmlUtil.closeTag(XmlSiteLog.TAG_GSACOPSXML));

        response.endResponse();
    }


    /**
     * _more_
     *
     * @param pw _more_
     */
    private void addFormInformation(PrintWriter pw) {
        /* such as
          <formInformation>
             <preparedBy>Scripps Orbit and Permanent Array</preparedBy>
             <datePrepared>2011-07-01</datePrepared>
          </formInformation>
        */
        pw.append( XmlUtil.tag( XmlSiteLog.TAG_FORMINFORMATION, "", XmlUtil.tag( XmlSiteLog.TAG_PREPAREDBY, "",
                   getRepository().getRepositoryName()) + XmlUtil.tag( XmlSiteLog.TAG_DATEPREPARED, "", iso8601UTCDateTime(new Date()) )));
                        // old with extra fixed value iso8601UTCDateTime(new Date()) + XmlUtil.tag( XmlSiteLog.TAG_REPORTTYPE, "", "DYNAMIC"))));
    }

    /**
     * _more_
     *
     * @param pw _more_
     * @param site _more_
     *
     * @throws Exception _more_
     */
    private void addSiteIdentification(PrintWriter pw, GsacSite site)
            throws Exception {
        pw.append(XmlUtil.openTag(XmlSiteLog.TAG_SITEIDENTIFICATION));
        // pairs with pw.append(XmlUtil.closeTag(XmlSiteLog.TAG_SITEIDENTIFICATION)); below

        pw.append(XmlUtil.tag(XmlSiteLog.TAG_FOURCHARACTERID, "", removeAndSymbol(site.getShortName()) )   );

        pw.append(XmlUtil.tag(XmlSiteLog.TAG_SITENAME, "", removeAndSymbol(site.getLongName ()) )   );
        // site.getLongName() can break the XML format correctness, if name has a "&" 

        if (site.getType() != null) {
            pw.append( XmlUtil.tag(XmlSiteLog.TAG_SITETYPE, "", site.getType().getName() )); 
        }
        else {
            pw.append( XmlUtil.tag(XmlSiteLog.TAG_SITETYPE, "", "" )); 
        }

        if (site.getStatus() != null) {
            pw.append( XmlUtil.tag(XmlSiteLog.TAG_SITESTATUS, "",  site.getStatus().getName() ) ); 
        }
        else {
            pw.append( XmlUtil.tag(XmlSiteLog.TAG_SITESTATUS, "", "" )); 
        }

        // "from date", the site begin-operations date; in the UNAVCO GSAC, from the archive db gps3 value
        Date date = site.getFromDate();
        if (date != null) {
            pw.append(XmlUtil.tag(XmlSiteLog.TAG_DATEINSTALLED, "", iso8601UTCDateTime(date)));
        }
        else {
            pw.append( XmlUtil.tag(XmlSiteLog.TAG_DATEINSTALLED, "", "")  );
        }

        date = site.getPublishDate();
        if (date != null) {
            pw.append(XmlUtil.tag(XmlSiteLog.TAG_PUBLISHDATE, "", iso8601UTCDateTime(date)));
        }
        else {
            pw.append( XmlUtil.tag(XmlSiteLog.TAG_PUBLISHDATE, "", "")  );
        }

        // "ModificationDate", in the UNAVCO GSAC, from the archive db gps3 value "Site modify date" in MV_DAI_PRO.COL_LAST_UPDATED
        if (site.getModificationDate() != null) {
            String dateString =  iso8601UTCDateTime(site.getModificationDate());  
            //System.out.println("  mod date="+dateString+"_"  );
            pw.append( XmlUtil.tag(XmlSiteLog.TAG_MODDATE, "", dateString)  );
        }
        else {
            pw.append( XmlUtil.tag(XmlSiteLog.TAG_MODDATE, "", "")  );
        }

        // " to date" the last day with data ()); in the UNAVCO GSAC, from the archive db gps3 value
        if (site.getToDate() != null) {
            String dateString =iso8601UTCDateTime (site.getToDate());  
            pw.append( XmlUtil.tag(XmlSiteLog.TAG_TODATE, "", dateString)  );
        }
        else {
            pw.append( XmlUtil.tag(XmlSiteLog.TAG_TODATE, "", "")  );
        }

        // "date of most recent archived data"; in the UNAVCO GSAC from the archive db gps3 value " to date"  CHECK verify this is the right value in the db for the meaning of the shown field.
        if (site.getToDate() != null) {
            String dateString =iso8601UTCDateTime (site.getToDate());  
            pw.append( XmlUtil.tag(XmlSiteLog.TAG_DATAARCHIVEDATE, "", dateString)  );
        }
        else {
            pw.append( XmlUtil.tag(XmlSiteLog.TAG_DATAARCHIVEDATE, "", "")  );
        }

        /* networks in GSAC are stored in the site.getResourceGroups  Networks */
        int gotnet=0;
        List<ResourceGroup> networks = site.getResourceGroups();
        if ( networks != null) {
            //System.out.println("   list of networks size is "+ networks.size() );
            if ( networks.size() >= 1) {
                for (ResourceGroup rg : networks) {
                    if ( rg != null) {
                        //System.out.println("   network is "+rg.getName() );
                        pw.append( XmlUtil.tag(XmlSiteLog.TAG_NETWORK, "", rg.getName() ) );
                        gotnet=1;
                    }
                }
            }
        }
        if (0==gotnet) { 
           pw.append( XmlUtil.tag(XmlSiteLog.TAG_NETWORK, "", "" ) );
        }

        // this line maybe equivalenced by value in the receiver info block
        String var = getProperty( site, GsacExtArgs.ARG_SAMPLE_INTERVAL, "");
        if (var != null && !var.equals(" ") &&  !var.contains("no") ) {
            pw.append( XmlUtil.tag(XmlSiteLog.TAG_SAMPLEINTERVAL, "", var)  );
        }
        else {
            pw.append( XmlUtil.tag(XmlSiteLog.TAG_SAMPLEINTERVAL, "", "" ));
        }

        //pw.append( XmlUtil.tag( XmlSiteLog.TAG_MONUMENTDESCRIPTION, "", getProperty( site, GsacExtArgs.SITE_METADATA_MONUMENTDESCRIPTION, "")));
        var = getProperty( site, GsacExtArgs.SITE_METADATA_MONUMENTDESCRIPTION, "");
        if (var != null && !var.equals(" ") &&  !var.contains("no") ) {
            pw.append( XmlUtil.tag( XmlSiteLog.TAG_MONUMENTDESCRIPTION, "", var ) );
        }
        else {
            pw.append( XmlUtil.tag(XmlSiteLog.TAG_MONUMENTDESCRIPTION, "", "" ));
        }

        //pw.append(XmlUtil.tag(XmlSiteLog.TAG_IERSDOMESNUMBER, "", getProperty(site, GsacExtArgs.SITE_METADATA_IERDOMES, "")));
        var = getProperty( site, GsacExtArgs.SITE_METADATA_IERDOMES, "");
        if (var != null && !var.equals(" ") &&  !var.contains("no") ) {
            pw.append( XmlUtil.tag( XmlSiteLog.TAG_IERSDOMESNUMBER, "", var ) );
        }
        else {
            pw.append( XmlUtil.tag(XmlSiteLog.TAG_IERSDOMESNUMBER, "", "" ));
        }

        pw.append(XmlUtil.closeTag(XmlSiteLog.TAG_SITEIDENTIFICATION));
    }


    /**
     * replace any  "&" in the input string 's' with "and" to prevent contamination of the XML
     *
     * @param s  string to remove & from
     *
     * @return the fixed string 
     */
    private String removeAndSymbol(String s) {
        s = s.replaceAll("&", "and");
        return s;
    }


    /**
     * _more_
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
     * _more_
     *
     * @param pw _more_
     * @param site _more_
     *
     * @throws Exception _more_
     */
    private void addSiteLocation(PrintWriter pw, GsacSite site)
            throws Exception {

        pw.append(XmlUtil.openTag(XmlSiteLog.TAG_SITELOCATION));

        EarthLocation el = site.getEarthLocation();
        pw.append(XmlUtil.tag(XmlSiteLog.TAG_LATITUDE_NORTH, "", formatLocation(el.getLatitude())));
        pw.append(XmlUtil.tag(XmlSiteLog.TAG_LONGITUDE_EAST, "", formatLocation(el.getLongitude())));
        String ellipsoidalheight = elevationFormat.format(el.getElevation()) ;
        pw.append(XmlUtil.tag(XmlSiteLog.TAG_ELEVATION_M_ELLIPS, "", ellipsoidalheight));

        /*
        EarthLocation el = site.getEarthLocation();
        latitude =formatLocation(el.getLatitude())  ;
        longitude =formatLocation(el.getLongitude()) ;

        these next three not needed, following use case shown at top of this file.
        if (el.hasXYZ()) {
            pw.append(XmlUtil.tag(XmlSiteLog.TAG_XCOORDINATEINMETERS, "", el.getX() + ""));
            pw.append(XmlUtil.tag(XmlSiteLog.TAG_YCOORDINATEINMETERS, "", el.getY() + ""));
            pw.append(XmlUtil.tag(XmlSiteLog.TAG_ZCOORDINATEINMETERS, "", el.getZ() + ""));
        } else {
            // no data so show empty tags?
            pw.append(XmlUtil.tag(XmlSiteLog.TAG_XCOORDINATEINMETERS, "",  ""));
            pw.append(XmlUtil.tag(XmlSiteLog.TAG_YCOORDINATEINMETERS, "",  ""));
            pw.append(XmlUtil.tag(XmlSiteLog.TAG_ZCOORDINATEINMETERS, "",  ""));
        }
        */

        List<GsacMetadata> politicalMetadata = site.findMetadata( new GsacMetadata.ClassMetadataFinder( PoliticalLocationMetadata.class));
        PoliticalLocationMetadata plm = null;
        if (politicalMetadata.size() > 0) {
            plm = (PoliticalLocationMetadata) politicalMetadata.get(0);
        }
        if (plm == null) {
            plm = new PoliticalLocationMetadata();
        }
        pw.append(XmlUtil.tag(XmlSiteLog.TAG_COUNTRY, "", getNonNullString(plm.getCountry())));
        pw.append(XmlUtil.tag(XmlSiteLog.TAG_STATE, "", removeAndSymbol(getNonNullString(plm.getState()))  ));
        pw.append(XmlUtil.tag(XmlSiteLog.TAG_CITY, "", getNonNullString(plm.getCity())));

        pw.append(XmlUtil.closeTag(XmlSiteLog.TAG_SITELOCATION));
    }


    /**
     * _more_
     *
     * @param v _more_
     *
     * @return _more_
     */
    private String formatLocation(double v) {
        v = (double) Math.round(v * 10000) / 10000;
        String s = latLonFormat.format(v);

        return s;
    }


    /**
     * _more_
     *
     * @param pw _more_
     * @param site _more_
     *
     * @throws Exception _more_
     */
    private void addSiteEquipment(PrintWriter pw, GsacSite site)
            throws Exception {
        List<GsacMetadata> equipmentMetadata = site.findMetadata( new GsacMetadata.ClassMetadataFinder(GnssEquipment.class));

        for (GsacMetadata metadata : equipmentMetadata) {
            GnssEquipment equipment = (GnssEquipment) metadata;

            // receiver
            if (equipment.hasReceiver()) {
                pw.append(XmlUtil.openTag(XmlSiteLog.TAG_GNSSRECEIVER));
                pw.append(makeTag(XmlSiteLog.TAG_EQUIP_RECEIVERTYPE, "", equipment.getReceiver() ));

                //pw.append(makeTag(XmlSiteLog.TAG_EQUIP_SAMPLEINTERVAL, "", (""+equipment.getSampleInterval()) )); 
                String var = ""+(equipment.getSampleInterval()); // ""+ does float to ascii
                if (var != null && !var.equals("0.0")  ) {
                    pw.append(makeTag(XmlSiteLog.TAG_EQUIP_SAMPLEINTERVAL, "", var ));// float to ascii
                }
                else {
                    pw.append( XmlUtil.tag(XmlSiteLog.TAG_SAMPLEINTERVAL, "", "" ));
                }

                pw.append(makeTag(XmlSiteLog.TAG_EQUIP_SERIALNUMBER, "", equipment.getReceiverSerial()));
                String fwvers =  equipment.getReceiverFirmware();
                if (fwvers.equals("none given")) { fwvers=""; }
                pw.append(makeTag(XmlSiteLog.TAG_EQUIP_FIRMWAREVERSION, "", fwvers ));
                pw.append(makeTag(XmlSiteLog.TAG_EQUIP_DATEINSTALLED, "",iso8601UTCDateTime (equipment.getFromDate())));
                pw.append(makeTag(XmlSiteLog.TAG_EQUIP_DATEREMOVED, "",  iso8601UTCDateTime(equipment.getToDate())));

                String satelliteSystem = equipment.getSatelliteSystem(); 
                if (satelliteSystem.length() == 0) {
                    satelliteSystem = "GPS";
                }
                pw.append(makeTag(XmlSiteLog.TAG_EQUIP_SATELLITESYSTEM, "", satelliteSystem));

                /*
                these next two not needed, following use case shown at top of this file:
                pw.append( makeTag( XmlSiteLog.TAG_EQUIP_ELEVATIONCUTOFFSETTING, "", ""));
                pw.append(makeTag(XmlSiteLog.TAG_EQUIP_NOTES, "", ""));
                */

                pw.append(XmlUtil.closeTag(XmlSiteLog.TAG_GNSSRECEIVER));
            }

            if (equipment.hasAntenna()) {
                pw.append(XmlUtil.openTag(XmlSiteLog.TAG_GNSSANTENNA));

                pw.append(makeTag(XmlSiteLog.TAG_EQUIP_ANTENNATYPE, "", getNonNullString(equipment.getAntenna())));

                pw.append( makeTag( XmlSiteLog.TAG_EQUIP_SERIALNUMBER, "", getNonNullString(equipment.getAntennaSerial())));

                double[] xyz = equipment.getXyzOffset();
                pw.append(makeTag(XmlSiteLog.TAG_EQUIP_MARKER_ARPUPECC,    "", offsetFormat.format(xyz[2])));
                pw.append(makeTag(XmlSiteLog.TAG_EQUIP_MARKER_ARPNORTHECC, "", offsetFormat.format(xyz[1])));
                pw.append(makeTag(XmlSiteLog.TAG_EQUIP_MARKER_ARPEASTECC,  "", offsetFormat.format(xyz[0])));

                /* next not needed, following use case shown at top of this file.
                pw.append( makeTag( XmlSiteLog.TAG_EQUIP_ALIGNMENTFROMTRUENORTH, "", "")); */

                pw.append(makeTag(XmlSiteLog.TAG_EQUIP_ANTENNARADOMETYPE, "", getNonNullString(equipment.getDome())));

                //pw.append( makeTag( XmlSiteLog.TAG_EQUIP_RADOMESERIALNUMBER, "", getNonNullString(equipment.getDomeSerial())));
                String var =  getNonNullString(equipment.getDomeSerial());
                if (var != null && !var.equals(" ") &&  !var.contains("no") ) {
                    pw.append( XmlUtil.tag( XmlSiteLog.TAG_EQUIP_RADOMESERIALNUMBER, "", var ) );
                }
                else {
                    pw.append( XmlUtil.tag(XmlSiteLog.TAG_EQUIP_RADOMESERIALNUMBER, "", "" ));
                }

                /* these next two not needed, following use case shown at top of this file:
                pw.append(makeTag(XmlSiteLog.TAG_EQUIP_ANTENNACABLETYPE, "", ""));
                pw.append(makeTag(XmlSiteLog.TAG_EQUIP_ANTENNACABLELENGTH, "", "")); */

                pw.append(makeTag(XmlSiteLog.TAG_EQUIP_DATEINSTALLED, "", iso8601UTCDateTime(equipment.getFromDate())));
                pw.append(makeTag(XmlSiteLog.TAG_EQUIP_DATEREMOVED, "", iso8601UTCDateTime(equipment.getToDate())));

                pw.append(XmlUtil.closeTag(XmlSiteLog.TAG_GNSSANTENNA));
            }
        }
    }


    /**
     * _more_
     *
     * @param tag _more_
     * @param attrs _more_
     * @param contents _more_
     *
     * @return _more_
     */
    private String makeTag(String tag, String attrs, String contents) {
        if (contents == null || contents.length() == 0) {
            contents = "";
            return XmlUtil.tag(tag, attrs, contents);
        }
        return XmlUtil.tag(tag, attrs, XmlUtil.getCdata(contents));
    }


    /**
     * This adds xml for the real time stream metadata. Only the Unavco GSAC creates that kind of metadata. This comes from the PBO real time stream info.
     *
     * @param pw _more_
     * @param site _more_
     *
     * @throws Exception _more_
     */
    private void addSiteStream(PrintWriter pw, GsacSite site)
            throws Exception {
        GsacMetadata.debug = true;
        //System.err.println("OpsXmlSiteOutputHandler:addSiteStream() ... Finding metadata");
        List<GsacMetadata> streamMetadata =
            site.findMetadata(
                new GsacMetadata.ClassMetadataFinder(StreamMetadata.class));
        GsacMetadata.debug = false;
        int cnt = 0;
        for (GsacMetadata metadata : streamMetadata) {
            StreamMetadata stream = (StreamMetadata) metadata;
            if (cnt == 0) {
                pw.append(
                    XmlUtil.openTag(XmlSiteLog.TAG_REALTIME_DATASTREAMS));
            }
            cnt++;
            stream.encode(pw, this, "appxml");
        }
        if (cnt > 0) {
            pw.append(XmlUtil.closeTag(XmlSiteLog.TAG_REALTIME_DATASTREAMS));
        }
    }


    /**
     * _more_
     *
     * @param tag _more_
     * @param contents _more_
     *
     * @return _more_
     */
    private String makeTag(String tag, String contents) {
        return makeTag(tag, "", contents);
    }


    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    private String getNonNullString(String s) {
        if (s == null) {
            return "";
        }
        return s;
    }

}
