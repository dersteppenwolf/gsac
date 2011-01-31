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
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.Misc;



import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;


/**
 * Handles all of the site related repository requests. If you are using the GsacRepositoryImpl/SiteManager
 * functionality then there are a minimum of 2 methods you need to overwrite:<pre>
 * getSite
 * handleSiteRequest
 * </pre>
 * This class has a default implementation of handleSiteRequest. To use this you need to
 * implement a number of other methods for creating the search clause, etc. See the
 * docs for handleSiteRequest
 *
 *  There are a number of doGet... methods that can be overwritten if desired (e.g., doGetSiteGroups)
 *
 * @author Jeff McWhirter mcwhirter@unavco.org
 */
public abstract class SiteManager extends GsacRepositoryManager {

    /**
     * ctor
     *
     * @param repository the repository
     */
    public SiteManager(GsacRepository repository) {
        super(repository);
    }



    /**
     * Get the site from the database
     *
     * @param siteId site id.
     *
     * @return the site or null if not found
     *
     * @throws Exception on badness
     */
    public abstract GsacSite getSite(String siteId) throws Exception;

    /**
     * Handle the site request. A derived class can overwrite this method to do
     * whatever they feel like doing. If not overwritten then this method
     * does a basic select query and processes the results making use of the
     * derived class methods:<pre>
     * getSiteClauses - returns a list of the select clauses. This list is then anded together to form the query
     * getSiteSelectColumns - The comma separated list of fully qualified (i.e., tablename prepended)
     *    column names to select
     * getSiteOrder - optional method to return the order by sql directive
     * makeSite  - This creates the GsacSite from the given resultset
     * </pre>
     *
     * @param request the resquest
     * @param response the response
     *
     * @throws Exception on badness
     */
    public void handleSiteRequest(GsacRequest request, GsacResponse response)
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
        long             t1   = System.currentTimeMillis();
        SqlUtil.Iterator iter = SqlUtil.getIterator(statement, offset, limit);
        while (iter.getNext() != null) {
            response.addSite(makeSite(iter.getResults()));
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
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
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
     * get all of the metadata for the given site
     *
     *
     * @param level _more_
     * @param gsacSite site
     *
     * @throws Exception On badness
     */
    public void doGetSiteMetadata(int level, GsacSite gsacSite)
            throws Exception {
        doGetFullSiteMetadata(gsacSite);
    }

    /**
     * _more_
     *
     * @param gsacSite _more_
     *
     * @throws Exception _more_
     */
    public void doGetFullSiteMetadata(GsacSite gsacSite) throws Exception {}

    /**
     * Get the extra site search capabilities. This
     * calls makeCapabilities to actually make them
     *
     * @return site search capabilities
     */
    public List<Capability> doGetSiteQueryCapabilities() {
        //default is to do nothing
        return new ArrayList<Capability>();
    }




    /**
     * Add basic bounding box query clauses to the list of clauses
     * and append to the search criteria msgBuff
     * This looks for the ARG_NORTH/ARG_SOUTH/ARG_EAST/ARG_WEST
     * url arguments and does a simple bounds query
     *
     * @param request The request
     * @param clauses list of clauses  to add to
     * @param latitudeColumn column name for latitude
     * @param longitudeColumn column name for longitude
     * @param msgBuff search criteria message buffer
     */
    public void addBoundingBoxSearch(GsacRequest request,
                                     List<Clause> clauses,
                                     String latitudeColumn,
                                     String longitudeColumn,
                                     StringBuffer msgBuff) {
        if (request.defined(ARG_NORTH)) {
            clauses.add(Clause.le(latitudeColumn,
                                  request.getLatLon(ARG_NORTH, 0.0)));
            appendSearchCriteria(msgBuff, "north&lt;=",
                                 "" + request.getLatLon(ARG_NORTH, 0.0));
        }
        if (request.defined(ARG_SOUTH)) {
            clauses.add(Clause.ge(latitudeColumn,
                                  request.getLatLon(ARG_SOUTH, 0.0)));
            appendSearchCriteria(msgBuff, "south&gt;=",
                                 "" + request.getLatLon(ARG_SOUTH, 0.0));
        }
        if (request.defined(ARG_EAST)) {
            clauses.add(Clause.le(longitudeColumn,
                                  request.getLatLon(ARG_EAST, 0.0)));
            appendSearchCriteria(msgBuff, "east&lt;=",
                                 "" + request.getLatLon(ARG_EAST, 0.0));
        }
        if (request.defined(ARG_WEST)) {
            clauses.add(Clause.ge(longitudeColumn,
                                  request.getLatLon(ARG_WEST, 0.0)));
            appendSearchCriteria(msgBuff, "west&gt;=",
                                 "" + request.getLatLon(ARG_WEST, 0.0));
        }
    }

    /**
     * Utility method to make a list of site statuses from the given
     * list of id values
     *
     * @param values site status values
     *
     * @return list of site statuses
     */
    public List<SiteStatus> makeSiteStatuses(String[] values) {
        return makeSiteStatuses(toTuples(values));
    }

    /**
     * Utility method to make a list of site statuses from the given
     * array of id/value pairs. Note: if values[N].length ==2
     * then values[N][0] is the id, values[N][1] is the label.
     * if values[N].length ==1 then we just use the id as the label
     *
     *
     * @param tuples id/value pairs
     *
     * @return list of site statuses
     */
    public List<SiteStatus> makeSiteStatuses(String[][] tuples) {
        List<SiteStatus> statuses = new ArrayList<SiteStatus>();
        for (String[] tuple : tuples) {
            if (tuple.length == 1) {
                statuses.add(new SiteStatus(tuple[0], tuple[0]));
            } else {
                statuses.add(new SiteStatus(tuple[0], tuple[1]));
            }
        }
        return statuses;
    }





    /** _more_ */
    public static final String GROUP_SITE_QUERY = "Site Query";

    /**
     * _more_
     *
     * @param capabilities _more_
     */
    public void addDefaultSiteCapabilities(List<Capability> capabilities) {
        String       help = HtmlOutputHandler.stringSearchHelp;
        Capability   siteCode;
        Capability   siteName;

        Capability[] dflt = {
            siteCode = initCapability(new Capability(ARG_SITE_CODE,
                "Site Code", Capability.TYPE_STRING), GROUP_SITE_QUERY,
                    "Short name of the site",
                    "Short name of the site. " + help),
            initCapability(siteName = new Capability(ARG_SITE_NAME,
                "Site Name", Capability.TYPE_STRING), GROUP_SITE_QUERY,
                    "Name of the site", "Name of site." + help),
            initCapability(new Capability(ARG_SITE_TYPE, "Site Type",
                                          new ArrayList<IdLabel>(),
                                          true), GROUP_SITE_QUERY,
                                              "Type of the site", null,
                                              makeVocabulary(ARG_SITE_TYPE)),
            initCapability(
                new Capability(
                    ARG_SITE_STATUS, "Site Status", new ArrayList<IdLabel>(),
                    true), GROUP_SITE_QUERY, "", "",
                           makeVocabulary(ARG_SITE_STATUS)),
            initCapability(new Capability(ARG_SITE_GROUP, "Site Group",
                                          new ArrayList<IdLabel>(),
                                          true), GROUP_SITE_QUERY, null),
            initCapability(
                new Capability(
                    ARG_BBOX, "Bounds",
                    Capability.TYPE_SPATIAL_BOUNDS), GROUP_SITE_QUERY,
                        "Spatial bounds within which the site lies")
        };
        siteCode.setBrowse(true);
        siteName.setBrowse(true);
        for (Capability capability : dflt) {
            capabilities.add(capability);
        }
    }


    /**
     * Utility method to make a list of site types from the given
     * list of id values
     *
     * @param values site type values
     *
     * @return list of site types
     */
    public List<SiteType> makeSiteTypes(String[] values) {
        return makeSiteTypes(toTuples(values));
    }


    /**
     * Utility method to make a list of site types from the given
     * array of id/value pairs. Note: if values[N].length ==2
     * then values[N][0] is the id, values[N][1] is the label.
     * if values[N].length ==1 then we just use the id as the label
     *
     *
     * @param tuples id/value pairs
     *
     * @return list of site types
     */
    public List<SiteType> makeSiteTypes(String[][] tuples) {
        List<SiteType> result = new ArrayList<SiteType>();
        for (String[] tuple : tuples) {
            SiteType type;
            if (tuple.length == 1) {
                type = new SiteType(tuple[0], tuple[0]);
            } else {
                type = new SiteType(tuple[0], tuple[1]);
            }
            result.add(type);
        }
        return result;
    }




}
