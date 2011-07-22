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
import org.gsac.gsl.output.file.*;
import org.gsac.gsl.output.site.*;
import org.gsac.gsl.util.*;

import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import java.util.ArrayList;
import java.util.HashSet;

import java.util.List;


import java.util.concurrent.*;


/**
 * This is the core implementation of the gsac federated repository. It provides the base GsacRepository class
 * a list of remote servers in the doMakeServerInfoList where it creates the servers from the resoruces/gsac.properties
 * file
 *
 * The queries are all handled by the handleFederatedRequest method
 *
 * @author Jeff McWhirter mcwhirter@unavco.org
 */
public class FederatedRepository extends GsacRepository implements GsacConstants {


    /** Seconds to wait for all requests to finish */
    public static final int SECONDS_TO_WAIT = 30;

    /** Max number of open requests per repository */
    public static final int MAX_OPEN_REQUESTS = 10;


    /** Max number of threads to use for remote queries */
    private static final int MAX_THREADS = 5;

    /** _more_          */
    private static final String ARG_REMOVEDUPLICATES = "removeduplicates";


    /** Singleton thread pool */
    private ExecutorService executor;


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

        boolean doTest = false;
        //servers.add(new GsacRepositoryInfo("http://facility.unavco.org", "Unavco GSAC Repository"));
        //Get the comma separated list of server ids
        String serverList = getProperty("gsac.federated.servers",
                                        (String) null);

        if (doTest) {
            servers.add(
                        new GsacRepositoryInfo(
                                               "http://localhost:8081/gsacws", "UNAVCO@local host",
                                               "http://www.unavco.org/favicon.ico"));
            /*
            servers.add(
                new GsacRepositoryInfo(
                    "http://localhost:8082/gsacws", "CDDIS@local host",
                    "http://cddis.nasa.gov/favicon.ico"));
            */
        } else if (serverList != null) {
            System.err.println("GSAC: servers:" + serverList);
            //Look at each repository id and find the url, name and icon properties.
            //Note: We end up asking each repository for its own information so things like the
            //name and the icon can get overwritten
            for (String server :
                    StringUtil.split(serverList, ",", true, true)) {
                String url = getProperty("gsac.federated." + server + ".url",
                                         (String) null);
                if (url == null) {
                    logError("No URL property defined for:"
                             + "gsac.federated." + server + ".url", null);
                    continue;
                }
                logInfo("Loading remote server:" + url);
                servers.add(new GsacRepositoryInfo(url,
                        getProperty("gsac.federated." + server + ".name",
                                    url), getProperty("gsac.federated."
                                    + server + ".icon", (String) null)));
            }
        } else {
            servers.add(
                new GsacRepositoryInfo(
                    "http://cddis.gsfc.nasa.gov/gsacws", "CDDIS GSAC Server",
                    "http://cddis.gsfc.nasa.gov/favicon.ico"));
            servers.add(
                new GsacRepositoryInfo(
                    "http://facdev.unavco.org:9090/gsacws",
                    "UNAVCO GSAC Server",
                    "http://www.unavco.org/favicon.ico"));
            servers.add(
                new GsacRepositoryInfo(
                    "http://geoappdev02.ucsd.edu/gsacws",
                    "SOPAC GSAC Server",
                    "http://sopac.ucsd.edu/favicon.ico"));

        }
    }

    /**
     * Find the servers to use. This looks  at the capabilities and sees if one or more search criteria is being
     * specified. If so it will only include the remote server if that server also has those same capabilities.
     * e.g., if we have a capability like tectonic_plate that the user searched on. If one of our remote repositories
     * does not have that capability to search on tectonic_plate then we don't include that repository
     *
     * @param request The request
     * @param forSite Is this for a site search
     * @param collectionType This is the capability collection type, e.g., sites, resources, etc
     *
     * @return List of servers to search
     */
    private List<GsacRepositoryInfo> getApplicableServers(
            GsacRequest request, ResourceClass resourceClass) {
        List<GsacRepositoryInfo> serversToUse =
            new ArrayList<GsacRepositoryInfo>(super.getServers(request));
        List<Capability> capabilities =
            getResourceManager(resourceClass).getCapabilityCollection().getCapabilities();
        for (Capability capability : capabilities) {
            if ( !request.defined(capability.getId())) {
                continue;
            }
            if ((capability.getDefault() != null)
                    && (capability.getDefault().length() > 0)) {
                continue;
            }
            for (GsacRepositoryInfo info :
                    new ArrayList<GsacRepositoryInfo>(serversToUse)) {
                boolean hasCapability = false;
                for (CapabilityCollection collection :
                        info.getCollections()) {
                    if (collection.getResourceClass().equals(resourceClass)
                            && collection.isCapabilityUsed(capability)) {
                        hasCapability = true;
                        break;
                    }
                }
                if ( !hasCapability) {
                    System.err.println("    Excluding " + info.getName()
                                       + " it doesn't have:"
                                       + capability.getId());
                    serversToUse.remove(info);
                }
            }
        }
        return serversToUse;
    }

    /**
     * This is a hook to add federated repository specific status for the /stats call
     *
     * @param sb buffer to append to
     */
    public void addStats(StringBuffer sb) {
        super.addStats(sb);
        StringBuffer stats = new StringBuffer("<table>");
        stats.append(HtmlUtil.col("Repository"));
        stats.append(HtmlUtil.col("#Open Connections"));
        for (GsacRepositoryInfo repository : super.getServers()) {
            stats.append("<tr>");
            stats.append(HtmlUtil.col(repository.getName()));
            stats.append(
                HtmlUtil.col("" + repository.getOpenRequestsCount()));
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
        return getApplicableServers(request, GsacSite.CLASS_SITE);
    }


    /**
     * get the servers to use for a resource search
     *
     * @param request the request
     *
     * @return List of servers to search
     */
    public List<GsacRepositoryInfo> getFileServers(GsacRequest request) {
        return getApplicableServers(request, GsacFile.CLASS_FILE);
    }


    /**
     * This gets called when generating HTML. If there are no remote servers then return false
     *
     * @param request the request object
     * @param response the response
     * @param sb buffer to append message to
     *
     * @return is request OK or not
     *
     * @throws Exception On badness
     */
    public boolean checkRequest(GsacRequest request, GsacResponse response,
                                Appendable sb)
            throws Exception {
        if (getServers().size() == 0) {
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
        boolean removeDuplicates = request.get(ARG_REMOVEDUPLICATES, false);
        //Find the servers to use
        List<GsacRepositoryInfo> servers = forSite
                                           ? getSiteServers(request)
                                           : getFileServers(request);

        final String             urlArgs = getRemoteUrlArgs(request);
        final StringBuffer       msgBuff = new StringBuffer();
        msgBuff.append("Repositories searched:<ul>");

        //Go through each server and only use those that don't have too many open requests
        List<RepositoryCallable> callables =
            new ArrayList<RepositoryCallable>();
        HashSet<String> seen = new HashSet<String>();
        for (GsacRepositoryInfo info : servers) {
            if (info.getOpenRequestsCount() > MAX_OPEN_REQUESTS) {
                msgBuff.append("<li> " + info.getName()
                               + ": Skipping due to too many open requests");
                continue;
            }

            //Make the callable
            RepositoryCallable callable = new RepositoryCallable(info, seen,
                                              removeDuplicates) {
                public Boolean call() {
                    try {
                        repository.incrementOpenRequestsCount();
                        requestRunning = true;
                        String message = "";
                        int    cnt     = 0;
                        if (forSite) {
                            cnt = processSiteRequest(this, urlArgs, response);
                            message = "Retrieved " + cnt + " sites";
                        } else {
                            cnt = processResourceRequest(this, urlArgs,
                                    response);
                            message = "Retrieved " + cnt + " resource";
                        }
                        requestRunning = false;
                        if ( !jobRunning) {
                            return Boolean.FALSE;
                        }
                        synchronized (msgBuff) {
                            msgBuff.append("<li> " + repository.getName()
                                           + ": " + message);
                        }
                    } catch (Exception exc) {
                        logError("Error processing request for server:"
                                 + repository, exc);
                        synchronized (msgBuff) {
                            msgBuff.append("<li> " + repository.getName()
                                           + ": An error occurred " + exc);
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
        if (callables.size() > 0) {
            processRequests(callables, msgBuff);
        } else {
            msgBuff.append("<b>No repositories available to search<b>");
        }
        getResourceManager(GsacSite.CLASS_SITE).setSearchCriteriaMessage(
            response, msgBuff);
    }





    /**
     * Process the  site request
     *
     * @param callable Holds the remote repository
     * @param urlArgs url search arguments
     * @param response The response
     *
     *
     * @return How many sites were retrieved
     * @throws Exception On badness
     */
    private int processSiteRequest(RepositoryCallable callable,
                                   String urlArgs, GsacResponse response)
            throws Exception {

        List<GsacSite> sites = (List<GsacSite>) getRemoteObject(
                                   callable.repository, URL_SITE_SEARCH,
                                   urlArgs,
                                   XmlSiteOutputHandler.OUTPUT_SITE_XML);
        if ( !callable.jobRunning) {
            return 0;
        }
        if (sites == null) {
            System.err.println("Bad request: "
                               + callable.repository.getUrl());
            return 0;
        }
        for (GsacSite site : sites) {
            String id = getRemoteId(callable.repository, site.getId());
            site.setId(id);
            site.setRepositoryInfo(callable.repository);
            response.addResource(site);
        }
        return sites.size();
    }


    /**
     * Process the  resource request
     *
     * @param callable Holds the remote repository
     * @param urlArgs url search arguments
     * @param response The response
     *
     * @return How many resources were retrieved
     * @throws Exception On badness
     */
    private int processResourceRequest(RepositoryCallable callable,
                                       String urlArgs, GsacResponse response)
            throws Exception {

        List<GsacFile> resources = (List<GsacFile>) getRemoteObject(
                                       callable.repository, URL_FILE_SEARCH,
                                       urlArgs,
                                       XmlFileOutputHandler.OUTPUT_FILE_XML);
        if (resources == null) {
            System.err.println("Bad request: "
                               + callable.repository.getUrl());
            return 0;
        }

        if ( !callable.jobRunning) {
            return 0;
        }

        for (GsacFile resource : resources) {
            if (callable.getRemoveDuplicates()) {
                String tail =
                    IOUtil.getFileTail(resource.getFileInfo().getUrl());
                if (callable.checkAndAddSeen(tail)) {
                    System.err.println("duplicate:"
                                       + resource.getFileInfo().getUrl());
                    continue;
                }
            }
            String id = getRemoteId(callable.repository, resource.getId());
            resource.setId(id);
            resource.setRepositoryInfo(callable.repository);
            response.addResource(resource);
        }
        return resources.size();
    }





    /**
     * Create if needed and return the singleton thread pooler
     *
     *
     * @param callables _more_
     *
     * @return _more_
     */
    private synchronized ExecutorService getExecutor(
            List<RepositoryCallable> callables) {
        if (executor == null) {
            executor = Executors.newFixedThreadPool(MAX_THREADS);
        }
        return executor;
        //        return  Executors.newFixedThreadPool(callables.size());
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
        ExecutorService executor = getExecutor(callables);
        //This will return when they are all done or when the timeout was reached
        //The list of results corresponds to the list of callables
        List<Future<Boolean>> results = executor.invokeAll(callables,
                                            SECONDS_TO_WAIT,
                                            TimeUnit.SECONDS);

        //Check for anything still running
        for (int i = 0; i < results.size(); i++) {
            Future<Boolean>    future   = results.get(i);
            RepositoryCallable callable = callables.get(i);
            callable.jobRunning = false;
            if (callable.isRunning()) {
                msgBuff.append("<li> " + callable.repository.getName()
                               + ": Request timed out");
            }
        }

        //NOTE: Don't do this if we are using the singleton executor
        //This clears out any threads
        //        executor.shutdownNow();

    }


    public GsacResourceManager doMakeResourceManager(
            ResourceClass resourceClass) {
        if (resourceClass.equals(GsacSite.CLASS_SITE)) {
            return new FederatedSiteManager(this);
        }
        if (resourceClass.equals(GsacFile.CLASS_FILE)) {
            return new FederatedFileManager(this);
        }
        return null;
    }




    /**
     * A class to hold information for each remote repository call
     * WHen we fire off the request we subclass this class to overwrite the Callable.call method
     *
     */
    private abstract static class RepositoryCallable implements Callable<Boolean> {

        /** _more_          */
        private HashSet<String> seen;

        /** _more_          */
        private boolean removeDuplicates = false;


        /** Is this request still running */
        boolean requestRunning = true;

        /**
         *   Is the overall job (i.e, all of the requests to the repositories) still running. We use this flag
         *   to determine if the request has timed out
         */
        boolean jobRunning = true;

        /** the remote repository */
        GsacRepositoryInfo repository;

        /**
         * ctor
         *
         * @param repository The remote repostiory
         * @param seen _more_
         * @param removeDuplicates _more_
         */
        public RepositoryCallable(GsacRepositoryInfo repository,
                                  HashSet<String> seen,
                                  boolean removeDuplicates) {
            this.repository       = repository;
            this.seen             = seen;
            this.removeDuplicates = removeDuplicates;
        }

        /**
         * _more_
         *
         * @param s _more_
         *
         * @return _more_
         */
        public boolean checkAndAddSeen(String s) {
            synchronized (seen) {
                if (seen.contains(s)) {
                    return true;
                }
                seen.add(s);
                return false;
            }
        }


        /**
         * Are we still running
         *
         * @return still running
         */
        public boolean isRunning() {
            return requestRunning;
        }

        /**
         * _more_
         *
         * @return _more_
         */
        public boolean getRemoveDuplicates() {
            return removeDuplicates;
        }

    }


    public List<Capability> doGetQueryCapabilities(ResourceClass resourceClass) {
        List<Capability> capabilities = new ArrayList<Capability>();
        HashSet          seen         = new HashSet();
        for (GsacRepositoryInfo info : getServers()) {
            CapabilityCollection collection = info.getCollection(resourceClass);
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
