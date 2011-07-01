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
       


    public static final String XMLNS_XMLNS = "http://sopac.ucsd.edu/ns/geodesy/doc/igsSiteLog/2011";
    public static final String XMLNS_XMLNS_CONTACT = "http://sopac.ucsd.edu/ns/geodesy/doc/igsSiteLog/contact/2004";
    public static final String XMLNS_XMLNS_EQUIP = "http://sopac.ucsd.edu/ns/geodesy/doc/igsSiteLog/equipment/2004";
    public static final String XMLNS_XMLNS_LI = "http://sopac.ucsd.edu/ns/geodesy/doc/igsSiteLog/localInterferences/2004";
    public static final String XMLNS_XMLNS_MI = "http://sopac.ucsd.edu/ns/geodesy/doc/igsSiteLog/monumentInfo/2004";
    public static final String XMLNS_XMLNS_REALTIME = "http://sopac.ucsd.edu/ns/geodesy/doc/igsSiteLog/realtimeDataInfo/2011";
    public static final String XMLNS_XMLNS_XSI = "http://www.w3.org/2001/XMLSchema-instance";

    public static final String TAG_IGSSITELOG = "igsSiteLog";
    public static final String TAG_FORMINFORMATION = "formInformation";
    public static final String TAG_MI_PREPAREDBY = "mi:preparedBy";
    public static final String TAG_MI_DATEPREPARED = "mi:datePrepared";
    public static final String TAG_MI_REPORTTYPE = "mi:reportType";
    public static final String TAG_SITEIDENTIFICATION = "siteIdentification";
    public static final String TAG_MI_SITENAME = "mi:siteName";
    public static final String TAG_MI_FOURCHARACTERID = "mi:fourCharacterID";
    public static final String TAG_MI_MONUMENTINSCRIPTION = "mi:monumentInscription";
    public static final String TAG_MI_IERSDOMESNUMBER = "mi:iersDOMESNumber";
    public static final String TAG_MI_CDPNUMBER = "mi:cdpNumber";
    public static final String TAG_MI_MONUMENTDESCRIPTION = "mi:monumentDescription";
    public static final String TAG_MI_HEIGHTOFTHEMONUMENT = "mi:heightOfTheMonument";
    public static final String TAG_MI_MONUMENTFOUNDATION = "mi:monumentFoundation";
    public static final String TAG_MI_FOUNDATIONDEPTH = "mi:foundationDepth";
    public static final String TAG_MI_MARKERDESCRIPTION = "mi:markerDescription";
    public static final String TAG_MI_DATEINSTALLED = "mi:dateInstalled";
    public static final String TAG_MI_GEOLOGICCHARACTERISTIC = "mi:geologicCharacteristic";
    public static final String TAG_MI_BEDROCKTYPE = "mi:bedrockType";
    public static final String TAG_MI_BEDROCKCONDITION = "mi:bedrockCondition";
    public static final String TAG_MI_FRACTURESPACING = "mi:fractureSpacing";
    public static final String TAG_MI_FAULTZONESNEARBY = "mi:faultZonesNearby";
    public static final String TAG_MI_DISTANCE_ACTIVITY = "mi:distance-Activity";
    public static final String TAG_MI_NOTES = "mi:notes";
    public static final String TAG_SITELOCATION = "siteLocation";
    public static final String TAG_MI_CITY = "mi:city";
    public static final String TAG_MI_STATE = "mi:state";
    public static final String TAG_MI_COUNTRY = "mi:country";
    public static final String TAG_MI_TECTONICPLATE = "mi:tectonicPlate";
    public static final String TAG_MI_APPROXIMATEPOSITIONITRF = "mi:approximatePositionITRF";
    public static final String TAG_MI_XCOORDINATEINMETERS = "mi:xCoordinateInMeters";
    public static final String TAG_MI_YCOORDINATEINMETERS = "mi:yCoordinateInMeters";
    public static final String TAG_MI_ZCOORDINATEINMETERS = "mi:zCoordinateInMeters";
    public static final String TAG_MI_LATITUDE_NORTH = "mi:latitude-North";
    public static final String TAG_MI_LONGITUDE_EAST = "mi:longitude-East";
    public static final String TAG_MI_ELEVATION_M_ELLIPS = "mi:elevation-m_ellips";
    public static final String TAG_GNSSRECEIVER = "gnssReceiver";
    public static final String TAG_EQUIP_RECEIVERTYPE = "equip:receiverType";
    public static final String TAG_EQUIP_SATELLITESYSTEM = "equip:satelliteSystem";
    public static final String TAG_EQUIP_SERIALNUMBER = "equip:serialNumber";
    public static final String TAG_EQUIP_FIRMWAREVERSION = "equip:firmwareVersion";
    public static final String TAG_EQUIP_ELEVATIONCUTOFFSETTING = "equip:elevationCutoffSetting";
    public static final String TAG_EQUIP_DATEINSTALLED = "equip:dateInstalled";
    public static final String TAG_EQUIP_DATEREMOVED = "equip:dateRemoved";
    public static final String TAG_EQUIP_TEMPERATURESTABILIZATION = "equip:temperatureStabilization";
    public static final String TAG_EQUIP_NOTES = "equip:notes";
    public static final String TAG_GNSSANTENNA = "gnssAntenna";
    public static final String TAG_EQUIP_ANTENNATYPE = "equip:antennaType";
    public static final String TAG_EQUIP_ANTENNAREFERENCEPOINT = "equip:antennaReferencePoint";
    public static final String TAG_EQUIP_MARKER_ARPUPECC= "equip:marker-arpUpEcc.";
    public static final String TAG_EQUIP_MARKER_ARPNORTHECC = "equip:marker-arpNorthEcc.";
    public static final String TAG_EQUIP_MARKER_ARPEASTECC = "equip:marker-arpEastEcc.";
    public static final String TAG_EQUIP_ALIGNMENTFROMTRUENORTH = "equip:alignmentFromTrueNorth";
    public static final String TAG_EQUIP_ANTENNARADOMETYPE = "equip:antennaRadomeType";
    public static final String TAG_EQUIP_RADOMESERIALNUMBER = "equip:radomeSerialNumber";
    public static final String TAG_EQUIP_ANTENNACABLETYPE = "equip:antennaCableType";
    public static final String TAG_EQUIP_ANTENNACABLELENGTH = "equip:antennaCableLength";
    public static final String TAG_FREQUENCYSTANDARD = "frequencyStandard";
    public static final String TAG_COLOCATIONINFORMATION = "colocationInformation";
    public static final String TAG_HUMIDITYSENSOR = "humiditySensor";
    public static final String TAG_PRESSURESENSOR = "pressureSensor";
    public static final String TAG_WATERVAPORSENSOR = "waterVaporSensor";
    public static final String TAG_OTHERINSTRUMENTATION = "otherInstrumentation";
    public static final String TAG_RADIOINTERFERENCES = "radioInterferences";
    public static final String TAG_MULTIPATHSOURCES = "multipathSources";
    public static final String TAG_SIGNALOBSTRUCTIONS = "signalObstructions";
    public static final String TAG_LOCALEPISODICEVENTS = "localEpisodicEvents";
    public static final String TAG_CONTACTAGENCY = "contactAgency";
    public static final String TAG_RESPONSIBLEAGENCY = "responsibleAgency";
    public static final String TAG_REALTIMEDATASTREAMS = "realtimeDataStreams";
    public static final String TAG_MOREINFORMATION = "moreInformation";
}