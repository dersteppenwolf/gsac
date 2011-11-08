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

package org.gsac.gsl.util;


/**
 * Class description
 *
 *
 * @version        Enter version here..., Wed, May 19, '10
 * @author         Enter your name here...
 */

public class XmlSiteLog {



    /** _more_ */
    public static final String ATTR_XMLNS_XMLNS = "xmlns";

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

    /** _more_ */
    public static final String VALUE_XSI_SCHEMALOCATION =
        "http://sopac.ucsd.edu/ns/geodesy/doc/igsSiteLog/2011  http://sopac.ucsd.edu/ns/geodesy/doc/igsSiteLog/2011/igsSiteLog.xsd";


    /** _more_ */
    public static final String XMLNS_XMLNS =
        "http://sopac.ucsd.edu/ns/geodesy/doc/igsSiteLog/2011";

    /** _more_ */
    public static final String XMLNS_XMLNS_CONTACT =
        "http://sopac.ucsd.edu/ns/geodesy/doc/igsSiteLog/contact/2004";

    /** _more_ */
    public static final String XMLNS_XMLNS_EQUIP =
        "http://sopac.ucsd.edu/ns/geodesy/doc/igsSiteLog/equipment/2004";

    /** _more_ */
    public static final String XMLNS_XMLNS_LI =
        "http://sopac.ucsd.edu/ns/geodesy/doc/igsSiteLog/localInterferences/2004";

    /** _more_ */
    public static final String XMLNS_XMLNS_MI =
        "http://sopac.ucsd.edu/ns/geodesy/doc/igsSiteLog/monumentInfo/2004";

    /** _more_ */
    public static final String XMLNS_XMLNS_REALTIME =
        "http://sopac.ucsd.edu/ns/geodesy/doc/igsSiteLog/realtimeDataInfo/2011";

    /** _more_ */
    public static final String XMLNS_XMLNS_XSI =
        "http://www.w3.org/2001/XMLSchema-instance";

    /** _more_ */
    public static final String TAG_IGSSITELOG = "igsSiteLog";

    /** _more_ */
    public static final String TAG_FORMINFORMATION = "formInformation";

    /** _more_ */
    public static final String TAG_MI_PREPAREDBY = "mi:preparedBy";

    /** _more_ */
    public static final String TAG_MI_DATEPREPARED = "mi:datePrepared";

    /** _more_ */
    public static final String TAG_MI_REPORTTYPE = "mi:reportType";

    /** _more_ */
    public static final String TAG_SITEIDENTIFICATION = "siteIdentification";

    /** _more_ */
    public static final String TAG_MI_SITENAME = "mi:siteName";

    /** _more_ */
    public static final String TAG_MI_FOURCHARACTERID = "mi:fourCharacterID";

    /** _more_ */
    public static final String TAG_MI_MONUMENTINSCRIPTION =
        "mi:monumentInscription";

    /** _more_ */
    public static final String TAG_MI_IERSDOMESNUMBER = "mi:iersDOMESNumber";

    /** _more_ */
    public static final String TAG_MI_CDPNUMBER = "mi:cdpNumber";

    /** _more_ */
    public static final String TAG_MI_MONUMENTDESCRIPTION =
        "mi:monumentDescription";

    /** _more_ */
    public static final String TAG_MI_HEIGHTOFTHEMONUMENT =
        "mi:heightOfTheMonument";

    /** _more_ */
    public static final String TAG_MI_MONUMENTFOUNDATION =
        "mi:monumentFoundation";

    /** _more_ */
    public static final String TAG_MI_FOUNDATIONDEPTH = "mi:foundationDepth";

    /** _more_ */
    public static final String TAG_MI_MARKERDESCRIPTION =
        "mi:markerDescription";

    /** _more_ */
    public static final String TAG_MI_DATEINSTALLED = "mi:dateInstalled";

    /** _more_ */
    public static final String TAG_MI_GEOLOGICCHARACTERISTIC =
        "mi:geologicCharacteristic";

    /** _more_ */
    public static final String TAG_MI_BEDROCKTYPE = "mi:bedrockType";

    /** _more_ */
    public static final String TAG_MI_BEDROCKCONDITION =
        "mi:bedrockCondition";

    /** _more_ */
    public static final String TAG_MI_FRACTURESPACING = "mi:fractureSpacing";

    /** _more_ */
    public static final String TAG_MI_FAULTZONESNEARBY =
        "mi:faultZonesNearby";

    /** _more_ */
    public static final String TAG_MI_DISTANCE_ACTIVITY =
        "mi:distance-Activity";

    /** _more_ */
    public static final String TAG_MI_NOTES = "mi:notes";

    /** _more_ */
    public static final String TAG_SITELOCATION = "siteLocation";

    /** _more_ */
    public static final String TAG_MI_CITY = "mi:city";

    /** _more_ */
    public static final String TAG_MI_STATE = "mi:state";

    /** _more_ */
    public static final String TAG_MI_COUNTRY = "mi:country";

    /** _more_ */
    public static final String TAG_MI_TECTONICPLATE = "mi:tectonicPlate";

    /** _more_ */
    public static final String TAG_MI_APPROXIMATEPOSITIONITRF =
        "mi:approximatePositionITRF";

    /** _more_ */
    public static final String TAG_MI_XCOORDINATEINMETERS =
        "mi:xCoordinateInMeters";

    /** _more_ */
    public static final String TAG_MI_YCOORDINATEINMETERS =
        "mi:yCoordinateInMeters";

    /** _more_ */
    public static final String TAG_MI_ZCOORDINATEINMETERS =
        "mi:zCoordinateInMeters";

    /** _more_ */
    public static final String TAG_MI_LATITUDE_NORTH = "mi:latitude-North";

    /** _more_ */
    public static final String TAG_MI_LONGITUDE_EAST = "mi:longitude-East";

    /** _more_ */
    public static final String TAG_MI_ELEVATION_M_ELLIPS =
        "mi:elevation-m_ellips";

    /** _more_ */
    public static final String TAG_GNSSRECEIVER = "gnssReceiver";

    /** _more_ */
    public static final String TAG_EQUIP_RECEIVERTYPE = "equip:receiverType";

    /** _more_ */
    public static final String TAG_EQUIP_SATELLITESYSTEM =
        "equip:satelliteSystem";

    /** _more_ */
    public static final String TAG_EQUIP_SERIALNUMBER = "equip:serialNumber";

    /** _more_ */
    public static final String TAG_EQUIP_FIRMWAREVERSION =
        "equip:firmwareVersion";

    /** _more_ */
    public static final String TAG_EQUIP_ELEVATIONCUTOFFSETTING =
        "equip:elevationCutoffSetting";

    /** _more_ */
    public static final String TAG_EQUIP_DATEINSTALLED =
        "equip:dateInstalled";

    /** _more_ */
    public static final String TAG_EQUIP_DATEREMOVED = "equip:dateRemoved";

    /** _more_ */
    public static final String TAG_EQUIP_TEMPERATURESTABILIZATION =
        "equip:temperatureStabilization";

    /** _more_ */
    public static final String TAG_EQUIP_NOTES = "equip:notes";

    /** _more_ */
    public static final String TAG_GNSSANTENNA = "gnssAntenna";

    /** _more_ */
    public static final String TAG_EQUIP_ANTENNATYPE = "equip:antennaType";

    /** _more_ */
    public static final String TAG_EQUIP_ANTENNAREFERENCEPOINT =
        "equip:antennaReferencePoint";

    /** _more_ */
    public static final String TAG_EQUIP_MARKER_ARPUPECC =
        "equip:marker-arpUpEcc.";

    /** _more_ */
    public static final String TAG_EQUIP_MARKER_ARPNORTHECC =
        "equip:marker-arpNorthEcc.";

    /** _more_ */
    public static final String TAG_EQUIP_MARKER_ARPEASTECC =
        "equip:marker-arpEastEcc.";

    /** _more_ */
    public static final String TAG_EQUIP_ALIGNMENTFROMTRUENORTH =
        "equip:alignmentFromTrueNorth";

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
    public static final String TAG_FREQUENCYSTANDARD = "frequencyStandard";

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
    public static final String TAG_OTHERINSTRUMENTATION =
        "otherInstrumentation";

    /** _more_ */
    public static final String TAG_RADIOINTERFERENCES = "radioInterferences";

    /** _more_ */
    public static final String TAG_MULTIPATHSOURCES = "multipathSources";

    /** _more_ */
    public static final String TAG_SIGNALOBSTRUCTIONS = "signalObstructions";

    /** _more_ */
    public static final String TAG_LOCALEPISODICEVENTS =
        "localEpisodicEvents";

    /** _more_ */
    public static final String TAG_CONTACTAGENCY = "contactAgency";

    /** _more_ */
    public static final String TAG_RESPONSIBLEAGENCY = "responsibleAgency";

    /** _more_ */
    public static final String TAG_REALTIMEDATASTREAMS =
        "realtimeDataStreams";

    /** _more_ */
    public static final String TAG_MOREINFORMATION = "moreInformation";



    /** _more_ */
    public static final String TAG_REALTIME_DATASTREAMS =
        "realtime:dataStreams";

    /** _more_ */
    public static final String TAG_REALTIME_SITESTREAM =
        "realtime:siteStream";

    /** _more_ */
    public static final String TAG_REALTIME_HOSTNAME = "realtime:hostname";

    /** _more_ */
    public static final String TAG_REALTIME_PORT = "realtime:port";

    /** _more_ */
    public static final String TAG_REALTIME_SAMPINTERVAL =
        "realtime:sampInterval";

    /** _more_ */
    public static final String TAG_REALTIME_DATAFORMAT =
        "realtime:dataFormat";

    /** _more_ */
    public static final String TAG_REALTIME_NTRIPMOUNT =
        "realtime:ntripMount";

    /** _more_ */
    public static final String TAG_REALTIME_MOUNTPOINT =
        "realtime:mountPoint";

    /** _more_ */
    public static final String TAG_REALTIME_SOURCEID = "realtime:sourceID";

    /** _more_ */
    public static final String TAG_REALTIME_COUNTRYCODE =
        "realtime:countryCode";

    /** _more_ */
    public static final String TAG_REALTIME_NETWORK = "realtime:network";

    /** _more_ */
    public static final String TAG_REALTIME_ALLOWCONNECTIONS =
        "realtime:allowConnections";

    /** _more_ */
    public static final String TAG_REALTIME_REQUIREAUTHENTICATION =
        "realtime:requireAuthentication";

    /** _more_ */
    public static final String TAG_REALTIME_ENCRIPTION =
        "realtime:encription";

    /** _more_ */
    public static final String TAG_REALTIME_FEESAPPLY = "realtime:feesApply";

    /** _more_ */
    public static final String TAG_REALTIME_BITRATE = "realtime:bitrate";

    /** _more_ */
    public static final String TAG_REALTIME_CARRIERPHASE =
        "realtime:carrierPhase";

    /** _more_ */
    public static final String TAG_REALTIME_NAVSYSTEM = "realtime:navSystem";

    /** _more_ */
    public static final String TAG_REALTIME_PUBLISHEDSTREAM =
        "realtime:publishedStream";

    /** _more_ */
    public static final String TAG_REALTIME_IPADDRESS = "realtime:ipAddress";




}
