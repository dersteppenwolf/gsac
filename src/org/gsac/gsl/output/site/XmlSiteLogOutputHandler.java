/*
 * Copyright 2010 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
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

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;

import java.util.Hashtable;
import java.util.List;

import javax.servlet.*;
import javax.servlet.http.*;


/**
 * Creates SOPAC's xml site log format
 *
 */
public class XmlSiteLogOutputHandler extends GsacOutputHandler {

    /** output id */
    public static final String OUTPUT_SITE_XMLLOG = "site.xmllog";

    /** date formatter */
    private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");

    /** date formatter */
    private SimpleDateFormat sdf2 =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z");

    /**
     * ctor
     *
     * @param gsacRepository the repository
     * @param resourceClass _more_
     */
    public XmlSiteLogOutputHandler(GsacRepository gsacRepository,
                                   ResourceClass resourceClass) {
        super(gsacRepository, resourceClass);
        getRepository().addOutput(getResourceClass(),
                                  new GsacOutput(this, OUTPUT_SITE_XMLLOG,
                                      "XML Site Log", "/site.xml", true));
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
        pw.append(XmlUtil.XML_HEADER + "\n");

        //Add the open tag with all of the namespaces
        pw.append(XmlUtil.openTag(XmlSiteLog.TAG_IGSSITELOG,
                                  XmlUtil.attrs(new String[] {
            XmlSiteLog.ATTR_XMLNS_XMLNS, XmlSiteLog.XMLNS_XMLNS,
            XmlSiteLog.ATTR_XMLNS_REALTIME, XmlSiteLog.XMLNS_XMLNS_REALTIME,
            XmlSiteLog.ATTR_XMLNS_EQUIP, XmlSiteLog.XMLNS_XMLNS_EQUIP,
            XmlSiteLog.ATTR_XMLNS_XSI, XmlSiteLog.XMLNS_XMLNS_XSI,
            XmlSiteLog.ATTR_XMLNS_MI, XmlSiteLog.XMLNS_XMLNS_MI,
            XmlSiteLog.ATTR_XMLNS_LI, XmlSiteLog.XMLNS_XMLNS_LI,
            XmlSiteLog.ATTR_XMLNS_CONTACT, XmlSiteLog.XMLNS_XMLNS_CONTACT,
            XmlSiteLog.ATTR_XSI_SCHEMALOCATION,
            XmlSiteLog.VALUE_XSI_SCHEMALOCATION,
        })));



        //We can have any number of sites here. Need to figure out how to handle multiple sites
        List<GsacSite> sites = response.getSites();
        for (GsacSite site : sites) {
            //Call this to ensure that all of the metadata is added to the site
            getRepository().doGetFullMetadata(-1, site);
            //Add the various content areas
            addFormInformation(pw);
            addSiteIdentification(pw, site);
            addSiteLocation(pw, site);
            addSiteEquipment(pw, site);
            addSiteStream(pw, site);
        }
        pw.append(XmlUtil.closeTag(XmlSiteLog.TAG_IGSSITELOG));
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
                XmlSiteLog.TAG_FORMINFORMATION, "",
                XmlUtil.tag(
                    XmlSiteLog.TAG_MI_PREPAREDBY, "",
                    getRepository().getRepositoryName()) + XmlUtil.tag(
                        XmlSiteLog.TAG_MI_DATEPREPARED, "",
                        sdf1.format(new Date())) + XmlUtil.tag(
                            XmlSiteLog.TAG_MI_REPORTTYPE, "", "DYNAMIC")));
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

        pw.append(XmlUtil.openTag(XmlSiteLog.TAG_SITEIDENTIFICATION));
        pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_SITENAME, "",
                              site.getLongName()));
        pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_FOURCHARACTERID, "",
                              site.getShortName()));
        Date date = site.getFromDate();
        if (date != null) {
            pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_DATEINSTALLED, "",
                                  sdf2.format(date)));
        }
        pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_MONUMENTINSCRIPTION, "", ""));
        pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_IERSDOMESNUMBER, "", ""));
        pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_CDPNUMBER, "", ""));
        pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_MONUMENTDESCRIPTION, "", ""));
        pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_HEIGHTOFTHEMONUMENT, "", ""));
        pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_MONUMENTFOUNDATION, "", ""));
        pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_FOUNDATIONDEPTH, "", ""));
        pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_MARKERDESCRIPTION, "", ""));
        pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_GEOLOGICCHARACTERISTIC, "",
                              ""));
        pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_BEDROCKTYPE, "", ""));
        pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_BEDROCKCONDITION, "", ""));
        pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_FRACTURESPACING, "", ""));
        pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_FAULTZONESNEARBY, "", ""));
        pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_DISTANCE_ACTIVITY, "", ""));
        pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_NOTES, "", ""));
        pw.append(XmlUtil.closeTag(XmlSiteLog.TAG_SITEIDENTIFICATION));
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
        pw.append(XmlUtil.openTag(XmlSiteLog.TAG_SITELOCATION));

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
        pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_COUNTRY, "",
                              getNonNullString(plm.getCountry())));
        pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_STATE, "",
                              getNonNullString(plm.getState())));
        pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_CITY, "",
                              getNonNullString(plm.getCity())));

        EarthLocation el = site.getEarthLocation();

        pw.append(XmlUtil.openTag(XmlSiteLog.TAG_MI_APPROXIMATEPOSITIONITRF));

        if (el.hasXYZ()) {
            pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_XCOORDINATEINMETERS, "",
                                  el.getX() + ""));
            pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_YCOORDINATEINMETERS, "",
                                  el.getY() + ""));
            pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_ZCOORDINATEINMETERS, "",
                                  el.getZ() + ""));
        } else {
            //What should we do here? Add empty tags?
        }

        pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_LATITUDE_NORTH, "",
                              el.getLatitude() + ""));
        pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_LONGITUDE_EAST, "",
                              el.getLongitude() + ""));
        pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_ELEVATION_M_ELLIPS, "",
                              el.getElevation() + ""));

        pw.append(
            XmlUtil.closeTag(XmlSiteLog.TAG_MI_APPROXIMATEPOSITIONITRF));

        pw.append(XmlUtil.closeTag(XmlSiteLog.TAG_SITELOCATION));
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
            pw.append(XmlUtil.openTag(XmlSiteLog.TAG_GNSSRECEIVER));

            pw.append(makeTag(XmlSiteLog.TAG_EQUIP_RECEIVERTYPE, "",
                              equipment.getReceiver()));
            pw.append(makeTag(XmlSiteLog.TAG_EQUIP_SERIALNUMBER, "",
                              equipment.getReceiverSerial()));
            pw.append(makeTag(XmlSiteLog.TAG_EQUIP_FIRMWAREVERSION, "",
                              equipment.getReceiverFirmware()));
            pw.append(makeTag(XmlSiteLog.TAG_EQUIP_DATEINSTALLED, "",
                              sdf2.format(equipment.getFromDate())));
            pw.append(makeTag(XmlSiteLog.TAG_EQUIP_DATEREMOVED, "",
                              sdf2.format(equipment.getToDate())));

            pw.append(makeTag(XmlSiteLog.TAG_EQUIP_SATELLITESYSTEM, "",
                              "GPS"));
            pw.append(makeTag(XmlSiteLog.TAG_EQUIP_ELEVATIONCUTOFFSETTING,
                              "", ""));
            pw.append(makeTag(XmlSiteLog.TAG_EQUIP_TEMPERATURESTABILIZATION,
                              "", ""));
            pw.append(makeTag(XmlSiteLog.TAG_EQUIP_NOTES, "", ""));
            pw.append(XmlUtil.closeTag(XmlSiteLog.TAG_GNSSRECEIVER));

            pw.append(XmlUtil.openTag(XmlSiteLog.TAG_GNSSANTENNA));

            pw.append(makeTag(XmlSiteLog.TAG_EQUIP_ANTENNATYPE, "",
                              getNonNullString(equipment.getAntenna())));
            pw.append(
                makeTag(
                    XmlSiteLog.TAG_EQUIP_SERIALNUMBER, "",
                    getNonNullString(equipment.getAntennaSerial())));

            pw.append(makeTag(XmlSiteLog.TAG_EQUIP_MARKER_ARPUPECC, "", ""));
            pw.append(makeTag(XmlSiteLog.TAG_EQUIP_MARKER_ARPNORTHECC, "",
                              ""));
            pw.append(makeTag(XmlSiteLog.TAG_EQUIP_MARKER_ARPEASTECC, "",
                              ""));
            pw.append(makeTag(XmlSiteLog.TAG_EQUIP_ALIGNMENTFROMTRUENORTH,
                              "", ""));

            pw.append(makeTag(XmlSiteLog.TAG_EQUIP_ANTENNARADOMETYPE, "",
                              getNonNullString(equipment.getDome())));
            pw.append(makeTag(XmlSiteLog.TAG_EQUIP_RADOMESERIALNUMBER, "",
                              getNonNullString(equipment.getDomeSerial())));

            pw.append(makeTag(XmlSiteLog.TAG_EQUIP_ANTENNACABLETYPE, "", ""));
            pw.append(makeTag(XmlSiteLog.TAG_EQUIP_ANTENNACABLELENGTH, "",
                              ""));

            pw.append(makeTag(XmlSiteLog.TAG_EQUIP_DATEINSTALLED, "",
                              sdf2.format(equipment.getFromDate())));
            pw.append(makeTag(XmlSiteLog.TAG_EQUIP_DATEREMOVED, "",
                              sdf2.format(equipment.getToDate())));
            pw.append(XmlUtil.closeTag(XmlSiteLog.TAG_GNSSANTENNA));




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
        if (contents.length() == 0) {
            return XmlUtil.tag(tag, attrs, contents);
        }
        return XmlUtil.tag(tag, attrs, XmlUtil.getCdata(contents));
    }




    /**
     * _more_
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
        System.err.println("Finding metadata");
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
            stream.encode(pw, this, "xmlsitelog");
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
