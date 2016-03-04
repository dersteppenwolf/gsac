/*
 * Copyright 2010; 2014 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
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

import org.gsac.gsl.ramadda.sql.Clause;
import org.gsac.gsl.ramadda.sql.SqlUtil;

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
 * revised Feb. 25 2013; Mar 4, 2013; July 8, 2014.  New choices and ordering of OutputHandlers; see method public void initOutputHandlers().
 *
 *
 * @author Jeff McWhirter mcwhirter@unavco.org
 */
public abstract class SiteManager extends GsacResourceManager {

    /** name for the basic site query capabilities */
    public static final String CAPABILITY_GROUP_SITE_QUERY = "Site Query";

    /** name for the advanced group of site query capabilities */
    public static final String CAPABILITY_GROUP_ADVANCED   = "Advanced Site Query";

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

        Capability[]        dflt       = {
            siteCode = initCapability(new Capability(ARG_SITE_CODE,
                "Site Code",
                Capability.TYPE_STRING), CAPABILITY_GROUP_SITE_QUERY,
                                         "Short name of the site (often 4 char ID)",
                                         "Short name of the site (often 4 char ID). " + help),
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
     * Create the output handlers for this GSAC repository's services, which handles (formats) the query results.
     *
     * Order of handlers below is the order presented to the user, in the GSAC site search forms, and in information page.
     *
     * Always list the HTML handler first,
     * because it seems that whichever handler is first in order below gets called when the web site search page 
     * (not a real search) is first called for, before any query is made, which for some handlers can cause a failure 
     * and error to browser and no site search results shown. The HTML handler always works.
     */
    @Override
    public void initOutputHandlers() {
        super.initOutputHandlers();

        /* 
           Comment out lines for handlers NOT wanted to be offered by your GSAC-WS repository.  
           For example if you do NOT want to provide ATOM, comment out new AtomSiteOutputHandler(getRepository(), getResourceClass());
        */

        // 1. results in HTML format, for GSAC web pages. The common first results for site searches.
        //  Always list the HTML handler first,
        new HtmlSiteOutputHandler(getRepository(), getResourceClass());

        // Order of handlers here below is the order presented to the user in the GSAC site search forms, and in information page.

        // for SINEX format  
        new SinexSiteOutputHandler(getRepository(), getResourceClass());

        // for GAMIT's station.info format  
        new StationInfoSiteOutputHandler(getRepository(), getResourceClass());

        // for Google Earth KMZ 
        new KmlSiteOutputHandler(getRepository(), getResourceClass());  

        // placeholder for the new IGS XML site log format, 2016. Not yet operational complete or correct.  Do not use.
        // new IGSXmlSiteLogOutputHandler(getRepository(), getResourceClass());

        // for SOPAC XML site log format  
        new XmlSiteLogOutputHandler(getRepository(), getResourceClass());

        // for the GSAC JSON site info format. This GSAC output is used by a supersites aggregator tool or web site.
        new JsonSiteOutputHandler(getRepository(), getResourceClass());
        
        // the 2010 GSAC short csv format
        new TextSiteOutputHandler(getRepository(), getResourceClass());   

        // for the new 2013 GSAC "Full Csv" format
        new CsvFullSiteOutputHandler(getRepository(), getResourceClass());

        // for the GSAC Ops XML site info format; new on 22 May 2014; creatted for UNAVCO field engineering operations.
        new OpsXmlSiteOutputHandler(getRepository(), getResourceClass());

        //  for the 2010 GSAC XML site info format; functionally replaced by the GSAC Ops XML site info format.
        new XmlSiteOutputHandler(getRepository(), getResourceClass());

        // there is no indication that anyone used these output formats since 2013:
        // new RssSiteOutputHandler(getRepository(), getResourceClass()); 
        // new AtomSiteOutputHandler(getRepository(), getResourceClass());
        // new PlainTextSiteOutputHandler(getRepository(), getResourceClass()); //  GSAC format in plain text for human readability; 
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
