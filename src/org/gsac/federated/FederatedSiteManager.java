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
import org.gsac.gsl.metadata.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.output.site.XmlSiteOutputHandler;
import org.gsac.gsl.util.*;

import ucar.unidata.sql.Clause;
import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.HtmlUtil;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import ucar.unidata.xml.XmlUtil;

import java.io.InputStream;

import java.net.URL;
import java.net.URLConnection;

import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;


import java.util.concurrent.*;
import java.util.zip.*;


/**
 * Example site manager.
 *
 * @author         Jeff McWhirter
 */
public class FederatedSiteManager extends SiteManager {



    /**
     * ctor
     *
     * @param repository the repository
     */
    public FederatedSiteManager(FederatedRepository repository) {
        super(repository);
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
     * _more_
     *
     * @param request _more_
     * @param response _more_
     *
     * @throws Exception _more_
     */
    public void handleRequest(final GsacRequest request,
                              final GsacResponse response)
            throws Exception {
        if (request.defined(ARG_SITE_ID)) {
            List<String> ids = request.get(ARG_SITE_ID, new ArrayList());
            for (String id : ids) {
                response.addObject(getRepository().getResource(request, GsacSite.TYPE_SITE,id));
            }
            return;
        }

        getFederatedRepository().handleFederatedRequest(request, response,
                true);
    }



    /**
     *
     * @param siteId site id.
     *
     * @return the site or null if not found
     *
     * @throws Exception on badness
     */
    public GsacResource getResource(String siteId) throws Exception {
        List<String> pair    = StringUtil.splitUpTo(siteId, ":", 2);
        String       id      = pair.get(1);
        String       baseUrl = new String(XmlUtil.decodeBase64(pair.get(0)));
        List<GsacSite> sites =
            (List<GsacSite>) getRepository().getRemoteObject(
                new GsacRepositoryInfo(baseUrl), URL_SITE_VIEW,
                HtmlUtil.args(new String[] { ARG_SITE_ID,
                                             id }), XmlSiteOutputHandler
                                                 .OUTPUT_SITE_XML);
        if (sites.size() == 0) {
            return null;
        }
        GsacSite           site = sites.get(0);
        GsacRepositoryInfo info = getRepository().getRepositoryInfo(baseUrl);
        if (info == null) {
            throw new IllegalArgumentException("Could not find repository:"
                    + baseUrl);
        }
        site.setRepositoryInfo(info);
        site.setId(getRepository().getRemoteId(info, site.getId()));
        return site;
    }



    /**
     * get all of the metadata for the given site
     *
     *
     * @param level _more_
     *
     * @throws Exception On badness
     */
    public void doGetMetadata(int level, GsacResource  gsacObject)
            throws Exception {
        //TODO: what to do here
        //The unavcorepository adds in GnssEquipment metadata and other things
    }


    /**
     * Get the extra site search capabilities.
     *
     * @return site search capabilities
     */
    public List<Capability> doGetQueryCapabilities() {
        List<Capability> capabilities = new ArrayList<Capability>();
        HashSet          seen         = new HashSet();
        for (GsacRepositoryInfo info :
                getFederatedRepository().getServers()) {
            CapabilityCollection collection = info.getCollection("site");
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
