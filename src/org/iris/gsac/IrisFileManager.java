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
import org.gsac.gsl.model.*;


import ucar.unidata.sql.Clause;
import ucar.unidata.sql.SqlUtil;

import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Handles all of the resource related repository requests. The main
 * entry point is {@link #handleRequest}
 * Look for the CHANGEME comments
 *
 * @author  Jeff McWhirter
 */
public class IrisFileManager extends FileManager {

    /**
     * ctor
     *
     * @param repository the repository
     */
    public IrisFileManager(IrisRepository repository) {
        super(repository);
    }



    /**
     * CHANGEME
     * handle the request
     *
     * @param request The request
     * @param response The response
     *
     * @throws Exception on badness
     */
    public void handleRequest(GsacRequest request, GsacResponse response)
            throws Exception {
    }


    /**
     * _more_
     *
     * @param level _more_
     * @param gsacResource _more_
     *
     * @throws Exception _more_
     */
    public void doGetResourceMetadata(int level, GsacFile gsacResource)
            throws Exception {}


    /**
     * CHANGEME
     * Get the columns to select for resources
     * @return resource columns
     */
    private String getResourceColumns() {
        return "files.column1,files.column2, etc";
        //e.g:
        //return Tables.FILES.COLUMNS;
    }


    /**
     * CHANGEME
     * Create a resource from the given results
     *
     * @param results result set
     *
     * @return The resource
     *
     * @throws Exception On badness
     */
    public GsacFile makeFile(ResultSet results) throws Exception {
        return null;
        /* e.g.:
        int    col            = 1;
        String exportID       = results.getString(col++);
        String fileID         = results.getString(col++);
        int    archiveTypeID  = results.getInt(col++);
        int    exportTypeID   = results.getInt(col++);
        int    siteID      = results.getInt(col++);
        long   fileSize       = results.getLong(col++);
        String path           = results.getString(col++);
        Date   publishTime    = results.getDate(col++);
        String md5            = results.getString(col++);
        int    sampleInterval = results.getInt(col++);

        //TODO: select the times from the other tables
        Date fromTime = publishTime;
        Date toTime   = publishTime;

        ExportType type = ExportType.findType(ExportType.GROUP_ALL_TYPES,
                              exportTypeID);
        GsacSite site =
            getSiteManager().getSiteForResource(siteID);

        //Convert the file path to the ftp url
        path = DBUtil.getExportFtpUrl(path);
        GsacFile resource = new GsacFile(exportID,
                                    new FileInfo(path, fileSize, md5),
                                    site, publishTime, fromTime, toTime,
                                    toResourceType(type));

        return resource;
        */

    }

    /**
     * CHANGEME
     * We don't have example code for this
     *
     * @param resourceId _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public GsacResource getResource(String resourceId) throws Exception {
        //TODO:
        return null;
    }



    /**
     * Create the list of resource types that are shown to the user
     *
     * @return resource types
     */
    public List<ResourceType> doGetResourceTypes() {
        List<ResourceType> resourceTypes = new ArrayList<ResourceType>();
        resourceTypes.add(new ResourceType("rinex", "RINEX Files"));
        resourceTypes.add(new ResourceType("qc", "QC Files"));
        return resourceTypes;
    }




    /**
     * helper method
     *
     * @return sitemanager
     */
    public IrisSiteManager getSiteManager() {
        return (IrisSiteManager) getRepository().getResourceManager(
            GsacSite.CLASS_SITE);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public List<Capability> doGetQueryCapabilities() {
        List<Capability> capabilities = new ArrayList<Capability>();
        //CHANGEME
        /*
          you can use the default site capabilities:
          addDefaultCapabilities(capabilities);
          or add you own, e.g.:
          Add in an example fruit enumerated query capability
          String[]values = {"banana","apple","orange"};
          Arrays.sort(values);
          capabilities.add(new Capability("fruit", "Fruit Label", values, true));
        */
        return capabilities;
    }



}
