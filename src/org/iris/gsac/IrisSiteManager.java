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
/*
 *
 */

package org.iris.gsac;


import org.gsac.gsl.*;
import org.gsac.gsl.metadata.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.output.*;
import org.gsac.gsl.util.*;

import org.w3c.dom.*;


import org.ramadda.sql.Clause;
import org.ramadda.sql.SqlUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.sql.ResultSet;
import java.sql.Statement;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;


/**
 */
public class IrisSiteManager extends SiteManager {

    /** _more_          */
    public static final String IRIS_URL =
        "http://www.iris.edu/ws/station/query";

    /** _more_          */
    public static final String ARG_SITE_STARTDATE = "site.startdate";

    /** _more_          */
    public static final String ARG_SITE_STARTDATE_FROM = ARG_SITE_STARTDATE
                                                         + ".from";

    /** _more_          */
    public static final String ARG_SITE_STARTDATE_TO = ARG_SITE_STARTDATE
                                                       + ".to";

    /** _more_          */
    public static final String ARG_SITE_ENDDATE = "site.enddate";

    /** _more_          */
    public static final String ARG_SITE_ENDDATE_FROM = ARG_SITE_ENDDATE
                                                       + ".from";

    /** _more_          */
    public static final String ARG_SITE_ENDDATE_TO = ARG_SITE_ENDDATE + ".to";

    /** _more_          */
    public static final String ARG_SITE_CHANNEL = "site.channel";


    /** _more_          */
    public static final String ARG_IRIS_STA = "sta";

    /** _more_          */
    public static final String ARG_IRIS_CHAN = "chan";

    /** _more_          */
    public static final String ARG_IRIS_NET = "net";

    /** _more_          */
    public static final String ARG_IRIS_MINLAT = "minlat";

    /** _more_          */
    public static final String ARG_IRIS_MAXLAT = "maxlat";

    /** _more_          */
    public static final String ARG_IRIS_MINLON = "minlon";

    /** _more_          */
    public static final String ARG_IRIS_MAXLON = "maxlon";

    /** _more_          */
    public static final String ARG_IRIS_TIMEWINDOW = "timewindow";

    /** _more_          */
    public static final String ARG_IRIS_STARTBEFORE = "startbefore";

    /** _more_          */
    public static final String ARG_IRIS_STARTAFTER = "startafter";

    /** _more_          */
    public static final String ARG_IRIS_ENDBEFORE = "endbefore";

    /** _more_          */
    public static final String ARG_IRIS_ENDAFTER = "endafter";




    /**
     * ctor
     *
     * @param repository the repository
     */
    public IrisSiteManager(IrisRepository repository) {
        super(repository);
    }


    /**
     * CHANGEME This is the main entry point for handling queries
     *   If you don't implement this method then the base SiteManager
     *   class will
     *
     * @param request _more_
     * @param response _more_
     *
     * @throws Exception _more_
     */
    public void handleRequest(GsacRequest request, GsacResponse response)
            throws Exception {
        StringBuffer msgBuff = new StringBuffer();

        List<String> args    = new ArrayList<String>();

        String[] gsacSpatialArgs = new String[] { ARG_NORTH, ARG_WEST,
                ARG_SOUTH, ARG_EAST };
        String[] irisSpatialArgs = new String[] { ARG_IRIS_MAXLAT,
                ARG_IRIS_MINLON, ARG_IRIS_MINLAT, ARG_IRIS_MAXLON, };

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(GsacOutputHandler.TIMEZONE_DEFAULT);
        Date[] timeWindow = request.getDateRange(ARG_SITE_DATE_FROM,
                                ARG_SITE_DATE_TO, null, null);
        if ((timeWindow[0] != null) && (timeWindow[1] != null)) {
            String value = sdf.format(timeWindow[0]) + ","
                           + sdf.format(timeWindow[1]);
            args.add(ARG_IRIS_TIMEWINDOW + "=" + value);
            appendSearchCriteria(msgBuff, "timewindow:", value);
        }



        Date[][] dateRanges = new Date[][] {
            request.getDateRange(ARG_SITE_STARTDATE_FROM,
                                 ARG_SITE_STARTDATE_TO, null, null),
            request.getDateRange(ARG_SITE_ENDDATE_FROM, ARG_SITE_ENDDATE_TO,
                                 null, null),
        };
        String[][] dateArgs = new String[][] {
            new String[] { ARG_IRIS_STARTAFTER, ARG_IRIS_STARTBEFORE },
            new String[] { ARG_IRIS_ENDAFTER, ARG_IRIS_ENDBEFORE },
        };


        for (int i = 0; i < dateRanges.length; i++) {
            Date[] dateRange = dateRanges[i];
            for (int j = 0; j < 2; j++) {
                if (dateRange[j] != null) {
                    String dateString = sdf.format(dateRange[j]);
                    args.add(HtmlUtil.arg(dateArgs[i][j], dateString));
                    appendSearchCriteria(msgBuff, dateArgs[i][j] + ":",
                                         dateString);
                }
            }
        }


        String[] spatialNames = new String[] { "North", "West", "South",
                "East", };
        for (int i = 0; i < gsacSpatialArgs.length; i++) {
            if (request.defined(gsacSpatialArgs[i])) {
                args.add(HtmlUtil.arg(irisSpatialArgs[i],
                                      request.get(gsacSpatialArgs[i], "")));
                appendSearchCriteria(msgBuff, spatialNames[i] + ":",
                                     request.get(gsacSpatialArgs[i], ""));
            }
        }

        List<String> siteCodes = request.get(ARG_SITE_CODE,
                                             new ArrayList<String>());
        for (String siteCode : siteCodes) {
            if (siteCode.length() == 0) {
                continue;
            }
            appendSearchCriteria(msgBuff, "station=", siteCode);
            args.add(HtmlUtil.arg(ARG_IRIS_STA, siteCode));
        }


        if (request.defined(ARG_SITE_CHANNEL)) {
            args.add(HtmlUtil.arg(ARG_IRIS_CHAN,
                                  request.get(ARG_SITE_CHANNEL, "")));
            appendSearchCriteria(msgBuff, "channel=",
                                 request.get(ARG_SITE_CHANNEL, ""));
        }
        List<String> groups = request.get(ARG_SITE_GROUP,
                                          new ArrayList<String>());
        for (String group : groups) {
            if (group.length() == 0) {
                continue;
            }
            appendSearchCriteria(msgBuff, "network=", group);
            args.add(HtmlUtil.arg(ARG_IRIS_NET, group));
        }

        List<GsacSite> sites = getSites(args);
        for (GsacSite site : sites) {
            response.addResource(site);
        }
        setSearchCriteriaMessage(response, msgBuff);
    }

    /**
     * _more_
     *
     * @param args _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private List<GsacSite> getSites(List<String> args) throws Exception {
        List<GsacSite> sites = new ArrayList<GsacSite>();
        String         url   = IRIS_URL + "?" + StringUtil.join("&", args);
        System.err.println("IRIS: url=" + url);
        String xml = null;

        //Iris throws a 404 if the are no results from the query
        try {
            xml = IOUtil.readContents(url, getClass());
        } catch (IOException ioe) {
            return sites;
        }
        Element root = XmlUtil.getRoot(xml);
        if (root == null) {
            System.err.println("GSAC: XML parse error:" + xml);
            return sites;
        }
        NodeList networkElements = XmlUtil.getElements(root,
                                       IrisXml.TAG_NETWORK);
        for (int i = 0; i < networkElements.getLength(); i++) {
            Element networkElement = (Element) networkElements.item(i);
            NodeList stationElements = XmlUtil.getElements(networkElement,
                                           IrisXml.TAG_STATION);
            for (int stationIdx = 0; stationIdx < stationElements.getLength();
                    stationIdx++) {
                Element stationElement =
                    (Element) stationElements.item(stationIdx);
                GsacSite site = makeSite(stationElement);
                sites.add(site);
            }
        }
        return sites;
    }

    /**
     * _more_
     *
     * @param stationElement _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private GsacSite makeSite(Element stationElement) throws Exception {
        String network = XmlUtil.getAttribute(stationElement,
                             IrisXml.ATTR_NET_CODE, "");
        String stationCode = XmlUtil.getAttribute(stationElement,
                                 IrisXml.ATTR_STA_CODE, "");
        GsacSite site = new GsacSite(stationCode, stationCode, stationCode);
        IconMetadata iconMetadata =
            new IconMetadata(
                "http://www.iris.edu/images/symbols/circle-dot-10x10-FF3333.png");
        iconMetadata.setForDisplay(false);
        site.addMetadata(iconMetadata);
        site.addMetadata(new LinkMetadata("http://www.iris.edu/mda/"
                                          + network + "/"
                                          + stationCode, stationCode
                                              + "@IRIS"));

        List<ResourceGroup> resourceGroups = new ArrayList<ResourceGroup>();
        resourceGroups.add(new ResourceGroup(network));
        site.setResourceGroups(resourceGroups);
        NodeList epochElements = XmlUtil.getElements(stationElement,
                                     IrisXml.TAG_STATIONEPOCH);
        for (int epochIdx = 0; epochIdx < epochElements.getLength();
                epochIdx++) {
            Element epochElement = (Element) epochElements.item(epochIdx);
            String lat = XmlUtil.getGrandChildText(epochElement,
                             IrisXml.TAG_LAT, "0.0");
            String lon = XmlUtil.getGrandChildText(epochElement,
                             IrisXml.TAG_LON, "0.0");
            String elevation = XmlUtil.getGrandChildText(epochElement,
                                   IrisXml.TAG_ELEVATION, "0.0");
            Element siteElement = XmlUtil.getElement(epochElement,
                                      IrisXml.TAG_SITE);
            if (siteElement != null) {
                String country = XmlUtil.getGrandChildText(siteElement,
                                     IrisXml.TAG_COUNTRY, (String) null);
                //This really isn't country
                if (country != null) {
                    site.addMetadata(new PropertyMetadata("location",
                            country, "Location"));
                }
            }
            site.setLatitude(Double.parseDouble(lat));
            site.setLongitude(Double.parseDouble(lon));
            site.setElevation(Double.parseDouble(elevation));
            site.setLongName(XmlUtil.getGrandChildText(epochElement,
                    IrisXml.TAG_NAME, stationCode));
            String numChannels = XmlUtil.getGrandChildText(epochElement,
                                     IrisXml.TAG_TOTALNUMBERCHANNELS,
                                     (String) null);
            if (numChannels != null) {
                site.addMetadata(new PropertyMetadata("numberofchannels",
                        numChannels, "Number of Channels"));

            }
            break;
        }
        return site;
    }


    /**
     * CHANGEME
     *
     * @param resourceId resource id. This isn't the resource code but actually the monument id
     *
     * @return the resource or null if not found
     *
     * @throws Exception on badness
     */
    public GsacResource getResource(String resourceId) throws Exception {
        List<String> args = new ArrayList<String>();
        args.add(HtmlUtil.arg(ARG_IRIS_STA, resourceId));
        List<GsacSite> sites = getSites(args);
        if (sites.size() == 0) {
            return null;
        }
        return sites.get(0);
    }



    /**
     * get all of the metadata for the given site
     *
     *
     * @param level _more_
     * @param gsacResource resource
     *
     * @throws Exception On badness
     */
    public void doGetMetadata(int level, GsacResource gsacResource)
            throws Exception {}

    /**
     * Get the extra site search capabilities.
     *
     * @return site search capabilities
     */
    public List<Capability> doGetQueryCapabilities() {
        List<Capability> capabilities = new ArrayList<Capability>();
        String           help         = HtmlOutputHandler.stringSearchHelp;
        Capability       siteCode;
        Vocabulary groupVocabulary =
            getRepository().getVocabularyFromType(ARG_SITE_GROUP);
        List<ResourceGroup> siteGroups =
            ResourceGroup.convertList(groupVocabulary.getValues());
        Capability[] dflt = { siteCode = initCapability(new Capability(ARG_SITE_CODE, "Site Code", Capability
                                .TYPE_STRING), CAPABILITY_GROUP_SITE_QUERY, "Short name of the site", "Short name of the site. "
                                    + help),
                              initCapability(
                                  new Capability(
                                      makeUrlArg(ARG_SUFFIX_GROUP),
                                      "Network", IdLabel.toList(siteGroups),
                                      true), CAPABILITY_GROUP_ADVANCED, null),
                              initCapability(new Capability(ARG_SITE_CHANNEL, "Channel", Capability
                                  .TYPE_STRING), CAPABILITY_GROUP_SITE_QUERY, "Spatial bounds within which the site lies"),
                              initCapability(new Capability(ARG_BBOX, "Bounds", Capability
                                  .TYPE_SPATIAL_BOUNDS), CAPABILITY_GROUP_SITE_QUERY, "Spatial bounds within which the site lies") };



        siteCode.setBrowse(true);
        for (Capability capability : dflt) {
            if (capability != null) {
                capabilities.add(capability);
            }
        }
        capabilities.add(initCapability(new Capability(ARG_SITE_DATE,
                "Time Window",
                Capability.TYPE_DATERANGE), CAPABILITY_GROUP_SITE_QUERY,
                    "Specify a time range that stations/channels must be operating within"));


        capabilities.add(initCapability(new Capability(ARG_SITE_STARTDATE,
                "Start Date",
                Capability.TYPE_DATERANGE), CAPABILITY_GROUP_SITE_QUERY,
                                            "Station start date"));

        capabilities.add(initCapability(new Capability(ARG_SITE_ENDDATE,
                "End Date",
                Capability.TYPE_DATERANGE), CAPABILITY_GROUP_SITE_QUERY,
                                            "Station end date"));

        return capabilities;
    }


    /**
     * Get the site group list. This is used by the addDefaultSiteCapabilities
     *
     * @return site group list
     */
    public List<ResourceGroup> doGetResourceGroups() {
        List<ResourceGroup> groups = new ArrayList<ResourceGroup>();

        /**
         *  CHANGEME
         * groups.add(new ResourceGroup("group1","Group 1"));
         * groups.add(new ResourceGroup("group2", "Group 2"));
         * groups.add(new ResourceGroup("group3","Group 3"));
         * Collections.sort((List) groups);
         */
        return groups;
    }




}
