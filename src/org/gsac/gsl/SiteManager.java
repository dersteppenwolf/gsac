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
 * Handles all of the site related repository requests. If you are using the GsacRepository/SiteManager
 * functionality then there are a minimum of 2 methods you need to overwrite:<br>
 * {@link #getSite} and {@link #handleSiteRequest}
 * This class has a default implementation of handleSiteRequest. To use this you need to
 * implement a number of other methods for creating the search clause, etc. See the
 * docs for {@link #handleSiteRequest}
 *
 *
 * @author Jeff McWhirter mcwhirter@unavco.org
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
     * Comment out lines for handlers (for formats of results sent to remote user) NOT to be offered by your GSAC-WS repository. 
     *
     * Note. is this right? -- whichever is first in order below gets called when the web site search page is first called for 
     *   -- before any query is made, which can cause a failure and error to browser and no site search page shown. HTML seems to always work OK.
     */
    @Override
    public void initOutputHandlers() {
        super.initOutputHandlers();

        // results put in HTML, for web pages and other HTML uses:
        new HtmlSiteOutputHandler(getRepository(), getResourceClass());

        // for SOPAC XMP site log format 
        new XmlSiteLogOutputHandler(getRepository(), getResourceClass());

        // for SINEX format  
        new SinexSiteOutputHandler(getRepository(), getResourceClass());

        // for GAMIT's station.info format  
        new StationInfoSiteOutputHandler(getRepository(), getResourceClass());

        // to make results as a IGS site log; LOOK FIX gives empty file
        //new SiteLogOutputHandler(getRepository(), getResourceClass()); 

        new TextSiteOutputHandler(getRepository(), getResourceClass());   // for csv formatted file

        // a plain text format to visually check what is available for sites' info.  Not for computer processing. Originally for GSAC developers. 
        new PlainTextSiteOutputHandler(getRepository(), getResourceClass()); 

        // the following formats only show a few parameters in results; more code needs to be written:
        //new KmlSiteOutputHandler(getRepository(), getResourceClass());  // for Google Earth KMZ and KML
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
