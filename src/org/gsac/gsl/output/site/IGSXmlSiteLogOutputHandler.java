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
 * Note this is a provisional draft and really only a place holder for what will be completely new code using online .xsd files for xml creation.
 * This is not operational, complete, or correct.
 *
 * References:

   key sites: 
   https://icsm.govspace.gov.au/egeodesy/geodesyml-0-2-schema/
   code starting point: http://stackoverflow.com/questions/12147428/creating-an-xml-file-from-xsd-from-jaxb/33233061#33233061
   see "After trying for couple of days, eventually i was able to create the xml from xsd properly using the code given below."
 
   creator of the thing: 
   nicholas.brown@ga.gov.au

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

        /*
          item order:

<?xml version="1.0" encoding="UTF-8"?>
done

<geo:GeodesyML gml:id="GEO_1" xmlns:gml="http:// ...
done

    <geo:Site gml:id="SITE_1">
done

    <gmd:CI_ResponsibleParty id="DrJohnDawsonGA">
done

    <geo:Monument gml:id="MONUMENT_1">              BIG lots of real information

    <geo:gnssReceiver gml:id="GNSS_REC_1">    <<<< MULTIPLE

    <geo:gnssAntenna gml:id="GNSS_ANT_2">

    ( dont use     <geo:SiteCertificate gml:id="SITECERT_1">  something for Australian bureaucracy.)

    <geo:Position gml:id="POS_1_H" srsName="http://www.opengis.net/gml/srs/epsg.xml#4283">

    <geo:Position gml:id="POS_1_V" srsName="http://www.opengis.net/gml/srs/epsg.xml#4283">

    <geo:siteLog gml:id="SITELOG_1">    This is BIG.                                                         (yep the site log xml format has one small section called siteLog )
    
    and a bunch more stuff which may be meaningless so far as GSAC data is concerned.

        /* write header lines like this:
<?xml version="1.0" encoding="UTF-8"?>
<geo:GeodesyML gml:id="GEO_1" xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:geo="urn:xml-gov-au:icsm:egeodesy:0.2" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:xlink="http://www.w3.org/1999/xlink" xsi:schemaLocation="urn:xml-gov-au:icsm:egeodesy:0.2 https://icsm.govspace.gov.au/files/2015/09/siteLog.xsd">
    <!--
        @Name MOBS_SiteLog.xml
        @Author Laurence Davies
        @Date 2015-06-03
        @Description: Demonstration file using GeodesyML 0.2 to demonstrate encapsulation of a site, site-log, regulation 13 site certificate, and a national adjustment weekly solution time series. 
    -->
        */

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

            addSiteEquipment_Ant (pw, site);

            addSiteLocation(pw, site);
            //addSiteStream(pw, site);
        }
        pw.append(XmlUtil.closeTag(IgsXmlSiteLog.TAG_IGSSITELOG));
        //pw.append("</geo:GeodesyML>");

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

        //pw.append("\n"); // optional, simply for human readability.

        pw.append(XmlUtil.openTag(IgsXmlSiteLog.TAG_geo_SITEMONUMENT +" gml:id=\"MONUMENT_"+monnumstr +"\"" ));

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
    private void addSiteIdentification(PrintWriter pw, GsacSite site, int sitenumber)
        throws Exception {
        String sitenumstr=""+sitenumber;

        /* do like this
<geo:Site gml:id="SITE_1"><geo:type codeSpace="">CORS</geo:type><geo:Monument xlink:href="#MONUMENT_1"/></geo:Site>i

<gmd:CI_ResponsibleParty id="DrJohnDawsonGA"><gmd:individualName><gco:CharacterString>Dr John Dawson</gco:CharacterString></gmd:individualName><gmd:organisationName><gco:CharacterString>Geoscience Australia</gco:CharacterString></gmd:organisationName><gmd:role><gmd:CI_RoleCode codeListValue="" codeList="">Section Leader - National Geospatial Reference Systems Section</gmd:CI_RoleCode></gmd:role></gmd:CI_ResponsibleParty>
        */
        pw.append("\n"); // optional, simply for human readability.
        pw.append(XmlUtil.openTag(IgsXmlSiteLog.TAG_geo_SITEIDENTIFICATION +" gml:id=\"SITE_"+sitenumstr +"\"" ));
        String stype=""; //"some type"
        String itemNA="Not Available"; 
        String line1="<geo:type codeSpace=\"\">"+itemNA+"</geo:type>";
        pw.append(line1);

        String line2="<geo:Monument xlink:href=\"#MONUMENT_"+sitenumstr+"\"/>";
        pw.append(line2);
        pw.append(XmlUtil.closeTag(IgsXmlSiteLog.TAG_geo_SITEIDENTIFICATION));

        /* do like this
<gmd:CI_ResponsibleParty id="DrJohnDawsonGA"><gmd:individualName><gco:CharacterString>Dr John Dawson</gco:CharacterString></gmd:individualName><gmd:organisationName><gco:CharacterString>Geoscience Australia</gco:CharacterString></gmd:organisationName><gmd:role><gmd:CI_RoleCode codeListValue="" codeList="">Section Leader - National Geospatial Reference Systems Section</gmd:CI_RoleCode></gmd:role></gmd:CI_ResponsibleParty>
         using 
    public static final String TAG_gmdCI_ResponsibleParty    = "gmd.CI_ResponsibleParty";
    public static final String TAG_gmdindividualName    = "gmd:individualName";
    public static final String TAG_gcoCharacterString    = "gco:CharacterString";
    public static final String TAG_gmdorganisationName    = "gmd:organisationName";
    public static final String TAG_gmdrole    = "gmd:role";
    public static final String TAG_gmdCI_RoleCode    = "gmd:CI_RoleCode";
        */
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


        /* old sopac code
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
        */
        // for the <geo:Monument gml:id="MONUMENT_1"> section:

        // notes pw.append(XmlUtil.tag(IgsXmlSiteLog.TAG_geo_NOTES, "", ""));

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
        /* old sopca for, example:
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

        /* IGS XML site log example:


        */

        pw.append("\n"); // FIX for development only

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

        // working elsewhere:
        //city       =getProperty(site, GsacExtArgs.ARG_CITY, "");
        //state      =getProperty(site, GsacExtArgs.ARG_STATE, "");
        //country    =getProperty(site, GsacExtArgs.ARG_COUNTRY, "");
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
    private void addSiteEquipment_Ant (PrintWriter pw, GsacSite site)
        throws Exception {
        List<GsacMetadata> equipmentMetadata = site.findMetadata( new GsacMetadata.ClassMetadataFinder(GnssEquipment.class));

        int counter=0;
        // for each equipment session ("visit") at this site
        for (GsacMetadata metadata : equipmentMetadata) {
            GnssEquipment equipment = (GnssEquipment) metadata;
            counter+=1;

            /*
            // add receiver section
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
            }  // end add receiver section
            */

            /*
            <geo:gnssAntenna gml:id="GNSS_ANT_2">
                <geo:manufacturerSerialNumber>CR20020709</geo:manufacturerSerialNumber>
                <geo:antennaType codeSpace="urn:igs-org:gnss-antenna-model-code" >ASH701945C_M      NONE</geo:antennaType>
                <geo:serialNumber>CR20020709</geo:serialNumber>
                <geo:antennaReferencePoint>BPA</geo:antennaReferencePoint>
                <geo:marker-arpUpEcc.>0.0</geo:marker-arpUpEcc.>
                <geo:marker-arpNorthEcc.>0.0</geo:marker-arpNorthEcc.>
                <geo:marker-arpEastEcc.>0.0</geo:marker-arpEastEcc.>
                <geo:alignmentFromTrueNorth>0</geo:alignmentFromTrueNorth>
                <geo:antennaRadomeType codeSpace="urn:igs-org:gnss-antenna-radome-type">NONE</geo:antennaRadomeType>
                <geo:radomeSerialNumber/>
                <geo:antennaCableType/>
                <geo:antennaCableLength>12</geo:antennaCableLength>
                <geo:dateInstalled>2002-10-21Z</geo:dateInstalled>
                <geo:dateRemoved/>
                <geo:notes/>
            </geo:gnssAntenna>

    public static final String TAG_GNSSANTENNA = "geo:gnssAntenna";
    public static final String TAG_EQUIP_SERIALNUMBER = "geo:manufacturerSerialNumber";
    public static final String TAG_EQUIP_ANTENNATYPE = "geo:antennaType";
    public static final String TAG_geo_SERIALNUMBER = "geo:serialNumber";
    public static final String TAG_EQUIP_ANTENNAREFERENCEPOINT = "geo:antennaReferencePoint";
    public static final String TAG_EQUIP_MARKER_ARPUPECC = "geo:marker-arpUpEcc";
    public static final String TAG_EQUIP_MARKER_ARPNORTHECC = "geo:marker-arpNorthEcc";
    public static final String TAG_EQUIP_MARKER_ARPEASTECC = "geo:marker-arpEastEcc";
    public static final String TAG_EQUIP_ALIGNMENTFROMTRUENORTH = "geo:alignmentFromTrueNorth";
    public static final String TAG_EQUIP_ANTENNARADOMETYPE = "geo:antennaRadomeType";
    public static final String TAG_EQUIP_RADOMESERIALNUMBER = "geo:radomeSerialNumber";
    public static final String TAG_EQUIP_ANTENNACABLETYPE = "geo:antennaCableType";
    public static final String TAG_EQUIP_ANTENNACABLELENGTH = "geo:antennaCableLength";
    public static final String TAG_geo_DATEINSTALLED = "geo:dateInstalled";
    public static final String TAG_geo_DATEREMOVED = "geo:dateRemoved";
    public static final String TAG_geo_NOTES = "geo:notes";

            */

            // Add antenna section
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
