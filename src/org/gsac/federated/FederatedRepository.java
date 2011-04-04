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

import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import java.util.ArrayList;

import java.util.List;


import java.util.concurrent.*;


/**
 *
 * @author Jeff McWhirter mcwhirter@unavco.org
 */
public class FederatedRepository extends GsacRepositoryImpl implements GsacConstants {


    /** Seconds to wait for all requests to finish     */
    public static final int SECONDS_TO_WAIT = 30;

    /** Max number of open requests per repository */
    public static final int MAX_OPEN_REQUESTS = 10;


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
     * This creates the initial list of servers to access
     *
     * @param servers List of servers
     */
    public void doMakeServerInfoList(List<GsacRepositoryInfo> servers) {
        
        boolean doTest  = false;
        //servers.add(new GsacRepositoryInfo("http://facility.unavco.org", "Unavco GSAC Repository"));
        String serverList = getProperty("gsac.federated.servers",(String)null);

        if(doTest) {
            /*
            servers.add(
                        new GsacRepositoryInfo(
                                               "http://localhost:8081/gsacws", "UNAVCO@local host",
                                               "http://www.unavco.org/favicon.ico"));
            */
            servers.add(
                        new GsacRepositoryInfo(
                                               "http://localhost:8082/gsacws", "CDDIS@local host",
                                               "http://cddis.nasa.gov/favicon.ico"));
        } else if(serverList!=null) {
            for(String server: StringUtil.split(serverList,",",true,true)) {
                String url = getProperty("gsac.federated." + server +".url",(String)null);
                if(url == null) {
                    logError("No URL property defined for:" +"gsac.federated." + server +".url",null);
                    continue;
                }
                logInfo("Loading remote server:" + url);
                servers.add(
                            new GsacRepositoryInfo(url,
                                                   getProperty("gsac.federated." + server +".name",url),
                                                   getProperty("gsac.federated." + server +".icon",(String)null)));
            }
        } else {
            servers.add(
                        new GsacRepositoryInfo(
                                               "http://cddis.gsfc.nasa.gov/gsacws", 
                                               "CDDIS GSAC Server",
                                               "http://cddis.gsfc.nasa.gov/favicon.ico"));
            servers.add(
                        new GsacRepositoryInfo(
                                               "http://facdev.unavco.org:9090/gsacws", "UNAVCO GSAC Server",
                                               "http://www.unavco.org/favicon.ico"));
            servers.add(
              new GsacRepositoryInfo(
                                     "http://geoappdev02.ucsd.edu/gsacws",
                                     "SOPAC GSAC Server",
                                     "http://sopac.ucsd.edu/favicon.ico"));

        }
    }

    /**
     * Find the servers to use. 
     *
     * @param request The request
     * @param forSite Is this for a site search
     *
     * @return List of servers to search
     */
    private List<GsacRepositoryInfo> getApplicableServers(
            GsacRequest request, String collectionType) {
        List<GsacRepositoryInfo> serversToUse =
            new ArrayList<GsacRepositoryInfo>(super.getServers(request));
        List<Capability> capabilities = getCapabilityCollection(collectionType).getCapabilities();
        for (Capability capability : capabilities) {
            if ( !request.defined(capability.getId())) {
                continue;
            }
            if(capability.getDefault()!=null && capability.getDefault().length()>0) {
                continue;
            }
            for (GsacRepositoryInfo info : new ArrayList<GsacRepositoryInfo>(serversToUse)) {
                boolean hasCapability = false;
                for(CapabilityCollection collection: info.getCollections ()) {
                    if(collection.getId().equals(collectionType) && collection.isCapabilityUsed(capability)) {
                        hasCapability = true;
                        break;
                    }
                }
                if ( !hasCapability) {
                    System.err.println("    Excluding " + info.getName() +" it doesn't have:" + capability.getId());
                    serversToUse.remove(info);
                }
            }
        }
        return serversToUse;
    }

    public void addStats(StringBuffer sb) {
	super.addStats(sb);
	StringBuffer stats = new StringBuffer("<table>");
	stats.append(HtmlUtil.col("Repository"));
	stats.append(HtmlUtil.col("#Open Connections"));
        for(GsacRepositoryInfo repository: super.getServers()) {
	    stats.append("<tr>");
	    stats.append(HtmlUtil.col(repository.getName()));
	    stats.append(HtmlUtil.col(""+repository.getOpenRequestsCount()));
	}
	stats.append("</table>");
	sb.append(HtmlUtil.formEntryTop("Remote Repositories:",
					stats.toString()));
				     
	
    }

    /**
     * get the servers to use for a site search
     *
     * @param request request
     *
     * @return List of servers to search
     */
    public List<GsacRepositoryInfo> getSiteServers(GsacRequest request) {
        return getApplicableServers(request, CAPABILITIES_SITE);
    }


    /**
     * get the servers to use for a resource search
     *
     * @param request the request
     *
     * @return List of servers to search
     */
    public List<GsacRepositoryInfo> getResourceServers(GsacRequest request) {
        return getApplicableServers(request, CAPABILITIES_RESOURCE);
    }


    public boolean checkRequest(GsacRequest request, GsacResponse response,
                                Appendable sb) throws Exception {
        if(getServers().size()==0) {
            sb.append(makeErrorDialog("No remote servers are available"));
            return false;
        }
        return true;
    }





    /**
     * Handle the request
     *
     * @param request the request
     * @param response The response
     * @param forSite Is this a site search
     *
     * @throws Exception On badness
     */
    public void handleFederatedRequest(final GsacRequest request,
                                       final GsacResponse response,
                                       final boolean forSite)
            throws Exception {
	//Find the servers to use
        List<GsacRepositoryInfo> servers  = forSite
                                            ? getSiteServers(request)
                                            : getResourceServers(request);

        final String             urlArgs  = getRemoteUrlArgs(request);
        final StringBuffer       msgBuff  = new StringBuffer();
        msgBuff.append("Repositories searched:<ul>");

	//Go through each server and only use those that don't have too many open requests
        List<RepositoryCallable> callables =
            new ArrayList<RepositoryCallable>();
        for (GsacRepositoryInfo info : servers) {
	    if(info.getOpenRequestsCount()>MAX_OPEN_REQUESTS) {
		msgBuff.append("<li> " + info.getName() +": Skipping due to too many open requests");
		continue;
	    }

	    //Make the callable
            RepositoryCallable callable = new RepositoryCallable(info) {
                public Boolean call() {
                    try {
			repository.incrementOpenRequestsCount();
			requestRunning = true;
			String message = "";
			int cnt = 0;
                        if (forSite) {
                            cnt = processSiteRequest(this, urlArgs, response);
			    message = "Retrieved " + cnt +" sites";
                        } else {
                            cnt = processResourceRequest(this, urlArgs,
							 response);
			    message = "Retrieved " + cnt +" resource";
                        }
			requestRunning = false;
			if(!jobRunning) {
			    return Boolean.FALSE;
			}
                        synchronized (msgBuff) {
			    msgBuff.append("<li> " +repository.getName()+": " + message);
                        }
		    } catch (Exception exc) {
                        logError("Error processing request for server:" + repository, exc);
                        synchronized (msgBuff) {
			    msgBuff.append("<li> " + repository.getName() +": An error occurred " + exc);
			}
                        return Boolean.FALSE;
                    } finally {
			requestRunning = false;
			repository.decrementOpenRequestsCount();
		    }
                    return Boolean.TRUE;
                }
            };
            callables.add(callable);
        }
	msgBuff.append("</ul>");

	//process the callables
        if(callables.size()>0) {
            processRequests(callables, msgBuff);
        } else  {
            msgBuff.append("<b>No repositories available to search<b>");
        }
        getSiteManager().setSearchCriteriaMessage(response, msgBuff);
    }


    private abstract static class RepositoryCallable implements Callable<Boolean> {
	boolean requestRunning = true;
	boolean jobRunning = true;
	GsacRepositoryInfo repository;
	public RepositoryCallable(GsacRepositoryInfo repository) {
	    this.repository = repository;
	}
	public boolean isRunning() {
	    return requestRunning;
	}

    }



    /**
     * _more_
     *
     * @param info _more_
     * @param urlArgs _more_
     * @param response The response
     *
     * @throws Exception On badness
     */
    private int  processSiteRequest(RepositoryCallable callable,  String urlArgs,
                                    GsacResponse response)
            throws Exception {

        List<GsacSite> sites = (List<GsacSite>) getRemoteObject(callable.repository,
                                   URL_SITE_SEARCH, urlArgs,
                                   XmlSiteOutputHandler.OUTPUT_SITE_XML);
	if(!callable.jobRunning) {
	    return 0;
	}
        if (sites == null) {
            System.err.println("Bad request: " + callable.repository.getUrl());
            return 0;
        }
        for (GsacSite site : sites) {
            String id = getRemoteId(callable.repository, site.getId());
            site.setId(id);
            site.setRepositoryInfo(callable.repository);
            response.addSite(site);
        }
	return sites.size();
    }


    /**
     * _more_
     *
     * @param info _more_
     * @param urlArgs _more_
     * @param response The response
     *
     * @throws Exception On badness
     */
    private int processResourceRequest(RepositoryCallable callable,
                                        String urlArgs, GsacResponse response)
            throws Exception {
	
        List<GsacResource> resources =
            (List<GsacResource>) getRemoteObject(callable.repository, URL_RESOURCE_SEARCH,
                urlArgs, XmlResourceOutputHandler.OUTPUT_RESOURCE_XML);
        if (resources == null) {
            System.err.println("Bad request: " + callable.repository.getUrl());
            return 0;
        }

	if(!callable.jobRunning) {
	    return 0;
	}

        for (GsacResource resource : resources) {
            String id = getRemoteId(callable.repository, resource.getId());
            resource.setId(id);
            resource.setRepositoryInfo(callable.repository);
            response.addResource(resource);
        }
	return resources.size();
    }





    /**
     * Execute the callables in parallel. 
     *
     * @param callables list of callables to execute
     * @param msgBuff For the end user message
     *
     * @throws Exception On badness
     */
    private void processRequests(List<RepositoryCallable> callables,
                                StringBuffer msgBuff)
            throws Exception {
        //Use one thread per callable
        ExecutorService executor =
            Executors.newFixedThreadPool(callables.size());
	//This will return when they are all done or when the timeout was reached
	//The list of results corresponds to the list of callables
        List<Future<Boolean>> results = executor.invokeAll(callables, SECONDS_TO_WAIT, TimeUnit.SECONDS);

	//Check for anything still running
	for (int i=0;i<results.size();i++) {
	    Future<Boolean> future =results.get(i);
	    RepositoryCallable callable =callables.get(i);
	    callable.jobRunning = false;
	    if (callable.isRunning()) {
		msgBuff.append("<li> " + callable.repository.getName() +": Request timed out");
	    }
	}

	//This clears out any threads
	executor.shutdownNow();

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


}
