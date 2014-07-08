/*
 * Copyright 2010, 2014 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
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
 * Class description
 *
 *
 * @version        Wed. May 19, '10, for XML site log format.
 * @author         J McWhirter
 * @version        21 Apr 2104.  more items added for improved GSAC XML site log format
 * @author         S K Wier
 */

public class XmlSiteLog {

    /** _more_ */
    public static final String ATTR_XMLNS_XMLNS = "xmlns";
    /** _more_ */
    public static final String XMLNS_XMLNS = "http://sopac.ucsd.edu/ns/geodesy/doc/igsSiteLog/2011";

    /** _more_ */
    public static final String ATTR_XMLNS_REALTIME = "xmlns:realtime";

    /** _more_ */
    public static final String ATTR_XMLNS_EQUIP = "xmlns:equip";

    /** _more_ */
    public static final String ATTR_XMLNS_XSI = "xmlns:xsi";

    /** _more_ */
    public static final String ATTR_XMLNS_MI = "xmlns:mi";

    /** _more_ */
    public static final String ATTR_XMLNS_LI = "xmlns:li";

    /** _more_ */
    public static final String ATTR_XMLNS_CONTACT = "xmlns:contact";

    /** _more_ */
    public static final String ATTR_XSI_SCHEMALOCATION = "xsi:schemaLocation";

    public static final String ATTR_GSAC_SCHEMALOCATION = "schemaLocation";
    /** _more_ */
    public static final String VALUE_XSI_SCHEMALOCATION =
        "http://sopac.ucsd.edu/ns/geodesy/doc/igsSiteLog/2011  http://sopac.ucsd.edu/ns/geodesy/doc/igsSiteLog/2011/igsSiteLog.xsd";

    public static final String VALUE_GSAC_SCHEMALOCATION = "";

    /** _more_ */
    public static final String XMLNS_XMLNS_CONTACT = "http://sopac.ucsd.edu/ns/geodesy/doc/igsSiteLog/contact/2004";

    /** _more_ */
    public static final String XMLNS_XMLNS_EQUIP = "http://sopac.ucsd.edu/ns/geodesy/doc/igsSiteLog/equipment/2004";

    /** _more_ */
    public static final String XMLNS_XMLNS_LI = "http://sopac.ucsd.edu/ns/geodesy/doc/igsSiteLog/localInterferences/2004";

    /** _more_ */
    public static final String XMLNS_XMLNS_MI = "http://sopac.ucsd.edu/ns/geodesy/doc/igsSiteLog/monumentInfo/2004";

    /** _more_ */
    public static final String XMLNS_XMLNS_REALTIME =
        "http://sopac.ucsd.edu/ns/geodesy/doc/igsSiteLog/realtimeDataInfo/2011";

    /** _more_ */
    public static final String XMLNS_XMLNS_XSI = "http://www.w3.org/2001/XMLSchema-instance";

    /** _more_ */
    public static final String TAG_IGSSITELOG = "igsSiteLog";

    /** for GSAC XML formatting */
    public static final String TAG_GSACOPSXML       = "GsacOpsXmlSiteInfo";
    public static final String TAG_DATEINSTALLED    = "FromDate"; //dateInstalled;
    public static final String TAG_PUBLISHDATE      = "PublishDate";
    public static final String TAG_MODDATE          = "ModificationDate";  // from the archive db gps3 value "Site modify date" in MV_DAI_PRO.COL_LAST_UPDATED
    public static final String TAG_TODATE           = "ToDate"; // most recent operational date; usually  yesterday (when data was received?) 
    public static final String TAG_SAMPLEINTERVAL   = "SampleInterval";
    public static final String TAG_NETWORK          = "network";
    public static final String TAG_LASTARCHIVEDATE  = "latestArchiveDate";
    public static final String TAG_SITESTATUS       = "Status";

    /** _more_ */
    public static final String TAG_FORMINFORMATION = "formInformation";

    /** _more_ */
    public static final String TAG_MI_PREPAREDBY = "mi:preparedBy";
    public static final String TAG_PREPAREDBY = "preparedBy";

    /** _more_ */
    public static final String TAG_MI_DATEPREPARED = "mi:datePrepared";
    public static final String TAG_DATEPREPARED = "datePrepared";

    /** _more_ */
    public static final String TAG_MI_REPORTTYPE = "mi:reportType";
    public static final String TAG_REPORTTYPE = "reportType";

    /** _more_ */
    public static final String TAG_SITEIDENTIFICATION = "siteIdentification";

    /** _more_ */
    public static final String TAG_MI_SITENAME = "mi:siteName";
    public static final String TAG_SITENAME = "Name"; // used by OpsXML output to mimic web page labels for one site

    public static final String TAG_SITETYPE = "Type";

    /** _more_ */
    public static final String TAG_MI_FOURCHARACTERID = "mi:fourCharacterID";
    public static final String TAG_FOURCHARACTERID = "Site"; // used by OpsXML output to mimic web page labels for one site

    /** _more_ */
    public static final String TAG_MI_MONUMENTINSCRIPTION = "mi:monumentInscription";
    public static final String TAG_MONUMENTINSCRIPTION = "monumentInscription";

    /** _more_ */
    public static final String TAG_MI_IERSDOMESNUMBER = "mi:iersDOMESNumber";
    public static final String TAG_IERSDOMESNUMBER = "IERSDOMES";

    public static final String TAG_DATAARCHIVEDATE = "latestDataArchiveDate";

    /** _more_ */
    public static final String TAG_MI_CDPNUMBER = "mi:cdpNumber";
    public static final String TAG_CDPNUMBER = "cdpNumber";

    /** _more_ */
    public static final String TAG_MI_MONUMENTDESCRIPTION = "mi:monumentDescription";
    public static final String TAG_MONUMENTDESCRIPTION = "MonumentStyle"; // used by OpsXML output to mimic web page labels for one site

    /** _more_ */
    public static final String TAG_MI_HEIGHTOFTHEMONUMENT = "mi:heightOfTheMonument";

    /** _more_ */
    public static final String TAG_MI_MONUMENTFOUNDATION = "mi:monumentFoundation";

    /** _more_ */
    public static final String TAG_MI_FOUNDATIONDEPTH = "mi:foundationDepth";

    /** _more_ */
    public static final String TAG_MI_MARKERDESCRIPTION = "mi:markerDescription";

    /** _more_ */
    public static final String TAG_MI_DATEINSTALLED = "mi:dateInstalled";
    
    /** _more_ */
    public static final String TAG_MI_GEOLOGICCHARACTERISTIC = "mi:geologicCharacteristic";

    /** _more_ */
    public static final String TAG_MI_BEDROCKTYPE = "mi:bedrockType";

    /** _more_ */
    public static final String TAG_MI_BEDROCKCONDITION = "mi:bedrockCondition";

    /** _more_ */
    public static final String TAG_MI_FRACTURESPACING = "mi:fractureSpacing";

    /** _more_ */
    public static final String TAG_MI_FAULTZONESNEARBY = "mi:faultZonesNearby";

    /** _more_ */
    public static final String TAG_MI_DISTANCE_ACTIVITY = "mi:distance-Activity";

    /** _more_ */
    public static final String TAG_MI_NOTES = "mi:notes";

    /** _more_ */
    public static final String TAG_SITELOCATION = "siteLocation";


    public static final String TAG_SITEQUALITY = "siteQuality";

    /** _more_ */
    public static final String TAG_MI_CITY = "mi:city";

    public static final String TAG_CITY =    "city_or_place";

    /** _more_ */
    public static final String TAG_MI_STATE = "mi:state";

    public static final String TAG_STATE =    "state_or_province";

    /** _more_ */
    public static final String TAG_MI_COUNTRY = "mi:country";

    public static final String TAG_COUNTRY =    "country";

    /** _more_ */
    public static final String TAG_MI_TECTONICPLATE = "mi:tectonicPlate";

    /** _more_ */
    public static final String TAG_MI_APPROXIMATEPOSITIONITRF = "mi:approximatePositionITRF";
    public static final String TAG_APPROXIMATEPOSITIONITRF = "approximatePositionITRF";

    /** _more_ */
    public static final String TAG_MI_XCOORDINATEINMETERS = "mi:xCoordinateInMeters";
    public static final String TAG_XCOORDINATEINMETERS = "xCoordinateInMeters";

    /** _more_ */
    public static final String TAG_MI_YCOORDINATEINMETERS = "mi:yCoordinateInMeters";
    public static final String TAG_YCOORDINATEINMETERS = "yCoordinateInMeters";

    /** _more_ */
    public static final String TAG_MI_ZCOORDINATEINMETERS = "mi:zCoordinateInMeters";
    public static final String TAG_ZCOORDINATEINMETERS = "zCoordinateInMeters";

    /** _more_ */
    public static final String TAG_MI_LATITUDE_NORTH = "mi:latitude-North";
    public static final String TAG_LATITUDE_NORTH = "latitude-North";

    /** _more_ */
    public static final String TAG_MI_LONGITUDE_EAST = "mi:longitude-East";
    public static final String TAG_LONGITUDE_EAST = "longitude-East";

    /** _more_ */
    public static final String TAG_MI_ELEVATION_M_ELLIPS = "mi:elevation-m_ellips";

    public static final String TAG_ELEVATION_M_ELLIPS = "ellipsoid_height";

    /** _more_ */
    public static final String TAG_GNSSRECEIVER = "gnssReceiver";

    /** _more_ */
    public static final String TAG_EQUIP_RECEIVERTYPE = "equip:receiverType";

    public static final String TAG_EQUIP_SAMPLEINTERVAL = "equip:sampleInterval";

    /** _more_ */
    public static final String TAG_EQUIP_SATELLITESYSTEM = "equip:satelliteSystem";

    /** _more_ */
    public static final String TAG_EQUIP_SERIALNUMBER = "equip:serialNumber";

    /** _more_ */
    public static final String TAG_EQUIP_FIRMWAREVERSION = "equip:firmwareVersion";

    /** _more_ */
    public static final String TAG_EQUIP_ELEVATIONCUTOFFSETTING = "equip:elevationCutoffSetting";

    /** _more_ */
    public static final String TAG_EQUIP_DATEINSTALLED = "equip:dateInstalled";

    /** _more_ */
    public static final String TAG_EQUIP_DATEREMOVED = "equip:dateRemoved";

    /** _more_ */
    public static final String TAG_EQUIP_TEMPERATURESTABILIZATION = "equip:temperatureStabilization";

    /** _more_ */
    public static final String TAG_EQUIP_NOTES = "equip:notes";

    /** _more_ */
    public static final String TAG_GNSSANTENNA = "gnssAntenna";

    /** _more_ */
    public static final String TAG_EQUIP_ANTENNATYPE = "equip:antennaType";

    /** _more_ */
    public static final String TAG_EQUIP_ANTENNAREFERENCEPOINT = "equip:antennaReferencePoint";

    /** _more_ */
    public static final String TAG_EQUIP_MARKER_ARPUPECC = "equip:marker-arpUpEcc";

    /** _more_ */
    public static final String TAG_EQUIP_MARKER_ARPNORTHECC = "equip:marker-arpNorthEcc";

    /** _more_ */
    public static final String TAG_EQUIP_MARKER_ARPEASTECC = "equip:marker-arpEastEcc";

    /** _more_ */
    public static final String TAG_EQUIP_ALIGNMENTFROMTRUENORTH = "equip:alignmentFromTrueNorth";

    /** _more_ */
    public static final String TAG_EQUIP_ANTENNARADOMETYPE = 
      "equip:antennaRadomeType";

    /** _more_ */
    public static final String TAG_EQUIP_RADOMESERIALNUMBER = 
           "equip:radomeSerialNumber";

    /** _more_ */
    public static final String TAG_EQUIP_ANTENNACABLETYPE = 
         "equip:antennaCableType";

    /** _more_ */
    public static final String TAG_EQUIP_ANTENNACABLELENGTH = 
         "equip:antennaCableLength";

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

