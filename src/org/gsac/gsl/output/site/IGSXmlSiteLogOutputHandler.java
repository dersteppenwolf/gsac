/*
 * Copyright 2016 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
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
 * Incomplete, provisional, and experimental code for GSAC site search results in IGS XML site log format, using "GeodesyML."
 * Not for for operational use.  Will be replaced in part by new Java code created for this purpose by IGS or its associates.
 *
 * March 9 2016
 *
 * Note this is a provisional draft and really only a place holder for what will be completely new code using online .xsd files for xml creation.
 * This is not operational, complete, or correct.
 *

          for future development, see this code starting point: http://stackoverflow.com/questions/12147428/creating-an-xml-file-from-xsd-from-jaxb/33233061#33233061
          see "After trying for couple of days, eventually i was able to create the xml from xsd properly using the code given below."
 
   See: 

   creator of the IGS XML format: 
   nicholas.brown@ga.gov.au

   References:

   https://icsm.govspace.gov.au/egeodesy/geodesyml-0-2-schema/
 
   a sample log, MOBS_SiteLog.xml from https://icsm.govspace.gov.au/files/2015/09/MOBS_SiteLog.xml 

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
        // debug only System.out.println("GSAC: request for IGS XML site log at time "+ft.format(now)+", from IP "+request.getOriginatingIP() );

        //Add the open tag with all of the namespaces
        String line2= "<geo:GeodesyML gml:id=\"GEO_1\" xmlns:gml=\"http://www.opengis.net/gml/3.2\" xmlns:geo=\"urn:xml-gov-au:icsm:egeodesy:0.2\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xsi:schemaLocation=\"urn:xml-gov-au:icsm:egeodesy:0.2 https://icsm.govspace.gov.au/files/2015/09/siteLog.xsd\">" ;
        pw.append( line2);

        String filelabel= " ";
        pw.append( " <!--\n      Provisional IGS XML site log. Not for operational use. Not complete.\n      The Geodesy ML is defined in https://icsm.govspace.gov.au/egeodesy/\n\n      @Name "+filelabel+"\n      @Author Made by GSAC web services at "+getRepository().getRepositoryName() +"\n      @Date "+myFormatDate(new Date())+ "\n      @Description:\n  -->" );

        //"We can have any number of sites here. Need to figure out how to handle multiple sites" - J MW
        List<GsacSite> sites = response.getSites();
        int sitenumber = 0;
        for (GsacSite site : sites) {
            sitenumber +=1;
            //Call this to ensure that all of the metadata is added to the site
            getRepository().doGetFullMetadata(-1, site);

            //Add the various content areas

            addSiteIdentification(pw, site, sitenumber);

            int monumentnumber=sitenumber; // unless a site has several monuments
            addMonument(pw, site, monumentnumber);

            addSiteEquipment_Receiver (pw, site);

            addSiteEquipment_Ant (pw, site);

            addSiteLog (pw, site, sitenumber); // which calls 
        }

        pw.append(XmlUtil.closeTag(IgsXmlSiteLog.TAG_IGSSITELOG));

        response.endResponse();
    }


    /**
     * _more_
     *
     * @param pw _more_
     */
    private void addFormInformation(PrintWriter pw) {
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
     * makes some of an "IGS XML GNSS site log" formatted file.
     *
     * @param pw _more_
     * @param site _more_
     *
     * @throws Exception _more_
     */
    private void addMonument(PrintWriter pw, GsacSite site, int monnumber)
        throws Exception {
        String monnumstr=""+monnumber;

        pw.append   (XmlUtil.openTag(IgsXmlSiteLog.TAG_geo_SITEMONUMENT +" gml:id=\"MONUMENT_"+monnumstr +"\"" ));
        //pw.append(XmlUtil.closeTag(IgsXmlSiteLog.TAG_geo_SITEMONUMENT));

        String itemNA="Not Available"; 
        String item=""; //some value 

        String nextline="<gml:description>"+itemNA+"</gml:description>";
        pw.append(nextline);

        item= site.getLongName (); //getProperty(site, GsacArgs.ARG_SITE_NAME, "");
        nextline="<gml:name codeSpace=\"urn:ga-gov-au:monument-siteName\">"+item+"</gml:name>";
        pw.append(nextline);

        item= site.getShortName (); //getProperty(site, GsacArgs.ARG_SITE_CODE, "");
        nextline="<gml:name codeSpace=\"urn:ga-gov-au:monument-fourCharacterID\">"+item+"</gml:name>";
        pw.append(nextline);

        item=  getProperty(site, GsacExtArgs.SITE_METADATA_IERDOMES, ""); //getProperty(site, GsacExtArgs.SITE_METADATA_IERDOMES, "");
        if (item.length() == 0) {
              item=itemNA;
        }
        nextline="<gml:name codeSpace=\"urn:ga-gov-au:monument-iersDOMESNumber\">"+item+"</gml:name>";
        pw.append(nextline);

        nextline="<gml:name codeSpace=\"urn:ga-gov-au:monument-cdpNumber\">"+itemNA+"</gml:name>";
        pw.append(nextline);

        nextline="<geo:type codeSpace=\"urn:ga-gov-au:monument-type\">"+itemNA+"</geo:type>";
        pw.append(nextline);

        // "from date", the site's begin-operations date (as in the UNAVCO GSAC from the db gps3 value)
        //SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); // ISO 8601 
        nextline="<geo:installedDate></geo:installedDate>";
        Date date = site.getFromDate();
        if (date != null) {
           SimpleDateFormat ft2 = new SimpleDateFormat("yyyy-MM-dd"); // ISO 8601 
           item = ft2.format(date);
           nextline="<geo:installedDate>"+item+"</geo:installedDate>";
        }
        pw.append(nextline);

        // only one of these parameters are available from any known GSAC:
        item=        getProperty( site, GsacExtArgs.SITE_METADATA_MONUMENTDESCRIPTION, "");
        nextline="<geo:remarks></geo:remarks><geo:inscription/><geo:monumentDescription codeSpace=\"urn:ga-gov-au:monument-description\">"+item+"</geo:monumentDescription><geo:height uomLabels=\"m\"></geo:height><geo:foundation codeSpace=\"urn:ga-gov-au:monument-foundation\"></geo:foundation><geo:foundationDepth uomLabels=\"m\"></geo:foundationDepth><geo:markerDescription></geo:markerDescription><geo:geologicCharacteristic codeSpace=\"urn:ga-gov-au:monument-geologicCharacteristic\"></geo:geologicCharacteristic><geo:bedrockType codeSpace=\"urn:ga-gov-au:monument-bedrockType\"></geo:bedrockType><geo:bedrockCondition codeSpace=\"urn:ga-gov-au:monument-bedrockCondition\"></geo:bedrockCondition><geo:fractureSpacing codeSpace=\"urn:ga-gov-au:monument-fractureSpacing\"></geo:fractureSpacing><geo:faultZonesNearby codeSpace=\"urn:ga-gov-au:monument-faultZonesNearby\"></geo:faultZonesNearby>";
        pw.append(nextline);

        pw.append(XmlUtil.closeTag(IgsXmlSiteLog.TAG_geo_SITEMONUMENT));
    }  // end of add Monument


    /**
     * makes some of an "IGS XML GNSS site log" formatted file.
     *
     * @param pw _more_
     * @param site _more_
     *
     * @throws Exception _more_
     */
    private void addSiteLog(PrintWriter pw, GsacSite site, int sitenumber)
        throws Exception {
        String sitenumstr=""+sitenumber;
        // example:
        /*
        <geo:siteLog gml:id="SITELOG_1">
          <geo:atSite xlink:href="#SITE_1"/>  
          <geo:formInformation><geo:preparedBy/><geo:datePrepared/><geo:reportType/></geo:formInformation>
          <geo:monumentIdentification xlink:href="#MONUMENT_1"/>
        */

        pw.append(XmlUtil.openTag(IgsXmlSiteLog.TAG_geo_SITELOG+" gml:id=\"SITELOG_"+sitenumstr +"\""));

        pw.append(XmlUtil.openTag (IgsXmlSiteLog.TAG_geo_atSite+" xlink:href=\"#SITE_"+sitenumstr +"\""));
        pw.append(XmlUtil.closeTag(IgsXmlSiteLog.TAG_geo_atSite));
        //pw.append(XmlUtil.openTag(IgsXmlSiteLog.TAG_geo_atSite+" xlink:href=\"#SITE_"+sitenumstr +"\"/>"));

        String line3="<geo:formInformation><geo:preparedBy/><geo:datePrepared/><geo:reportType/></geo:formInformation>";
        pw.append(line3);

        // only one line: again nuttin:
        pw.append("<geo:monumentIdentification xlink:href=\"#MONUMENT_"+sitenumstr +"\"/>" );
        //pw.append(XmlUtil.closeTag(IgsXmlSiteLog.TAG_geo_SITEMONUMENT));

        addSiteLocation(pw, site);

        // LOTs more boiler plate goes here...

        pw.append(XmlUtil.closeTag(IgsXmlSiteLog.TAG_geo_SITELOG));

     }


    /**
     * makes some of an "IGS XML GNSS site log" formatted file.
     *
     * @param pw _more_
     * @param site _more_
     *
     * @throws Exception _more_
     */
    private void addSiteIdentification(PrintWriter pw, GsacSite site, int sitenumber)
        throws Exception {
        String sitenumstr=""+sitenumber;

        pw.append("\n"); // optional, simply for human readability.
        pw.append(XmlUtil.openTag(IgsXmlSiteLog.TAG_geo_SITEIDENTIFICATION +" gml:id=\"SITE_"+sitenumstr +"\"" ));
        String stype=""; //"some type"
        String itemNA="Not Available"; 
        String line1="<geo:type codeSpace=\"\">"+itemNA+"</geo:type>";
        pw.append(line1);

        String line2="<geo:Monument xlink:href=\"#MONUMENT_"+sitenumstr+"\"/>";
        pw.append(line2);
        pw.append(XmlUtil.closeTag(IgsXmlSiteLog.TAG_geo_SITEIDENTIFICATION));

        String na="Not Available";
        String rpname=na; // not available from std gsac
        String name2= na; // not available from std gsac
        String rolename= na; // not available from std gsac
        
        String      agencyname =getProperty(site, GsacExtArgs.SITE_METADATA_NAMEAGENCY, ""); 

        pw.append(XmlUtil.openTag (IgsXmlSiteLog.TAG_gmdCI_ResponsibleParty +" id=\""+ rpname +"\"" ));

        pw.append(XmlUtil.openTag (IgsXmlSiteLog.TAG_gmdindividualName));
        pw.append(XmlUtil.openTag (IgsXmlSiteLog.TAG_gcoCharacterString));
        pw.append(name2);
        pw.append(XmlUtil.closeTag(IgsXmlSiteLog.TAG_gcoCharacterString));
        pw.append(XmlUtil.closeTag(IgsXmlSiteLog.TAG_gmdindividualName));

        pw.append(XmlUtil.openTag (IgsXmlSiteLog.TAG_gmdorganisationName));
        pw.append(XmlUtil.openTag (IgsXmlSiteLog.TAG_gcoCharacterString));
        pw.append(agencyname);
        pw.append(XmlUtil.closeTag(IgsXmlSiteLog.TAG_gcoCharacterString));
        pw.append(XmlUtil.closeTag(IgsXmlSiteLog.TAG_gmdorganisationName));

        pw.append(XmlUtil.openTag (IgsXmlSiteLog.TAG_gmdrole));
        pw.append(XmlUtil.openTag (IgsXmlSiteLog.TAG_gmdCI_RoleCode +"  codeListValue=\"\" codeList=\"\" "));
        pw.append(rolename);
        pw.append(XmlUtil.closeTag(IgsXmlSiteLog.TAG_gmdCI_RoleCode));
        pw.append(XmlUtil.closeTag(IgsXmlSiteLog.TAG_gmdrole));

        pw.append(XmlUtil.closeTag(IgsXmlSiteLog.TAG_gmdCI_ResponsibleParty));
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
            (List<GsacMetadata>) site.findMetadata( new GsacMetadata.ClassMetadataFinder(PropertyMetadata.class));

        for (int i = 0; i < propertyMetadata.size(); i++) {
            PropertyMetadata metadata = (PropertyMetadata) propertyMetadata.get(i);
            if (metadata.getName().equals(propertyId)) { return metadata.getValue();
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

        pw.append(XmlUtil.openTag(IgsXmlSiteLog.TAG_SITELOCATION));

        List<GsacMetadata> politicalMetadata =
            site.findMetadata(
                new GsacMetadata.ClassMetadataFinder(
                    PoliticalLocationMetadata.class));
        PoliticalLocationMetadata plm = null;

        // should be only one item returned from:
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

        String Xstr       =getProperty(site, GsacExtArgs.SITE_TRF_X, "");
        String Ystr       =getProperty(site, GsacExtArgs.SITE_TRF_Y, "");
        String Zstr       =getProperty(site, GsacExtArgs.SITE_TRF_Z, "");
        //mondesc    =getProperty(site, GsacExtArgs.SITE_METADATA_MONUMENTDESCRIPTION, "");

        // show x,y,z
        //if (el.hasXYZ()) {
            pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_XCOORDINATEINMETERS, "", Xstr + ""));
                                  // was el.getX() + ""));
            pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_YCOORDINATEINMETERS, "", Ystr + ""));
                                  //el.getY() + ""));
            pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_ZCOORDINATEINMETERS, "", Zstr + ""));
                                  //el.getZ() + ""));
        //} 

        // show latitude longitude height
        pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_LATITUDE_NORTH, "", formatLocation(el.getLatitude())));
        pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_LONGITUDE_EAST, "", formatLocation(el.getLongitude())));
        pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_ELEVATION_M_ELLIPS, "", elevationFormat.format(el.getElevation())));

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

            /*

    private void addSiteEquipment_Rec (PrintWriter pw, GsacSite site)
        throws Exception {
            System.err.println("GSAC: IGSXmlSiteLogOutputHandler: find equip ");
        List<GsacMetadata> equipmentMetadata = site.findMetadata( new GsacMetadata.ClassMetadataFinder(GnssEquipment.class));
            System.err.println("GSAC: IGSXmlSiteLogOutputHandler: find metadata ");
        int counter=0;
        // for each equipment session ("visit") at this site
        for (GsacMetadata metadata : equipmentMetadata) {
            GnssEquipment equipment = (GnssEquipment) metadata;
            counter+=1;

            System.err.println("GSAC: IGSXmlSiteLogOutputHandler: find  receiver ");

            String value="";
            //if (equipment.hasAntenna()) {
               <geo:gnssReceiver gml:id="GNSS_REC_1">
TAG_geo_GNSSRECEIVER
                    <geo:manufacturerSerialNumber>ZR220012001</geo:manufacturerSerialNumber>
same as ant TAG_EQUIP_SERIALNUMBER = "geo:manufacturerSerialNumber";
                    <geo:receiverType codeSpace="urn:igs-org:gnss-receiver-model-code">ASHTECH UZ-12</geo:receiverType>
TAG_EQUIP_RECEIVERTYPE = "geo:receiverType";
                    <geo:satelliteSystem>GPS</geo:satelliteSystem>
TAG_EQUIP_SATELLITESYSTEM = "geo:satelliteSystem";
                    <geo:serialNumber>ZR220012001</geo:serialNumber>
same as ant TAG_geo_SERIALNUMBER = "geo:serialNumber";
                    <geo:firmwareVersion>CK00</geo:firmwareVersion>
TAG_EQUIP_FIRMWAREVERSION = "geo:firmwareVersion";
                    <geo:elevationCutoffSetting>0</geo:elevationCutoffSetting>
TAG_EQUIP_ELEVATIONCUTOFFSETTING = "geo:elevationCutoffSetting";
                    <geo:dateInstalled>2006-01-24T00:00:00Z</geo:dateInstalled>
                    <geo:dateRemoved>2007-06-20T23:59:00Z</geo:dateRemoved>
    public static final String TAG_geo_DATEINSTALLED = "geo:dateInstalled";
    public static final String TAG_geo_DATEREMOVED = "geo:dateRemoved";

                    <geo:temperatureStabilization>none</geo:temperatureStabilization>
                    <geo:notes/>
                </geo:gnssReceiver>
            */

    private void addSiteEquipment_Receiver (PrintWriter pw, GsacSite site)
            throws Exception {
        List<GsacMetadata> equipmentMetadata = site.findMetadata( new GsacMetadata.ClassMetadataFinder(GnssEquipment.class));

        int counter=0;
        String value="";

        for (GsacMetadata metadata : equipmentMetadata) {
            GnssEquipment equipment = (GnssEquipment) metadata;

            // receiver
            if (equipment.hasReceiver()) {
                System.err.println("GSAC: IGSXmlSiteLogOutputHandler:addSiteEquip_Rec has receiver ");
                counter+=1;

                pw.append(XmlUtil.openTag(IgsXmlSiteLog.TAG_geo_GNSSRECEIVER +" gml:id=\"GNSS_REC_"+counter+"\"" ));

                value=getNonNullString(equipment.getReceiverSerial()); 
                pw.append(makeTag(IgsXmlSiteLog.TAG_EQUIP_SERIALNUMBER, "", value));
                                 // equipment.getReceiverSerial()));

                value=getNonNullString(equipment.getReceiver()); 
                pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_EQUIP_RECEIVERTYPE, "", value));

                value=getNonNullString(equipment.getReceiver()); 
                pw.append(makeTag(IgsXmlSiteLog.TAG_EQUIP_RECEIVERTYPE, "", value));
                               
                String satelliteSystem = equipment.getSatelliteSystem();
                if (satelliteSystem == null) {
                    satelliteSystem = "Not Available";
                }
                else if (satelliteSystem.length() <= 0) {
                    satelliteSystem = "Not Available";
                }
                pw.append(makeTag(IgsXmlSiteLog.TAG_EQUIP_SATELLITESYSTEM, "", satelliteSystem));

                value=getNonNullString(equipment.getReceiverSerial()); 
                pw.append(makeTag(IgsXmlSiteLog.TAG_EQUIP_SERIALNUMBER, "", value));
                                 // equipment.getReceiverSerial()));

                pw.append(makeTag(IgsXmlSiteLog.TAG_EQUIP_FIRMWAREVERSION, "",
                                  equipment.getReceiverFirmware()));

                pw.append(makeTag(IgsXmlSiteLog.TAG_geo_DATEINSTALLED, "",
                                  myFormatDateTime(equipment.getFromDate())));

                pw.append(makeTag(IgsXmlSiteLog.TAG_geo_DATEREMOVED, "",
                                  myFormatDateTime(equipment.getToDate())));

                pw.append( makeTag( IgsXmlSiteLog.TAG_EQUIP_ELEVATIONCUTOFFSETTING, "", ""));

                pw.append( makeTag( IgsXmlSiteLog.TAG_EQUIP_TEMPERATURESTABILIZATION, "", ""));

                pw.append(makeTag(IgsXmlSiteLog.TAG_EQUIP_NOTES, "", ""));

                pw.append(XmlUtil.closeTag(IgsXmlSiteLog.TAG_geo_GNSSRECEIVER));
                System.err.println("GSAC: IGSXmlSiteLogOutputHandler:addSiteEquip_Rec did add receiver ");
            }  // end add receiver section

        }
    } // end addSiteEquipment_Rec 

    /**
     * _more_
     *
     * @param pw _more_
     * @param site _more_
     *
     * @throws Exception _more_
     */
    private void addSiteEquipment_Ant (PrintWriter pw, GsacSite site)
        throws Exception {
        List<GsacMetadata> equipmentMetadata = site.findMetadata( new GsacMetadata.ClassMetadataFinder(GnssEquipment.class));

        int counter=0;
        // for each equipment session ("visit") at this site
        for (GsacMetadata metadata : equipmentMetadata) {
            GnssEquipment equipment = (GnssEquipment) metadata;
            counter+=1;

            // Add antenna 
            String value="";
            if (equipment.hasAntenna()) {
                pw.append(XmlUtil.openTag(IgsXmlSiteLog.TAG_GNSSANTENNA +" gml:id=\"GNSS_ANT_"+counter+"\"" ));

                value=""; 
                pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_EQUIP_SERIALNUMBER, "", value));

                value=getNonNullString(equipment.getAntenna()); 
                pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_EQUIP_ANTENNATYPE, "", value));

                //value=       getNonNullString(equipment.getAntenna()); // sample code lines
                //pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_, "", value));

                value=       getNonNullString(equipment.getAntennaSerial());
                pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_SERIALNUMBER, "", value));

                pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_EQUIP_ANTENNAREFERENCEPOINT, "", ""));

                double[] xyz = equipment.getXyzOffset();
                pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_EQUIP_MARKER_ARPUPECC, "", 
                      offsetFormat.format(xyz[2]) ));
                pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_EQUIP_MARKER_ARPNORTHECC, "", 
                      offsetFormat.format(xyz[1]) ));
                pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_EQUIP_MARKER_ARPEASTECC, "", 
                      offsetFormat.format(xyz[0]) ));

                pw.append( makeTag( IgsXmlSiteLog.TAG_EQUIP_ALIGNMENTFROMTRUENORTH, "", ""));

                pw.append(makeTag(IgsXmlSiteLog.TAG_EQUIP_ANTENNARADOMETYPE, "",
                                  getNonNullString(equipment.getDome())));

                pw.append( makeTag( IgsXmlSiteLog.TAG_EQUIP_RADOMESERIALNUMBER, "",
                        getNonNullString(equipment.getDomeSerial())));

                pw.append(makeTag(IgsXmlSiteLog.TAG_EQUIP_ANTENNACABLETYPE, "", ""));

                pw.append(makeTag(IgsXmlSiteLog.TAG_EQUIP_ANTENNACABLELENGTH, "", ""));

                pw.append(makeTag(IgsXmlSiteLog.TAG_geo_DATEINSTALLED, "",
                                  myFormatDateTime(equipment.getFromDate())));

                pw.append(makeTag(IgsXmlSiteLog.TAG_geo_DATEREMOVED, "",
                                  myFormatDateTime(equipment.getToDate())));

                pw.append(makeTag(IgsXmlSiteLog.TAG_geo_NOTES, "", ""));

                pw.append(XmlUtil.closeTag(IgsXmlSiteLog.TAG_GNSSANTENNA));
            }
            // end Add antenna section
        }
    } // end addSiteEquipment_Ant 


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
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
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



/*
    / **
     * This adds xml for the real time stream metadata. Only the Unavco GSAC creates that kind of metadata. This comes from the PBO real time stream info._
     *
     * @param pw _more_
     * @param site _more_
     *
     * @throws Exception _more_
     * /
    private void addSiteStream(PrintWriter pw, GsacSite site)
            throws Exception {
        / *
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
        * /
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
*/


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
