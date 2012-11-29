/*
 * Copyright 2012 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
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

import java.util.Hashtable;
import java.util.List;

import javax.servlet.*;
import javax.servlet.http.*;


/**
 * 
 *
 */
public class PlainTextSiteOutputHandler extends GsacOutputHandler {

    /** output id */
    public static final String OUTPUT_SITE_PLAIN = "site.plaintext";

    /** date formatter */
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /** date formatter */
    private SimpleDateFormat dateTimeFormat =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z");

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
    public PlainTextSiteOutputHandler(GsacRepository gsacRepository,
                                   ResourceClass resourceClass) {
        super(gsacRepository, resourceClass);
        getRepository().addOutput(getResourceClass(),
                                  new GsacOutput(this, OUTPUT_SITE_PLAIN,
                                      "Plain text", "/site.plaintext", true));
    }



    /**
     * handle the request: format sites' information in plain text so you can easily see what is avaibable for these sites in this repository.
     *
     * @param request the request
     * @param response the response to write to
     *
     *
     * @throws Exception on badness
     */
    public void handleResult(GsacRequest request, GsacResponse response)
            throws Exception {
        response.startResponse("text");
        PrintWriter pw = response.getPrintWriter();
        addHeader(pw);

        //We can have any number of sites here. 
        List<GsacSite> sites = response.getSites();
        //  for each site:
        for (GsacSite site : sites) {
            pw.append(    " \n");
            //Call this to ensure that all of the metadata is added to the site
            getRepository().doGetFullMetadata(-1, site);
            //Add the various content areas
            addSiteIdentification(pw, site);
            addSiteLocation(pw, site);
            addSiteEquipment(pw, site);
            addSiteStream(pw, site);
        }
        response.endResponse();
    }


    /**
     * label top of results; a header.
     *
     * @param pw _more_
     */
    private void addHeader (PrintWriter pw) {
        pw.append(  "   GSAC-WS Repository Site information in plain text. \n");
        pw.append(  "   From the "+ getRepository().getRepositoryName()  + " on "+ myFormatDate(new Date()) + "\n"); 
        pw.append(  "   Unknown values are empty, but empty 'equipment removed' dates often mean 'not yet removed.' \n"); 
        pw.append(  "   This format is only for a quick visual check of what is available. Not for computer processing.  \n");
    }

    /**
     * print results of site id details for this format style
     *
     * @param pw _more_
     * @param site _more_
     *
     * @throws Exception _more_
     */
    private void addSiteIdentification(PrintWriter pw, GsacSite site)
            throws Exception {
        pw.append(    " site:\n");
        pw.append(    " site 4 char ID:       "+ site.getShortName() + "\n");
        pw.append(    " site long name:       "+ site.getLongName() + "\n");
        pw.append(    " site IERSDOMES        "+ getProperty(site, GsacExtArgs.SITE_METADATA_IERDOMES, "") + "\n");
        Date date = site.getFromDate();
        if (date != null) {
            pw.append(" site installed date: "+ myFormatDateTime(date) + "\n");
        }
        else {
            pw.append(" site installed date: \n");
        }
        pw.append(    " site monum. descript. "+ getProperty(site, GsacExtArgs.SITE_METADATA_MONUMENTDESCRIPTION, "") + "\n");
        pw.append(    " site cdp number       "+ getProperty(site, GsacExtArgs.SITE_METADATA_CDPNUM, "") + "\n");
    }


    /**
     *  from the ipnput GSAC 'site' object, extract the value of the named field or API argument.
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
     * print results of site location for this format style
     *
     * @param pw _more_
     * @param site _more_
     *
     * @throws Exception _more_
     */
    private void addSiteLocation(PrintWriter pw, GsacSite site)
            throws Exception {
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
        pw.append(    " site country:                 "+ getNonNullString(plm.getCountry())  + "\n");
        pw.append(    " site state or province:       "+ getNonNullString(plm.getState()  ) + "\n");
        pw.append(    " site city:                    "+ getNonNullString(plm.getCity()) + "\n");
        EarthLocation el = site.getEarthLocation();
        pw.append(    " site latitude:                "+ formatLocation(el.getLatitude())  + "" + "\n");
        pw.append(    " site longitude:               "+ formatLocation(el.getLongitude()) + "" + "\n");
        pw.append(    " site ellipsoidal height:      "+ elevationFormat.format(el.getElevation()) + "" + "\n");
        if (el.hasXYZ()) {
            pw.append(" site X coordinate:            "+ el.getX() + "" + "\n");
            pw.append(" site Y coordinate:            "+ el.getY() + "" + "\n");
            pw.append(" site Z coordinate:            "+ el.getZ() + "" + "\n");
        } else {
            pw.append(" site X coordinate:            " + "\n");
            pw.append(" site Y coordinate:            " + "\n");
            pw.append(" site Z coordinate:            " + "\n");
        }
    }

    /**
     * format a double value (such as lat and long) to 4 sig figs. 0.0001 lat res is about 1/100 of a km or 10 meters.
     *
     * @param v  a double number
     *
     * @return a String 
     */
    private String formatLocation(double v) {
        v = (double) Math.round(v * 10000) / 10000;
        String s = latLonFormat.format(v);
        return s;
    }


    /**
     * print results of site equipment ('sessions') for this format style
     *
     * @param pw _more_
     * @param site _more_
     *
     * @throws Exception _more_
     */
    private void addSiteEquipment(PrintWriter pw, GsacSite site)
            throws Exception {
        List<GsacMetadata> equipmentMetadata =
            site.findMetadata(
                new GsacMetadata.ClassMetadataFinder(GnssEquipment.class));
        for (GsacMetadata metadata : equipmentMetadata) {
            GnssEquipment equipment = (GnssEquipment) metadata;

            if (equipment.hasReceiver()) {
                pw.append("    new equipment session (receiver):   \n");
                pw.append("      receiver type:          "+ equipment.getReceiver() + "\n");
                pw.append("      receiver SN:            "+ equipment.getReceiverSerial()  + "\n");
                pw.append("      receiver firmware vers: "+ equipment.getReceiverFirmware() + "\n");
                pw.append("      receiver installed date:"+ myFormatDateTime(equipment.getFromDate()) + "\n");
                pw.append("      receiver removed:       "+ myFormatDateTime(equipment.getToDate()) + "\n");
            }

            if (equipment.hasAntenna()) {
                pw.append("    new equipment session (antenna):   \n");
                pw.append("      antenna type:           "+ getNonNullString(equipment.getAntenna()) + "\n");
                pw.append("      antenna SN:             "+ getNonNullString(equipment.getAntennaSerial()) + "\n");
                double[] xyz = equipment.getXyzOffset();
                pw.append("      antenna offset Ht or UP:"+ offsetFormat.format(xyz[2]) + "\n");
                pw.append("      antenna offset North:   "+ offsetFormat.format(xyz[1]) + "\n");
                pw.append("      antenna offset East:    "+ offsetFormat.format(xyz[0]) + "\n");
                pw.append("      antenna installed date: "+ myFormatDateTime(equipment.getFromDate()) + "\n");
                pw.append("      antenna removed:        "+ myFormatDateTime(equipment.getToDate()) + "\n");
                //pw.append( _ALIGNMENTFROMTRUENORTH, "", ""));
                //pw.append(EQUIP_ANTENNACABLETYPE, "",
                //pw.append(EQUIP_ANTENNACABLELENGTH,
                pw.append("      Dome type:              "+ getNonNullString(equipment.getDome()) + "\n");
                pw.append("      Dome SN:                "+ getNonNullString(equipment.getDomeSerial()) + "\n");
            }
        }
    }


    /**
     * _more_
     *
     * @param date _more_
     *
     * @return _more_
     */
    private String myFormatDateTime(Date date) {
        if (date == null) { return ""; }
        synchronized (dateTimeFormat) {
            return dateTimeFormat.format(date);
        }
    }

    /**
     * _more_
     *
     * @param date _more_
     *
     * @return _more_
     */
    private String myFormatDate(Date date) {
        if (date == null) { return ""; }
        synchronized (dateFormat) {
            return dateFormat.format(date);
        }
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
        GsacMetadata.debug = true;
        System.err.println(" error: PlainTextSiteOutputHandler.addSiteStream ():  Finding metadata");
        List<GsacMetadata> streamMetadata =
            site.findMetadata(
                new GsacMetadata.ClassMetadataFinder(StreamMetadata.class));
        GsacMetadata.debug = false;
        int cnt = 0;
        for (GsacMetadata metadata : streamMetadata) {
            StreamMetadata stream = (StreamMetadata) metadata;
            if (cnt == 0) {
                ; //pw.append( XmlUtil.openTag(XmlSiteLog.TAG_REALTIME_DATASTREAMS));
            }
            cnt++;
            stream.encode(pw, this, "plainsitelog");
        }
        if (cnt > 0) {
            ; //pw.append(XmlUtil.closeTag(XmlSiteLog.TAG_REALTIME_DATASTREAMS));
        }
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