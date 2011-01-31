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
import org.gsac.gsl.util.*;


import ucar.unidata.sql.Clause;
import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.Misc;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


/**
 * Handles all of the resource related repository requests
 *
 *
 * @author  Jeff McWhirter
 */
public abstract class ResourceManager extends GsacRepositoryManager {

    /**
     * ctor
     *
     * @param repository the repository
     */
    public ResourceManager(GsacRepository repository) {
        super(repository);
    }


    /**
     * handle the request
     *
     * @param request The request
     * @param response The response
     *
     * @throws Exception on badness
     */
    public abstract void handleResourceRequest(GsacRequest request,
            GsacResponse response)
     throws Exception;



    /**
     * Create the GsacResource for the given id
     *
     * @param resourceId resource id
     *
     * @return GsacResource or null if none found
     *
     * @throws Exception on badness
     */
    public abstract GsacResource getResource(String resourceId)
     throws Exception;


    /**
     * Create a resource from the given results
     *
     * @param results result set
     *
     * @return The resource
     *
     * @throws Exception On badness
     */
    public GsacResource makeResource(ResultSet results) throws Exception {
        return null;
    }



    /**
     * Read the resources for the given statement
     *
     * @param request request
     * @param response response
     * @param statement resource query statement
     * @param offset select offset
     * @param limit select limit
     *
     * @return how many resources were added
     *
     * @throws Exception On badness
     */
    public int processStatement(GsacRequest request, GsacResponse response,
                                Statement statement, int offset, int limit)
            throws Exception {
        long             t1   = System.currentTimeMillis();
        SqlUtil.Iterator iter = SqlUtil.getIterator(statement, offset, limit);
        while (iter.getNext() != null) {
            //      makeResource(iter.getResults());
            response.addResource(makeResource(iter.getResults()));
            if ( !iter.countOK()) {
                response.setExceededLimit();
                break;
            }
        }
        long t2 = System.currentTimeMillis();
        //      System.err.println("Time:" + (t2-t1));
        iter.close();
        getDatabaseManager().closeAndReleaseConnection(statement);
        return iter.getCount();
    }




    /**
     * Add full metadata to the resource
     *
     *
     * @param level _more_
     * @param gsacResource the resource
     *
     * @throws Exception On badness
     */
    public void doGetResourceMetadata(int level, GsacResource gsacResource)
            throws Exception {
        //default is to do nothing
    }

    /**
     * Get the extra resource search capabilities. This
     * calls makeCapabilities to actually make them
     *
     * @return resource search capabilities
     */
    public List<Capability> doGetResourceQueryCapabilities() {
        return new ArrayList<Capability>();
    }

    /**
     * helper method to add the file size clauses
     *
     * @param request request
     * @param clauses list of clauses to add to
     * @param fileSizeColumn file size column
     * @param msgBuff search criteria message buffer
     */
    public void addFileSizeClauses(GsacRequest request, List<Clause> clauses,
                                   String fileSizeColumn,
                                   StringBuffer msgBuff) {
        if (request.defined(ARG_FILESIZE_MIN)) {
            clauses.add(Clause.ge(fileSizeColumn,
                                  request.get(ARG_FILESIZE_MIN, 0)));
            appendSearchCriteria(msgBuff, "Filesize&gt;=",
                                 "" + request.get(ARG_FILESIZE_MIN, 0));
        }

        if (request.defined(ARG_FILESIZE_MAX)) {
            clauses.add(Clause.le(fileSizeColumn,
                                  request.get(ARG_FILESIZE_MAX, 0)));
            appendSearchCriteria(msgBuff, "Filesize&lt;=",
                                 "" + request.get(ARG_FILESIZE_MAX, 0));
        }

    }

    /**
     * _more_
     *
     * @param capabilities _more_
     */
    public void addDefaultResourceCapabilities(
            List<Capability> capabilities) {
        Capability cap;
        Capability[] dflt = { initCapability(new Capability(ARG_RESOURCE_TYPE,
                                "Resource Type",
                                                            new ArrayList<IdLabel>(),
                                                            true), "Resource Query",
                                             "Type of file or resource", null,
                                             getRepository().getVocabulary(ARG_RESOURCE_TYPE,
                                                                           true)),
                              initCapability(new Capability(ARG_RESOURCE_DATADATE,
                                  "Data Date",
                                  Capability
                                      .TYPE_DATERANGE), "Resource Query",
                                          "Date the data this resource holds was collected"),
                              initCapability(new Capability(ARG_RESOURCE_PUBLISHDATE,
                                  "Publish Date",
                                  Capability
                                      .TYPE_DATERANGE), "Resource Query",
                                          "Date when this resource was first published to the repository"),
                              initCapability(cap = new Capability(ARG_RESOURCE_FILESIZE,
                                  "File Size",
                                  Capability
                                      .TYPE_NUMBERRANGE), "Resource Query",
                                          "File size") };
        cap.setSuffixLabel("&nbsp;(bytes)");
        for (Capability capability : dflt) {
            capabilities.add(capability);
        }
    }


    /**
     * Utility to add query clauses for the publish date argument
     *
     * @param request request
     * @param clauses list of clauses to add to
     * @param columnName publish date column name
     * @param msgBuff search criteria message buffer
     *
     * @throws Exception _more_
     */
    public void addPublishDateClauses(GsacRequest request,
                                      List<Clause> clauses,
                                      String columnName, StringBuffer msgBuff)
            throws Exception {
        Date[] dateRange =
            request.getDateRange(ARG_RESOURCE_PUBLISHDATE_FROM,
                                 ARG_RESOURCE_PUBLISHDATE_TO, null, null);

        if (dateRange[0] != null) {
            clauses.add(Clause.ge(columnName, dateRange[0]));
            appendSearchCriteria(msgBuff, "Publish date&gt;=",
                                 "" + format(dateRange[0]));
        }

        if (dateRange[1] != null) {
            clauses.add(Clause.le(columnName, dateRange[1]));
            appendSearchCriteria(msgBuff, "Publish date&lt;=",
                                 "" + format(dateRange[1]));
        }
    }




}
