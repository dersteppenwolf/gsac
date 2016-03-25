/*
 * Copyright 2010-2016 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
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


import org.apache.log4j.Logger;

import org.gsac.gsl.database.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.output.*;
import org.gsac.gsl.output.file.*;
import org.gsac.gsl.output.site.*;
import org.gsac.gsl.util.*;

import org.w3c.dom.*;

import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlEncoder;
import ucar.unidata.xml.XmlUtil;

import java.io.*;
import java.io.InputStream;
import java.lang.management.*;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.zip.*;
import javax.servlet.*;
import javax.servlet.http.*;


/**
 * This is the base class for the GSAC GSL repository. The main entry point is
 * the method:{@link #handleRequest(GsacRequest)}
 * This serves to dispatch the request to the appropriate handler
 *
 * The repository has one or more GsacResourceManager-s. Each of these handle
 * a certain ResourceClass of resources (e.g., GsacSite.CLASS_SITE, GsacFile.CLASS_FILE).
 * The resourceManager creates a set of GsacOutputHandler-s. These
 * do the work of encoding the results (e.g., into HTML, XML, CSV, etc).
 *
 * @author  Jeff McWhirter mcwhirter@unavco.org original 2010.
 * @version SK Wier improve date-time string formats so not in local time zone; others; change Base  URL code (grep for BaseU); latest June 24 2015.
 */
public class GsacRepository implements GsacConstants {

    /** gsac java package path prefix */
    public static final String GSAC_PATH_ROOT = "/org/gsac/gsl";

    /** gsac java package path to the htdocs dir */
    public static final String GSAC_PATH_HTDOCS = GSAC_PATH_ROOT + "/htdocs";

    /** gsac java package path to the help dir */
    public static final String GSAC_PATH_HELP = GSAC_PATH_ROOT + "/help";

    /** gsac java package path to the resources dir */
    public static final String GSAC_PATH_RESOURCES = GSAC_PATH_ROOT
                                                     + "/resources";
    /** property for the repository class to instantiate */
    public static final String PROP_REPOSITORY_CLASS = "gsac.repository.class";

    /** property for the base url, e.g., /gsacws */
    public static final String PROP_BASEURL = "gsac.baseurl";

    /** property for the local file directory to write logs to, etc */
    public static final String PROP_GSACDIRECTORY = "gsac.directory";

    /** property name for the repository name */
    public static final String PROP_REPOSITORY_NAME = "gsac.repository.name";

    /** property name for the repository icon */
    public static final String PROP_REPOSITORY_ICON = "gsac.repository.icon";

    /** property name for the repository description */
    public static final String PROP_REPOSITORY_DESCRIPTION = "gsac.repository.description";

    /**
     * property for host name. This is the external host name that is used for things like kml, etc,
     *   where we need a full hostname/url
     */
    private static final String PROP_HOSTNAME = "gsac.server.hostname";

    /** property for port we run on */
    private static final String PROP_PORT = "gsac.server.port";

    /** the servlet */
    private GsacServlet servlet;

    /** local directory to write stuff to */
    private File gsacDirectory;

    /** _more_ */
    private String userAgent;

    /** the database manager */
    private GsacDatabaseManager databaseManager;

    /** handles logging */
    private GsacLogManager logManager;

    /** mapping from ResourceClass to GsacResourceManager. */
    private Hashtable<ResourceClass, GsacResourceManager> resourceManagerMap =
        new Hashtable<ResourceClass, GsacResourceManager>();

    /** All the resource managers */
    private List<GsacResourceManager> resourceManagers =
        new ArrayList<GsacResourceManager>();

    /** html header. Initialize in initServlet */
    private String htmlHeader = "<html><body>";

    /** html footer. Initialize in initServlet */
    private String htmlFooter = "</body></html>";

    /** html header for mobile. Initialize in initServlet */
    private String mobileHeader = "<html><body>";

    /** html footer for mobile. Initialize in initServlet */
    private String mobileFooter = "</body></html>";

    /** holds phrase translations */
    private Properties msgProperties = new Properties();

    /** general properties */
    private Properties properties = new Properties();

    /** from the cmd line_ */
    private Properties cmdLineProperties;

    /** Make a cached list of servers. Cache for 6 hours */
    private TTLObject<List<GsacRepositoryInfo>> servers =
        new TTLObject<List<GsacRepositoryInfo>>(TTLCache.MS_IN_AN_HOUR * 6);

    /**
     * The base path of the url, e.g. /gsacws
     *   This is repository specific, so, for example, the federated repository uses /gsacfederated
     */
    private String urlBase;

    /** This repositories information */
    private GsacRepositoryInfo myInfo;

    /** Keeps track of the number of service requests for the stats page */
    private int numServiceRequests = 0;

    /** Keeps track of the  requests  */
    private long requestsCount = 0L;

    /** for the stats page */
    private Date startDate = new Date();

    /** reference to html output handler */
    private HtmlOutputHandler htmlOutputHandler;

    /** handles the browse pages */
    private BrowseOutputHandler browseOutputHandler;

    /** Map of vocab id (usually the url argument id) to the vocabulary */
    private Hashtable<String, Vocabulary> vocabularies =
        new Hashtable<String, Vocabulary>();

    /** All the vocabularies */
    private List<Vocabulary> vocabularyList = new ArrayList<Vocabulary>();

    /** _more_ */
    private List<String> helpIndex;

    /**
     * noop constructor
     */
    public GsacRepository() {
        //This says not to popup a dialog box
        LogUtil.setTestMode(true);
    }


    /**
     * Constructor
     *
     * @param servlet the servlet
     */
    public GsacRepository(GsacServlet servlet) {
        this();
        this.servlet = servlet;
    }





    /**
     * Initialize the servlet
     *
     * @param servlet the servlet
     *
     * @throws Exception On badness
     */
    public void initServlet(GsacServlet servlet) throws Exception {
        this.servlet      = servlet;
        cmdLineProperties = servlet.getProperties();
        init();
    }

    /**
     * Do initialization
     *
     * @throws Exception On badness
     */
    public void init() throws Exception {

        InputStream inputStream;
        //load property files first
        String[] propertyFiles = { GSAC_PATH_RESOURCES + "/gsac.properties",
                                   getLocalResourcePath("/gsac.properties"),
                                   getLocalResourcePath(
                                       "/gsacserver.properties") };
        for (String file : propertyFiles) {
            inputStream = getResourceInputStream(file);
            if (inputStream != null) {
                properties.load(inputStream);
            }
        }

        //Get the html header and footer
        mobileHeader =
            IOUtil.readContents(GSAC_PATH_RESOURCES + "/mobileheader.html",
                                mobileHeader);
        mobileFooter =
            IOUtil.readContents(GSAC_PATH_RESOURCES + "/mobilefooter.html",
                                mobileFooter);
        mobileHeader = replaceMacros(mobileFooter);
        mobileFooter = replaceMacros(mobileFooter);

        inputStream  =
            getResourceInputStream(getLocalResourcePath("/header.html"));
        if (inputStream != null) {
            htmlHeader = IOUtil.readContents(inputStream);
            htmlHeader = replaceMacros(htmlHeader);
        }

        inputStream =
            getResourceInputStream(getLocalResourcePath("/footer.html"));
        if (inputStream != null) {
            htmlFooter = IOUtil.readContents(inputStream);
            htmlFooter = replaceMacros(htmlFooter);
        }

        //Load in the phrases
        String[] phraseFiles = { GSAC_PATH_RESOURCES + "/phrases.properties",
                                 getLocalResourcePath(
                                     "/phrases.properties") };
        for (String file : phraseFiles) {
            inputStream = getResourceInputStream(file);
            if (inputStream != null) {
                msgProperties.load(inputStream);
            }
        }

        //Now look around the tomcat environment for a configuration file
        //System.err.println("System.properties:" + System.getProperties());
        //System.err.println("System.env:" + System.getenv());
        String catalinaBase = null;
        for (String arg : new String[] { "CATALINA_BASE", "catalina.base",
                                         "CATALINA_HOME", "catalina.home" }) {
            catalinaBase = getProperty(arg);
            if (catalinaBase != null) {
                break;
            }
        }

        if (catalinaBase != null) {
            //System.err.println("GSAC: catalina base:" + catalinaBase);
            //Use the  url base as the tail of the local tomcat properties file
            //in case we have different repositories running under the same tomcat
            File[] catalinaConfFiles = { new File(catalinaBase
                                           + "/conf/gsac.properties"),
                                         new File(catalinaBase + "/conf/"
                                             + getUrlBase()
                                             + ".properties"), };

            for (File catalinaConfFile : catalinaConfFiles) {
                if (catalinaConfFile.exists()) {
                    //System.err.println("GSAC: loading tomcat environment configuration file" + catalinaConfFile);
                    properties.load(new FileInputStream(catalinaConfFile));
                }
            }
        }

        //See if we have a local directory to write the logs to
        String dir = getProperty(PROP_GSACDIRECTORY, (String) null);
        if (dir != null) {
            gsacDirectory = new File(dir);
            //System.err.println("GSAC: gsacDirectory to write the logs to, from properties file: " + gsacDirectory);
        } else {
            String userHome = System.getProperty("user.home");
            //System.err.println( "GSAC: attempting to set gsacDirectory from user.home system property: " + userHome);
            if (userHome != null) {
                File localDir = new File(userHome + "/.gsac");
                if (localDir.exists()) {
                    gsacDirectory = localDir;
                    //System.err.println( "GSAC: gsacDirectory from userHome/.gsac: " + gsacDirectory);
                } else {
                    //System.err.println( "GSAC: userHome/.gsac directory does not exist: " + userHome + "/.gsac; no gsacDirectory set");
                }
            } else {
                System.err.println( "GSAC: user.home system property is null, no gsacDirectory set");
            }
        }


        if (gsacDirectory != null) {
            //System.err.println("GSAC: using gsac directory: " + gsacDirectory);
            //getLogManager().initLogDir(gsacDirectory);
            File localPropertiesFile = new File(gsacDirectory
                                           + "/gsac.properties");
            //System.err.println("GSAC: looking for: " + localPropertiesFile);
            if (localPropertiesFile.exists()) {
                //System.err.println("GSAC: loading " + localPropertiesFile);
                properties.load(new FileInputStream(localPropertiesFile));
            }
        }
        //Create the resource managers
        initResourceManagers();

        //Create the output handlers
        initOutputHandlers();

        getRepositoryInfo();
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param label _more_
     * @param contents _more_
     *
     * @return _more_
     */
    public String formEntry(GsacRequest request, String label,
                            String contents) {
        if (request.isMobile()) {
            return "<tr><td><div class=\"formlabel\">" + label + "</div>"
                   + contents + "</td></tr>";
        } else {
            return HtmlUtil.formEntry(label, contents);
        }
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param label _more_
     * @param contents _more_
     *
     * @return _more_
     */
    public String formEntryTop(GsacRequest request, String label,
                               String contents) {
        if (request.isMobile()) {
            return "<tr><td><div class=\"formlabel\">" + label + "</div>"
                   + contents + "</td></tr>";
        } else {
            return HtmlUtil.formEntryTop(label, contents);
        }
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param label _more_
     * @param contents _more_
     *
     * @return _more_
     */
    public String capabilityFormEntry(GsacRequest request, String label,
                                      String contents) {
        if (request.isMobile()) {
            return "<tr><td><div class=\"formlabel\">" + label + "</div>"
                   + contents + "</td></tr>";
        } else {
            if (label.endsWith(":")) {
                label = label.substring(0, label.length() - 1);
            }

            return "<tr><td colspan=2><fieldset class=\"gsac-fieldset\"><legend class=\"gsac-legend\">"
                   + label + "</legend>" + contents + "</fieldset></td></tr>";
            //            return HtmlUtil.formEntry(label, contents);
        }
    }



    /**
     * Create the default set of resource managers
     */
    public void initResourceManagers() {
        getResourceManager(GsacSite.CLASS_SITE);
        getResourceManager(GsacFile.CLASS_FILE);
    }



    /**
     * _more_
     *
     * @param resourceClass Type of resource
     *
     * @return _more_
     */
    public GsacResourceManager getResourceManager(
            ResourceClass resourceClass) {
        GsacResourceManager gom = resourceManagerMap.get(resourceClass);
        if (gom == null) {
            gom = doMakeResourceManager(resourceClass);
            if (gom == null) {
                throw new IllegalArgumentException("Unknown resource class:"
                        + resourceClass.getName());
            }
            addResourceManager(resourceClass, gom);
        }

        return gom;
    }

    /**
     * Get the list of resource managers
     *
     * @return list of resource managers
     */
    public List<GsacResourceManager> getResourceManagers() {
        return resourceManagers;
    }


    /**
     * Create the default set of output handlers
     */
    public void initOutputHandlers() {
        //Just create one for local access
        htmlOutputHandler   = new HtmlOutputHandler(this);
        browseOutputHandler = new BrowseOutputHandler(this);

        //Go through the resource managers and have them create their output handlers
        for (GsacResourceManager resourceManager : getResourceManagers()) {
            resourceManager.initOutputHandlers();
        }
    }


    public static String getUTCnowString() {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String utcTime = sdf.format(new Date());
        return utcTime;
    }


    /**
     * Main entry point for incoming GSAC requests.
     *
     * @param request the request
     *
     * @throws IOException On badness
     * @throws ServletException On badness
     */
    public void handleRequest(GsacRequest request)
            throws IOException, ServletException {

        // DEBUG System.out.println    ("GSAC: Start GsacRepository:handleRequest()  at time "+getUTCnowString()) ;  //+", from IP "+request.getOriginatingIP() );
        // makes many on each new gsac deploy o tomcat

        String uri   = request.getRequestURI();

        // to list every single thing used to make a new WEB PAGE, not the GSAC query result: 
        // System.err.println("GSAC: got requestURI:_" + uri +"_" ); // debug only

        int    index = uri.indexOf("?");
        if (index >= 0) {
            uri = uri.substring(0, index);
        }
        if (uri.endsWith("/")) {
            uri = uri.substring(0, uri.length() - 1);
        }

        //Check for bots
        if (repelRobots()) {
            if (request.isSpider()) {
                request.sendError(HttpServletResponse.SC_FORBIDDEN, "No bots for now"); 
                return;
            }
        }

        //TODO: what to do with a head request
        if (request.getMethod().toUpperCase().equals("HEAD")) {
            // debug only System.err.println("GSAC: ' a head request': got requestURI:_" + uri);
            return;
        }

        // Time the complete GSAC request handling               ttttime
        long starttime = System.currentTimeMillis();

        requestsCount++;

        // import java.text.SimpleDateFormat;
        //Date now = new Date();
        //SimpleDateFormat iso8601fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z"); // ISO 8601
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        //String utcTime = sdf.format(new Date());

        try {
            //We either have service requests or /htdocs requests
            boolean serviceRequest = uri.indexOf(URL_HTDOCS_BASE) < 0;
            if (serviceRequest) {
                numServiceRequests++;
                // to debug only:  LOOK show in log line the new GSAC request (and make string of new request)
                // String reqstr=request.toString();
                //System.out.println    ("GSAC: new request "+reqstr+" at time "+getUTCnowString()  ); 
            }
            String  what              = "other";

            boolean isResourceRequest = false;
            //Check the resource managers
            GsacResponse response = null;
            for (GsacResourceManager resourceManager : resourceManagers) {
                //If this resource manager handles the request then find the
                //output handler for the given ResourceClass/request
                //and tell it to do its stuff
                if (resourceManager.canHandleUri(uri)) {
                    what = resourceManager.getResourceClass().getName();
                    GsacOutputHandler outputHandler =
                        getOutputHandler(resourceManager.getResourceClass(),
                                         request);
                     
                    //System.out.println    ("GSAC: ResourceClass ="+ (resourceManager.getResourceClass()).toString() ); // DEBUG gets "site"
                    //System.out.println    ("GSAC: request  ="+ (request).toString() ); // DEBUG
                    response = outputHandler.handleRequest(
                        resourceManager.getResourceClass(), request);
                    isResourceRequest = true;
                }
            }
            //Check for other content requests
            if (isResourceRequest) {
                //already done
            } else if (uri.indexOf(URL_BROWSE_BASE) >= 0) {
                //browse request
                response = browseOutputHandler.doMakeResponse(request);
                ResourceClass resourceClass =
                    browseOutputHandler.handleRequestBrowse(request,
                        response);
                if (resourceClass != null) {
                    what = resourceClass.getName();
                }
            } else if (uri.indexOf(URL_STATS_BASE) >= 0) {
                handleRequestStats(request,
                                   response = new GsacResponse(request));
            } else if (uri.indexOf(URL_HELP) >= 0) {
                handleRequestHelp(request,
                                  response = new GsacResponse(request));
            } else if (uri.indexOf(URL_HTDOCS_BASE) >= 0) {
                handleRequestHtdocs(request);
            } else if (uri.endsWith(URL_BASE) || uri.equals(getUrlBase())) {
                //This is for /gsacws/gsacpi top level requests. It just lists the index page.
                handleRequestIndex(request, response = new GsacResponse(request));
            } else if (uri.indexOf(URL_REPOSITORY_VIEW) >= 0) {
                //Repository information
                handleRequestView(request, response = new GsacResponse(request));
            } else {
                throw new UnknownRequestException("");
                //getLogManager().logError("Unknown request:" + uri, null);
            }

            // JMcW: "log the access if it is actually a service request (as opposed to htdocs requests)" - seems to log all requests...
            if (serviceRequest) {
                
                String reqstr=request.toString();

                int resourceCnt = 0;
                if (response != null) {
                    resourceCnt = response.getNumResources();
                }
                if (resourceCnt == -1) {
                    resourceCnt = 0;
                }

                // old  getLogManager().logAccess(request, what, resourceCnt);

                // Time the complete GSAC request handling               ttttime
                // from above: long starttime = System.currentTimeMillis();
                long donetime = System.currentTimeMillis();
                // if request was a file search, site search, or something else like asking for a page on a GSAC web tool
                //System.err.println("GSAC: request # "+requestsCount+" GsacRepository timing: this file search total time used " + (donetime - starttime) + "ms to find "+resourceCnt+" files"); 
                if (reqstr.indexOf("file/search")>=0 ) {
                    System.err.println("GSAC: timing: request # "+requestsCount+" took " + (donetime - starttime) + "ms to find "+resourceCnt+" files"); 
                } else if (reqstr.indexOf("site/search")>=0 ) {
                    System.err.println("GSAC: timing: request # "+requestsCount+" took " + (donetime - starttime) + "ms to find "+resourceCnt+" sites"); 
                } else { 
                    if  ( (resourceCnt+1)> 0 ) {
                    System.err.println("GSAC: timing: request # "+requestsCount+" took " + (donetime - starttime) + "ms to return "+ (resourceCnt+1) +" items (web page)"); 
                    }
                }

                // LOOK do not change this line; it is key for GSAC use metrics
                System.out.println    ("GSAC: completed the request "+requestsCount+"  "+reqstr+" at time "+getUTCnowString()+", from "+request.getOriginatingIP()  );    
                //  + " (RequestIP="+ request.getRequestIP()  ); 

                /* DEBUG
                // LOOK weird: the IP from request.getOriginatingIP() can show the IP 69.44.86.107 = wes.unavco.org NOT the actual remote non-unavco incoming IP!
                // known to occur for COCONet GSACs
                // + "; URI "+request.getRequestURI(); // +"  resourceCnt ="+resourceCnt ); 
                */

            }
        } catch (UnknownRequestException exc) {
            //getLogManager().logError   ("GSAC: unknown request is: " + uri + "?" + request.getUrlArgs(), null);
            //request.sendError(HttpServletResponse.SC_NOT_FOUND, "GSAC: unknown request is: " + uri);
            long donetime = System.currentTimeMillis();
            System.err.println("GSAC: request # "+requestsCount+" GsacRepository: 'unknown' GSAC request: total time used " + (donetime - starttime) + "ms"); 
            System.out.println("GSAC: request # "+requestsCount+" GsacRepository: the request is unrecognized: "+request.toString()+ "  at time "+getUTCnowString()+", from IP "+request.getOriginatingIP());
            System.err.println("GSAC: request # "+requestsCount+" unknown request uri is: " + uri);
        } catch (java.net.SocketException sexc) {
            //Ignore the client closing the connection 
            // LOOK check database db connection count is?
        } catch (Exception exc) {
            //getLogManager().logError("Error processing request:" + uri + "?" + request.getUrlArgs(), thr);
            long donetime = System.currentTimeMillis();
            System.err.println("GSAC: request # "+requestsCount+" 'bad' GSAC request: total time used " + (donetime - starttime) + "ms"); 
            System.err.println("GSAC: request # "+requestsCount+" 'bad' GSAC request was " +request.toString()+"  at time "+getUTCnowString()+", from "+request.getOriginatingIP() );
            System.err.println("GSAC: request # "+requestsCount+" 'bad' request uri is: " + uri);
            //Get the actual exception, and log the stack trace.
            Throwable thr = LogUtil.getInnerException(exc);
            System.out.println("GSAC: request # "+requestsCount+" 'bad' GSAC request, error's Java exception is: \n"+thr.toString() );
            System.out.println("GSAC: request # "+requestsCount+" 'bad' GSAC request, stacktrace is:" );
            exc.printStackTrace(System.out);
            // old System.out.println         ("GSAC: request # "+requestsCount+".  Request with Error processing request: "+request.toString()+ "  at time "+getUTCnowString()+", from IP "+request.getOriginatingIP());
            //try {
            //    request.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred:" + thr);
            //} catch (Exception ignoreThisOne) {}
        }

    }




    /**
     * Get the servlet
     *
     * @return The servlet
     */
    public GsacServlet getServlet() {
        return servlet;
    }


    /**
     * Add output type
     *
     *
     * @param resourceClass Type of resource
     * @param output  output type
     */
    public void addOutput(ResourceClass resourceClass, GsacOutput output) {
        if (getProperty(output.getProperty("enabled"), true)) {
            getResourceManager(resourceClass).addOutput(output);
        }
    }


    /**
     * Find the output handler
     *
     *
     * @param resourceClass Type of resource
     * @param output output type
     *
     * @return output handler
     */
    public GsacOutputHandler getOutputHandler(ResourceClass resourceClass,
            String output) {
        return getResourceManager(resourceClass).getOutput(
            output).getOutputHandler();
    }


    /**
     * Find the output handler specified by the ARG_OUTPUT in the request within the given group of
     * output handlers
     *
     * @param resourceClass Type of resource
     * @param request the request
     *
     * @return the output handler
     */
    public GsacOutputHandler getOutputHandler(ResourceClass resourceClass,
            GsacRequest request) {
        return getResourceManager(resourceClass).getOutputHandler(request);
    }



    /**
     * Convert the given external vocabulary value to the one or more internal mappings
     * for the given vocabulary
     *
     * @param vocabularyId the given vocabulary (e,g, ARG_GROUP)
     * @param value The external value
     *
     * @return internal local mapping of the external value
     */
    public List<String> externalToInternal(String vocabularyId,
                                           String value) {

        Vocabulary vocabulary = getVocabulary(vocabularyId);
        if (vocabulary != null) {
            return vocabulary.externalToInternal(value);
        }
        List<String> list = new ArrayList<String>();
        list.add(value);

        return list;
    }

    /**
     * Map the internal vocab value to its external form
     *
     * @param vocabularyId which vocab to use
     * @param value internal value
     *
     * @return mapped value. IdLabel holds an id and a label
     */
    public IdLabel internalToExternal(String vocabularyId, String value) {
        Vocabulary vocabulary = getVocabulary(vocabularyId);
        if (vocabulary != null) {
            String externalValue = vocabulary.internalToExternal(value);
            if (externalValue != null) {
                return vocabulary.getIdLabel(externalValue);
            }
        }

        return new IdLabel(value);
    }


    /**
     * Utility to convert a list of external vocab values
     *
     * @param key vocab id
     * @param incoming external values
     *
     * @return converted values
     */
    public List<String> convertToInternal(String key, List<String> incoming) {
        return convertToInternal(getVocabulary(key), key, incoming);
    }



    /**
     * Utility to convert a list of external vocab values
     *
     * @param vocabulary the vocab
     * @param key vocab id
     * @param incoming external values
     *
     * @return converted values
     */
    public List<String> convertToInternal(Vocabulary vocabulary, String key,
                                          List<String> incoming) {
        if (vocabulary == null) {
            return incoming;
        }
        List<String> result = new ArrayList<String>();
        for (String incomingValue : incoming) {
            for (String s : vocabulary.expandValue(incomingValue)) {
                result.addAll(vocabulary.externalToInternal(s));
            }
        }

        return result;
    }


    /**
     * find the vocabulary for the given id
     *
     * @param id vocab id
     *
     * @return vocabulary
     */
    public Vocabulary getVocabulary(String id) {
        return getVocabulary(id, false);
    }

    /**
     * find the vocabulary for the given id
     *
     * @param id vocab id
     * @param createIfNeeded If true then create the vocabulary if it does not exist
     *
     * @return vocabulary
     */
    public Vocabulary getVocabulary(String id, boolean createIfNeeded) {
        Vocabulary vocabulary = vocabularies.get(id);
        if ((vocabulary == null) && createIfNeeded) {
            vocabulary = getVocabularyFromType(id);
        }

        return vocabulary;
    }

    /**
     * Add the vocabulary to the list of vocabs
     *
     * @param vocabulary  the vocab
     */
    public void addVocabulary(Vocabulary vocabulary) {
        if (vocabularies.get(vocabulary.getId()) == null) {
            vocabularyList.add(vocabulary);
        }
        vocabularies.put(vocabulary.getId(), vocabulary);
    }


    /**
     * Convert the object into an xml representation
     *
     * @param object Object to convert
     *
     * @return xml of the object
     *
     * @throws Exception On badness
     */
    public String encodeObject(Object object) throws Exception {
        return XmlEncoder.encodeObject(object);
    }


    /**
     * decode the object from an xml representation
     *
     * @param xml xml
     *
     * @return the object
     *
     * @throws Exception On badness
     */
    public Object decodeObject(String xml) throws Exception {
        return XmlEncoder.decodeXml(xml);
    }



    /**
     * Utility to get an input stream for a java resource
     *
     * @param path resource path
     *
     * @return input stream or null if we couldn't open it
     *
     * @throws Exception On badness
     */
    public InputStream getResourceInputStream(String path) throws Exception {
        InputStream inputStream = getClass().getResourceAsStream(path);
        if (inputStream == null) {
            inputStream = GsacRepository.class.getResourceAsStream(path);
        }

        if (inputStream == null) {
            List classLoaders = Misc.getClassLoaders();
            for (int i = 0; i < classLoaders.size(); i++) {
                try {
                    ClassLoader cl = (ClassLoader) classLoaders.get(i);
                    inputStream = cl.getResourceAsStream(path);
                    if (inputStream != null) {
                        break;
                    }
                } catch (Exception exc) {}
            }
        }

        return inputStream;
    }





    /**
     * This reads the given resource file. It looks in the local repository resources package
     * and then the gsl/resources package. It will return null if the resource cannot be found
     *
     * @param fileName resource file name
     *
     * @return file contents or null
     */
    public String readResource(String fileName) {
        String[] paths = { getLocalResourcePath(fileName),
                           getCoreResourcePath(fileName) };

        for (String path : paths) {
            try {
                String c = IOUtil.readContents(path, getClass());
                if (c != null) {
                    return c;
                }
            } catch (Exception noop) {}
        }

        return null;
    }


    /**
     * A hook to check the request. This allows the derived repository class to
     * to tell the html output to not try to process the request. Not sure why we do this
     * but there must be a reason
     *
     * @param request the request
     * @param response The response to write to
     * @param sb Buffer to append to
     *
     * @return Is request OK
     *
     * @throws Exception On badness
     */
    public boolean checkRequest(GsacRequest request, GsacResponse response,
                                Appendable sb)
            throws Exception {
        return true;
    }


    /**
     * Get list of remote servers we deal with. Default is none but the FederatedRepository
     * gives some back
     *
     * @return remote repositories
     */
    public List<GsacRepositoryInfo> getServers() {
        List<GsacRepositoryInfo> list = servers.get();
        if (list != null) {
            return new ArrayList<GsacRepositoryInfo>(list);
        }
        synchronized (servers) {
            list = new ArrayList<GsacRepositoryInfo>();
            doMakeServerInfoList(list);
            List<GsacRepositoryInfo> goodList =
                new ArrayList<GsacRepositoryInfo>();
            Hashtable<String, Hashtable<String, Capability>> collectionToUsedCapabilities =
                new Hashtable<String, Hashtable<String, Capability>>();
            boolean anyErrors = false;
            for (GsacRepositoryInfo info : list) {
                try {
                    initRemoteRepository(info, collectionToUsedCapabilities);
                    goodList.add(info);
                } catch (Exception exc) {
                    anyErrors = true;
                    // System.err.println("GSAC: Initializing remote repository:" + info + " " + exc);
                    exc.printStackTrace();
                    //getLogManager().logError( "Initializing remote repository:" + info, exc);
                }
            }
            //If there were any errors then reset the ttl for the list holder
            //to check back in 15 minutes
            //else if no errors then set it to the default 60
            if (anyErrors) {
                servers.setTimeThreshold(TTLCache.MS_IN_A_MINUTE * 15);
            } else {
                servers.setTimeThreshold(TTLCache.MS_IN_AN_HOUR * 6);
            }
            list = goodList;
            servers.put(list);

            for (Enumeration collectionKeys =
                    collectionToUsedCapabilities.keys();
                    collectionKeys.hasMoreElements(); ) {
                Hashtable<String, Capability> caps =
                    collectionToUsedCapabilities.get(
                        collectionKeys.nextElement());
                for (Enumeration keys =
                        caps.keys(); keys.hasMoreElements(); ) {
                    Capability capability =
                        (Capability) caps.get(keys.nextElement());
                    String group = null;
                    if (capability.getRepositories().size() == 1) {
                        group = capability.getRepositories().get(0).getName()
                                + ":";
                    } else {
                        group = "Remote Repositories";
                        group = "";
                    }
                    if (group != null) {
                        if (capability.hasGroup()) {
                            capability.setGroup(group
                                    + capability.getGroup());
                        } else {
                            capability.setGroup(group);
                        }
                    }
                }
            }
        }

        return new ArrayList<GsacRepositoryInfo>(list);
    }


    /**
     * Initialize the remote repository. Merge its capabilities with all the others
     *
     * @param info repository info
     * @param collectionToUsedCapabilities holds the capabilities
     *
     * @throws Exception On badness
     */
    private void initRemoteRepository(
            GsacRepositoryInfo info,
            Hashtable<String,
                      Hashtable<String,
                                Capability>> collectionToUsedCapabilities)
            throws Exception {
        GsacRepositoryInfo gri = (GsacRepositoryInfo) getRemoteObject(info,
                                     URL_REPOSITORY_VIEW, "", OUTPUT_GSACXML);


        info.initWith(gri);


        for (CapabilityCollection collection : gri.getCollections()) {
            Hashtable<String, Capability> used =
                collectionToUsedCapabilities.get(
                    collection.getResourceClass().getName());
            if (used == null) {
                used = new Hashtable<String, Capability>();
                collectionToUsedCapabilities.put(
                    collection.getResourceClass().getName(), used);
            }

            List<Capability> mergedCapabilities =
                Capability.mergeCapabilities(collection.getCapabilities(),
                                             used);
            //            capability.addRepository(info);
        }
    }


    /**
     * Get the remote repositories specified in the request
     *
     * @param request the request
     *
     * @return selected remote repositories
     */
    public List<GsacRepositoryInfo> getServers(GsacRequest request) {
        List<GsacRepositoryInfo> allServers = getServers();
        if (allServers.size() == 0) {
            return allServers;
        }
        if ( !request.defined(ARG_REPOSITORY)) {
            return allServers;
        }
        List urls = request.get(ARG_REPOSITORY, new ArrayList());
        List<GsacRepositoryInfo> selectedServers =
            new ArrayList<GsacRepositoryInfo>();
        for (GsacRepositoryInfo server : allServers) {
            if (urls.contains(server.getUrl())) {
                selectedServers.add(server);
            }
        }

        return selectedServers;
    }


    /**
     * Find the remote repository that is at the given url
     *
     * @param url url
     *
     * @return remote repository or null if none found
     */
    public GsacRepositoryInfo getRepositoryInfo(String url) {
        for (GsacRepositoryInfo info : getServers()) {
            if (url.equals(info.getUrl())) {
                return info;
            }
        }

        return null;
    }

    /**
     * This fetches the remote repository information from the server given by the repositoryUrl
     * Right now this uses the Unidata java object/xml encoder/decoder. We need to move to a simple
     * xml representation
     *
     * @param repositoryUrl Points to remote server.
     *
     * @return repository info object.
     *
     * @throws Exception On badness
     */
    public GsacRepositoryInfo retrieveRepositoryInfo(String repositoryUrl)
            throws Exception {
        GsacRepositoryInfo gri =
            (GsacRepositoryInfo) getRemoteObject(repositoryUrl,
                URL_REPOSITORY_VIEW, "", OUTPUT_GSACXML);

        return gri;
    }


    /**
     * A hook for derived classes to create and define remote repositories
     *
     * @param servers list to add to
     */
    public void doMakeServerInfoList(List<GsacRepositoryInfo> servers) {}





    /**
     * Get the base path of the url, e.g., /gsacws. This looks for the PROP_BASEURL property
     *
     * @return url base path
     */
    public String getUrlBase() {
        if (urlBase == null) {
            urlBase = getProperty(PROP_BASEURL, "");
        }

        return urlBase;
    }

    /**
     * get name to use
     *
     * @return repository name
     */
    public String getRepositoryName() {
        return getProperty(PROP_REPOSITORY_NAME, "GSAC Repository");
    }


    /**
     * get icon to use
     *
     * @return repository icon path
     */
    public String getRepositoryIcon() {
        return getProperty(PROP_REPOSITORY_ICON, (String) null);
    }

    /**
     * get repository description
     *
     * @return repository description
     */
    public String getRepositoryDescription() {
        return getProperty(PROP_REPOSITORY_DESCRIPTION, "");
    }

    /**
     * Does this repository handle resources.
     *
     * @return can do resources
     */
    public boolean canDoResources() {
        return true;
    }


    /**
     * This gets the path to the given file that is in the derived repositories resources dir. e.g:
     * /org/unavco/projects/gsac/repository/resources/fileTail
     *
     * @param fileTail the file tail
     *
     * @return full path to file tail
     */
    public String getLocalResourcePath(String fileTail) {
        if ( !fileTail.startsWith("/")) {
            fileTail = "/" + fileTail;
        }
        String packagePath = getPackagePath();

        return packagePath + "/resources" + fileTail;
    }


    /**
     * Get the java package path. We use this to look up things in the resources subdir.
     * This  will be the path  to the derived class, e.g., /org/unavco/projects/gsac/repository
     *
     * @return java package path
     */
    public String getPackagePath() {
        String packageName = getClass().getPackage().getName();
        packageName = "/" + packageName.replace(".", "/");

        return packageName;
    }


    /**
     * Get the full java resource path to the local gsl/resources dir
     *
     * @param fileTail file name under resources
     *
     * @return full path
     */
    public String getCoreResourcePath(String fileTail) {
        return GSAC_PATH_RESOURCES + fileTail;
    }

    /**
     * get the path to the derived repository's htdocs dir
     *
     * @param fileTail file name under htdocs
     *
     * @return Full path to htdocs file
     */
    public String getLocalHtdocsPath(String fileTail) {
        if (fileTail.startsWith("/")) {
            return getPackagePath() + "/htdocs" + fileTail;
        }

        return getPackagePath() + "/htdocs/" + fileTail;
    }

    /**
     * Add content for stats page
     *
     * @param sb buffer
     */
    public void addStats(StringBuffer sb) {
        if (databaseManager != null) {
            sb.append(HtmlUtil.formEntry("DB pool:",
                                         databaseManager.getPoolStats()));
        }
    }


    /**
     * Get the databasemanager.
     * If not created yet then this calls the factory method doMakeDatabaseManager
     *
     * @return databasemanager
     */
    public GsacDatabaseManager getDatabaseManager() {
        if (databaseManager == null) {
            try {
                databaseManager = doMakeDatabaseManager();
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            }
        }

        return databaseManager;
    }


    /**
     * Factory method for making the GsacDatabaseManager.
     * Derived classes should override this method and create their
     * own database manager.
     *
     * @return database manager
     *
     * @throws Exception On badness
     */
    public GsacDatabaseManager doMakeDatabaseManager() throws Exception {
        return null;
    }



    /**
     * Get the logmanager. If not created yet then this calls the factory method doMakeLogManager
     *
     * @return logmanager
     */
    public GsacLogManager getLogManager() {
        if (logManager == null) {
            logManager = doMakeLogManager();
        }

        return logManager;
    }


    /**
     * Factory method for making the GsacLogManager.
     * Derived classes should override this method and create their
     * own log manager.
     *
     * @return log manager
     */
    public GsacLogManager doMakeLogManager() {
        return new GsacLogManager(this);
    }

    /**
     * translate the given phrase. use the msgProperties
     *
     * @param phrase phrase
     *
     * @return translated phrase or null
     */
    public String translatePhrase(String phrase) {
        return (String) msgProperties.get(phrase.replace(" ", "_"));
    }



    /**
     * Is this repository capable of certain things.
     *
     *
     * @param arg This is usually the name of the URL argument, e.g., ARG_FILE_SORT_VALUE, ARG_FILE_SORT_ORDER, ARG_FILE_SIZE
     *
     * @return Is this repository capable
     */
    public boolean isCapable(String arg) {
        String  key    = "capability." + arg;
        boolean result = getProperty(key, false);

        return result;
    }


    /**
     * Return the full hostname of the server. If null then the GSL uses the hostname from the localhost inet address
     *
     * @return server hostname
     */
    public String getHostname() {
        return getProperty(PROP_HOSTNAME);
    }

    /**
     * get the server port
     *
     * @return defaults to 8080
     */
    public int getPort() {
        //System.err.println("GSAC:    getPort() set to 8080" );  
        return getProperty(PROP_PORT, 8080);
    }




    /**
     * _more_
     *
     * @param arg _more_
     * @param value _more_
     *
     * @return _more_
     */
    public String toRepositoryNamespace(String arg, String value) {
        return value;
    }



    /**
     * Should we check block robots
     *
     * @return true by default
     */
    public boolean repelRobots() {
        return true;
    }

    /**
     * the GsacRepository holds all of its web content in the java jar files and
     * accesses them as java resources. This looks in gsl/htdocs as well
     * as the repository instances <repository>/htdocs dir for content.
     * It is a .js, .css or .jnlp file then they are treated as templates
     * and the macros ${fullurlroot} and ${urlroot} are replaced with their
     * appropriate value
     *
     *
     * @param request the request
     *
     * @throws Exception On badness
     */
    public void handleRequestHtdocs(GsacRequest request) throws Exception {
        String      uri         = request.getRequestURI();
        int idx = uri.indexOf(URL_HTDOCS_BASE) + URL_HTDOCS_BASE.length();
        String      path        = uri.substring(idx);

        InputStream inputStream = null;
        String[]    paths       = new String[] { getLocalHtdocsPath(path),
                GSAC_PATH_HTDOCS + path };

        for (String fullPath : paths) {
            try {
                inputStream = getResourceInputStream(fullPath);
                if (inputStream != null) {
                    break;
                }
            } catch (Exception exc) {}
        }

        if (inputStream == null) {
            request.sendError(HttpServletResponse.SC_NOT_FOUND,
                              "Could not find:" + path);

            return;
        }
        if (uri.endsWith(".js") || uri.endsWith(".css")
                || uri.endsWith(".jnlp") || uri.endsWith(".xml")) {
            String content = IOUtil.readContents(inputStream);
            inputStream.close();
            content     = replaceMacros(request, content);
            content     = replaceMacros(request, content);
            inputStream = new ByteArrayInputStream(content.getBytes());
        }
        OutputStream outputStream = request.getOutputStream();
        IOUtil.writeTo(inputStream, outputStream);
        IOUtil.close(outputStream);
    }


    /**
     * _more_
     *
     * @param template _more_
     *
     * @return _more_
     */
    private String replaceMacros(String template) {
        return replaceMacros(null, template);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param template _more_
     *
     * @return _more_
     */
    private String replaceMacros(GsacRequest request, String template) {

        template = template.replace("${htdocs}",
                                    getUrlBase() + URL_HTDOCS_BASE);

        template = template.replace("${description}",
                                    getRepositoryDescription());
        template = template.replace("${name}", getRepositoryName());
        template = template.replace("${urlroot}", getUrlBase() + URL_BASE);
        template = template.replace("${fullurlroot}",
                                    getAbsoluteUrl(request,
                                        getUrlBase() + URL_BASE));

        return template;
    }


    /**
     * This handles the request for the main page of the repository.
     *
     * @param request the request
     * @param response the response
     *
     * @throws Exception On badness
     */
    public void handleRequestIndex(GsacRequest request, GsacResponse response)
            throws Exception {
        response.startResponse(GsacResponse.MIME_HTML);
        StringBuffer sb = new StringBuffer();
        htmlOutputHandler.initHtml(request, response, sb, "");

        //Look in the derived repository and the base one for an index.html file
        String[] files = { getLocalHtdocsPath("/index.html"),
                           GSAC_PATH_HTDOCS + "/index.html" };
        for (String file : files) {
            InputStream inputStream = getResourceInputStream(file);
            if (inputStream != null) {
                String contents = IOUtil.readContents(inputStream);
                sb.append(contents);

                break;
            }
        }
        htmlOutputHandler.finishHtml(request, response, sb);
    }



    /**
     * Shows the integrated help
     *
     * @param request the request
     * @param response The response to write to
     *
     * @throws Exception On badness
     */
    public void handleRequestHelp(GsacRequest request, GsacResponse response)
            throws Exception {
        if (helpIndex == null) {
            InputStream inputStream =
                servlet.getResourceInputStream(GSAC_PATH_HELP + "/index.txt");
            helpIndex = StringUtil.split(IOUtil.readContents(inputStream),
                                         "\n", true, true);
            inputStream.close();
        }

        String path = request.getGsacUrlPath().substring(URL_HELP.length());
        if (path.length() == 0) {
            path = "/index.html";
        }

        InputStream inputStream = getResourceInputStream(GSAC_PATH_HELP
                                      + path);
        if (inputStream == null) {
            //TODO:         inputStream = getResourceInputStream(path);
        }
        if ( !path.endsWith(".html")) {
            OutputStream outputStream = request.getOutputStream();
            IOUtil.writeTo(inputStream, outputStream);
            IOUtil.close(outputStream);
            IOUtil.close(inputStream);

            return;
        }

        StringBuffer sb = new StringBuffer();
        htmlOutputHandler.initHtml(request, response, sb, "Help");
        String contents = "Could not read file:" + path;
        if (inputStream != null) {
            contents = IOUtil.readContents(inputStream);
            inputStream.close();
            contents = replaceMacros(request, contents);
            contents = replaceMacros(request, contents);
        }
        sb.append(contents);
        htmlOutputHandler.finishHtml(request, response, sb);
    }


    /**
     * This shows server stats and a stack trace. Will need to turn this off once we go into productio
     *
     * @param request the request
     * @param response The response to write to
     *
     * @throws Exception On badness
     */
    public void handleRequestStats(GsacRequest request, GsacResponse response)
            throws Exception {
        response.startResponse(GsacResponse.MIME_TEXT);
        request.put(ARG_DECORATE, "false");
        StringBuffer sb = new StringBuffer();
        htmlOutputHandler.initHtml(request, response, sb, "Statistics");

        sb.append(HtmlUtil.formTable());
        DecimalFormat fmt         = new DecimalFormat("#0");

        double        totalMemory = (double) Runtime.getRuntime().maxMemory();
        double        freeMemory  = (double) Runtime.getRuntime().freeMemory();
        double highWaterMark = (double) Runtime.getRuntime().totalMemory();
        double        usedMemory  = (highWaterMark - freeMemory);
        totalMemory = totalMemory / 1000000.0;
        usedMemory  = usedMemory / 1000000.0;
        sb.append(HtmlUtil.formEntry("Total Memory Available:",
                                     fmt.format(totalMemory) + " (MB)"));
        sb.append(HtmlUtil.formEntry("Used Memory:",
                                     fmt.format(usedMemory) + " (MB)"));

        sb.append(HtmlUtil.formEntry("# Requests:", "" + numServiceRequests));
        sb.append(HtmlUtil.formEntry("Start Time:", "" + startDate));


        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        sb.append(HtmlUtil.formEntry("Up Time:",
                                     fmt.format((double) (uptime / 1000
                                         / 60)) + " " + msg("minutes")));

        addStats(sb);
        sb.append(HtmlUtil.formTableClose());

        sb.append(LogUtil.getStackDump(true));
        htmlOutputHandler.finishHtml(request, response, sb);
    }




    /**
     * Handle the request
     *
     *
     * @param resourceClass Type of resource
     * @param request The request
     * @param response The response
     *
     * @throws Exception on badnesss
     */
    public void processRequest(ResourceClass resourceClass,
                               GsacRequest request, GsacResponse response)
            throws Exception {
        GsacResourceManager gom = getResourceManager(resourceClass);
        if (gom != null) {
            gom.handleRequest(request, response);

            return;
        }
        notImplemented("No handler for " + resourceClass);
    }


    /**
     * helper method.
     *
     * @return the html output handler
     */
    HtmlOutputHandler getHtmlOutputHandler() {
        return htmlOutputHandler;
    }


    /**
     * This reads and processes  a properties file and returns a list of IdLabel objecs
     *
     * @param path resource path
     *
     * @return IdLabel objects
     *
     * @throws Exception On badness
     */
    public List<IdLabel> readProperties(String path) throws Exception {
        InputStream inputStream = servlet.getResourceInputStream(path);
        if (inputStream == null) {
            return null;
        }
        List<IdLabel> results = new ArrayList<IdLabel>();
        for (String line :
                StringUtil.split(IOUtil.readContents(inputStream), "\n",
                                 true, true)) {
            if ((line.length() == 0) || line.startsWith("#")) {
                continue;
            }
            List<String> tuple = StringUtil.splitUpTo(line, "=", 2);
            if (tuple.size() == 1) {
                results.add(new IdLabel(tuple.get(0)));
            } else {
                results.add(new IdLabel(tuple.get(0), tuple.get(1)));
            }
        }
        inputStream.close();

        return results;
    }

    /**
     * _more_
     *
     * @param type _more_
     *
     * @return _more_
     */
    public String getInternalVocabulary(String type) {
        return readVocabulary(getLocalResourcePath("/vocabulary/" + type
                + ".properties"));
    }


    /**
     * _more_
     *
     * @param type _more_
     *
     * @return _more_
     */
    public String getInternalVocabularyMap(String type) {
        return readVocabulary(getLocalResourcePath("/vocabulary/" + type
                + ".map"));
    }


    /**
     * _more_
     *
     * @param type _more_
     *
     * @return _more_
     */
    public String getExternalVocabulary(String type) {
        return readVocabulary(getCoreResourcePath("/vocabulary/" + type
                + ".properties"));
    }

    /**
     * _more_
     *
     * @param path _more_
     *
     * @return _more_
     */
    public String readVocabulary(String path) {
        try {
            InputStream is = getResourceInputStream(path);
            if (is == null) {
                //                System.err.println("GSAC: Failed to read vocabulary for:" + path);
                return "";
            }

            return IOUtil.readContents(is);
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }


    /**
     * This method creates  a Vocabulary object for the given vocabulary type.
     * It reads the external vocabulary file: org/gsac/gsl/resources/vocabulary/&lt;type&gt;.properties
     * It reads 2 files from the local repository path:
     * &lt;gsac repository implementation path&gt;/resources/vocabulary
     * &lt;type&gt;.properties
     * &lt;type&gt;.map
     * </pre>
     * The GSL type.properties file has a mapping from a vocabulary value to a descriptive label
     * The local type.properties file allows a derived repository to add to the this value=label list
     * The local type.map defines a mapping from the external values (e.g., the ones defined in the .properties file)
     * to the internal values used by the derived repository.
     *
     *
     * @param type vocabulary type
     *
     * @return the new vocabulary object
     */
    public Vocabulary getVocabularyFromType(String type) {

        Hashtable<String, List<String>> externalToInternal =
            new Hashtable<String, List<String>>();
        Hashtable<String, String> internalToExternal = new Hashtable<String,
                                                           String>();
        List<IdLabel>   vocabulary         = new ArrayList<IdLabel>();
        HashSet<String> coreMap            = new HashSet<String>();
        HashSet<String> internalMap        = new HashSet<String>();

        String[]        vocabularyContents = { getExternalVocabulary(type),
                getInternalVocabulary(type) };

        //        boolean debug = type.indexOf("file.type")>=0;
        boolean debug = false;
        for (int i = 0; i < vocabularyContents.length; i++) {
            if (debug) {
                System.err.println("processing " + ((i == 0)
                        ? "external"
                        : "internal"));
            }
            for (List<String> toks :
                    tokenizeVocabulary(vocabularyContents[i])) {
                if (debug) {
                    System.err.println("\ttoks (1): " + toks);
                }
                String value = toks.get(0);
                String label = ((toks.size() == 2)
                                ? toks.get(1)
                                : value);
                if (debug) {
                    System.err.println("\tadding to vocabulary: " + value
                                       + " " + label);
                }
                vocabulary.add(new IdLabel(value, label));
                if (i == 0) {
                    coreMap.add(value);
                } else {
                    internalMap.add(value);
                    if (debug) {
                        System.err.println("\tadding internal vocab: "
                                           + value);
                    }
                }
            }
        }

        List<IdLabel> values = new ArrayList<IdLabel>();
        boolean       localRepositoryHasDefinedAMapping = false;
        for (List<String> toks :
                tokenizeVocabulary(getInternalVocabularyMap(type))) {
            localRepositoryHasDefinedAMapping = true;
            String coreValue           = toks.get(0);
            String internalValueString = ((toks.size() == 2)
                                          ? toks.get(1)
                                          : "");
            //If there is a core value defined for a internal repository that is not in the core list then add it
            if ( !coreMap.contains(coreValue)
                    && !internalMap.contains(coreValue)) {
                vocabulary.add(new IdLabel(coreValue, coreValue));
            }
            internalMap.add(coreValue);
            List<String> internalValues = externalToInternal.get(coreValue);
            if (internalValues == null) {
                internalValues = new ArrayList<String>();
                externalToInternal.put(coreValue, internalValues);
            }

            List<String> internalToks = StringUtil.split(internalValueString,
                                            ",", true, true);
            for (String internalTok : internalToks) {
                internalToExternal.put(internalTok, coreValue);
            }
            internalValues.addAll(internalToks);
        }

        //Set this to true now while we figure out how to handle the
        //case where there is nothing defined interally
        //        localRepositoryHasDefinedAMapping = true;

        if ( !localRepositoryHasDefinedAMapping) {}



        //Now prune out from the vocab list anything that isn't used by the internal repository
        //We do 2 passes here
        //First we add all the wildcards plus any non-wildcard that should be included
        //Next we go through the wildcards and only include those that have a non-wildcard match
        List<IdLabel> valuesWithoutWildcards = new ArrayList<IdLabel>();
        List<IdLabel> valuesWithBoth         = new ArrayList<IdLabel>();
        for (IdLabel value : vocabulary) {
            boolean isWildcard = Vocabulary.isWildcard(value.getId());
            if (internalMap.contains(value.getId()) || isWildcard
                    || !localRepositoryHasDefinedAMapping) {
                valuesWithBoth.add(value);
                if ( !isWildcard) {
                    valuesWithoutWildcards.add(value);
                }
            } else {
                if (debug) {
                    System.err.println("Skipping: " + value);
                }
            }
        }

        for (IdLabel value : valuesWithBoth) {
            boolean isWildcard = Vocabulary.isWildcard(value.getId());
            if ( !isWildcard || !localRepositoryHasDefinedAMapping) {
                values.add(value);
            } else {
                //Check if there is anything in the list that matches a wildcard
                String s = value.getId().substring(0,
                               value.getId().length() - 1);
                for (IdLabel nonWildcardValue : valuesWithoutWildcards) {
                    if (nonWildcardValue.getId().startsWith(s)) {
                        values.add(value);

                        break;
                    }
                }
            }
        }

        Vocabulary vocab = new Vocabulary(type, values, externalToInternal,
                                          internalToExternal);
        vocabularyList.add(vocab);
        vocabularies.put(type, vocab);

        return vocab;

    }


    /**
     * split the string on newline and look for either:
     * <pre>key=value</pre>
     * Or just:
     * <pre>key</pre>
     * Lines beginning with "#" are treated as comments
     *
     * @param contents _more_
     *
     * @return _more_
     */
    private List<List<String>> tokenizeVocabulary(String contents) {
        List<List<String>> lines = new ArrayList<List<String>>();
        for (String line : StringUtil.split(contents, "\n", true, true)) {
            if (line.startsWith("#")) {
                continue;
            }
            List<String> toks = StringUtil.split(line, "=", true, true);
            if (toks.size() == 0) {
                continue;
            }
            lines.add(toks);
        }

        return lines;
    }


    /**
     * This creates if needed and returns a GsacRepositoryInfo object that
     * holds the information about this repository, e.g., name, description, url, icon
     * and (most importantly) the query capabilities
     *
     * @return info about me
     */
    public GsacRepositoryInfo getRepositoryInfo() {
        if (myInfo == null) {
            GsacRepositoryInfo gri =
                new GsacRepositoryInfo(
                    getServlet().getAbsoluteUrl(getUrlBase()),    // to get Base URL determined by Java from where built (NOT hard-coded by user)
                    getRepositoryName(), getRepositoryIcon());
            gri.setDescription(getRepositoryDescription());
            for (GsacResourceManager gom : resourceManagers) {
                gri.addCollection(gom.getCapabilityCollection());
            }
            myInfo = gri;
            for (CapabilityCollection collection : gri.getCollections()) {
                for (Capability capability : collection.getCapabilities()) {
                    String key = "capability." + capability.getId();
                    properties.put(key, "true");
                }
            }
        }

        return myInfo;
    }


    /**
     * _more_
     *
     * @param resourceClass Type of resource
     * @param gom _more_
     */
    public void addResourceManager(ResourceClass resourceClass,
                                   GsacResourceManager gom) {
        resourceManagerMap.put(resourceClass, gom);
        resourceManagers.add(gom);
    }


    /**
     * _more_
     *
     * @param resourceClass Type of resource
     *
     * @return _more_
     */
    public GsacResourceManager doMakeResourceManager(
            ResourceClass resourceClass) {
        return null;
        /*
        if (resourceClass.equals(GsacSite.CLASS_SITE)) {
            return new SiteManager(this) {
                public GsacResource getResource(String resourceId)
                        throws Exception {
                    return null;
                }
            };
        }
        if (resourceClass.equals(GsacFile.CLASS_FILE)) {
            return new FileManager(this) {
                public void handleRequest(GsacRequest request,
                                          GsacResponse response)
                        throws Exception {}
                public GsacResource getResource(String resourceId)
                        throws Exception {
                    return null;
                }
            };
        }
        return null;*/
    }


    /**
     * _more_
     *
     * @param name _more_
     *
     * @return _more_
     */
    public ResourceClass getResourceClass(String name) {
        for (GsacResourceManager gom : resourceManagers) {
            if (gom.getResourceClass().getName().equals(name)) {
                return gom.getResourceClass();
            }
        }

        return null;
    }




    /**
     * _more_
     *
     * @param request the request
     * @param type _more_
     * @param resourceId _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public GsacResource getResource(GsacRequest request, ResourceClass type,
                                    String resourceId)
            throws Exception {
        GsacResourceManager gom      = getResourceManager(type);
        GsacResource        resource = gom.getResourceFromCache(resourceId);
        if (resource != null) {
            return resource;
        }
        resource = doGetResource(type, resourceId);
        if (resource != null) {
            gom.cacheResource(resource);
        }

        return resource;
    }



    /**
     * This should be overwritten by derived classes to create the resource
     *
     *
     * @param type _more_
     * @param resourceId resource id
     *
     * @return the resource
     *
     * @throws Exception on badnesss
     */
    public GsacResource doGetResource(ResourceClass type, String resourceId)
            throws Exception {
        GsacResourceManager gom = getResourceManager(type);

        return gom.getResource(resourceId);
    }

    /**
     * This gets called by OutputHandlers when they need all of the metadata for a resource
     * If the given  resource has all of its metadata already then this method just returns.
     * Else the method doGetFillMetadata is called. A repository implementation can overwrite
     * this method to add the full metadata to the resource
     *
     *
     * @param request the request
     * @param gsacResource _more_
     *
     * @throws Exception On badness
     */
    public void getMetadata(GsacRequest request, GsacResource gsacResource)
            throws Exception {
        int level = request.get(ARG_METADATA_LEVEL, 1);
        if (gsacResource.getMetadataLevel() >= level) {
            return;
        }
        doGetFullMetadata(level, gsacResource);
        gsacResource.setMetadataLevel(level);
    }


    /**
     * Gets called to add the full metadata to the resource.
     * If there is a resourcemanager then this is just a pass through
     * to it.
     *
     *
     * @param level For now describes the level of detail wanted for the given metadata.
     * We need to revamp that and maybe use a string name for the metadata group
     * @param gsacResource _more_
     *
     * @throws Exception On badness
     */
    public void doGetFullMetadata(int level, GsacResource gsacResource)
            throws Exception {
        GsacResourceManager gom =
            getResourceManager(gsacResource.getResourceClass());
        if (gom != null) {
            gom.doGetMetadata(level, gsacResource);
        }
    }

    /**
     * Clear the cache
     */
    public void clearCache() {
        for (GsacResourceManager gom : resourceManagers) {
            gom.clearCache();
        }
    }




    /**
     * add extra site search form elements
     *
     * @param request request
     * @param buffer buffer to append to
     * @param resourceClass Type of resource
     *
     * @return _more_
    
    */
    public boolean addToSearchForm(GsacRequest request, Appendable buffer,
                                   ResourceClass resourceClass) {
        //e.g.:
        //        buffer.append(HtmlUtil.formEntry("City:",
          //                     HtmlUtil.input(ARG_CITY,
            //                   request.get(ARG_CITY, (String) null)));
        return true;
    }


    /** LOOK: */
    boolean readHtmlEveryTime = true;

    /**
     * Override this to return the html header to use for html pages
     *
     * @param request The request
     *
     * @return html header
     */
    public String getHtmlHeader(GsacRequest request) {
        if (readHtmlEveryTime) {
            try {
                mobileHeader = IOUtil.readContents(GSAC_PATH_RESOURCES
                        + "/mobileheader.html", mobileHeader);
                mobileHeader = replaceMacros(mobileHeader);
                InputStream inputStream = getResourceInputStream(
                                              getLocalResourcePath(
                                                  "/header.html"));
                if (inputStream != null) {
                    htmlHeader = IOUtil.readContents(inputStream);
                    htmlHeader = replaceMacros(htmlHeader);
                    inputStream.close();
                }
            } catch (Exception exc) {}
        }
        if (request.isMobile()) {
            return mobileHeader;
        }
  
        // add thing to get browser to show utf-8 characters ok:
        htmlHeader = htmlHeader.replaceFirst("<head>", "<head><meta charset='utf-8'>");

        //System.err.println(" html header "+htmlHeader);
        return htmlHeader;
    }

    /**
     * Override this to return the html footer to use for html pages
     *
     * @param request The request
     *
     * @return html footer
     */
    public String getHtmlFooter(GsacRequest request) {
        if (readHtmlEveryTime) {
            try {
                mobileFooter = IOUtil.readContents(GSAC_PATH_RESOURCES
                        + "/mobilefooter.html", mobileFooter);
                mobileFooter = replaceMacros(mobileFooter);
                InputStream inputStream = getResourceInputStream(
                                              getLocalResourcePath(
                                                  "/footer.html"));
                if (inputStream != null) {
                    htmlFooter = IOUtil.readContents(inputStream);
                    htmlFooter = replaceMacros(htmlFooter);
                    inputStream.close();
                }
            } catch (Exception exc) {}
        }

        if (request.isMobile()) {
            return mobileFooter;
        }

        return htmlFooter;
    }


    /**
     * Override this to decorate the html
     *
     * @param request the request
     * @param sb the html
     *
     * @return decorated html
     */
    public Appendable decorateHtml(GsacRequest request, Appendable sb) {
        return sb;
    }



    /**
     * Log the error
     *
     * @param message message
     * @param exc exception
     */
    public void logError(String message, Throwable exc) {
        getLogManager().logError(message, exc);
    }

    /**
     * Log the info
     *
     * @param message message
     */
    public void logInfo(String message) {
        getLogManager().logInfo(message);
    }





    /**
     * throws error
     *
     * @param message error message
     */
    private void notImplemented(String message) {
        throw new IllegalArgumentException("Not implemented:" + message);
    }



    /**
     * _more_
     *
     * @param info _more_
     * @param id _more_
     *
     * @return _more_
     */
    public String getRemoteId(GsacRepositoryInfo info, String id) {
        return XmlUtil.encodeBase64(info.getUrl().getBytes()).trim() + ":"
               + id;
    }


    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    public String[] decodeRemoteId(String id) {
        List<String> pair     = StringUtil.splitUpTo(id, ":", 2);
        String       host     = new String(XmlUtil.decodeBase64(pair.get(0)));
        String       remoteId = pair.get(1);

        return new String[] { host, remoteId };
    }

    /**
     * _more_
     *
     * @param resource _more_
     *
     * @return _more_
     */
    public String getRemoteHref(GsacResource resource) {
        GsacRepositoryInfo info = resource.getRepositoryInfo();
        if (info == null) {
            return "";
        }
        String icon = info.getIcon();
        if (icon == null) {
            icon = iconUrl("/favicon.ico");
        }

        return HtmlUtil.href(getRemoteUrl(resource),
                             HtmlUtil.img(icon, "View at " + info.getName()));
    }


    /**
     * _more_
     *
     * @param resource _more_
     *
     * @return _more_
     */
    public boolean isRemoteResource(GsacResource resource) {
        if (resource.getRepositoryInfo() == null) {
            return false;
        }

        return true;
    }


    /**
     * If the given resource is from a remote repository then create and return
     * the view URL that points to that resource. Else return null.
     *
     * @param resource the resource
     *
     * @return its remote url or null if its not a remote resource
     */
    public String getRemoteUrl(GsacResource resource) {
        if ( !isRemoteResource(resource)) {
            return null;
        }
        List<String> pair = StringUtil.splitUpTo(resource.getId(), ":", 2);
        String       id   = pair.get(1);

        return resource.getRepositoryInfo().getUrl() + resource.getViewUrl()
               + "?" + HtmlUtil.args(new String[] { resource.getIdArg(),
                id });
    }

    /**
     * _more_
     *
     * @param info _more_
     */
    public void remoteRepositoryHadError(GsacRepositoryInfo info) {
        info.incrementErrorCount();
        if (info.getErrorCount() > 10) {
            //TODO: what to do here? remove it from the list?
            //After 10 errors
        }
    }


    /**
     * _more_
     *
     * @param info _more_
     * @param urlPath _more_
     * @param urlArgs _more_
     * @param output _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public Object getRemoteObject(GsacRepositoryInfo info, String urlPath,
                                  String urlArgs, String output)
            throws Exception {
        try {
            Object results = getRemoteObject(info.getUrl(), urlPath, urlArgs,
                                             output);
            //If successful then reset the error count
            info.resetErrorCount();

            return results;
        } catch (Exception exc) {
            remoteRepositoryHadError(info);

            throw exc;
        }
    }




    /** _more_ */
    public static final int URL_TIMEOUT_SECONDS = 60;


    /**
     * _more_
     *
     * [used for a federated GSAC, to connect to one remote GSAC and get its XML capabilites file]
     *
     * @param repositoryUrl _more_
     * @param urlPath _more_
     * @param urlArgs _more_
     * @param output _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public Object getRemoteObject(String repositoryUrl, String urlPath,
                                  String urlArgs, String output)
            throws Exception {

        // debug federated gsac System.err.println("\nGSAC:    GSACRepository: getRemoteObject was called to connect to a remote GSAC and get its XML capabilities file (federating GSACs).");
        // repositoryUrl is to the remote GSAC to federate
        // debug federated gsac nSystem.err.println  ("GSAC:    GSACRepository: getRemoteObject input is repositoryUrl="+ repositoryUrl+", urlPath="+urlPath+", urlArgs="+urlArgs+", output="+output+".");

        boolean     zipit          = false;
        GsacServlet servlet        = getServlet();
        String      thisRepository = "client";

        if (servlet != null) {
            thisRepository = servlet.getAbsoluteUrl(getUrlBase());
            //System.err.println("GSAC:    GSACRepository: getRemoteObject  THIS (my local) GSAC repository's UrlBase = "+ thisRepository);
            // Base URL :  this code returns here  the URL  http://www.unavco.org:8080/gsacfederated   with 8080 in unavco federated GSAC on facdev
        }

        String url = repositoryUrl + urlPath + "?" + urlArgs + "&"
                     + HtmlUtil.args(new String[] { ARG_REMOTEREPOSITORY, thisRepository, ARG_GZIP, zipit + "", ARG_OUTPUT, output });

        // 1.
        // debug federated gsac System.err.println("GSAC:    GSACRepository: getRemoteObject connect to remote GSAC at URL "+ repositoryUrl); // some more GSAC URL:  + "   "+urlPath +"  "+urlArgs);  
        URLConnection connection = new URL(url).openConnection();
        // debug federated gsac System.err.println("GSAC:    GSACRepository:                  OK did openConnection() at that URL to the remote GSAC, and " );
        // debug federated gsac System.err.println("GSAC:    GSACRepository:                  the URLConnection with the GSAC API request for the XML file is:" );
        // debug federated gsac System.err.println("         "+ connection.toString() );

        // 2. 
        //System.err.println    ("GSAC:    GSACRepository: getRemoteObject call getUserAgent() " );
        String  userAgent  = getUserAgent();  // for example userAgent = "gsac federated"
        if (userAgent != null) {
            //System.err.println("GSAC:    GSACRepository: getRemoteObject ok did getUserAgent(): userAgent = "+ userAgent.toString() );

            //System.err.println("GSAC:    GSACRepository: getRemoteObject call connection.setRequestProperty() " );
            connection.setRequestProperty("User-Agent", userAgent);
            // debug federated gsac System.err.println("GSAC:    GSACRepository: getRemoteObject  OK did connection.setRequestProperty('User-Agent', userAgent)" );
        }
        else {
            // debug federated gsac System.err.println("GSAC:    GSACRepository: getRemoteObject call to getUserAgent() got NULL; return null from getRemoteObject(). no capabilities file recovered.  \n " ); 
            return null;
        }

        // 3. 
        connection.setConnectTimeout(1000 * URL_TIMEOUT_SECONDS);
        // debug federated gsac System.err.println("GSAC:    GSACRepository: getRemoteObject call connection.getInputStream() for the XML capabilites file, from "+ repositoryUrl );

        InputStream inputStream = connection.getInputStream();
        Object xmlfile;
        if (inputStream != null) {
           ;// System.err.println("GSAC:    GSACRepository: getRemoteObject  OK did connection.getInputStream()" );
        }
        else {
            // debug federated gsac System.err.println("GSAC:    GSACRepository: getRemoteObject  failed: connection.getInputStream() got NULL; no capabilities file recovered.  \n " ); 
            //return null;
            ;
        }

        if (zipit) {
            inputStream = new GZIPInputStream(inputStream);
        }

        // original code return decodeObject(IOUtil.readContents(inputStream));

        xmlfile = decodeObject(IOUtil.readContents(inputStream));
        if (xmlfile != null) {
            // debug federated gsac System.err.println("GSAC:    GSACRepository: getRemoteObject: did connection.getInputStream() and decoded the inputStream(XML capab file) , so can use remote GSAC.  ");
            return xmlfile;
        }
        else {
            // debug federated gsac System.err.println("GSAC:    GSACRepository: getRemoteObject  failed to decode the inputStream. getRemoteObject returns NULL.  No connection with the remote GSAC. \n  " ); 
            return xmlfile;
        }
    }

    /**
     * _more_
     *
     * @param request the request
     * @param response The response to write to
     *
     * @throws Exception On badness
     */
    public void handleRequestCapabilities(GsacRequest request,
                                          GsacResponse response)
            throws Exception {
        response.startResponse(GsacResponse.MIME_XML);
        PrintWriter pw  = response.getPrintWriter();
        String      xml = encodeObject(getRepositoryInfo());
        pw.print(xml);
        response.endResponse();
    }



    /**
     * _more_
     *
     * @param request the request
     * @param response The response to write to
     *
     * @throws Exception On badness
     */
    public void handleRequestCapability(GsacRequest request,
                                        GsacResponse response)
            throws Exception {

        String capabilityId = request.get(ARG_CAPABILITY, "");
        response.startResponse(GsacResponse.MIME_CSV);
        GsacRepositoryInfo gri        = getRepositoryInfo();
        PrintWriter        pw         = response.getPrintWriter();
        Capability         capability = gri.getCapability(capabilityId);
        if (capability == null) {
            throw new IllegalArgumentException("Could not find capability:"
                    + capabilityId);
        }

        if ( !capability.isEnumeration()) {
            throw new IllegalArgumentException(
                "Capability is not an eumeration");
        }

        for (IdLabel idLabel : capability.getEnums()) {
            pw.append(idLabel.getId());
            pw.append(",");
            pw.append(idLabel.getLabel());
            pw.append("\n");
        }
        response.endResponse();
    }


    /**
     * _more_
     *
     * @param request the request
     * @param response The response to write to
     *
     * @throws Exception On badness
     */
    public void handleRequestViewXml(GsacRequest request,
                                     GsacResponse response)
            throws Exception {
        response.startResponse(GsacResponse.MIME_XML);
        PrintWriter        pw  = response.getPrintWriter();
        GsacRepositoryInfo gri = getRepositoryInfo();
        gri.toXml(this, request, pw);
        response.endResponse();
    }


    /**
     *
     *
     * _more_
     *
     * @param request the request
     * @param response The response to write to
     *
     * @throws Exception On badness
     */
    public void handleRequestView(GsacRequest request, GsacResponse response)
            throws Exception {

        if (request.get(ARG_OUTPUT, "").equals(OUTPUT_XML)) {
            handleRequestViewXml(request, response);

            return;
        }

        if (request.get(ARG_OUTPUT, "").equals(OUTPUT_GSACXML)) {
            handleRequestCapabilities(request, response);

            return;
        }
        if (request.defined(ARG_CAPABILITY)) {
            handleRequestCapability(request, response);

            return;
        }

        List<GsacRepositoryInfo> servers = getServers();
        GsacRepositoryInfo       gri     = getRepositoryInfo();
        StringBuffer             sb      = new StringBuffer();

        getHtmlOutputHandler().initHtml(request, response, sb, "Repository Information");

        // get and print the string value of 'gsac.repository.name' in the file  myarea/gsac/resources/gsac.properties .   SKW
        sb.append(getHeader(gri.getName()));

        sb.append(HtmlUtil.br());  // print a <br> probably

        // get and print the string 'gsac.repository.description' in the file  myarea/gsac/resources/gsac.properties .   SKW
        // seems redundant abfter the html page header and the above call to sb.append(getHeader(gri.getName()));
        // sb.append(gri.getDescription());

        sb.append("GSAC API Information ");

        sb.append("<p>GSAC enables searches and downloads from archives of geoscience data, including GPS/GNSS station information and instrument data files.");
        sb.append("<br>GSAC can also support searches and downloads of GPS derived product files, and data files from other Earth science instruments such as tide gages.");

        sb.append("<p>To learn how to use GSAC and to see what GSAC provides, begin with  the "+
                  " <a href='http://www.unavco.org/software/data-management/gsac/lib/docs/UNAVCO_GSAC_User_Guide.pdf'>GSAC User Guide</a>.  ");

        sb.append("</p> <p>With information on this page you can compose a GSAC API request. ");
        sb.append(" A GSAC API can be invoked with the Linux curl utility. For example, this command gets site P666 and site P123  information from the UNAVCO archive GSAC, presented in a SINEX format file:" );

        sb.append("<br> curl \"http://www.unavco.org/gsacws/gsacapi/site/search?site.code.searchtype=exact&output=site.snx&limit=50&site.code=P666;P123&site.name.searchtype=exact\" </p> ");

        sb.append("<p>This link ");
        sb.append(HtmlUtil.href(getUrl(URL_REPOSITORY_VIEW) + "?" + ARG_OUTPUT + "=xml", msg("Repository information xml")));
        sb.append(" is an XML file of all capabilities provided by this particular GSAC service. <br> (The Repository information xml file may also be read by other GSAC installations operating a federated GSAC incorporating this GSAC.)</p>");

        sb.append("<p>The Base URL, shown next just below, is used in composing API queries to this GSAC. The four sections following the Base URL list all capabilities (option-argument pairs) used in composing API queries to this GSAC.");

        StringBuffer contents = new StringBuffer();

        // this method adds more text to the  "Appendable pw" = 'contents':
        showRepositoryInfo(request, contents, gri, true);

        //System.err.println("    GsacRepository: handleRequestView(): where built ="+ gri );  
        // like  gri of http://swierd:8080/prototypegsac 

        //sb.append("The four sections following the Base URL list all capabilities (option-argument pairs) used in composing API queries to this GSAC. ");

        StringBuffer tmp = new StringBuffer();

        contents.append(HtmlUtil.p());  

        sb.append("<p>   "); 

        contents.append(getHeader(msg("Misc. Arguments")));

        // debug: sb.append("<br><!-- debug did contents.append(getHeader ... -->  ");

        tmp = new StringBuffer();

        String[] args  = { ARG_LIMIT, ARG_OFFSET, ARG_GZIP };
        String[] descs = { "Number of returned results, e.g., " + ARG_LIMIT
                           + "=2000",
                           "Get next set of results, e.g., " + ARG_OFFSET
                           + "=2000",
                           "GZIP the results, e.g. " + ARG_GZIP + "=true" };

        tmp.append(HtmlUtil.formTable());
        for (int i = 0; i < args.length; i++) {
            tmp.append(HtmlUtil.row(HtmlUtil.cols(args[i], descs[i])));
        }
        tmp.append(HtmlUtil.formTableClose());

        contents.append(HtmlUtil.makeShowHideBlock("", tmp.toString(),
                false));


        tmp = new StringBuffer();
        contents.append(HtmlUtil.p());
        contents.append(getHeader(msg("Output types (Results formats)")));
        tmp.append(HtmlUtil.formTable());
        for (GsacResourceManager resourceManager : resourceManagers) {
            tmp.append(
                HtmlUtil.row(
                    HtmlUtil.colspan(
                        HtmlUtil.b(
                            msg(
                            resourceManager.getResourceLabel(false)
                            + " Outputs")), 2)));
            for (GsacOutput output : resourceManager.getOutputs()) {
                if (output.getForUser()) {
                    tmp.append(HtmlUtil.row(HtmlUtil.cols(output.getLabel(),
                            ARG_OUTPUT + "=" + output.getId())));
                }
            }
        }

        tmp.append(HtmlUtil.formTableClose());
        contents.append(HtmlUtil.makeShowHideBlock("", tmp.toString(),
                false));
        sb.append(contents.toString());

        /*
          sb.append(
          HtmlUtil.makeShowHideBlock(
          msg("Web Service API Documentation"),
          HtmlUtil.insetDiv(contents.toString(), 0, 20, 0, 0), false));
        */


        if (vocabularyList.size() > 0) {
            Appendable pw = sb;
            pw.append(HtmlUtil.p());
            StringBuffer vsb = new StringBuffer();
            for (Vocabulary vocabulary : vocabularyList) {
                StringBuffer vvsb = new StringBuffer();
                vvsb.append("<table>");
                vvsb.append(
                    "<tr><td><b>External Name</b></td><td><b>ID</b></td><td></td><td><b>Internal</b></td></tr>");
                for (IdLabel value : vocabulary.getValues()) {
                    List<String> internals =
                        vocabulary.externalToInternal(value.getId());
                    if (internals != null) {
                        vvsb.append("<tr><td>" + value.getLabel()
                                    + "</td><td>" + value.getId()
                                    + "</td><td>=</td><td>"
                                    + StringUtil.join(",", internals));
                    } else {
                        vvsb.append("<tr><td colspan=4>" + msgLabel("Local")
                                    + value.getLabel() + " (" + value.getId()
                                    + ")</td>");
                    }

                }
                vvsb.append("</table>");
                vsb.append(HtmlUtil.makeShowHideBlock(vocabulary.getId(),
                        HtmlUtil.insetDiv(vvsb.toString(), 0, 20, 0, 0),
                        false));


            }
            pw.append(getHeader(msg("Vocabularies")));
            pw.append(HtmlUtil.makeShowHideBlock("",
                    HtmlUtil.insetDiv(vsb.toString(), 0, 20, 0, 0), false));
        }




        sb.append(HtmlUtil.p());
        if (servers.size() > 0) {
            sb.append(getHeader(msg("Remote repositories")));
            for (GsacRepositoryInfo info : servers) {
                StringBuffer repSB = new StringBuffer();
                showRepositoryInfo(request, repSB, info, false);
                String label =
                    HtmlUtil.href(info.getUrl() + URL_REPOSITORY_VIEW,
                                  info.getName());

                sb.append(HtmlUtil.makeShowHideBlock(label,
                        HtmlUtil.insetDiv(repSB.toString(), 0, 20, 0, 0),
                        false));

                sb.append("<p>");
            }
            sb.append("</ul>");
        } else {
            //            showRepositoryInfo(request, contents, gri, true);
        }

        getHtmlOutputHandler().finishHtml(request, response, sb);


    }

    /**
     *
     *  showRepositoryInfo(----): this is where the Base URL is made for the API Information page
     *
     * @param request the request
     * @param pw _more_
     * @param info _more_
     * @param showList _more_
     *
     * @throws Exception On badness
     */
    private void showRepositoryInfo(GsacRequest request, Appendable pw,
                                    GsacRepositoryInfo info, boolean showList)
      throws Exception {
        int cnt    = 0;

        // to specify the String labeled "Base URL" the API Information web page

        // old form:
        //String baseURL = info.getUrl();   //this originally-coded so-called Base URL is made by Java from system info where built: NOT from the true GSAC URL seen by the remote user in a browser. 
        //System.err.println("GSAC:   showRepositoryInfo() baseURL = "+ baseURL );
        //String[] urls   = { baseURL };
        //String[] labels = { "Base URL" };
        //for (int i = 0; i < urls.length; i++) {
            // append the base URL on the info page:
            //pw.append(HtmlUtil.formEntry(msgLabel(labels[i]), urls[i]));
        //}

        String BaseURL = getServlet().getAbsoluteUrl(getUrlBase());
        System.err.println("GSAC: GsacRepository:showRepositoryInfo() BaseURL = "+ BaseURL+" (added to API Information page here)")  ;

        pw.append(HtmlUtil.formTable());

        pw.append(HtmlUtil.formEntry(msgLabel("Base URL "), BaseURL ));

        pw.append(HtmlUtil.formTableClose());

        StringBuffer sb = new StringBuffer();

        for (CapabilityCollection collection : info.getCollections()) {
            // seems to be Site Query or File Query
            cnt = 0;
            sb  = new StringBuffer();

            // set collection URL 
            String collectionUrl = collection.getUrl();
            // CHANGE   Or replace the line above with the next line:
            //collectionUrl = baseURL; // alternate choice
            //sb.append("<b>" + msgLabel("URL") + "</b> " + collectionUrl );  

            for (Capability capability : collection.getCapabilities()) {
                if (cnt++ == 0) {
                    pw.append(HtmlUtil.p());
                    pw.append(getHeader(msg(collection.getName())));
                    sb.append(
                        "<table width=100% cellspacing=10><tr><td><b>Item</b></td><td><b>API/URL Argument</b></td><td><b>Type</b></td><td></td><td><b>Description or values</b></td></tr>");
                }
                showCapabilityInfo(sb, capability, collectionUrl); 
            }
            if (cnt > 0) {
                sb.append("</table>");
                pw.append(HtmlUtil.makeShowHideBlock("", sb.toString(),
                        false));
            }
        }
    }


    /**
     * _more_
     *
     * @param header _more_
     *
     * @return _more_
     */
    public String getHeader(String header) {
        return HtmlUtil.div(header, HtmlUtil.cssClass("gsac-formheader"));
    }

    /**
     * _more_
     *
     * @param sb _more_
     * @param capability _more_
     * @param url _more_
     *
     * @throws Exception On badness
     */
    private void showCapabilityInfo(Appendable sb, Capability capability,
                                    String url)
            throws Exception {
        String       id      = capability.getId();
        String       desc    = capability.getDescription();
        StringBuffer message = new StringBuffer();
        if (desc != null) {
            message.append(desc);
            message.append(HtmlUtil.br());
        }
        String type = capability.getType();
        if (capability.isEnumeration()) {
            StringBuffer sb2     = new StringBuffer();
            String capabilityUrl = HtmlUtil.url(getUrl(URL_REPOSITORY_VIEW)
                                       + "/capability.csv", new String[] {
                                           ARG_CAPABILITY,
                                           capability.getId() });
            sb2.append(HtmlUtil.href(capabilityUrl,
                                     HtmlUtil.img(iconUrl("/csv.png"),
                                         msg("CSV"))));
            sb2.append(HtmlUtil.space(1));
            if (capability.getAllowMultiples()) {
                sb2.append("Zero or more of:");
            } else {
                sb2.append("Zero or one of:");
            }
            sb2.append(
                "<table border=1 cellspacing=0 cellpadding=2 class=gsac-enumtable>");
            for (IdLabel idLabel : capability.getEnums()) {
                String value = idLabel.getId();
                String label = idLabel.getLabel();
                if (value.equals(label)) {
                    sb2.append(HtmlUtil.row(HtmlUtil.cols(HtmlUtil.href(url
                            + "?" + id + "=" + value, value))));
                } else {
                    sb2.append(HtmlUtil.row(HtmlUtil.cols(HtmlUtil.href(url
                            + "?" + id + "=" + value, value), label)));
                }
            }
            sb2.append("</table>");
            message.append(
                HtmlUtil.makeShowHideBlock(
                    msg("Enumeration values"), sb2.toString(), false));
        } else if (type.equals(Capability.TYPE_NUMBERRANGE)) {
            message.append("Numeric range. One or both of URL arguments.");
            StringBuffer ids = new StringBuffer();
            ids.append(id + ".min<br>");
            ids.append(id + ".max<br>");
            id = ids.toString();
        } else if (type.equals(Capability.TYPE_DATERANGE)) {
            message.append("Date range. One or both of URL arguments.<br>"
                           + HtmlOutputHandler.dateHelp);
            StringBuffer ids = new StringBuffer();
            ids.append(id + ".from<br>");
            ids.append(id + ".to<br>");
            id = ids.toString();
        } else if (type.equals(Capability.TYPE_SPATIAL_BOUNDS)) {
            StringBuffer ids = new StringBuffer();
            ids.append(id + ARG_NORTH_SUFFIX + "<br>");
            ids.append(id + ARG_WEST_SUFFIX + "<br>");
            ids.append(id + ARG_SOUTH_SUFFIX + "<br>");
            ids.append(id + ARG_EAST_SUFFIX + "<br>");
            message.append(
                "Use any of the spatial bounds arguments. Longitudes are in degrees east. e.g.<br>");
            for (String[] tuple : new String[][] {
                { id + ARG_NORTH_SUFFIX + "= 40.0", "  most northerly latitude (valid range -90.0 to 90.0)" },
                { id + ARG_SOUTH_SUFFIX + "=30.0",  "  most southerly latitude (valid range -90.0 to 90.0)" },
                { id + ARG_EAST_SUFFIX + "=100.0",  "  most easterly longitude (-180 to 180)" },
                { id + ARG_WEST_SUFFIX + "=-110.0", "  most westerly longitude (-180 to 180)" }
            }) {
                message.append(HtmlUtil.href(url + "?" + tuple[0], tuple[0]));
                message.append(tuple[1]);
                message.append(HtmlUtil.br());
            }
            id = ids.toString();
        } else if (type.equals(Capability.TYPE_STRING)) {
            message.append(
                ("String search. Use any number of arguments. <br> " + desc
                 != null)
                ? ""
                : HtmlOutputHandler.stringSearchHelp);
        } else if (type.equals(Capability.TYPE_BOOLEAN)) {
            message.append("true<br>false<br>");
        } else {}
        type = HtmlUtil.href(getUrl(URL_HELP) + "/api.html#" + type, type);
        sb.append(HtmlUtil.rowTop(HtmlUtil.cols(capability.getLabel(), id,
                type, capability.getSuffixLabel(), message.toString())));

    }










    /**
     * _more_
     *
     * @param msg _more_
     *
     * @return _more_
     */
    public String msgLabel(String msg) {
        return msg(msg) + ":";
    }



    /**
     * _more_
     *
     * @param request the request
     *
     * @return _more_
     */
    public String getRemoteUrlArgs(GsacRequest request) {
        Hashtable<String, String> newArg     = new Hashtable<String, String>();
        HashSet<String>           exceptArgs = new HashSet<String>();
        exceptArgs.add(ARG_REPOSITORY);
        exceptArgs.add(ARG_GZIP);
        exceptArgs.add(ARG_OUTPUT);

        return request.getUrlArgs(newArg, exceptArgs);
    }


    /**
     * Make the given path into a full url
     *
     *
     * @param request _more_
     * @param path path
     *
     * @return full url
     */
    public String getAbsoluteUrl(GsacRequest request, String path) {
        return servlet.getAbsoluteUrl(request, path);
    }

    /**
     * _more_
     *
     * @param path _more_
     *
     * @return _more_
     */
    public String getAbsoluteUrl(String path) {
        return servlet.getAbsoluteUrl(null, path);
    }

    /**
     * get the local directory we can write things to
     *
     * @return local gsac dir
     */
    public File getGsacDirectory() {
        return gsacDirectory;
    }


    /**
     * Make the icon url. This prepends the url base/icons to the given icon.
     *
     * @param icon icon
     *
     * @return icon url
     */
    public String iconUrl(String icon) {
        return getUrl(URL_HTDOCS_BASE + "/icons" + icon);
    }

    /**
     * _more_
     *
     * @param path _more_
     *
     * @return _more_
     */
    public String htdocsUrl(String path) {
        return getUrl(URL_HTDOCS_BASE + path);
    }


    /**
     * preprend the url base to  the given path
     *
     * @param path path
     *
     * @return full url
     */
    public String getUrl(String path) {
        return getUrlBase() + path;
    }


    /**
     * _more_
     *
     * @param path _more_
     * @param args _more_
     *
     * @return _more_
     */
    public String getUrl(String path, String[] args) {
        return HtmlUtil.url(getUrlBase() + path, args);
    }

    /**
     * _more_
     *
     * @param resourceClass Type of resource
     * @return _more_
     */
    public List<GsacOutput> getOutputs(ResourceClass resourceClass) {
        return getResourceManager(resourceClass).getOutputs();
    }



    /**
     * get the property
     *
     * @param name property name
     *
     * @return property value or null of not found
     */
    public String getProperty(String name) {
        //Always look at the system properties
        String value = System.getProperty(name);
        if (value != null) {
            return value;
        }
        //Next look at the system env
        value = System.getenv(name);
        if (value != null) {
            return value;
        }
        if (cmdLineProperties != null) {
            value = (String) cmdLineProperties.get(name);
            if (value != null) {
                return value;
            }
        }

        return (String) properties.get(name);
    }




    /**
     * get property value or dflt if not found
     *
     * @param name property name
     * @param dflt default value
     *
     * @return get property value or dflt if not found
     */
    public boolean getProperty(String name, boolean dflt) {
        String prop = getProperty(name);
        if (prop != null) {
            return new Boolean(prop).booleanValue();
        }

        return dflt;
    }


    /**
     * get property value or dflt if not found
     *
     * @param name property name
     * @param dflt default value
     *
     * @return property
     */
    public String getProperty(String name, String dflt) {
        String prop = getProperty(name);
        if (prop != null) {
            return prop;
        }

        return dflt;
    }



    /**
     * get property value or dflt if not found
     *
     * @param name property name
     * @param dflt default value
     *
     * @return property
     */
    public int getProperty(String name, int dflt) {
        String prop = getProperty(name);
        if (prop != null) {
            return new Integer(prop).intValue();
        }

        return dflt;
    }


    /**
     * get property value or dflt if not found
     *
     * @param name property name
     * @param dflt default value
     *
     * @return property
     */
    public long getProperty(String name, long dflt) {
        String prop = getProperty(name);
        if (prop != null) {
            return new Long(prop).longValue();
        }

        return dflt;
    }

    /**
     * get property value or dflt if not found
     *
     * @param name property name
     * @param dflt default value
     *
     * @return property
     */
    public double getProperty(String name, double dflt) {
        String prop = getProperty(name);
        if (prop != null) {
            return new Double(prop).doubleValue();
        }

        return dflt;
    }




    /**
     * utility for making an html dialog
     *
     * @param message message
     *
     * @return html
     */
    public String makeInformationDialog(String message) {
        return makeDialog(message, "/information.png", true);
    }

    /**
     * utility for making an html dialog
     *
     * @param message messaeg
     *
     * @return html
     */
    public String makeWarningDialog(String message) {
        return makeDialog(message, "/warning.png", true);
    }

    /**
     * utility for making an html dialog
     *
     * @param message message
     *
     * @return html
     */
    public String makeErrorDialog(String message) {
        return makeDialog(message, "/error.png", true);
    }




    /**
     * utility for making an html dialog
     *
     * @param message message
     * @param icon icon to use
     * @param showClose add a close button
     *
     * @return html
     */
    public String makeDialog(String message, String icon, boolean showClose) {
        String html =
            HtmlUtil.jsLink(HtmlUtil.onMouseClick("hide('messageblock')"),
                            HtmlUtil.img(iconUrl("/close.gif")));
        if ( !showClose) {
            html = "&nbsp;";
        }
        message =
            "<div class=\"gsac-innernote\"><table cellspacing=\"0\" cellpadding=\"0\" border=\"0\"><tr><td valign=\"top\">"
            + HtmlUtil.img(iconUrl(icon)) + HtmlUtil.space(2)
            + "</td><td valign=\"bottom\"><span class=\"gsac-notetext\">"
            + message + "</span></td></tr></table></div>";

        return "\n<table border=\"0\" id=\"messageblock\"><tr><td><div class=\"gsac-note\"><table><tr valign=top><td>"
               + message + "</td><td>" + html + "</td></tr></table>"
               + "</div></td></tr></table>\n";
    }


    /**
     * utility for mapping a phrase in the external translation of it
     *
     * @param msg message
     *
     * @return translated message
     */
    public String msg(String msg) {
        String newMsg = translatePhrase(msg);
        if (newMsg != null) {
            return newMsg;
        }

        return msg;
    }


    /**
     * for debugging
     *
     * @return print out the vocabs
     */
    public boolean shouldPrintVocabularies() {
        return false;
        //        return true;
    }


    /**
     * for development
     *
     * @param gri the repository
     */
    public void printVocabularies(GsacRepositoryInfo gri) {
        if ( !shouldPrintVocabularies()) {
            return;
        }
        //System.err.println("GSAC: printing  vocabularies");
        //        File f = new File("vocabulary");
        File     f = null;
        String[] s = new String[] { "" };

        for (CapabilityCollection collection : gri.getCollections()) {
            for (Capability capability : collection.getCapabilities()) {
                if (capability.isEnumeration()) {
                    printVocabulary(f, capability.getId(),
                                    capability.getEnums());
                }
            }
        }
    }


    /**
     * _more_
     *
     * @param dir _more_
     * @param what _more_
     * @param values _more_
     */
    public void printVocabulary(File dir, String what, List values) {
        //System.err.println("GSAC: printing vocab:" + what);
        try {
            String           tail  = what + ".local.properties";
            File             f     = (dir == null)
                                     ? new File(tail)
                                     : new File(dir + "/" + tail);
            FileOutputStream fos   = new FileOutputStream(f);
            PrintWriter      pw    = new PrintWriter(fos);

            String           tail2 = what + ".map";
            File             f2    = (dir == null)
                                     ? new File(tail2)
                                     : new File(dir + "/" + tail2);
            FileOutputStream fos2  = new FileOutputStream(f2);
            PrintWriter      pw2   = new PrintWriter(fos2);


            pw.append("#\n");
            pw.append("#Generated listing from " + getRepositoryName()
                      + "\n");
            pw.append("#This maps the internal value used by "
                      + getRepositoryName() + " to the core value\n");
            pw.append("#\n");


            pw2.append("#\n");
            pw2.append("#External values for  " + what + "\n");
            pw2.append("#This maps the gsac value to a description\n");
            pw2.append("#\n");

            for (int i = 0; i < values.size(); i++) {
                IdLabel idLabel = (IdLabel) values.get(i);
                String  id      = idLabel.getName();
                id = id.trim().toLowerCase();
                id = id.replaceAll(" ", "_");
                id = id.replaceAll("\\/", "_");
                id = id.replaceAll("-", "_");
                id = id.replaceAll("\\.", "_");
                id = id.replaceAll("\\(", "_");
                id = id.replaceAll("\\)", "_");
                id = id.replaceAll("__", "_");
                id = id.replaceAll("__", "_");
                id = id.replaceAll("__", "_");

                pw2.append("\n#");
                pw2.append(idLabel.getName());
                pw2.append("\n");
                pw2.append(id);
                pw2.append("=");
                pw2.append(idLabel.getId());
                pw2.append("\n");

                pw.append(idLabel.getId());
                pw.append("=");
                pw.append(idLabel.getName());
                pw.append("\n");
            }
            pw.close();
            fos.close();
            pw2.close();
            fos2.close();
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }


    /**
     * _more_
     *
     * @param values _more_
     * @param delimiter _more_
     *
     * @return _more_
     */
    public String join(String[] values, String delimiter) {
        StringBuffer sb = new StringBuffer();
        for (String s : values) {
            sb.append(s);
            sb.append(delimiter);
        }

        return sb.toString();
    }


    /**
     * _more_
     *
     * @param userAgent _more_
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }


    /**
     * Get the UserAgent property. This is sent when this repository makes
     * a request to another repository (e.g. in the federated repository).
     *
     *  @return The UserAgent
     */
    public String getUserAgent() {
        return userAgent;
    }




}
