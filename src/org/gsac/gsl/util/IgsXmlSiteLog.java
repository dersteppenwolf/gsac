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

package org.gsac.gsl.util;


/**
 * Defined values to support generation of IGS XML site logs for GSAC site search results.
 *
 * Note this is a provisional draft for what will be completely new code using online .xsd files for xml creation.
 *
 * @version        2 Mar 2016
 * @author         S K Wier
 * @version        2016 first development; 7 Mar; 
 */

public class IgsXmlSiteLog {

    public static final String TAG_IGSSITELOG = "geo:GeodesyML";

    public static final String TAG_gmdCI_ResponsibleParty    = "gmd.CI_ResponsibleParty";
    public static final String TAG_gcoCharacterString    = "gco:CharacterString";
    public static final String TAG_gmdindividualName    = "gmd:individualName";
    public static final String TAG_gmdorganisationName  = "gmd:organisationName";
    public static final String TAG_gmdrole              = "gmd:role";
    public static final String TAG_gmdCI_RoleCode       = "gmd:CI_RoleCode";
    
    //public static final String TAG_    = "";

    public static final String ATTR_XMLNS_XMLNS = "xmlns";
    public static final String ATTR_XMLNS_REALTIME = "xmlns:realtime";
    public static final String ATTR_XMLNS_EQUIP = "xmlns:equip";
    public static final String ATTR_XMLNS_XSI = "xmlns:xsi";
    public static final String ATTR_XMLNS_MI = "xmlns:mi";
    public static final String ATTR_XMLNS_LI = "xmlns:li";
    public static final String ATTR_XMLNS_CONTACT = "xmlns:contact";
    public static final String ATTR_XSI_SCHEMALOCATION = "xsi:schemaLocation";
    public static final String ATTR_GML1 = "gml:id";

    /** for IGS XML site log formatting */
    public static final String VALUE_GML1 = "GEO_1";
    public static final String XMLNS_XMLNS = "http://www.w3.org/2000/svg"; 
    public static final String TAG_geo_SITEFOURCHARACTERID = "geo:fourCharacterID";
    public static final String TAG_geo_SITENAME              = "geo:siteName";
    public static final String TAG_geo_SITEIDENTIFICATION    = "geo:Site";
    public static final String TAG_geo_SITELOG               = "geo:siteLog";
    public static final String TAG_geo_atSite                = "geo:atSite";
    public static final String TAG_geo_monumentIdentification= "geo:monumentIdentification";
    public static final String TAG_geo_SITEMONUMENT          = "geo:Monument";
    public static final String TAG_geo_FOURCHARACTERID       = "geo:fourCharacterID";
    public static final String TAG_geo_REPORTTYPE = "geo:reportType";
    public static final String TAG_geo_PREPAREDBY = "geo:preparedBy";
    public static final String TAG_geo_DATEPREPARED = "geo:datePrepared";
    public static final String TAG_geo_CDPNUMBER = "geo:cdpNumber";
    public static final String TAG_geo_MONUMENTDESCRIPTION = "geo:monumentDescription";
    public static final String TAG_MONUMENTDESCRIPTION = "MonumentStyle"; // used by OpsXML output to mimic web page labels for one site
    public static final String TAG_geo_HEIGHTOFTHEMONUMENT = "geo:heightOfTheMonument";
    public static final String TAG_geo_MONUMENTFOUNDATION = "geo:monumentFoundation";
    public static final String TAG_geo_MONUMENTINSCRIPTION = "geo:monumentInscription";
    public static final String TAG_geo_IERSDOMESNUMBER = "geo:iersDOMESNumber";
    public static final String TAG_geo_GEOLOGICCHARACTERISTIC = "geo:geologicCharacteristic";

    /** _more_ */
    public static final String TAG_geo_BEDROCKTYPE = "geo:bedrockType";

    /** _more_ */
    public static final String TAG_geo_BEDROCKCONDITION = "geo:bedrockCondition";

    /** _more_ */
    public static final String TAG_geo_FRACTURESPACING = "geo:fractureSpacing";

    /** _more_ */
    public static final String TAG_geo_FAULTZONESNEARBY = "geo:faultZonesNearby";

    /** _more_ */
    public static final String TAG_geo_DISTANCE_ACTIVITY = "geo:distance-Activity";
    public static final String TAG_geo_FOUNDATIONDEPTH = "geo:foundationDepth";
    public static final String TAG_geo_MARKERDESCRIPTION = "geo:markerDescription";
    public static final String TAG_SITELOCATION = "geo:siteLocation";



    /** for GSAC XML formatting, and anyone else who can use one. */
    public static final String TAG_GSACOPSXML       = "GsacOpsXmlSiteInfo";
    public static final String TAG_DATEINSTALLED    = "FromDate"; //dateInstalled;
    public static final String TAG_PUBLISHDATE      = "PublishDate";
    public static final String TAG_MODDATE          = "ModificationDate";  // from the archive db gps3 value "Site modify date" in MV_DAI_PRO.COL_LAST_UPDATED
    public static final String TAG_TODATE           = "ToDate"; // most recent operational date; usually  yesterday (when data was received?) 
    public static final String TAG_SAMPLEINTERVAL   = "SampleInterval";
    public static final String TAG_NETWORK          = "network";
    public static final String TAG_LASTARCHIVEDATE  = "latestArchiveDate";
    public static final String TAG_SITESTATUS       = "Status";

    public static final String TAG_FORMINFORMATION = "formInformation";
    public static final String TAG_SITEBLOCK = "site";
    public static final String TAG_PREPAREDBY = "preparedBy";
    public static final String TAG_DATEPREPARED = "datePrepared";
    public static final String TAG_REPORTTYPE = "reportType";

    public static final String TAG_SITENAME = "Name"; // used by OpsXML output to mimic web page labels for one site
    public static final String TAG_SITETYPE = "Type";

    public static final String TAG_FOURCHARACTERID = "Site"; // used by OpsXML output to mimic web page labels for one site
    public static final String TAG_MONUMENTINSCRIPTION = "monumentInscription";
    public static final String TAG_IERSDOMESNUMBER = "IERSDOMES";
    public static final String TAG_DATAARCHIVEDATE = "latestDataArchiveDate";
    public static final String TAG_CDPNUMBER = "cdpNumber";



    /** _more_ */
    
    /** _more_ */

    /** _more_ */

    /** _more_ */

    /** _more_ */
    public static final String TAG_SITEQUALITY = "siteQuality";

    /** _more_ */
    public static final String TAG_geo_CITY = "geo:city";

    /** _more_ */
    public static final String TAG_CITY =    "city_or_place";

    /** _more_ */
    public static final String TAG_geo_STATE = "geo:state";

    /** _more_ */
    public static final String TAG_STATE =    "state_or_province";

    /** _more_ */
    public static final String TAG_geo_COUNTRY = "geo:country";

    public static final String TAG_COUNTRY =    "country";

    /** _more_ */
    public static final String TAG_geo_TECTONICPLATE = "geo:tectonicPlate";

    /** _more_ */
    public static final String TAG_geo_APPROXIMATEPOSITIONITRF = "geo:approximatePositionITRF";
    public static final String TAG_APPROXIMATEPOSITIONITRF = "approximatePositionITRF";

    /** _more_ */
    public static final String TAG_geo_XCOORDINATEINMETERS = "geo:xCoordinateInMeters";
    public static final String TAG_XCOORDINATEINMETERS = "xCoordinateInMeters";

    /** _more_ */
    public static final String TAG_geo_YCOORDINATEINMETERS = "geo:yCoordinateInMeters";
    public static final String TAG_YCOORDINATEINMETERS = "yCoordinateInMeters";

    /** _more_ */
    public static final String TAG_geo_ZCOORDINATEINMETERS = "geo:zCoordinateInMeters";
    public static final String TAG_ZCOORDINATEINMETERS = "zCoordinateInMeters";

    /** _more_ */
    public static final String TAG_geo_LATITUDE_NORTH = "geo:latitude-North";
    public static final String TAG_LATITUDE_NORTH = "latitude-North";

    /** _more_ */
    public static final String TAG_geo_LONGITUDE_EAST = "geo:longitude-East";
    public static final String TAG_LONGITUDE_EAST = "longitude-East";

    /** _more_ */
    public static final String TAG_geo_ELEVATION_M_ELLIPS = "geo:elevation-m_ellips";

    public static final String TAG_ELEVATION_M_ELLIPS = "ellipsoid_height";

    /** _more_ */
    public static final String TAG_geo_GNSSRECEIVER = "geo:gnssReceiver";

    /** _more_ */
    public static final String TAG_EQUIP_RECEIVERTYPE = "geo:receiverType";

    public static final String TAG_EQUIP_SAMPLEINTERVAL = "equip:sampleInterval";

    /** _more_ */
    public static final String TAG_EQUIP_SATELLITESYSTEM = "geo:satelliteSystem";


    /** _more_ */
    public static final String TAG_EQUIP_FIRMWAREVERSION = "geo:firmwareVersion";

    /** _more_ */
    public static final String TAG_EQUIP_ELEVATIONCUTOFFSETTING = "geo:elevationCutoffSetting";

    /** _more_ */
    public static final String TAG_EQUIP_DATEINSTALLED = "equip:dateInstalled";

    /** _more_ */
    public static final String TAG_EQUIP_DATEREMOVED = "equip:dateRemoved";

    /** _more_ */
    public static final String TAG_EQUIP_TEMPERATURESTABILIZATION = "equip:temperatureStabilization";

    /** _more_ */
    public static final String TAG_EQUIP_NOTES = "equip:notes";

/*
           // Add antenna section
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
            // end Add antenna section
*/
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


    /** _more_ */
    public static final String TAG_FREQUENCYSTANDARD = 
          "frequencyStandard";

    /** _more_ */
    public static final String TAG_COLOCATIONINFORMATION = 
          "colocationInformation";

    /** _more_ */
    public static final String TAG_HUMIDITYSENSOR = "humiditySensor";

    /** _more_ */
    public static final String TAG_PRESSURESENSOR = "pressureSensor";

    /** _more_ */
    public static final String TAG_WATERVAPORSENSOR = "waterVaporSensor";

    /** _more_ */
    public static final String TAG_OTHERINSTRUMENTATION = "otherInstrumentation";

    /** _more_ */
    public static final String TAG_RADIOINTERFERENCES = "radioInterferences";

    /** _more_ */
    public static final String TAG_MULTIPATHSOURCES = "multipathSources";

    /** _more_ */
    public static final String TAG_SIGNALOBSTRUCTIONS = "signalObstructions";

    /** _more_ */
    public static final String TAG_LOCALEPISODICEVENTS = "localEpisodicEvents";

    /** _more_ */
    public static final String TAG_CONTACTAGENCY = "contactAgency";

    /** _more_ */
    public static final String TAG_RESPONSIBLEAGENCY = "responsibleAgency";

    /** _more_ */
    public static final String TAG_REALTIMEDATASTREAMS = "realtimeDataStreams";

    /** _more_ */
    public static final String TAG_MOREINFORMATION = "moreInformation";

    /** _more_ */
    public static final String TAG_REALTIME_DATASTREAMS = "realtime:dataStreams";

    /** _more_ */
    public static final String TAG_REALTIME_SITESTREAM = "realtime:siteStream";

    /** _more_ */
    public static final String TAG_REALTIME_HOSTNAME = "realtime:hostname";

    /** _more_ */
    public static final String TAG_REALTIME_PORT = "realtime:port";

    /** _more_ */
    public static final String TAG_REALTIME_NMEA = "realtime:nmea";

    /** _more_ */
    public static final String TAG_REALTIME_SOLUTION = "realtime:solution";

    /** _more_ */
    public static final String TAG_REALTIME_SAMPINTERVAL = "realtime:sampInterval";

    /** _more_ */
    public static final String TAG_REALTIME_DATAFORMAT = "realtime:dataFormat";

    /** _more_ */
    public static final String TAG_REALTIME_NTRIPPARAMS = "realtime:ntripParams";

    /** _more_ */
    public static final String TAG_REALTIME_NTRIPMOUNT = "realtime:ntripMount";

    /** _more_ */
    public static final String TAG_REALTIME_MOUNTPOINT = "realtime:mountPoint";

    /** _more_ */
    public static final String TAG_REALTIME_SOURCEID = "realtime:sourceID";

    /** _more_ */
    public static final String TAG_REALTIME_COUNTRYCODE = "realtime:countryCode";

    /** _more_ */
    public static final String TAG_REALTIME_NETWORK = "realtime:network";

    /** _more_ */
    public static final String TAG_REALTIME_ALLOWCONNECTIONS = "realtime:allowConnections";

    /** _more_ */
    public static final String TAG_REALTIME_REQUIREAUTHENTICATION = "realtime:requireAuthentication";

    /** _more_ */
    public static final String TAG_REALTIME_ENCRYPTION = "realtime:encryption";

    /** _more_ */
    public static final String TAG_REALTIME_FEESAPPLY = "realtime:feesApply";

    /** _more_ */
    public static final String TAG_REALTIME_BITRATE = "realtime:bitrate";

    /** _more_ */
    public static final String TAG_REALTIME_CARRIERPHASE = "realtime:carrierPhase";

    /** _more_ */
    public static final String TAG_REALTIME_NAVSYSTEM = "realtime:navSystem";

    /** _more_ */
    public static final String TAG_REALTIME_PUBLISHEDSTREAM = "realtime:publishedStream";

    /** _more_ */
    public static final String TAG_REALTIME_IPADDRESS = "realtime:ipAddress";
}

