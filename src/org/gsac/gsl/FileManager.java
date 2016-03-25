/*
 * Copyright 2015 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
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
import org.gsac.gsl.output.file.*;
import org.gsac.gsl.util.*;

import org.gsac.gsl.ramadda.sql.Clause;
import org.gsac.gsl.ramadda.sql.SqlUtil;

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
 * Handles all of the file related repository requests
 *
 *
 * @author  Jeff McWhirter 2010
 */
public abstract class FileManager extends GsacResourceManager {

    /**
     * ctor
     *
     * @param repository the repository
     */
    public FileManager(GsacRepository repository) {
        super(repository, GsacFile.CLASS_FILE);
    }



    /**
     * Create the output handlers for this resource. Each makes a different format for file search results.
     * The order of the cstrs here fixes the order of the itmes in the web site choice box for file search results output type.  
     * Revised 11 Feb 2104 to show choices in order of approximate interest.
     * Revised 13 Apr 2015 to remove the RSS choice.
     * Revised March 25 2016. reorder and remove the GSAC-style XML format which will be replaced soon by the IGS XML format choice.
     */
    @Override
    public void initOutputHandlers() {
        super.initOutputHandlers();
        new HtmlFileOutputHandler(getRepository(), getResourceClass());

        new UrlFileOutputHandler(getRepository(), getResourceClass());

        new CsvFileOutputHandler(getRepository(), getResourceClass());

        new WgetFileOutputHandler(getRepository(), getResourceClass());

        new ZipFileOutputHandler(getRepository(), getResourceClass());

        new JsonFileOutputHandler(getRepository(), getResourceClass());

        // to provide a jnlp script for files downloading with Java Webstart, very little or no interest. Since this choice was removed no one has asked for it after > 300,000 GSAC API queries:
        //                       new DownloaderFileOutputHandler(getRepository(), getResourceClass());
        // no interest for this: new RssFileOutputHandler(getRepository(), getResourceClass());
        // ditto :               new XmlFileOutputHandler(getRepository(), getResourceClass()); // which will be replaced soon by the IGS XML format choice.
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
                ? "Files"
                : "File");
    }



    /**
     * handle the request
     *
     * @param request The request
     * @param response The response
     *
     * @throws Exception on badness
     */
    public abstract void handleRequest(GsacRequest request,
                                       GsacResponse response)
     throws Exception;



    /**
     * Create a file from the given results
     *
     * @param results result set
     *
     * @return The file
     *
     * @throws Exception On badness
     */
    public GsacFile makeFile(ResultSet results) throws Exception {
        System.err.println(  "  make File returns null");
        return null;
    }



    /**
     * Read the files for the given statement
     *
     * @param request request
     * @param response response
     * @param statement file query statement
     * @param offset select offset
     * @param limit select limit
     *
     * @return how many files were added
     *
     * @throws Exception On badness
     */
    public int processStatement(GsacRequest request, GsacResponse response,
                                Statement statement, int offset, int limit)
            throws Exception {

        // for log print line below     long t1   = System.currentTimeMillis();

        SqlUtil.Iterator iter = SqlUtil.getIterator(statement, offset, limit);
        while (iter.getNext() != null) {
            response.addResource(makeFile(iter.getResults()));
            if ( !iter.countOK()) {
                response.setExceededLimit();
                break;
            }
        }

        // long t2 = System.currentTimeMillis();
        //System.err.println("GSAC: new request;     read the files for the given statement in " + (t2-t1) + " ms");

        iter.close();
        getDatabaseManager().closeAndReleaseConnection(statement);

        return iter.getCount();
    }




    /**
     * Add full metadata to the file
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
    public void addDefaultCapabilities(List<Capability> capabilities) {
        Capability   cap;
        Capability[] dflt = { initCapability(
                                new Capability(
                                    ARG_FILE_TYPE, "File Type",
                                    new ArrayList<IdLabel>(),
                                    true), "File Query", "Type of file",
                                           null,
                                           getRepository().getVocabulary(
                                               ARG_FILE_TYPE, true)),
                              initCapability(new Capability(ARG_FILE_DATADATE,
                                  "Data Date",
                                  Capability.TYPE_DATERANGE), "File Query",
                                      "Date the data this file holds was collected"),
                              initCapability(new Capability(ARG_FILE_PUBLISHDATE,
                                  "Publish Date",
                                  Capability.TYPE_DATERANGE), "File Query",
                                      "Date when this file was first published to the repository"),
                              initCapability(cap =
                                  new Capability(ARG_FILE_FILESIZE,
                                      "File Size",
                                      Capability
                                          .TYPE_NUMBERRANGE), "File Query",
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
     * @throws Exception On badness
     */
    public void addPublishDateClauses(GsacRequest request,
                                      List<Clause> clauses,
                                      String columnName, StringBuffer msgBuff)
            throws Exception {
        Date[] dateRange = request.getDateRange(ARG_FILE_PUBLISHDATE_FROM,
                               ARG_FILE_PUBLISHDATE_TO, null, null);

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
