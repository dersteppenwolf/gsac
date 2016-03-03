/*
 * Copyright 2015,2016 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
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
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.List;
import javax.servlet.*;
import javax.servlet.http.*;


/**
 * Creates GSAC site search results in IGS XML site log format, using "GeodesyML."
 *
 * References (as of February '16:
    https://igscb.jpl.nasa.gov/pipermail/igs-dcwg/2015-June/000112.html
    with:
    The latest version of GeodesyML (v0.2): https://icsm.govspace.gov.au/egeodesy/geodesyml-0-2-schema/
     which links to the sample log, MOBS_SiteLog.xml from https://icsm.govspace.gov.au/files/2015/09/MOBS_SiteLog.xml 

    The-Use-of-GeodesyML-to-Encode-IGS-Site-Log-Data_04062015.pdf
    Url : https://igscb.jpl.nasa.gov/pipermail/igs-dcwg/attachments/20150604/e32d991f/attachment-0002.pdf 

    White Paper - Metadata Standard from Global Geodesy.pdf
    https://igscb.jpl.nasa.gov/pipermail/igs-dcwg/attachments/20150604/e32d991f/attachment-0003.pdf 

 *
 */
public class IGSXmlSiteLogOutputHandler extends GsacOutputHandler {

    /** output id */
    public static final String OUTPUT_SITE_IGS_XMLLOG = "site.IgsXmlSitelog";

    /** date formatter */
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /** date formatter */
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z"); // ISO 8601

    /** _more_          */
    private DecimalFormat latLonFormat = new DecimalFormat("####0.####");

    /** _more_          */
    private DecimalFormat elevationFormat = new DecimalFormat("####0.##");

    /** _more_          */
    private DecimalFormat offsetFormat = new DecimalFormat("####0.####");


    /**
     * ctor
     *
     * @param gsacRepository the repository
     * @param resourceClass _more_
     */
    public IGSXmlSiteLogOutputHandler(GsacRepository gsacRepository,
                                   ResourceClass resourceClass) {
        super(gsacRepository, resourceClass);
        //                                                     args: this   ...,                   label on web page Results choice,  [gsac type],     
        getRepository().addOutput(getResourceClass(), new GsacOutput(this, OUTPUT_SITE_IGS_XMLLOG, "IGS XML Site Log",              "/site.igsxmllog", true));
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
    public void handleResult(GsacRequest request, GsacResponse response)
            throws Exception {
        response.startResponse(GsacResponse.MIME_XML);
        PrintWriter pw = response.getPrintWriter();
        // pw.append(XmlUtil.XML_HEADER + "\n"); which is <?xml version="1.0" encoding="ISO-8859-1"?>
        String line1=  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        pw.append(  line1 + "\n"); 

        Date now = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); // ISO 8601 
        System.out.println("GSAC: request for IGS XML site log at time "+ft.format(now)+", from IP "+request.getOriginatingIP() );

        /* make this:
<?xml version="1.0" encoding="UTF-8"?>
<geo:GeodesyML gml:id="GEO_1" xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:geo="urn:xml-gov-au:icsm:egeodesy:0.2" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:xlink="http://www.w3.org/1999/xlink" xsi:schemaLocation="urn:xml-gov-au:icsm:egeodesy:0.2 https://icsm.govspace.gov.au/files/2015/09/siteLog.xsd">
    <!--
        @Name MOBS_SiteLog.xml
        @Author Laurence Davies
        @Date 2015-06-03
        @Description: Demonstration file using GeodesyML 0.2 to demonstrate encapsulation of a site, site-log, regulation 13 site certificate, and a national adjustment weekly solution time series. 
    -->
        */

        //Add the open tag with all of the namespaces
        
        String line2= "<geo:GeodesyML gml:id=\"GEO_1\" xmlns:gml=\"http://www.opengis.net/gml/3.2\" xmlns:geo=\"urn:xml-gov-au:icsm:egeodesy:0.2\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xsi:schemaLocation=\"urn:xml-gov-au:icsm:egeodesy:0.2 https://icsm.govspace.gov.au/files/2015/09/siteLog.xsd\">" ;
        pw.append( line2);

        /*  pw.append(XmlUtil.openTag(IgsXmlSiteLog.TAG_IGSSITELOG,
                                  XmlUtil.attrs(new String[] {
            IgsXmlSiteLog.ATTR_GML1, IgsXmlSiteLog.VALUE_GML1,
            IgsXmlSiteLog.ATTR_XMLNS_XMLNS, IgsXmlSiteLog.XMLNS_XMLNS,
            IgsXmlSiteLog.ATTR_XMLNS_REALTIME, IgsXmlSiteLog.XMLNS_XMLNS_REALTIME,
            IgsXmlSiteLog.ATTR_XMLNS_EQUIP, IgsXmlSiteLog.XMLNS_XMLNS_EQUIP,
            IgsXmlSiteLog.ATTR_XMLNS_XSI, IgsXmlSiteLog.XMLNS_XMLNS_XSI,
            IgsXmlSiteLog.ATTR_XMLNS_MI, IgsXmlSiteLog.XMLNS_XMLNS_MI,
            IgsXmlSiteLog.ATTR_XMLNS_LI, IgsXmlSiteLog.XMLNS_XMLNS_LI,
            IgsXmlSiteLog.ATTR_XMLNS_CONTACT, IgsXmlSiteLog.XMLNS_XMLNS_CONTACT,
            IgsXmlSiteLog.ATTR_XSI_SCHEMALOCATION, IgsXmlSiteLog.VALUE_XSI_SCHEMALOCATION
        }))); */


        //pw.append( ' <!--' +'\n' +'      @Name MOBS_SiteLog.xml' +'\n' +'      @Author some name'' +'\n' +'      @Date 2015-06-03' +'\n' +'      @Description: made by GSAC web services' +'\n' +'  -->' );


        //"We can have any number of sites here. Need to figure out how to handle multiple sitea" - J MW
        List<GsacSite> sites = response.getSites();
        for (GsacSite site : sites) {
            //Call this to ensure that all of the metadata is added to the site
            getRepository().doGetFullMetadata(-1, site);
            //Add the various content areas
            // not in igs wml log addFormInformation(pw);
            //addSiteIdentification(pw, site);
            addSiteLocation(pw, site);
            //addSiteEquipment(pw, site);
            //addSiteStream(pw, site);
        }
        //pw.append(XmlUtil.closeTag(IgsXmlSiteLog.TAG_IGSSITELOG));
        pw.append("</geo:GeodesyML>");
        //Done
        response.endResponse();
    }


    /**
     * _more_
     *
     * @param pw _more_
     */
    private void addFormInformation(PrintWriter pw) {
        /*
          <formInformation>
          <mi:preparedBy>Scripps Orbit and Permanent Array</mi:preparedBy>
          <mi:datePrepared>2011-07-01</mi:datePrepared>
          <mi:reportType>DYNAMIC</mi:reportType>
          </formInformation>
        */
        pw.append(
            XmlUtil.tag(
                IgsXmlSiteLog.TAG_FORMINFORMATION, "",
                XmlUtil.tag(
                    IgsXmlSiteLog.TAG_geo_PREPAREDBY, "",
                    getRepository().getRepositoryName()) + XmlUtil.tag(
                        IgsXmlSiteLog.TAG_geo_DATEPREPARED, "",
                        myFormatDate(new Date())) + XmlUtil.tag(
                            IgsXmlSiteLog.TAG_geo_REPORTTYPE, "", "DYNAMIC")));
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
        /*
          <siteIdentification>
          <mi:siteName>TresPiedraNM2006</mi:siteName>
          <mi:fourCharacterID>P123</mi:fourCharacterID>
          <mi:monumentInscription/>
          <mi:iersDOMESNumber/>
          <mi:cdpNumber/>
          <mi:monumentDescription>DEEP-DRILLED BRACED</mi:monumentDescription>
          <mi:heightOfTheMonument> (m)</mi:heightOfTheMonument>
          <mi:monumentFoundation/>
          <mi:foundationDepth> (m)</mi:foundationDepth>
          <mi:markerDescription>NONE</mi:markerDescription>
          <mi:dateInstalled>2006-02-27T00:00:00Z</mi:dateInstalled>
          <mi:geologicCharacteristic/>
          <mi:bedrockType/>
          <mi:bedrockCondition/>
          <mi:fractureSpacing/>
          <mi:faultZonesNearby/>
          <mi:distance-Activity/>
          <mi:notes/>
          </siteIdentification>
        */

        pw.append(XmlUtil.openTag(IgsXmlSiteLog.TAG_SITEIDENTIFICATION));
        pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_SITENAME, "",
                              // site.getLongName())); can fail if name has a "&"
                              removeAndSymbol(site.getLongName ()) )   );
        pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_FOURCHARACTERID, "",
                              removeAndSymbol(site.getShortName()) )   );
        Date date = site.getFromDate();
        if (date != null) {
            pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_DATEINSTALLED, "",
                                  myFormatDateTime(date)));
        }
        pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_MONUMENTINSCRIPTION, "", ""));
        pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_IERSDOMESNUMBER, "",
                              getProperty(site,
                                          GsacExtArgs.SITE_METADATA_IERDOMES,
                                          "")));
        pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_CDPNUMBER, "",
                              getProperty(site,
                                          GsacExtArgs.SITE_METADATA_CDPNUM,
                                          "")));
        pw.append(
            XmlUtil.tag(
                IgsXmlSiteLog.TAG_geo_MONUMENTDESCRIPTION, "",
                getProperty(
                    site, GsacExtArgs.SITE_METADATA_MONUMENTDESCRIPTION,
                    "")));
        pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_HEIGHTOFTHEMONUMENT, "", ""));
        pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_MONUMENTFOUNDATION, "", ""));
        pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_FOUNDATIONDEPTH, "", ""));
        pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_MARKERDESCRIPTION, "", ""));
        pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_GEOLOGICCHARACTERISTIC, "",
                              ""));
        pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_BEDROCKTYPE, "", ""));
        pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_BEDROCKCONDITION, "", ""));
        pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_FRACTURESPACING, "", ""));
        pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_FAULTZONESNEARBY, "", ""));
        pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_DISTANCE_ACTIVITY, "", ""));
        pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_NOTES, "", ""));
        pw.append(XmlUtil.closeTag(IgsXmlSiteLog.TAG_SITEIDENTIFICATION));
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
        /*
          <siteLocation>
          <mi:city>Tres Piedras</mi:city>
          <mi:state>New Mexico</mi:state>
          <mi:country>United States</mi:country>
          <mi:tectonicPlate>North America</mi:tectonicPlate>
          <mi:approximatePositionITRF>
          <mi:xCoordinateInMeters>-1405300.44</mi:xCoordinateInMeters>
          <mi:yCoordinateInMeters>-4929803.00</mi:yCoordinateInMeters>
          <mi:zCoordinateInMeters>3786420.59</mi:zCoordinateInMeters>
          <mi:latitude-North>+363813.92</mi:latitude-North>
          <mi:longitude-East>-1055460.00</mi:longitude-East>
          <mi:elevation-m_ellips>2411.2</mi:elevation-m_ellips>
          </mi:approximatePositionITRF>
          <mi:notes/>
          </siteLocation>
        */
        pw.append(XmlUtil.openTag(IgsXmlSiteLog.TAG_SITELOCATION));

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
        pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_COUNTRY, "",
                              getNonNullString(plm.getCountry())));
        pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_STATE, "",
                              removeAndSymbol(getNonNullString(plm.getState()))  ));
        pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_CITY, "",
                              getNonNullString(plm.getCity())));

        EarthLocation el = site.getEarthLocation();

        pw.append(XmlUtil.openTag(IgsXmlSiteLog.TAG_geo_APPROXIMATEPOSITIONITRF));

        if (el.hasXYZ()) {
            pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_XCOORDINATEINMETERS, "",
                                  el.getX() + ""));
            pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_YCOORDINATEINMETERS, "",
                                  el.getY() + ""));
            pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_ZCOORDINATEINMETERS, "",
                                  el.getZ() + ""));
        } else {
            //What should we do here? Add empty tags?
        }

        pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_LATITUDE_NORTH, "",
                              formatLocation(el.getLatitude())));
        pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_LONGITUDE_EAST, "",
                              formatLocation(el.getLongitude())));

        // About ellipsoidal height:
        // could show zero value here if you have elevation above a geoid, not true ellipsoid heights:
        //pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_ELEVATION_M_ELLIPS, "",
         //                     elevationFormat.format(el.getElevation())));
        pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_ELEVATION_M_ELLIPS, "", elevationFormat.format( 0.0 )));

        pw.append(
            XmlUtil.closeTag(IgsXmlSiteLog.TAG_geo_APPROXIMATEPOSITIONITRF));

        pw.append(XmlUtil.closeTag(IgsXmlSiteLog.TAG_SITELOCATION));
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
        /*
          <gnssReceiver>
          <equip:receiverType>TRIMBLE NETRS</equip:receiverType>
          <equip:satelliteSystem>GPS</equip:satelliteSystem>
          <equip:serialNumber>4545260798</equip:serialNumber>
          <equip:firmwareVersion>1.1-2 19 Apr 2005</equip:firmwareVersion>
          <equip:elevationCutoffSetting>0 deg</equip:elevationCutoffSetting>
          <equip:dateInstalled>2006-02-28T00:00:00.000Z</equip:dateInstalled>
          <equip:dateRemoved>2010-07-08T20:53:00.000Z</equip:dateRemoved>
          <equip:temperatureStabilization/>
          <equip:notes/>
          </gnssReceiver>
        */
        List<GsacMetadata> equipmentMetadata =
            site.findMetadata(
                new GsacMetadata.ClassMetadataFinder(GnssEquipment.class));
        for (GsacMetadata metadata : equipmentMetadata) {
            GnssEquipment equipment = (GnssEquipment) metadata;
            if (equipment.hasReceiver()) {
                pw.append(XmlUtil.openTag(IgsXmlSiteLog.TAG_GNSSRECEIVER));
                pw.append(makeTag(IgsXmlSiteLog.TAG_EQUIP_RECEIVERTYPE, "",
                                  equipment.getReceiver()));
                pw.append(makeTag(IgsXmlSiteLog.TAG_EQUIP_SERIALNUMBER, "",
                                  equipment.getReceiverSerial()));
                pw.append(makeTag(IgsXmlSiteLog.TAG_EQUIP_FIRMWAREVERSION, "",
                                  equipment.getReceiverFirmware()));
                pw.append(makeTag(IgsXmlSiteLog.TAG_EQUIP_DATEINSTALLED, "",
                                  myFormatDateTime(equipment.getFromDate())));
                pw.append(makeTag(IgsXmlSiteLog.TAG_EQUIP_DATEREMOVED, "",
                                  myFormatDateTime(equipment.getToDate())));
                String satelliteSystem = equipment.getSatelliteSystem();
                if (satelliteSystem == null) {
                    satelliteSystem = "GPS";
                    System.err.println("GSAC: IGSXmlSiteLogOutputHandler:addSiteEquip() "+site.getLongName()+" satelliteSystem is null ");
                }
                else if (satelliteSystem.length() == 0) {
                    satelliteSystem = "GPS";
                    System.err.println("GSAC: IGSXmlSiteLogOutputHandler:addSiteEquip() "+site.getLongName()+" satelliteSystem is empty ''  ");
                }

                pw.append(makeTag(IgsXmlSiteLog.TAG_EQUIP_SATELLITESYSTEM, "",
                                  satelliteSystem));
                pw.append(
                    makeTag(
                        IgsXmlSiteLog.TAG_EQUIP_ELEVATIONCUTOFFSETTING, "", ""));
                pw.append(
                    makeTag(
                        IgsXmlSiteLog.TAG_EQUIP_TEMPERATURESTABILIZATION, "",
                        ""));
                pw.append(makeTag(IgsXmlSiteLog.TAG_EQUIP_NOTES, "", ""));
                pw.append(XmlUtil.closeTag(IgsXmlSiteLog.TAG_GNSSRECEIVER));
            }

            if (equipment.hasAntenna()) {
                pw.append(XmlUtil.openTag(IgsXmlSiteLog.TAG_GNSSANTENNA));

                pw.append(makeTag(IgsXmlSiteLog.TAG_EQUIP_ANTENNATYPE, "",
                                  getNonNullString(equipment.getAntenna())));
                pw.append(
                    makeTag(
                        IgsXmlSiteLog.TAG_EQUIP_SERIALNUMBER, "",
                        getNonNullString(equipment.getAntennaSerial())));

                double[] xyz = equipment.getXyzOffset();
                pw.append(makeTag(IgsXmlSiteLog.TAG_EQUIP_MARKER_ARPUPECC, "",
                                  offsetFormat.format(xyz[2])));
                pw.append(makeTag(IgsXmlSiteLog.TAG_EQUIP_MARKER_ARPNORTHECC,
                                  "", offsetFormat.format(xyz[1])));
                pw.append(makeTag(IgsXmlSiteLog.TAG_EQUIP_MARKER_ARPEASTECC, "",
                                  offsetFormat.format(xyz[0])));
                pw.append(
                    makeTag(
                        IgsXmlSiteLog.TAG_EQUIP_ALIGNMENTFROMTRUENORTH, "", ""));

                pw.append(makeTag(IgsXmlSiteLog.TAG_EQUIP_ANTENNARADOMETYPE, "",
                                  getNonNullString(equipment.getDome())));
                pw.append(
                    makeTag(
                        IgsXmlSiteLog.TAG_EQUIP_RADOMESERIALNUMBER, "",
                        getNonNullString(equipment.getDomeSerial())));

                pw.append(makeTag(IgsXmlSiteLog.TAG_EQUIP_ANTENNACABLETYPE, "",
                                  ""));
                pw.append(makeTag(IgsXmlSiteLog.TAG_EQUIP_ANTENNACABLELENGTH,
                                  "", ""));

                pw.append(makeTag(IgsXmlSiteLog.TAG_EQUIP_DATEINSTALLED, "",
                                  myFormatDateTime(equipment.getFromDate())));
                pw.append(makeTag(IgsXmlSiteLog.TAG_EQUIP_DATEREMOVED, "",
                                  myFormatDateTime(equipment.getToDate())));
                pw.append(XmlUtil.closeTag(IgsXmlSiteLog.TAG_GNSSANTENNA));

            }


        }

    }


    /**
     * ISO 8601 date time format
     *
     * @param date _more_
     *
     * @return _more_
     */
    private String myFormatDateTime(Date date) {
        if (date == null) {
            return "";
        }
        /* synchronized (dateTimeFormat) {
            return dateTimeFormat.format(date);
        } */
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
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
        if (date == null) {
            return "";
        }
        /*synchronized (dateFormat) {
            return dateFormat.format(date);
        }*/
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return formatter.format(date);
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
     * This adds xml for the real time stream metadata. Only the Unavco GSAC creates that kind of metadata. This comes from the PBO real time stream info._
     *
     * @param pw _more_
     * @param site _more_
     *
     * @throws Exception _more_
     */
    private void addSiteStream(PrintWriter pw, GsacSite site)
            throws Exception {
        /*
          <realtime:publishedStream>
              <realtime:ipAddress>132.239.152.74</realtime:ipAddress>
              <realtime:port>6005</realtime:port>
              <realtime:sampInterval>1</realtime:sampInterval>
              <realtime:dataFormat>RTCM_3.0</realtime:dataFormat>
              <realtime:ntripParams>
              <realtime:mountPoint>WHYT0</realtime:mountPoint>
              <realtime:sourceID>WHTY:Lake Forest, CA</realtime:sourceID>
              <realtime:countryCode>USA</realtime:countryCode>
              <realtime:network>OCRTN</realtime:network>
              <realtime:allowConnections>true</realtime:allowConnections>
              <realtime:requireAuthentication>true</realtime:requireAuthentication>
              <realtime:encryption>false</realtime:encryption>
              <realtime:feesApply>false</realtime:feesApply>
              <realtime:bitrate>8000</realtime:bitrate>
              <realtime:carrierPhase>L1+L2</realtime:carrierPhase>
              <realtime:navSystem>GPS</realtime:navSystem>
              <realtime:nmea></realtime:nmea>
              <realtime:solution></realtime:solution>
              </realtime:ntripParams>
              <realtime:startDate/>
          </realtime:publishedStream>
        */
        GsacMetadata.debug = true;
        //System.err.println("IGSXmlSiteLogOutputHandler:addSiteStream() ... Finding metadata");
        List<GsacMetadata> streamMetadata =
            site.findMetadata(
                new GsacMetadata.ClassMetadataFinder(StreamMetadata.class));
        GsacMetadata.debug = false;
        int cnt = 0;
        for (GsacMetadata metadata : streamMetadata) {
            StreamMetadata stream = (StreamMetadata) metadata;
            if (cnt == 0) {
                pw.append(
                    XmlUtil.openTag(IgsXmlSiteLog.TAG_REALTIME_DATASTREAMS));
            }
            cnt++;
            stream.encode(pw, this, "xmlsitelog");
        }
        if (cnt > 0) {
            pw.append(XmlUtil.closeTag(IgsXmlSiteLog.TAG_REALTIME_DATASTREAMS));
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
