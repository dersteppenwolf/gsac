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

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;

import java.util.Hashtable;
import java.util.List;

import javax.servlet.*;
import javax.servlet.http.*;


/**
 * Writes GSAC query site results in IGS site log format.   LOOK FIX needs to be implemented.
 */
public class SiteLogOutputHandler extends GsacOutputHandler {

    /*
     P123 Site Information Form (site log)
     International GPS Service
     See Instructions at:
     ftp://igscb.jpl.nasa.gov/pub/station/general/sitelog_instr.txt
*/

    /*
0.   Form

     Prepared by (full name)  : Susan Jeffries
     Date Prepared            : 2011-01-24
     Report Type              : UPDATE
     If Update:
      Previous Site Log       : p123_20100719.log
      Modified/Added Sections : (n.n,n.n,...)
    */





    /** output id */
    public static final String OUTPUT_SITE_IGSLOG = "site.log";

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
    public SiteLogOutputHandler(GsacRepository gsacRepository,
                                ResourceClass resourceClass) {
        super(gsacRepository, resourceClass);
        getRepository().addOutput(getResourceClass(),
                                  new GsacOutput(this, OUTPUT_SITE_IGSLOG,
                                      "IGS Site Log", "/site.log", true));
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
        response.startResponse(GsacResponse.MIME_TEXT);
        PrintWriter    pw    = response.getPrintWriter();
        List<GsacSite> sites = response.getSites();
        for (GsacSite site : sites) {
            //Call this to ensure that all of the metadata is added to the site
            getRepository().doGetFullMetadata(-1, site);
            //Add the various content areas
            /*
            addFormInformation(pw);
            addSiteIdentification(pw, site);
            addSiteLocation(pw, site);
            addSiteEquipment(pw, site);
            */
        }
        //        pw.append(XmlUtil.closeTag(XmlSiteLog.TAG_IGSSITELOG));
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
        pw.append(
            XmlUtil.tag(
                XmlSiteLog.TAG_FORMINFORMATION, "",
                XmlUtil.tag(
                    XmlSiteLog.TAG_MI_PREPAREDBY, "",
                    getRepository().getRepositoryName()) + XmlUtil.tag(
                        XmlSiteLog.TAG_MI_DATEPREPARED, "",
                        sdf1.format(new Date())) + XmlUtil.tag(
                            XmlSiteLog.TAG_MI_REPORTTYPE, "", "DYNAMIC")));
        */
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
        */

        /*
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
        */
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
        */
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
        List<GsacMetadata> equipmentMetadata =
            site.findMetadata(
                new GsacMetadata.ClassMetadataFinder(GnssEquipment.class));
        for (GsacMetadata metadata : equipmentMetadata) {
            GnssEquipment equipment = (GnssEquipment) metadata;
            pw.append(XmlUtil.openTag(XmlSiteLog.TAG_GNSSRECEIVER));

            pw.append(XmlUtil.tag(XmlSiteLog.TAG_EQUIP_RECEIVERTYPE, "",
                                  equipment.getReceiver()));
            pw.append(XmlUtil.tag(XmlSiteLog.TAG_EQUIP_SERIALNUMBER, "",
                                  equipment.getReceiverSerial()));
            pw.append(XmlUtil.tag(XmlSiteLog.TAG_EQUIP_FIRMWAREVERSION, "",
                                  equipment.getReceiverFirmware()));
            pw.append(XmlUtil.tag(XmlSiteLog.TAG_EQUIP_DATEINSTALLED, "",
                                  sdf2.format(equipment.getFromDate())));
            pw.append(XmlUtil.tag(XmlSiteLog.TAG_EQUIP_DATEREMOVED, "",
                                  sdf2.format(equipment.getToDate())));

            pw.append(XmlUtil.tag(XmlSiteLog.TAG_EQUIP_SATELLITESYSTEM, "",
                                  "GPS"));
            pw.append(
                XmlUtil.tag(
                    XmlSiteLog.TAG_EQUIP_ELEVATIONCUTOFFSETTING, "", ""));
            pw.append(
                XmlUtil.tag(
                    XmlSiteLog.TAG_EQUIP_TEMPERATURESTABILIZATION, "", ""));
            pw.append(XmlUtil.tag(XmlSiteLog.TAG_EQUIP_NOTES, "", ""));
            pw.append(XmlUtil.closeTag(XmlSiteLog.TAG_GNSSRECEIVER));

            pw.append(XmlUtil.openTag(XmlSiteLog.TAG_GNSSANTENNA));

            pw.append(XmlUtil.tag(XmlSiteLog.TAG_EQUIP_ANTENNATYPE, "",
                                  getNonNullString(equipment.getAntenna())));
            pw.append(
                XmlUtil.tag(
                    XmlSiteLog.TAG_EQUIP_SERIALNUMBER, "",
                    getNonNullString(equipment.getAntennaSerial())));

            pw.append(XmlUtil.tag(XmlSiteLog.TAG_EQUIP_MARKER_ARPUPECC, "",
                                  ""));
            pw.append(XmlUtil.tag(XmlSiteLog.TAG_EQUIP_MARKER_ARPNORTHECC,
                                  "", ""));
            pw.append(XmlUtil.tag(XmlSiteLog.TAG_EQUIP_MARKER_ARPEASTECC, "",
                                  ""));
            pw.append(
                XmlUtil.tag(
                    XmlSiteLog.TAG_EQUIP_ALIGNMENTFROMTRUENORTH, "", ""));

            pw.append(XmlUtil.tag(XmlSiteLog.TAG_EQUIP_ANTENNARADOMETYPE, "",
                                  getNonNullString(equipment.getDome())));
            pw.append(
                XmlUtil.tag(
                    XmlSiteLog.TAG_EQUIP_RADOMESERIALNUMBER, "",
                    getNonNullString(equipment.getDomeSerial())));

            pw.append(XmlUtil.tag(XmlSiteLog.TAG_EQUIP_ANTENNACABLETYPE, "",
                                  ""));
            pw.append(XmlUtil.tag(XmlSiteLog.TAG_EQUIP_ANTENNACABLELENGTH,
                                  "", ""));

            pw.append(XmlUtil.tag(XmlSiteLog.TAG_EQUIP_DATEINSTALLED, "",
                                  sdf2.format(equipment.getFromDate())));
            pw.append(XmlUtil.tag(XmlSiteLog.TAG_EQUIP_DATEREMOVED, "",
                                  sdf2.format(equipment.getToDate())));
            pw.append(XmlUtil.closeTag(XmlSiteLog.TAG_GNSSANTENNA));




        }
        */
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
