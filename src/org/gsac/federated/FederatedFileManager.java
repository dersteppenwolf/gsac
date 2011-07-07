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

package org.gsac.federated;


import org.gsac.gsl.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.output.*;
import org.gsac.gsl.output.file.*;
import org.gsac.gsl.output.site.*;
import org.gsac.gsl.util.*;

import ucar.unidata.sql.Clause;
import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.StringUtil;


import ucar.unidata.xml.XmlUtil;

import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import java.util.concurrent.*;


/**
 * Handles all of the resource related repository requests
 *
 *
 * @author  Jeff McWhirter
 */
public class FederatedFileManager extends FileManager {

    /**
     * ctor
     *
     * @param repository the repository
     */
    public FederatedFileManager(FederatedRepository repository) {
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
    public void handleRequest(GsacRequest request,
                                      GsacResponse response)
            throws Exception {
        if (request.defined(ARG_RESOURCE_ID)) {
            List<String> ids = request.get(ARG_RESOURCE_ID, new ArrayList());
            for (String id : ids) {
                response.addObject(getRepository().getResource(request,
                                                             GsacFile.TYPE_RESOURCE,
                        id));
            }
            return;
        }

        getFederatedRepository().handleFederatedRequest(request, response,
                false);
    }


    /**
     * _more_
     *
     * @param level _more_
     * @param gsacResource _more_
     *
     * @throws Exception _more_
     */
    public void doGetMetadata(int level, GsacFile gsacResource)
            throws Exception {}




    /**
     *
     * @param resourceId _more_
     *
     * @return _more_
     * @throws Exception _more_
     */
    public GsacResource getResource(String resourceId) throws Exception {
        List<String> pair = StringUtil.splitUpTo(resourceId, ":", 2);
        String       id = pair.get(1);
        String baseUrl = new String(XmlUtil.decodeBase64(pair.get(0)));
        List<GsacFile> resources =
            (List<GsacFile>) getRepository().getRemoteObject(
                new GsacRepositoryInfo(baseUrl), URL_RESOURCE_VIEW,
                HtmlUtil.args(new String[] { ARG_RESOURCE_ID,
                                             id }), XmlFileOutputHandler
                                                 .OUTPUT_RESOURCE_XML);
        if (resources.size() == 0) {
            return null;
        }
        GsacFile       resource = resources.get(0);
        GsacRepositoryInfo info = getRepository().getRepositoryInfo(baseUrl);
        if (info == null) {
            throw new IllegalArgumentException("Could not find repository:"
                    + baseUrl);
        }
        resource.setRepositoryInfo(info);
        resource.setId(getRepository().getRemoteId(info, resource.getId()));
        return resource;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public FederatedRepository getFederatedRepository() {
        return (FederatedRepository) getRepository();
    }




    /**
     * helper method
     *
     * @return sitemanager
     */
    public FederatedSiteManager getSiteManager() {
        return (FederatedSiteManager) getRepository().getResourceManager(GsacSite.TYPE_SITE);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public List<Capability> doGetQueryCapabilities() {
        List<Capability> capabilities = new ArrayList<Capability>();
        HashSet          seen         = new HashSet();
        for (GsacRepositoryInfo info :
                getFederatedRepository().getServers()) {
            CapabilityCollection collection = info.getCollection("resource");
            if (collection != null) {
                for (Capability capability : collection.getCapabilities()) {
                    if (seen.contains(capability.getId())) {
                        continue;
                    }
                    seen.add(capability.getId());
                    capabilities.add(capability);
                }
            }
        }
        return capabilities;
    }




}
