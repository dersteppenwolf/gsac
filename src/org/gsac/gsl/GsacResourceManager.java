/*
 * Copyright 2014 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
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


import org.ramadda.sql.Clause;
import org.ramadda.sql.SqlUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.Misc;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


/**
 * Base class for site and file managers
 *
 *
 * @author  Jeff McWhirter
 */
public abstract class GsacResourceManager extends GsacRepositoryManager {

    /** cache */
    private TTLCache<Object, GsacResource> resourceCache =
        new TTLCache<Object, GsacResource>(TTLCache.MS_IN_A_DAY);

    /** _more_ */
    private ResourceClass resourceClass;

    /** _more_ */
    private List<GsacOutput> outputs = new ArrayList<GsacOutput>();

    /** _more_ */
    private Hashtable<String, GsacOutput> outputMap = new Hashtable<String,
                                                          GsacOutput>();


    /** _more_ */
    private CapabilityCollection capabilityCollection;


    /** _more_ */
    private String urlPrefix;

    /**
     * _more_
     *
     * @param repository _more_
     * @param resourceClass _more_
     */
    public GsacResourceManager(GsacRepository repository,
                               ResourceClass resourceClass) {
        super(repository);
        this.resourceClass = resourceClass;
        urlPrefix          = getRepository().getUrlBase() + URL_BASE + "/"
                    + getResourceClass().getName();

    }

    /**
     * _more_
     *
     */
    public static String getUTCnowString() {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String utcTime = sdf.format(new Date());
        return utcTime;
    }


    /**
     * _more_
     *
     */
    public void initOutputHandlers() {}


    /**
     * _more_
     *
     * @return _more_
     */
    public List<GsacOutput> getOutputs() {
        return outputs;
    }

    /**
     * _more_
     *
     * @param output _more_
     */
    public void addOutput(GsacOutput output) {
        outputs.add(output);
        outputMap.put(output.getId(), output);
    }

    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    public GsacOutput getOutput(String id) {
        return outputMap.get(id);
    }


    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public GsacOutputHandler getOutputHandler(GsacRequest request) {
        String arg = request.get(ARG_OUTPUT, (String) null);
        for (GsacOutput output : outputs) {
            if (request.defined(output.getId())) {
                arg = output.getId();

                break;
            }
        }
        if (arg == null) {
            //See if we have an output id as a submit button name
            for (GsacOutput output : outputs) {
                if (request.defined(output.getId())) {
                    return output.getOutputHandler();
                }
            }
            arg = outputs.get(0).getId();
        }

        GsacOutput output = getOutput(arg);
        if (output == null) {
            throw new IllegalArgumentException("Unknown output type: " + arg);
        }

        return output.getOutputHandler();
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return super.toString() + " " + getResourceLabel(false);
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
                ? "Resources"
                : "Resource");
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getUrlArgPrefix() {
        return getResourceClass().getName();
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getIdUrlArg() {
        return makeUrlArg(ARG_SUFFIX_ID);
    }

    /**
     * _more_
     *
     * @param suffix _more_
     *
     * @return _more_
     */
    public String makeUrlArg(String suffix) {
        return getUrlArgPrefix() + "." + suffix;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String makeSearchUrl() {
        return makeResourceUrl(URL_SUFFIX_SEARCH);
    }

    /**
     * _more_
     *
     * @param uri _more_
     *
     * @return _more_
     */
    public boolean canHandleUri(String uri) {
        return uri.startsWith(urlPrefix);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String makeViewUrl() {
        return makeResourceUrl(URL_SUFFIX_VIEW);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String makeFormUrl() {
        return makeResourceUrl(URL_SUFFIX_FORM);
    }

    /**
     * _more_
     *
     * @param suffix _more_
     *
     * @return _more_
     */
    public String makeResourceUrl(String suffix) {
        return urlPrefix + suffix;
    }



    /**
     * get all of the metadata for the given resource
     *
     *
     * @param level Specifies the depth of metadata that is being requeste - note: this is stupid and will change
     * @param gsacResource _more_
     *
     * @throws Exception On badness
     */
    public void doGetMetadata(int level, GsacResource gsacResource)
            throws Exception {
        doGetFullMetadata(gsacResource);
    }



    /**
     * add the full metadata to the resource
     *
     *
     *
     * @param gsacResource _more_
     * @throws Exception On badness
     */
    public void doGetFullMetadata(GsacResource gsacResource)
            throws Exception {}

    /**
     * _more_
     *
     * @param resourceId _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public abstract GsacResource getResource(String resourceId)
     throws Exception;

    /**
     * This method will first look in the local resourceCache for the resource.
     * If not found it calls doGetResource which should be overwritten by derived classes
     *
     * @param resourceId resource id
     *
     * @return The resource or null if not found
     *
     */
    public GsacResource getResourceFromCache(String resourceId) {
        return resourceCache.get(resourceId);
    }


    /**
     * _more_
     */
    public void clearCache() {
        resourceCache = new TTLCache<Object,
                                     GsacResource>(TTLCache.MS_IN_A_DAY);
    }

    /**
     * Is  cachable
     *
     * @return default true
     */
    public boolean shouldCacheResources() {
        return true;
    }


    /**
     * Put the given resource into the resourceCache
     *
     *
     * @param resource _more_
     */
    public void cacheResource(GsacResource resource) {
        cacheResource(resource.getId(), resource);
    }

    /**
     * Put the given resource into the resourceCache with the given cache key
     *
     * @param key Key to cache with
     * @param resource _more_
     */
    public void cacheResource(String key, GsacResource resource) {
        String resourceClass = resource.getResourceClass().getName();
        resourceCache.put(resourceClass + "_" + key, resource);
    }

    /**
     * retrieve the resource from the cache
     *
     * @param key key
     *
     * @return resource or null
     */
    public GsacResource getCachedResource(String key) {
        return resourceCache.get(key);
    }





    /**
     * _more_
     *
     * @return _more_
     */
    public CapabilityCollection getCapabilityCollection() {
        if (capabilityCollection == null) {
            capabilityCollection = new CapabilityCollection(
                getResourceClass(), getResourceLabel(false) + "  Query",
                getRepository().getServlet().getAbsoluteUrl(makeSearchUrl()),
                doGetQueryCapabilities());

        }

        return capabilityCollection;
    }



    /**
     * _more_
     *
     * @param capabilities _more_
     */
    public void addDefaultCapabilities(List<Capability> capabilities) {}

    /**
     * Get the extra search capabilities.  Derived classes should override this
     *
     * @return search capabilities
     */
    public List<Capability> doGetQueryCapabilities() {
        //default is to do nothing
        return new ArrayList<Capability>();
    }





    /**
     * _more_
     *
     * @param type _more_
     *
     * @return _more_
     */
    public boolean canHandleQueryCapabilities(String type) {
        if (this.resourceClass != null) {
            return this.resourceClass.getName().equals(type);
        }

        return false;
    }



    /**
     *  Get the ResourceClass property.
     *
     *  @return The ResourceClass
     */
    public ResourceClass getResourceClass() {
        return resourceClass;
    }


    /**
     * return the list of ResourceGroups. This is only used by the {@link #addDefaultSiteCapabilities}
     *
     * @return list of site groups
     */
    public List<ResourceGroup> doGetResourceGroups() {
        return new ArrayList<ResourceGroup>();
    }



    /**
     * Main entry point to handle search requests for resources
     * Handle the resource request. A derived class can overwrite this method to do
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
    public void handleRequest(GsacRequest request, GsacResponse response) throws Exception {

        System.err.println("GSAC: new request received by GsacResourceManager at time "+getUTCnowString() );

        String columns = getResourceSelectColumns();
        if (columns == null) {
            return;
        }
        if (getDatabaseManager() == null) {
            return;
        }

        List<String> tableNames = new ArrayList<String>();
        Clause       clause = getResourceClause(request, response, tableNames);

        String suffix=getResourceSelectSuffix(request);

        //System.err.println("GSAC: GsacResourceManager: handleRequest:getResourceSelectSuffix(request) gives "+ suffix );

        if ("" == suffix) { // LOOK why need this?
            suffix = request.getsqlWhereSuffix();
            //System.err.println("     GsacResourceManager: handleRequest: new suffix:       "+ suffix );
        }

        Statement statement = getDatabaseManager().select(columns, clause.getTableNames(tableNames), clause, suffix, -1 );                       

        //System.err.println("GSAC: GsacResourceManager, handleRequest() db query SQL is " + statement.toString() 
        //       + "; at time "+getUTCnowString()  ); // DEBUG.   LOOK toString  for oracle jbdc ; OK for mysql

        // DEBUG
        //    NOTE do this , for oracle (and other db-s), since oracle jdbc statement.toString() does nothing 
        //String fromtables= (clause.getTableNames(tableNames)).toString();
        // DEBUG LOOK print to LOG the SQL query made on the UNAVCO database gps3 (usually gps3) 
        //System.err.println("GSAC: GsacResourceManager: query SQL is  SELECT " + columns.toString() +" from "+ fromtables.substring(1, fromtables.length()-1) +" where " +clause +" "+ suffix.toString() ); // DEBUG sql string


        // DEBUG
        //   long t1 = System.currentTimeMillis();
        int nr = processStatement(request, response, statement, request.getOffset(), request.getLimit());
        // DEBUG
        //   long t2 = System.currentTimeMillis();
        //System.err.println("      GsacResourceManager: processStatement() completed and got "+nr+" rows in " + (t2 - t1) + " ms; at time "+getUTCnowString()); // DEBUG
    }



    /**
     * Iterate on the query statement and create resource.
     * Skip by the given offset and only process limit resources
     *
     * @param request the request
     * @param response the response
     * @param statement statement
     * @param offset skip
     * @param limit max number of resources to create
     *
     * @return count of how many resources were created
     *
     * @throws Exception On badness
     */
    public int processStatement(GsacRequest request, GsacResponse response, Statement statement, int offset, int limit) throws Exception {
        long t1 = System.currentTimeMillis();
        long t0=t1;
        long totalms=0;

        //Iterate on the query results
        SqlUtil.Iterator iter = SqlUtil.getIterator(statement, offset, limit);

        long t2 = System.currentTimeMillis();
        totalms += (t2 - t1); 
        //System.err.println      ("      processStatement():SqlUtil.getIterator() completed in " + (t2 - t1) + " ms"); // DEBUG

        while (iter.getNext() != null) {
            //t1 = System.currentTimeMillis();

            GsacResource resource = makeResource(request, iter.getResults());

            t2 = System.currentTimeMillis();
            totalms += (t2 - t1) ;
            //System.err.println("         processStatement():makeResource() completed in " + (t2 - t1) + " ms;  total = "+totalms+" ms "  ); // DEBUG

            if (resource == null) {
                continue;
            }

            //t1 = System.currentTimeMillis();
            response.addResource(resource);
            //t2 = System.currentTimeMillis();
            // about 0 ms System.err.println  ("         processStatement():addResource()  completed in " + (t2 - t1) + " ms"); // DEBUG

            if ( !iter.countOK()) {
                response.setExceededLimit();
                break;
            }
        }
        iter.close();

        getDatabaseManager().closeAndReleaseConnection(statement);

        int itcount=iter.getCount();

        //t2 = System.currentTimeMillis();
        //System.err.println("      GsacResourceManager, processStatement() took "+ (t2-t1)+ " ms"); // DEBUG

        return itcount;
    }



    /**
     * get the resource query clause. This also sets the seach criteria message on the response
     *
     * @param request the resquest
     * @param response the response
     * @param tableNames List of table names for the query
     *
     * @return resource query clause
     */
    public Clause getResourceClause(GsacRequest request,
                                    GsacResponse response,
                                    List<String> tableNames) {
        StringBuffer msgBuff = new StringBuffer();
        List<Clause> clauses = getResourceClauses(request, response,
                                   tableNames, msgBuff);
        setSearchCriteriaMessage(response, msgBuff);
        //System.err.println("GSAC: did    GRM getResourceClause");

        return Clause.and(clauses);
    }


    /**
     * Get the comma delimited list of columns to select on a resource query
     *
     *
     * @return resource columns
     */
    public String getResourceSelectColumns() {
        notImplemented("getResourceSelectColumns needs to be implemented");

        return "";
    }


    /**
     * this returns the order by clause and anything else that needs to be tacked onto the end of the resource query
     *
     * @param request The request
     *
     * @return the sql suffix
     */
    public String getResourceSelectSuffix(GsacRequest request) {
        return getResourceOrder(request); 
    }


    /**
     * Get the order by sql directive when doing resource queries; implement by child classes
     *
     * @param request the request
     *
     * @return the order by sql directive
     */
    public String getResourceOrder(GsacRequest request) {
        return "";
    }


    /**
     * Get the list of clauses when querying resources; implement by child classes
     *
     * @param request the request
     * @param response the response
     * @param tableNames List of table names. add any table names in the query to this list
     * @param msgBuff for the search criteria
     *
     * @return resource query clauses
     */
    public List<Clause> getResourceClauses(GsacRequest request,
                                           GsacResponse response,
                                           List<String> tableNames,
                                           StringBuffer msgBuff) {
        notImplemented("getResourceClauses needs to be implemented");

        return null;
    }



    /**
     * Create a single resource from the given resultset
     *
     *
     * @param request _more_
     * @param results db results
     *
     * @return the site
     *
     * @throws Exception on badness
     */
    public GsacResource makeResource(GsacRequest request, ResultSet results)
            throws Exception {
        return makeResource(results);
    }

    /**
     * _more_
     *
     * @param results _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public GsacResource makeResource(ResultSet results) throws Exception {
        notImplemented("makeResource needs to be implemented");

        return null;
    }





}
