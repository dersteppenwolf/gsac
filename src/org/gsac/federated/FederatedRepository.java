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
import org.gsac.gsl.database.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.output.*;
import org.gsac.gsl.output.site.*;
import org.gsac.gsl.output.resource.*;
import org.gsac.gsl.util.*;

import ucar.unidata.util.Misc;

import java.util.ArrayList;

import java.util.List;


import java.util.concurrent.*;


/**
 *
 * @author Jeff McWhirter mcwhirter@unavco.org
 */
public class FederatedRepository extends GsacRepositoryImpl implements GsacConstants {


    /** _more_          */
    public static final int SECONDS_TO_WAIT = 30;

    private int pendingRequests = 0;

    /**
     * ctor
     */
    public FederatedRepository() {
        try {
            initResources();
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param forSite _more_
     *
     * @return _more_
     */
    private List<GsacRepositoryInfo> getApplicableServers(
            GsacRequest request, boolean forSite) {
        List<GsacRepositoryInfo> serversToUse =
            new ArrayList<GsacRepositoryInfo>(super.getServers(request));
        List<Capability> capabilities = forSite
                                        ? getSiteQueryCapabilities()
                                        : getResourceQueryCapabilities();
        for (Capability capability : capabilities) {
            if ( !request.defined(capability.getId())) {
                continue;
            }
            for (GsacRepositoryInfo info : new ArrayList<GsacRepositoryInfo>(serversToUse)) {
                boolean hasCapability = (forSite
                                         ? info.isSiteCapabilityUsed(
                                             capability)
                                         : info.isResourceCapabilityUsed(
                                             capability));
                if ( !hasCapability) {
                    System.err.println("    Excluding " + info.getName());
                    serversToUse.remove(info);
                }
            }
        }
        return serversToUse;
    }

    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public List<GsacRepositoryInfo> getSiteServers(GsacRequest request) {
        return getApplicableServers(request, true);
    }


    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public List<GsacRepositoryInfo> getResourceServers(GsacRequest request) {
        return getApplicableServers(request, false);
    }


    public boolean checkRequest(GsacRequest request, GsacResponse response,
                                Appendable sb) throws Exception {
        if(getServers().size()==0) {
            sb.append(getServlet().makeErrorDialog("No remote servers are available"));
            return false;
        }
        return true;
    }


    /**
     * initialize resources
     * CHANGME: Change the header.html and footer.html
     *
     * @throws Exception on badness
     */
    private void initResources() throws Exception {
        String packageName = getClass().getPackage().getName();
        packageName = packageName.replace(".", "/");
    }


    /**
     * _more_
     *
     * @param servers _more_
     */
    public void doMakeServerInfoList(List<GsacRepositoryInfo> servers) {
        //servers.add(new GsacRepositoryInfo("http://facility.unavco.org", "Unavco GSAC Repository"));
/*
        servers.add(
            new GsacRepositoryInfo(
                "http://localhost:8082/gsacws", "UNAVCO@local host",
                "http://www.unavco.org/favicon.ico"));
        servers.add(
            new GsacRepositoryInfo(
                "http://localhost:8081/gsacws", "CDDIS@local host",
                "http://cddis.nasa.gov/favicon.ico"));
*/

        servers.add(
            new GsacRepositoryInfo(
                "http://facility.unavco.org/gsacws", "UNAVCO GSAC Development Server",
                "http://www.unavco.org/favicon.ico"));

        servers.add(
            new GsacRepositoryInfo(
                                   "http://geoappdev02.ucsd.edu/gsacws",
                                   "SOPAC GSAC Development Server",
                                   "http://sopac.ucsd.edu/favicon.ico"));
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param response _more_
     * @param forSite _more_
     *
     * @throws Exception _more_
     */
    public void handleFederatedRequest(final GsacRequest request,
                                       final GsacResponse response,
                                       final boolean forSite)
            throws Exception {
        List<GsacRepositoryInfo> servers  = forSite
                                            ? getSiteServers(request)
                                            : getResourceServers(request);
        final String             urlArgs  = getRemoteUrlArgs(request);
        final StringBuffer       msgBuff  = new StringBuffer();
        List<Callable<Boolean>> callables =
            new ArrayList<Callable<Boolean>>();
        for (GsacRepositoryInfo info : servers) {
            final GsacRepositoryInfo theInfo  = info;
            Callable                 callable = new Callable() {
                public Boolean call() {
                    try {
                        if (forSite) {
                            processSiteRequest(theInfo, urlArgs, response);
                        } else {
                            processResourceRequest(theInfo, urlArgs,
                                    response);
                        }
                        synchronized (msgBuff) {
                            msgBuff.append("<li>");
                            msgBuff.append(theInfo.getName());
                        }
                    } catch (Exception exc) {
                        logError("Error processing request for server:"
                                 + theInfo, exc);
                        return Boolean.FALSE;
                    }
                    return Boolean.TRUE;
                }
            };
            callables.add(callable);
        }
        if(callables.size()>0) {
            processRequests(callables, msgBuff);
            msgBuff.append("</ul>");
        } else  {
            msgBuff.append("<b>No repositories were selected<b>");
        }
        getSiteManager().setSearchCriteriaMessage(response, msgBuff);
    }



    /**
     * _more_
     *
     * @param info _more_
     * @param urlArgs _more_
     * @param response _more_
     *
     * @throws Exception _more_
     */
    private void processSiteRequest(GsacRepositoryInfo info, String urlArgs,
                                    GsacResponse response)
            throws Exception {
        List<GsacSite> sites = (List<GsacSite>) getRemoteObject(info,
                                   URL_SITE_SEARCH, urlArgs,
                                   XmlSiteOutputHandler.OUTPUT_SITE_XML);
        if (sites == null) {
            System.err.println("Bad request: " + info.getUrl());
            return;
        }
        for (GsacSite site : sites) {
            String id = getRemoteId(info, site.getId());
            site.setId(id);
            site.setRepositoryInfo(info);
            response.addSite(site);
        }
    }


    /**
     * _more_
     *
     * @param info _more_
     * @param urlArgs _more_
     * @param response _more_
     *
     * @throws Exception _more_
     */
    private void processResourceRequest(GsacRepositoryInfo info,
                                        String urlArgs, GsacResponse response)
            throws Exception {
        List<GsacResource> resources =
            (List<GsacResource>) getRemoteObject(info, URL_RESOURCE_SEARCH,
                urlArgs, XmlResourceOutputHandler.OUTPUT_RESOURCE_XML);
        if (resources == null) {
            System.err.println("Bad request: " + info.getUrl());
            return;
        }
        for (GsacResource resource : resources) {
            String id = getRemoteId(info, resource.getId());
            resource.setId(id);
            resource.setRepositoryInfo(info);
            response.addResource(resource);
        }
    }



    /**
     * _more_
     *
     * @param callables _more_
     * @param msgBuff _more_
     *
     * @throws Exception _more_
     */
    public void processRequests(List<Callable<Boolean>> callables,
                                StringBuffer msgBuff)
            throws Exception {
        msgBuff.append("Repositories searched:<ul>");
        //Use one thread per callable
        ExecutorService executor =
            Executors.newFixedThreadPool(callables.size());
        List<Future<Boolean>> results    = executor.invokeAll(callables);
        long                  startTime  = System.currentTimeMillis();
        boolean               anyRunning = true;
        while (anyRunning) {
            long timeDiff = System.currentTimeMillis() - startTime;
            if (timeDiff > 1000 * SECONDS_TO_WAIT) {
                break;
            }
            Misc.sleep(5);
            anyRunning = false;
            for (Future<Boolean> future : results) {
                if ( !future.isDone()) {
                    anyRunning = true;
                    msgBuff.append("Some requests timed out<br>");
                    break;
                }
            }
        }
    }



    /**
     * Factory method to create the SiteManager
     *
     * @return site manager
     */
    public SiteManager doMakeSiteManager() {
        return new FederatedSiteManager(this);
    }


    /**
     * Factory method to create the ResourceManager
     *
     * @return resource manager
     */
    public ResourceManager doMakeResourceManager() {
        return new FederatedResourceManager(this);
    }



    /**
     * _more_
     *
     * @param arg _more_
     *
     * @return _more_
     */
    public boolean isCapable(String arg) {
        return super.isCapable(arg);
    }


    /**
     * get the html header. This just uses the base class' method which
     * will read the resources/header.html in this package. So, just edit that file
     * to define your own html header
     *
     * @param request the request
     *
     * @return html header
     */
    public String getHtmlHeader(GsacRequest request) {
        return super.getHtmlHeader(request);
    }


    /**
     * get the html footer. This just uses the base class' method which
     * will read the resources/footer.html in this package. So, just edit that file
     * to define your own html footer
     *
     * @param request the request
     *
     * @return html footer
     */
    public String getHtmlFooter(GsacRequest request) {
        return super.getHtmlFooter(request);
    }

}
