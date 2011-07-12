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

package org.gsac.gsl;


import org.gsac.gsl.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.output.*;
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
 * Handles all of the site related repository requests. If you are using the GsacRepositoryImpl/SiteManager
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


    public String getResourceLabel(boolean plural) {
        return (plural?"Sites":"Site");
    }


    /**
     * Handle the site request. A derived class can overwrite this method to do
     * whatever they feel like doing. If not overwritten then this method
     * does a basic select query and processes the results making use of the
     * derived class methods:<pre>
     * {@link #getSiteClauses} - returns a list of the select clauses. This list is then anded together to form the query
     * {@link #getSiteSelectColumns} - The comma separated list of fully qualified (i.e., tablename prepended) column names to select
     * {@link #getSiteOrder} - optional method to return the order by sql directive
     * {@link #makeSite}  - This creates the GsacSite from the given resultset
     * </pre>
     *
     * @param request the resquest
     * @param response the response
     *
     * @throws Exception on badness
     */
    public void handleRequest(GsacRequest request, GsacResponse response)
            throws Exception {
        String columns = getSiteSelectColumns();
        if (columns == null) {
            return;
        }
        if (getDatabaseManager() == null) {
            return;
        }
        long         t1         = System.currentTimeMillis();
        List<String> tableNames = new ArrayList<String>();
        Clause       clause     = getSiteClause(request, response,
                                      tableNames);
        Statement statement = getDatabaseManager().select(columns,
                                  clause.getTableNames(tableNames), clause,
                                  getSiteSelectSuffix(request), -1);

        processStatement(request, response, statement, request.getOffset(),
                         request.getLimit());
    }



    /**
     * Iterate on the query statement and create sites.
     * Skip by the given offset and only process limit sites
     *
     * @param request the request
     * @param response the response
     * @param statement statement
     * @param offset skip
     * @param limit max number of sites to create
     *
     * @return count of how many sites were created
     *
     * @throws Exception On badness
     */
    public int processStatement(GsacRequest request, GsacResponse response,
                                Statement statement, int offset, int limit)
            throws Exception {
        long t1 = System.currentTimeMillis();
        //Iterate on the query results
        SqlUtil.Iterator iter = SqlUtil.getIterator(statement, offset, limit);
        while (iter.getNext() != null) {
            GsacSite site = makeSite(iter.getResults());
            if (site == null) {
                continue;
            }
            response.addResource(site);
            if ( !iter.countOK()) {
                response.setExceededLimit();
                break;
            }
        }
        iter.close();
        getDatabaseManager().closeAndReleaseConnection(statement);
        long t2 = System.currentTimeMillis();
        System.err.println("read " + iter.getCount() + " sites in "
                           + (t2 - t1) + "ms");
        return iter.getCount();
    }



    /**
     * get the site query clause. This also sets the seach criteria message on the response
     *
     * @param request the resquest
     * @param response the response
     * @param tableNames List of table names for the query
     *
     * @return site query clause
     */
    public Clause getSiteClause(GsacRequest request, GsacResponse response,
                                List<String> tableNames) {
        StringBuffer msgBuff = new StringBuffer();
        List<Clause> clauses = getSiteClauses(request, response, tableNames,
                                   msgBuff);
        setSearchCriteriaMessage(response, msgBuff);
        return Clause.and(clauses);
    }


    /**
     * Get the comma delimited list of columns to select on a site query
     *
     *
     * @return site columns
     */
    public String getSiteSelectColumns() {
        notImplemented("getSiteSelectColumns needs to be implemented");
        return "";
    }


    /**
     * this returns the order by clause and anything else that needs to be tacked onto the end of the site query
     *
     * @param request The request
     *
     * @return the sql suffix
     */
    public String getSiteSelectSuffix(GsacRequest request) {
        return getSiteOrder(request);
    }


    /**
     * Get the order by sql directive when doing site queries
     *
     * @param request the request
     *
     * @return the order by sql directive
     */
    public String getSiteOrder(GsacRequest request) {
        return "";
    }


    /**
     * Get the list of clauses when querying sites
     *
     * @param request the request
     * @param response the response
     * @param tableNames List of table names. add any table names in the query to this list
     * @param msgBuff for the search criteria
     *
     * @return site query clauses
     */
    public List<Clause> getSiteClauses(GsacRequest request,
                                       GsacResponse response,
                                       List<String> tableNames,
                                       StringBuffer msgBuff) {
        notImplemented("getSiteClauses needs to be implemented");
        return null;
    }



    /**
     * Create a single site from the given resultset
     *
     * @param results db results
     *
     * @return the site
     *
     * @throws Exception on badness
     */
    public GsacSite makeSite(ResultSet results) throws Exception {
        notImplemented("makeSite needs to be implemented");
        return null;
    }


    /**
     * Helper method to create default site query capabilities.
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
        String          help = HtmlOutputHandler.stringSearchHelp;
        Capability      siteCode;
        Capability      siteName;
        List<ResourceGroup> siteGroups = doGetResourceGroups();
        Capability[]    dflt       = {
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
                    true), CAPABILITY_GROUP_SITE_QUERY, "", "",
                           makeVocabulary(ARG_SITE_STATUS)),
            ((siteGroups.size() == 0)
             ? null
             : initCapability(
                 new Capability(
                     ARG_RESOURCE_GROUP, "Site Group",
                     IdLabel.toList(siteGroups),
                     true), CAPABILITY_GROUP_SITE_QUERY, null)),
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



}
