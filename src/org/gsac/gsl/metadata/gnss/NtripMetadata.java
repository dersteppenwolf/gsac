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

package org.gsac.gsl.metadata.gnss;


import org.gsac.gsl.*;
import org.gsac.gsl.metadata.*;
import org.gsac.gsl.model.*;

import org.gsac.gsl.output.*;
import org.gsac.gsl.util.*;


import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;
import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.net.URL;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;


/**
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class NtripMetadata extends StreamMetadata {

    /** _more_ */
    private static final String COL_DELIMITER = ";";

    /** _more_ */
    public static final String TYPE_NTRIP = "gnss.ntrip";

    /** _more_ */
    public static final String NTRIP_TYPE_STR = "STR";

    /** _more_ */
    public static final String NTRIP_TYPE_CAS = "CAS";

    /** _more_ */
    public static final String NTRIP_TYPE_NET = "NET";

    /** _more_ */
    private String mountPoint;

    /** _more_ */
    private String urlRoot;

    /** _more_ */
    private String identifier;


    /** _more_ */
    private String formatDetails;

    /** _more_ */
    private String carrier;

    /** _more_ */
    private String navSystem;

    /** _more_ */
    private String network;

    /** _more_ */
    private String country;

    /** _more_ */
    private double latitude;

    /** _more_ */
    private double longitude;

    /** _more_ */
    private int nmea;

    /** _more_ */
    private int solution;

    /** _more_ */
    private String generator;

    /** _more_ */
    private String compression;

    /** _more_ */
    private String authentication;

    /** _more_ */
    private String fee;




    /**
     * _more_
     */
    public NtripMetadata() {
        super(TYPE_NTRIP);
    }

    /**
     * _more_
     *
     * @param args _more_
     */
    public static void main(String[] args) {

        System.err.println(
            NtripMetadata.class.isAssignableFrom(StreamMetadata.class));
        System.err.println(
            StreamMetadata.class.isAssignableFrom(NtripMetadata.class));
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getHtml() {
        return getFormat() + COL_DELIMITER + formatDetails + COL_DELIMITER
               + carrier + COL_DELIMITER + navSystem + COL_DELIMITER
               + network + COL_DELIMITER + country + COL_DELIMITER + latitude
               + COL_DELIMITER + longitude + COL_DELIMITER + nmea
               + COL_DELIMITER + solution + COL_DELIMITER + generator
               + COL_DELIMITER + compression + COL_DELIMITER + authentication
               + COL_DELIMITER + fee + COL_DELIMITER + getBitRate();
    }


    /**
     * _more_
     *
     * @param metadata _more_
     *
     * @return _more_
     */
    public static List<NtripMetadata> getMetadata(
            List<GsacMetadata> metadata) {
        return (List<NtripMetadata>) findMetadata(metadata,
                NtripMetadata.class);
    }

    /**
     * _more_
     *
     * @param pw _more_
     * @param outputHandler _more_
     * @param type _more_
     *
     * @throws Exception _more_
     */
    public void encodeInner(Appendable pw, GsacOutputHandler outputHandler,
                            String type)
            throws Exception {
        pw.append(XmlUtil.openTag(XmlSiteLog.TAG_REALTIME_NTRIPPARAMS));

        pw.append(tag(XmlSiteLog.TAG_REALTIME_MOUNTPOINT,
                      this.getMountPoint()));
        pw.append(tag(XmlSiteLog.TAG_REALTIME_SOURCEID,
                      this.getIdentifier()));
        pw.append(tag(XmlSiteLog.TAG_REALTIME_COUNTRYCODE,
                      this.getCountry()));
        pw.append(tag(XmlSiteLog.TAG_REALTIME_NETWORK, this.getNetwork()));

        //Hard code allowconnections
        pw.append(tag(XmlSiteLog.TAG_REALTIME_ALLOWCONNECTIONS, "true"));

        pw.append(tag(XmlSiteLog.TAG_REALTIME_REQUIREAUTHENTICATION,
                      this.getAuthentication()));
        //compression is same as encryption
        pw.append(tag(XmlSiteLog.TAG_REALTIME_ENCRYPTION,
                      this.getCompression()));
        pw.append(tag(XmlSiteLog.TAG_REALTIME_FEESAPPLY,
                      this.getFee().toLowerCase().equals("y")
                      ? "true"
                      : "false"));
        pw.append(tag(XmlSiteLog.TAG_REALTIME_BITRATE,
                      "" + this.getBitRate()));
        pw.append(tag(XmlSiteLog.TAG_REALTIME_CARRIERPHASE,
                      this.getCarrier()));
        pw.append(tag(XmlSiteLog.TAG_REALTIME_NAVSYSTEM,
                      this.getNavSystem()));
        pw.append(tag(XmlSiteLog.TAG_REALTIME_NMEA, "" + this.getNmea()));
        pw.append(tag(XmlSiteLog.TAG_REALTIME_SOLUTION,
                      "" + this.getSolution()));
        pw.append(XmlUtil.closeTag(XmlSiteLog.TAG_REALTIME_NTRIPPARAMS));
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param gsacResource _more_
     * @param outputHandler _more_
     * @param pw _more_
     *
     * @return _more_
     *
     * @throws IOException _more_
     */
    public boolean addHtml(GsacRequest request, GsacResource gsacResource,
                           HtmlOutputHandler outputHandler, Appendable pw)
            throws IOException {
        super.addHtml(request, gsacResource, outputHandler, pw);

        pw.append("<br>");
        pw.append(HtmlUtil.space(3));
        pw.append(HtmlUtil.makeToggleInline("", " STREAM:" + this.getHtml(),
                                            false));
        pw.append("<br>");
        return true;
    }



    /**
     * _more_
     *
     * @param url _more_
     * @param errorBuff _more_
     *
     *
     * @return _more_
     * @throws Exception _more_
     */
    public static List<NtripMetadata> readSourceTable(String url,
            StringBuffer errorBuff)
            throws Exception {
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }


        String contents = IOUtil.readContents(url, NtripMetadata.class, null);
        if (contents == null) {
            errorBuff.append("Could not read url:" + url);
            return null;
        }
        //Don't ask...
        contents = contents.replaceAll("<br>", "");
        List<String> toks = StringUtil.split(contents, "\n", false, false);

        List<NtripMetadata> results = new ArrayList();
        int                 myCnt   = 0;
        int                 lineCnt = 0;
        boolean             tooMany = toks.size() > 100;
        for (; lineCnt < toks.size(); lineCnt++) {
            String line = toks.get(lineCnt).trim();
            if (lineCnt == 0) {
                if ( !line.startsWith("SOURCETABLE ")) {
                    errorBuff.append("Bad source table:" + line);
                    return null;
                }
            }
            if (line.length() == 0) {
                lineCnt++;
                break;
            }

        }
        for (; lineCnt < toks.size(); lineCnt++) {
            String textMetadata = toks.get(lineCnt).trim();
            if (textMetadata.equals("ENDSOURCETABLE")) {
                break;
            }


            List<String> cols = StringUtil.split(textMetadata, COL_DELIMITER,
                                    false, false);
            if (cols.size() == 0) {
                continue;
            }
            int    col  = 0;
            String type = cols.get(col++);
            if ( !type.equals(NTRIP_TYPE_STR)) {
                if ( !type.equals(NTRIP_TYPE_NET)
                        && !type.equals(NTRIP_TYPE_CAS)) {
                    errorBuff.append("Unknown type:" + textMetadata);
                }
                continue;
            }

            NtripMetadata line = new NtripMetadata();
            try {
            line.mountPoint = cols.get(col++);
            line.urlRoot    = url;
            line.setUrl(line.urlRoot + "/" + line.mountPoint);
            line.identifier = cols.get(col++).trim();
            if (line.identifier.length() == 0) {
                line.identifier = line.mountPoint;
            }
            line.setFormat(cols.get(col++));
            line.formatDetails = cols.get(col++);
            line.carrier       = cols.get(col++);
            line.navSystem     = cols.get(col++);
            line.network       = cols.get(col++);
            line.country       = cols.get(col++);
            line.latitude      = Double.parseDouble(cols.get(col++));
            line.longitude = ucar.unidata.geoloc.LatLonPointImpl.lonNormal(
                Double.parseDouble(cols.get(col++)));
            line.nmea           = Integer.parseInt(cols.get(col++));
            line.solution       = Integer.parseInt(cols.get(col++));
            line.generator      = cols.get(col++);
            line.compression    = cols.get(col++);
            line.authentication = cols.get(col++);
            line.fee            = cols.get(col++);
            line.setBitRate(Integer.parseInt(cols.get(col++)));
            results.add(line);
            } catch(NumberFormatException nfe) {
                System.err.println ("Format error:" + nfe+" "  + cols);
            }
        }
        return results;
    }


    /**
     * Set the MountPoint property.
     *
     * @param value The new value for MountPoint
     */
    public void setMountPoint(String value) {
        mountPoint = value;
    }

    /**
     * Get the MountPoint property.
     *
     * @return The MountPoint
     */
    public String getMountPoint() {
        return mountPoint;
    }

    /**
     *  Set the UrlRoot property.
     *
     *  @param value The new value for UrlRoot
     */
    public void setUrlRoot(String value) {
        urlRoot = value;
    }

    /**
     *  Get the UrlRoot property.
     *
     *  @return The UrlRoot
     */
    public String getUrlRoot() {
        return urlRoot;
    }


    /**
     * Set the Identifier property.
     *
     * @param value The new value for Identifier
     */
    public void setIdentifier(String value) {
        identifier = value;
    }

    /**
     * Get the Identifier property.
     *
     * @return The Identifier
     */
    public String getIdentifier() {
        return identifier;
    }


    /**
     * Set the FormatDetails property.
     *
     * @param value The new value for FormatDetails
     */
    public void setFormatDetails(String value) {
        formatDetails = value;
    }

    /**
     * Get the FormatDetails property.
     *
     * @return The FormatDetails
     */
    public String getFormatDetails() {
        return formatDetails;
    }

    /**
     * Set the Carrier property.
     *
     * @param value The new value for Carrier
     */
    public void setCarrier(String value) {
        carrier = value;
    }

    /**
     * Get the Carrier property.
     *
     * @return The Carrier
     */
    public String getCarrier() {
        return carrier;
    }

    /**
     * Set the NavSystem property.
     *
     * @param value The new value for NavSystem
     */
    public void setNavSystem(String value) {
        navSystem = value;
    }

    /**
     * Get the NavSystem property.
     *
     * @return The NavSystem
     */
    public String getNavSystem() {
        return navSystem;
    }

    /**
     * Set the Network property.
     *
     * @param value The new value for Network
     */
    public void setNetwork(String value) {
        network = value;
    }

    /**
     * Get the Network property.
     *
     * @return The Network
     */
    public String getNetwork() {
        return network;
    }

    /**
     * Set the Country property.
     *
     * @param value The new value for Country
     */
    public void setCountry(String value) {
        country = value;
    }

    /**
     * Get the Country property.
     *
     * @return The Country
     */
    public String getCountry() {
        return country;
    }

    /**
     * Set the Latitude property.
     *
     * @param value The new value for Latitude
     */
    public void setLatitude(double value) {
        latitude = value;
    }

    /**
     * Get the Latitude property.
     *
     * @return The Latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Set the Longitude property.
     *
     * @param value The new value for Longitude
     */
    public void setLongitude(double value) {
        longitude = value;
    }

    /**
     * Get the Longitude property.
     *
     * @return The Longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Set the Nmea property.
     *
     * @param value The new value for Nmea
     */
    public void setNmea(int value) {
        nmea = value;
    }

    /**
     * Get the Nmea property.
     *
     * @return The Nmea
     */
    public int getNmea() {
        return nmea;
    }

    /**
     * Set the Solution property.
     *
     * @param value The new value for Solution
     */
    public void setSolution(int value) {
        solution = value;
    }

    /**
     * Get the Solution property.
     *
     * @return The Solution
     */
    public int getSolution() {
        return solution;
    }

    /**
     * Set the Generator property.
     *
     * @param value The new value for Generator
     */
    public void setGenerator(String value) {
        generator = value;
    }

    /**
     * Get the Generator property.
     *
     * @return The Generator
     */
    public String getGenerator() {
        return generator;
    }

    /**
     * Set the Compression property.
     *
     * @param value The new value for Compression
     */
    public void setCompression(String value) {
        compression = value;
    }

    /**
     * Get the Compression property.
     *
     * @return The Compression
     */
    public String getCompression() {
        return compression;
    }

    /**
     * Set the Authentication property.
     *
     * @param value The new value for Authentication
     */
    public void setAuthentication(String value) {
        authentication = value;
    }

    /**
     * Get the Authentication property.
     *
     * @return The Authentication
     */
    public String getAuthentication() {
        return authentication;
    }

    /**
     * Set the Fee property.
     *
     * @param value The new value for Fee
     */
    public void setFee(String value) {
        fee = value;
    }

    /**
     * Get the Fee property.
     *
     * @return The Fee
     */
    public String getFee() {
        return fee;
    }

}
