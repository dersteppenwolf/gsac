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

package org.gsac.gsl;

import org.gsac.gsl.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.output.*;

import org.gsac.gsl.output.site.*;
import org.gsac.gsl.util.*;

import ucar.unidata.sql.Clause;
import ucar.unidata.sql.SqlUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;


/**
 * Handles all of the site related repository requests. 
 *
 * CHANGEME. If you are using the GsacRepository/SiteManager
 * functionality then there are a minimum of 2 methods you need to overwrite:<br>
 * {@link #getSite} and {@link #handleSiteRequest}
 * This class has a default implementation of handleSiteRequest. To use this you need to
 * implement a number of other methods for creating the search clause, etc. See the
 * docs for {@link #handleSiteRequest}
 *
 *
 * @author Jeff McWhirter mcwhirter@unavco.org
 * revised Feb. 25 2013
 */
public abstract class SiteManager extends GsacResourceManager {

    /** name for the basic site query capabilities */
    public static final String CAPABILITY_GROUP_SITE_QUERY = "Site Query";

    /** name for the advanced group of site query capabilities */
    public static final String CAPABILITY_GROUP_ADVANCED =
        "Advanced Site Query";

    /**
     * ctor
     *
     * @param repository the repository
     */
    public SiteManager(GsacRepository repository) {
        super(repository, GsacSite.CLASS_SITE);
    }


    /**
     * _more_
     *
     * @param plural _more_
     *
     * @return _more_
     */
    public String getResourceLabel(boolean plural) {
        return (plural
                ? "Sites"
                : "Site");
    }


    /**
     * Helper method to create default resource query capabilities.
     * This adds capabilities for:<ul>
     * <li> site code
     * <li> site name
     * <li> site type
     * <li> site status
     * <li> site groups if there are any
     * <li> site spatial bounds
     * </ul>
     *
     * @param capabilities list of capabailities to add to
     */
    public void addDefaultCapabilities(List<Capability> capabilities) {
        String              help = HtmlOutputHandler.stringSearchHelp;
        Capability          siteCode;
        Capability          siteName;
        List<ResourceGroup> siteGroups = doGetResourceGroups();

        Capability[]        dflt       = {
            siteCode = initCapability(new Capability(ARG_SITE_CODE,
                "Site Code",
                Capability.TYPE_STRING), CAPABILITY_GROUP_SITE_QUERY,
                                         "Short name of the site",
                                         "Short name of the site. " + help),
            initCapability(siteName = new Capability(ARG_SITE_NAME,
                "Site Name",
                Capability.TYPE_STRING), CAPABILITY_GROUP_SITE_QUERY,
                                         "Name of the site",
                                         "Name of site." + help),
            initCapability(new Capability(ARG_SITE_TYPE, "Site Type",
                                          new ArrayList<IdLabel>(),
                                          true), CAPABILITY_GROUP_SITE_QUERY,
                                              "Type of the site", null,
                                              makeVocabulary(ARG_SITE_TYPE)),
            initCapability(
                new Capability(
                    ARG_SITE_STATUS, "Site Status", new ArrayList<IdLabel>(),
                    true), CAPABILITY_GROUP_ADVANCED, "", "",
                           makeVocabulary(ARG_SITE_STATUS)),
            ((siteGroups.size() == 0)
             ? null
             : initCapability(
                 new Capability(
                     makeUrlArg(ARG_SUFFIX_GROUP), "Site Group",
                     IdLabel.toList(siteGroups),
                     true), CAPABILITY_GROUP_ADVANCED, null)),
            initCapability(new Capability(ARG_BBOX, "Bounds",
                Capability.TYPE_SPATIAL_BOUNDS), CAPABILITY_GROUP_SITE_QUERY,
                    "Spatial bounds within which the site lies")
        };
        siteCode.setBrowse(true);
        siteName.setBrowse(true);
        for (Capability capability : dflt) {
            if (capability != null) {
                capabilities.add(capability);
            }
        }
    }



    /**
     * Create the output handlers for this resource, which handles (formats) the query results.
     *
     * Note. is this right? -- whichever is first in order below gets called when the web site search page is first called for, 
     *   *before* any query is made, which can cause a failure and error to browser and no site search page shown. HTML seems to always work OK.
     */
    @Override
    public void initOutputHandlers() {
        super.initOutputHandlers();

        /* CHANGEME: Comment out lines for handlers (which make output in files with particular formats of results to be sent to remote user) NOT wanted to be offered by your GSAC-WS repository.  */
        /* for example if you do not want to provide the SOPAC XMP site log format, comment out (put "//" before) new XmlSiteLogOutputHandler(getRepository(), getResourceClass()); */
        /* However you are encouraged to allow all these no commented out in GSAC code on sourceforge, to permit use of federated GSAC collections. */

        // results put in HTML, for web pages and other HTML uses:
        new HtmlSiteOutputHandler(getRepository(), getResourceClass());

        // for SOPAC XMP site log format 
        new XmlSiteLogOutputHandler(getRepository(), getResourceClass());

        // for SINEX format  
        new SinexSiteOutputHandler(getRepository(), getResourceClass());

        // for GAMIT's station.info format  
        new StationInfoSiteOutputHandler(getRepository(), getResourceClass());

        // there is no call for this yet: to make results as a IGS site log; FIX SiteLogOutputHandler gives empty file
        //new SiteLogOutputHandler(getRepository(), getResourceClass()); 

        // a plain text format to visually check what is available for sites' info.  Not for computer processing. Originally for GSAC developers. 
        new PlainTextSiteOutputHandler(getRepository(), getResourceClass()); 

        new CsvFullSiteOutputHandler(getRepository(), getResourceClass());   // for full csv formatted file of site metadata

        new TextSiteOutputHandler   (getRepository(), getResourceClass());   // for short csv formatted file of limited contents

        // the following formats only show a few parameters in results; more code needs to be written:
        //new KmlSiteOutputHandler(getRepository(), getResourceClass());  // for Google Earth KMZ and KML
        /* FIX KmlSiteOutputHandler fails with
    javax.xml.parsers.FactoryConfigurationError: Provider org.apache.xerces.jaxp.DocumentBuilderFactoryImpl not found
    at javax.xml.parsers.DocumentBuilderFactory.newInstance(DocumentBuilderFactory.java:129)
    at ucar.unidata.xml.XmlUtil.getDocument(XmlUtil.java:1561)
    at ucar.unidata.xml.XmlUtil.makeDocument(XmlUtil.java:1487)
    at ucar.unidata.data.gis.KmlUtil.kml(KmlUtil.java:175)
    at org.gsac.gsl.output.site.KmlSiteOutputHandler.handleRequest(KmlSiteOutputHandler.java:145)
    at org.gsac.gsl.output.GsacOutputHandler.handleRequest(GsacOutputHandler.java:193)
    at org.gsac.gsl.GsacRepository.handleRequest(GsacRepository.java:567)
    at org.gsac.gsl.GsacServlet.doGet(GsacServlet.java:310)
    at javax.servlet.http.HttpServlet.service(HttpServlet.java:735)
    at javax.servlet.http.HttpServlet.service(HttpServlet.java:848)
    at org.mortbay.jetty.servlet.ServletHolder.handle(ServletHolder.java:511)
        */

        // the following are usually not used in geodesy data repositories at date of the last revision to this file.
        //new JsonSiteOutputHandler(getRepository(), getResourceClass());
        //new RssSiteOutputHandler(getRepository(), getResourceClass());
        //new AtomSiteOutputHandler(getRepository(), getResourceClass());
        //how is this different from the above XmlSiteLogOutputHandler:  XmlSiteOutputHandler(getRepository(), getResourceClass());
    }



    /**
     * For backwards compatability with sopac
     *
     * public String getSiteSelectSuffix(GsacRequest request) {
     *   return getResourceSelectSuffix(request);
     * }
     *
     *
     * For backwards compatability with sopac
     *
     *
     * @param request _more_
     * @param response _more_
     * @param tableNames _more_
     *
     * @return _more_
     */
    public Clause getSiteClause(GsacRequest request, GsacResponse response,
                                List<String> tableNames) {
        return getResourceClause(request, response, tableNames);
    }



}
