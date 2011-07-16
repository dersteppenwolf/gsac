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

package org.gsac.gsl.test;


import org.gsac.gsl.*;

import org.gsac.gsl.model.*;

import java.util.ArrayList;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;


/**
 * This is an example of a GsacRepository. It creates some dummy sites and resources
 * and shows a basic implementation of the API
 *
 *
 * @author    Jeff McWhirter (mcwhirter@unavco.org)
 */
public abstract class DummyRepository extends GsacRepository {

    /** _more_ */
    private String urlBase = "";

    /** _more_ */
    private Hashtable<String, GsacSite> siteMap = new Hashtable<String,
                                                      GsacSite>();

    /** _more_ */
    private List<GsacSite> sites = new ArrayList<GsacSite>();

    /** _more_ */
    private Hashtable<String, GsacFile> resourceMap = new Hashtable<String,
                                                          GsacFile>();

    /** _more_ */
    private List<GsacFile> resources = new ArrayList<GsacFile>();

    /** _more_ */
    private List<ResourceGroup> siteGroups = new ArrayList<ResourceGroup>();


    /**
     * _more_
     *
     * @param urlBase _more_
     * @param servlet _more_
     */
    public DummyRepository(String urlBase, GsacServlet servlet) {
        super(servlet);
        this.urlBase = urlBase;
        initExamples();
    }

    /**
     * ctor
     */
    public DummyRepository() {
        initExamples();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public List<ResourceGroup> getResourceGroups() {
        return siteGroups;
    }

    /**
     * _more_
     */
    private void initExamples() {
        int cnt = 0;

        for (int i = 0; i < 10; i++) {
            siteGroups.add(new ResourceGroup("group" + i, "Site Group " + i));
        }

        for (int i = 0; i < 10; i++) {
            String id = "siteid" + i;
            GsacSite site = new GsacSite(id, "stn" + i, "Test site " + i,
                                         20 + i * 5, -107, 1000);

            site.addResourceGroup(siteGroups.get((int) (Math.random() * 1000)
                    % (siteGroups.size() - 1)));
            site.addResourceGroup(siteGroups.get((int) (Math.random() * 1000)
                    % (siteGroups.size() - 1)));
            addSite(site);

            int rcnt = 0;
            for (String type : new String[] { "rinex", "binex", "stream" }) {
                String resourceId = "resource_" + i + "_" + (rcnt++);
                addResource(
                    new GsacFile(
                        resourceId,
                        new FileInfo(
                            "ftp://data-out.unavco.org/pub/rinex/obs/file"
                            + rcnt, (long) (Math.random() * 10000000),
                                    ""), site, new Date(), new Date(),
                                         new ResourceType(type)));
            }

        }
    }


    /**
     * _more_
     *
     * @param site _more_
     */
    private void addSite(GsacSite site) {
        sites.add(site);
        siteMap.put(site.getId(), site);
    }


    /**
     * _more_
     *
     * @param resource _more_
     */
    private void addResource(GsacFile resource) {
        resources.add(resource);
        resourceMap.put(resource.getId(), resource);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getUrlBase() {
        return urlBase;
    }

    /**
     * _more_
     *
     * @param request the request
     * @param siteId _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public GsacSite getSiteInner(GsacRequest request, String siteId)
            throws Exception {
        return siteMap.get(siteId);
    }

    /**
     * _more_
     *
     *
     * @param request the request
     * @param resourceId _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public GsacFile getResource(GsacRequest request, String resourceId)
            throws Exception {
        return resourceMap.get(resourceId);
    }



    /**
     * _more_
     *
     * @param request the request
     * @param response The response
     *
     * @throws Exception On badness
     */
    public void handleResourceRequest(GsacRequest request,
                                      GsacResponse response)
            throws Exception {
        for (GsacFile resource : resources) {
            response.addResource(resource);
        }
    }

    /**
     * _more_
     *
     * @param request the request
     * @param response The response
     *
     * @throws Exception On badness
     */
    public void handleSiteRequest(GsacRequest request, GsacResponse response)
            throws Exception {
        String group = request.get(ARG_RESOURCE_GROUP, (String) null);
        for (GsacSite site : sites) {
            if ((group != null) && !site.hasGroup(group)) {
                continue;
            }
            response.addResource(site);
        }
    }


    /**
     * _more_
     *
     * @param request the request
     *
     * @return _more_
     */
    public String getHtmlHeader(GsacRequest request) {
        return "<html><body style=\"font-size: 12pt;font-family: Arial, Helvetica, sans-serif;margin: 0px; padding: 10px;\"><h1>Example GSAC Repository</h1>";
    }

    /**
     * return the html footer
     *
     * @param request the request
     *
     * @return html footer
     */
    public String getHtmlFooter(GsacRequest request) {
        return "<div class=\"footer\"></div></body></html>";
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public List<ResourceType> doGetSiteTypes() {
        List<ResourceType> types = new ArrayList<ResourceType>();
        types.add(new ResourceType("gnss", "GNSS Site"));
        types.add(new ResourceType("other", "Other Site"));
        return types;
    }


}
