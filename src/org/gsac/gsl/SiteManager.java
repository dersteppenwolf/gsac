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

import org.ramadda.sql.Clause;
import org.ramadda.sql.SqlUtil;

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
 * revised Feb. 25 2013; Mar 4, 2013.  New choices and ordering of OutputHandlers; see method public void initOutputHandlers().
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
     * Order here is order presented to the user, in the GSAC site search form menu, and in information page:
     *
     * FIX bug: it seems that whichever handler is first in order below gets called when the web site search page (the from page, not a real search) is first called for, 
     * BEFORE any query is made, which for some handlers can cause a failure and error to browser and no site search results shown. 
     * But the HTML handler seems to always work OK.
     */
    @Override
    public void initOutputHandlers() {
        super.initOutputHandlers();

        /* Comment out lines for handlers NOT wanted to be offered by your GSAC-WS repository.  */
        /* For example if you do NOT want to provide the GSAC "Short csv" format, comment out (put // before) new TextSiteLogOutputHandler(getRepository(), getResourceClass()); */
        /* However you are encouraged to allow all these, to show consistent results from all GSAC repositories. */
        /* There is no harm in offering all the choices, even if you do not use one or more.  Others may be using them.*/

        // in approximate order of expected popularity of use.

        // results put in HTML, for web pages and other HTML uses:
        new HtmlSiteOutputHandler(getRepository(), getResourceClass());

        // for SINEX format  
        new SinexSiteOutputHandler(getRepository(), getResourceClass());

        // for GAMIT's station.info format  
        new StationInfoSiteOutputHandler(getRepository(), getResourceClass());

        // for SOPAC XMP site log format  (and,  what format is XmlSiteOutputHandler(getRepository(), getResourceClass()); ?)
        new XmlSiteLogOutputHandler(getRepository(), getResourceClass());

        // for Google Earth KMZ and KML
        new KmlSiteOutputHandler(getRepository(), getResourceClass());  

        // JSON.  this output format this is used by Scott Baker's geodesy aggregator search tool, so has real and current use.
        new JsonSiteOutputHandler(getRepository(), getResourceClass());
        

        // some good but less-used formats

        // the GSAC "plain text format" used to visually check what is available for sites' info.  Not for computer processing. 
        new PlainTextSiteOutputHandler(getRepository(), getResourceClass()); 

        // the GSAC "full csv" format
        new CsvFullSiteOutputHandler(getRepository(), getResourceClass());   // for full csv formatted file of site metadata


        // relics and other formats of no apparent use for geodecists.

        // the GSAC "short csv format, an old legacy short csv formatted file of limited contents, an old minor format kept only for backward compatibility in case anyone ever used it:
        new TextSiteOutputHandler   (getRepository(), getResourceClass());   

        // other IT formats; not yet providing anything useful but someome may wish to complete them, or use these formats.
        new AtomSiteOutputHandler(getRepository(), getResourceClass());
        new RssSiteOutputHandler(getRepository(), getResourceClass());
        
        //  GSAC (short) XML , different from XmlSiteLogOutputHandler  for SOPAC XML.  Not at all clear why we need this.  
        new XmlSiteOutputHandler(getRepository(), getResourceClass());

        // not yet implemented: IGS site log; FIX SiteLogOutputHandler gives empty file. Do we want to encourage this format? Only if users demand it.
        //new SiteLogOutputHandler(getRepository(), getResourceClass()); 
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
