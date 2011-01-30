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


import org.apache.log4j.Logger;


import org.gsac.gsl.database.*;


import org.gsac.gsl.model.*;
import org.gsac.gsl.output.*;

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

import java.net.URL;
import java.net.URLConnection;

import java.util.ArrayList;
import java.util.Date;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import java.util.zip.*;


/**
 * This provides a default implementation of the GsacRepository interface.
 *
 * At a minimum you need to create a derived class that implements the methods: <pre>
 * handleSiteRequest
 * handleResourceRequest
 * </pre>
 *
 * However, this class can also make use of a GsacDatabaseManager, SiteManager and ResourceManager
 * classes.
 * There are a number of other methods (e.g., getSite, getSiteTypes, etc.) where this
 * class implements a cache for the results (e.g., caching the GsacSite). If it cannot find the
 * requested object in the cache then it ends up calling a doGet... method, e.g., getSite ends up
 * calling doGetSite. Derived classes should overwrite the appropriate doGet methods. At a minumum doGetSite
 * needs to be overwritten.
 *
 * All of the other doGet methods are optional. But if your repository has, for example,
 * site types that you want to list and search on  then overwrite the  doGetSiteTypes method.
 * All of these objects (SiteType, SiteStatus, ResourceType, etc.) all take the form of having an id
 * and a name (or label). The HTML output handlers make search form inputs from these id/labels pairs.
 *
 * @author  Jeff McWhirter mcwhirter@unavco.org
 */
public class GsacRepository implements GsacConstants {

    /** _more_ */
    private Logger LOG;

    /** _more_ */
    private Logger ACCESSLOG;

    /** _more_ */
    private File logDirectory;

    /** cache id */
    private static final String PROP_SITEGROUPS = "prop.sitegroups";

    /** cache id */
    private static final String PROP_SITETYPES = "prop.sitetypes";

    /** cache id */
    private static final String PROP_SITESTATUSES = "prop.sitestatuses";

    /** cache id */
    private static final String PROP_SITECAPABILITIES =
        "prop.sitecapabilities";

    /** cache id */
    private static final String PROP_RESOURCECAPABILITIES =
        "prop.resourcecapabilities";

    /** cache id */
    private static final String PROP_RESOURCETYPES = "prop.resourcetypes";

    /** _more_ */
    private static final String PROP_HOSTNAME = "gsac.server.hostname";

    /** _more_ */
    private static final String PROP_PORT = "gsac.server.port";

    /** _more_ */
    public static final String TAG_REPOSITORY = "repository";

    /** _more_ */
    public static final String TAG_DESCRIPTION = "description";

    /** _more_ */
    public static final String ATTR_NAME = "name";

    /** _more_ */
    public static final String ATTR_URL = "url";



    /** the servlet */
    private GsacServlet servlet;

    /** the database manager_ */
    private GsacDatabaseManager databaseManager;

    /** the site manager */
    private SiteManager siteManager;

    /** the resource manager */
    private ResourceManager resourceManager;


    /** site cache */
    private TTLCache<Object, GsacSite> siteCache =
        new TTLCache<Object, GsacSite>(TTLCache.MS_IN_A_DAY);

    /** caches site group and types, etc. */
    private TTLCache<String, Object> cache =
        new TTLCache<String, Object>(TTLCache.MS_IN_AN_HOUR * 6);

    /** html header. Initialize in initServlet */
    private String htmlHeader = "<html><body>";

    /** html footer. Initialize in initServlet */
    private String htmlFooter = "</body></html>";

    /** holds phrase translations */
    private Properties msgProperties = new Properties();

    /** general properties */
    private Properties properties = new Properties();


    /** Make a cached list of servers. Cache for 6 hours */
    private TTLObject<List<GsacRepositoryInfo>> servers =
        new TTLObject<List<GsacRepositoryInfo>>(TTLCache.MS_IN_AN_HOUR * 6);

    /** _more_ */
    private String urlBase;

    /** _more_ */
    private GsacRepositoryInfo myInfo;

    /** _more_ */
    private Hashtable<String, Vocabulary> vocabularies =
        new Hashtable<String, Vocabulary>();

    /** _more_ */
    private File gsacDirectory;


    /**
     * noop constructor
     */
    public GsacRepository() {}


    /**
     * Constructor
     *
     * @param servlet the servlet
     */
    public GsacRepository(GsacServlet servlet) {
        this.servlet = servlet;
    }

    /**
     * _more_
     *
     * @param vocabularyId _more_
     * @param value _more_
     *
     * @return _more_
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
     * _more_
     *
     * @param vocabularyId _more_
     * @param value _more_
     *
     * @return _more_
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
     * _more_
     *
     * @param key _more_
     * @param incoming _more_
     *
     * @return _more_
     */
    public List<String> convertToInternal(String key, List<String> incoming) {
        return convertToInternal(getVocabulary(key), key, incoming);
    }





    /**
     * _more_
     *
     * @param vocabulary _more_
     * @param key _more_
     * @param incoming _more_
     *
     * @return _more_
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
     * _more_
     *
     * @return _more_
     */
    public File getGsacDirectory() {
        return gsacDirectory;
    }

    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    public Vocabulary getVocabulary(String id) {
        return getVocabulary(id, false);
    }

    /**
     * _more_
     *
     * @param id _more_
     * @param createIfNeeded _more_
     *
     * @return _more_
     */
    public Vocabulary getVocabulary(String id, boolean createIfNeeded) {
        Vocabulary vocabulary = vocabularies.get(id);
        if ((vocabulary == null) && createIfNeeded) {
            vocabulary = getVocabularyFromType(id);
        }
        return vocabulary;
    }

    /**
     * _more_
     *
     * @param vocabulary _more_
     */
    public void addVocabulary(Vocabulary vocabulary) {
        vocabularies.put(vocabulary.getId(), vocabulary);
    }


    /**
     * _more_
     *
     * @param object _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String encodeObject(Object object) throws Exception {
        return XmlEncoder.encodeObject(object);
    }


    /**
     * _more_
     *
     * @param xml _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Object decodeObject(String xml) throws Exception {
        return XmlEncoder.decodeXml(xml);
    }


    /**
     * Initialize the servlet
     *
     * @param servlet the servlet
     *
     * @throws Exception _more_
     */
    public void initServlet(GsacServlet servlet) throws Exception {
        this.servlet = servlet;
        init();
    }

    /**
     * _more_
     *
     * @throws Exception _more_
     */
    public void init() throws Exception {

        LogUtil.setTestMode(true);
        InputStream inputStream;
        //load property files first
        String[] propertyFiles = { "/org/gsac/gsl/resources/gsac.properties",
                                   getLocalResourcePath("/gsac.properties"),
                                   getLocalResourcePath(
                                       "/gsacserver.properties") };
        for (String file : propertyFiles) {
            inputStream = getResourceInputStream(file);
            if (inputStream != null) {
                properties.load(inputStream);
            }
        }

        inputStream =
            getResourceInputStream(getLocalResourcePath("/header.html"));
        if (inputStream != null) {
            htmlHeader = IOUtil.readContents(inputStream);
            htmlHeader = htmlHeader.replace("${urlroot}",
                                            getUrlBase() + URL_BASE);
        }

        inputStream =
            getResourceInputStream(getLocalResourcePath("/footer.html"));
        if (inputStream != null) {
            htmlFooter = IOUtil.readContents(inputStream);
            htmlFooter = htmlFooter.replace("${urlroot}",
                                            getUrlBase() + URL_BASE);
        }

        String[] files = { "/org/gsac/gsl/resources/phrases.properties",
                           getLocalResourcePath("/phrases.properties") };
        for (String file : files) {
            inputStream = getResourceInputStream(file);
            if (inputStream != null) {
                msgProperties.load(inputStream);
            }
        }

        //Now look around the tomcat environment
        //        System.err.println("System.properties:" + System.getProperties());
        //        System.err.println("System.env:" + System.getenv());
        String catalinaBase = null;
        for (String arg : new String[] { "CATALINA_BASE", "catalina.base",
                                         "CATALINA_HOME", "catalina.home" }) {
            catalinaBase = getProperty(arg);
            if (catalinaBase != null) {
                break;
            }
        }
        System.err.println("GSAC: catalina base:" + catalinaBase);
        if (catalinaBase != null) {
            File catalinaConfFile = new File(catalinaBase
                                             + "/conf/gsac.properties");
            System.err.println("GSAC: looking for:" + catalinaConfFile);
            if (catalinaConfFile.exists()) {
                System.err.println("GSAC: loading " + catalinaConfFile);
                properties.load(new FileInputStream(catalinaConfFile));
            }
        }

        String dir = getProperty(PROP_GSACDIRECTORY, (String) null);
        if (dir != null) {
            gsacDirectory = new File(dir);
            System.err.println("GSAC: gsacDirectory from properties file: "
                               + gsacDirectory);
        } else {
            String userHome = System.getProperty("user.home");
            System.err.println(
                "GSAC: attempt to set gsacDirectory from user.home system property: "
                + userHome);
            if (userHome != null) {
                File localDir = new File(userHome + "/.gsac");
                if (localDir.exists()) {
                    gsacDirectory = localDir;
                    System.err.println(
                        "GSAC: gsacDirectory from userHome/.gsac: "
                        + gsacDirectory);
                } else {
                    System.err.println(
                        "GSAC: userHome/.gsac directory does not exist: "
                        + userHome + "/.gsac; no gsacDirectory set");
                }
            } else {
                System.err.println(
                    "GSAC: user.home system property is null, no gsacDirectory set");
            }
        }


        System.err.println("GSAC: using gsacDirectory: " + gsacDirectory);
        if (gsacDirectory != null) {

            initLogDir(gsacDirectory);
            File localPropertiesFile = new File(gsacDirectory
                                           + "/gsac.properties");
            System.err.println("GSAC: looking for: " + localPropertiesFile);
            if (localPropertiesFile.exists()) {
                System.err.println("GSAC: loading " + localPropertiesFile);
                properties.load(new FileInputStream(localPropertiesFile));
            }
        }



        getSiteQueryCapabilities();
        getResourceQueryCapabilities();

        getRepositoryInfo();

    }


    /**
     * _more_
     *
     * @param path _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
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
     * _more_
     *
     * @param gsacDir _more_
     */
    public void initLogDir(File gsacDir) {
        logDirectory = new File(gsacDirectory.toString() + "/logs");
        if ( !logDirectory.exists()) {
            logDirectory.mkdir();
            System.err.println("Logging to:" + logDirectory);
        }
        File   log4JFile = new File(logDirectory + "/" + "log4j.properties");
        String contents  = readResource("/log4j.properties");
        if (true || !log4JFile.exists()) {
            try {
                contents = contents.replace("${gsac.logdir}",
                                            logDirectory.toString());
                IOUtil.writeFile(log4JFile, contents);
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            }
        }
        org.apache.log4j.PropertyConfigurator.configure(log4JFile.toString());

    }


    /**
     * _more_
     *
     * @param fileName _more_
     *
     * @return _more_
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
     * _more_
     *
     * @param request _more_
     * @param response _more_
     * @param sb _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public boolean checkRequest(GsacRequest request, GsacResponse response,
                                Appendable sb)
            throws Exception {
        return true;
    }


    /**
     * _more_
     *
     * @return _more_
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
                    logError("Initializing remote repository:" + info, null);
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
                        group = capability.getRepositories().get(0).getName();
                    } else {
                        group = "Remote Repositories";
                    }
                    if (group != null) {
                        if (capability.hasGroup()) {
                            capability.setGroup(group + ": "
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
     * _more_
     *
     * @param info _more_
     * @param usedSiteCapabilities _more_
     * @param usedResourceCapabilities _more_
     * @param collectionToUsedCapabilities _more_
     *
     * @throws Exception _more_
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
        /*
        //TODO:
        List<Capability> siteCapabilities;
        List<Capability> resourceCapabilities;
        info.setSiteCapabilities(siteCapabilities =
            Capability.mergeCapabilities(gri.getSiteCapabilities(),
                                         usedSiteCapabilities));
        info.setResourceCapabilities(resourceCapabilities =
            Capability.mergeCapabilities(gri.getResourceCapabilities(),
                                         usedResourceCapabilities));

        for (Capability capability : siteCapabilities) {
            capability.addRepository(info);
        }

        for (Capability capability : resourceCapabilities) {
            capability.addRepository(info);
        }
        */

    }


    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
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
     * _more_
     *
     * @param url _more_
     *
     * @return _more_
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
     * _more_
     *
     * @param repositoryUrl _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public GsacRepositoryInfo retrieveRepositoryInfo(String repositoryUrl)
            throws Exception {
        GsacRepositoryInfo gri =
            (GsacRepositoryInfo) getRemoteObject(repositoryUrl,
                URL_REPOSITORY_VIEW, "", OUTPUT_GSACXML);

        return gri;
    }


    /**
     * _more_
     *
     * @param servers _more_
     */
    public void doMakeServerInfoList(List<GsacRepositoryInfo> servers) {}





    /**
     * _more_
     *
     * @return _more_
     */
    public String getUrlBase() {
        if (urlBase == null) {
            urlBase = getProperty(PROP_BASEURL, "");
        }
        return urlBase;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getRepositoryName() {
        return getProperty(PROP_REPOSITORY_NAME, "GSAC Repository");
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getRepositoryDescription() {
        return getProperty(PROP_REPOSITORY_DESCRIPTION, "");
    }

    /**
     * _more_
     *
     * @return _more_
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
     * _more_
     *
     * @return _more_
     */
    public String getPackagePath() {
        String packageName = getClass().getPackage().getName();
        packageName = "/" + packageName.replace(".", "/");
        return packageName;
    }


    /**
     * _more_
     *
     * @param fileTail _more_
     *
     * @return _more_
     */
    public String getCoreResourcePath(String fileTail) {
        return "/org/gsac/gsl/resources" + fileTail;
    }

    /**
     * _more_
     *
     * @param fileTail _more_
     *
     * @return _more_
     */
    public String getLocalHtdocsPath(String fileTail) {
        return getPackagePath() + "/htdocs/" + fileTail;
    }

    /**
     * _more_
     *
     * @param sb _more_
     */
    public void addStats(StringBuffer sb) {
        if (databaseManager != null) {
            sb.append(HtmlUtil.formEntry("DB pool:",
                                         databaseManager.getPoolStats()));
        }
    }


    /**
     * Get the databasemanager. If not created yet then this calls the factory method doMakeDatabaseManager
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
     * Get the site manager. If not created yet this method calls the factory
     * method doMakeSiteManager
     *
     * @return The site manager
     */
    public SiteManager getSiteManager() {
        if (siteManager == null) {
            siteManager = doMakeSiteManager();
        }
        return siteManager;
    }


    /**
     * Factory method to create the site manager. Derived classes should
     * overwrite this method if they want to make use of the site manager facility
     *
     * @return the site manager
     */
    public SiteManager doMakeSiteManager() {
        return new SiteManager(this) {
            public GsacSite getSite(String siteId) throws Exception {
                return null;
            }
        };
    }


    /**
     * Get the resource manager. If not created yet this method calls the factory
     * method doMakeResourceManager
     *
     * @return The resource manager
     */
    public ResourceManager getResourceManager() {
        if (resourceManager == null) {
            resourceManager = doMakeResourceManager();
        }
        return resourceManager;
    }

    /**
     * Factory method to create the resource manager. Derived classes should
     * overwrite this method if they want to make use of the resource manager facility
     *
     * @return the resource manager
     */
    public ResourceManager doMakeResourceManager() {
        return null;
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
     * _more_
     *
     * @param arg _more_
     *
     * @return _more_
     */
    public boolean isCapable(String arg) {
        String  key    = "capability." + arg;
        boolean result = getProperty(key, false);
        return result;
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
        return getProperty(PROP_PORT, 8080);
    }


    /**
     * gets called for resource requests
     *
     * @param request The request
     * @param response response
     *
     * @throws Exception on badnesss
     */
    public void handleResourceRequest(GsacRequest request,
                                      GsacResponse response)
            throws Exception {
        if (getResourceManager() != null) {
            getResourceManager().handleResourceRequest(request, response);
            return;
        }
        notImplemented(
            "Derived class needs to implement handleResourceRequest");
    }


    /**
     * Handle the site search request
     *
     * @param request The request
     * @param response The response
     *
     * @throws Exception on badnesss
     */
    public void handleSiteRequest(GsacRequest request, GsacResponse response)
            throws Exception {
        if (getSiteManager() != null) {
            getSiteManager().handleSiteRequest(request, response);
            return;
        }
        notImplemented("Derived class needs to implement handleSiteRequest");
    }

    /**
     * This method will first look in the local siteCache for the site.
     * If not found it calls doGetSite which should be overwritten by derived classes
     *
     * @param request The request
     * @param siteId site id
     *
     * @return The site or null if not found
     *
     * @throws Exception On badness
     */
    public GsacSite getSiteFromCache(String siteId) {
        return siteCache.get(siteId);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param siteId _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public GsacSite getSite(GsacRequest request, String siteId)
            throws Exception {
        GsacSite site = getSiteFromCache(siteId);
        if (site != null) {
            return site;
        }
        site = doGetSite(siteId);
        //Cache the dummy site
        if ((site == null) && shouldCacheSites()) {
            cacheSite(new GsacSite(siteId, "", "", 0, 0, 0));
        }

        if ((site != null) && shouldCacheSites()) {
            cacheSite(site);
        }
        return site;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean shouldPrintVocabularies() {
        return false;
        //        return true;
    }


    /**
     * _more_
     *
     * @param gri _more_
     */
    public void printVocabularies(GsacRepositoryInfo gri) {
        if ( !shouldPrintVocabularies()) {
            return;
        }
        System.err.println("printing  vocabularies");
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
        System.err.println("    printing vocab:" + what);
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
     * @param path _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
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
                System.err.println("Failed to read vocabulary for:" + path);
                return "";
            }
            return IOUtil.readContents(is);
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }






    /**
     * _more_
     *
     * @param type _more_
     *
     * @return _more_
     */
    public Vocabulary getVocabularyFromType(String type) {

        Hashtable<String, List<String>> externalToInternal =
            new Hashtable<String, List<String>>();
        Hashtable<String, String> internalToExternal = new Hashtable<String,
                                                           String>();
        List<IdLabel>   vocabulary  = new ArrayList<IdLabel>();
        HashSet<String> coreMap     = new HashSet<String>();
        HashSet<String> internalMap = new HashSet<String>();

        String[] vocabularyContents = { getExternalVocabulary(type),
                                        getInternalVocabulary(type) };

        for (int i = 0; i < vocabularyContents.length; i++) {
            for (List<String> toks :
                    tokenizeVocabulary(vocabularyContents[i])) {
                String value = toks.get(0);
                String label = ((toks.size() == 2)
                                ? toks.get(1)
                                : "");
                vocabulary.add(new IdLabel(value, label));
                if (i == 0) {
                    coreMap.add(value);
                } else {
                    internalMap.add(value);
                    //                    System.err.println("adding internal vocab: " + value);
                }
            }
        }

        List<IdLabel> values     = new ArrayList<IdLabel>();
        boolean       hasMapping = false;
        for (List<String> toks :
                tokenizeVocabulary(getInternalVocabularyMap(type))) {
            hasMapping = true;
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
        hasMapping = true;


        //Now prune out from the vocab list anything that isn't used by the internal repository
        //We do 2 passes here
        //First we add all the wildcards plus any non-wildcard that should be included
        //Next we go through the wildcards and only include those that have a non-wildcard match
        List<IdLabel> valuesWithoutWildcards = new ArrayList<IdLabel>();
        List<IdLabel> valuesWithBoth         = new ArrayList<IdLabel>();
        for (IdLabel value : vocabulary) {
            boolean isWildcard = Vocabulary.isWildcard(value.getId());
            if (internalMap.contains(value.getId()) || isWildcard
                    || !hasMapping) {
                valuesWithBoth.add(value);
                if ( !isWildcard) {
                    valuesWithoutWildcards.add(value);
                }
            } else {
                //                System.err.println("Skipping: " + value);
            }
        }

        for (IdLabel value : valuesWithBoth) {
            boolean isWildcard = Vocabulary.isWildcard(value.getId());
            if ( !isWildcard || !hasMapping) {
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
        vocabularies.put(type, vocab);
        return vocab;

    }


    /**
     * _more_
     *
     * @param path _more_
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
     * _more_
     *
     * @return _more_
     */
    public GsacRepositoryInfo getRepositoryInfo() {
        if (myInfo == null) {
            GsacRepositoryInfo gri =
                new GsacRepositoryInfo(
                    getServlet().getAbsoluteUrl(getUrlBase()),
                    getRepositoryName());

            gri.setDescription(getRepositoryDescription());
            gri.addCollection(new CapabilityCollection("site", "Site Query",
                    getServlet().getAbsoluteUrl(getUrlBase()
                        + URL_SITE_SEARCH), getSiteQueryCapabilities()));
            gri.addCollection(new CapabilityCollection("resource",
                    "Resource Query",
                    getServlet().getAbsoluteUrl(getUrlBase()
                        + URL_RESOURCE_SEARCH), getResourceQueryCapabilities()));
            myInfo = gri;
            printVocabularies(gri);
        }
        return myInfo;
    }


    /**
     * This should be overwritten by derived classes to create the site
     *
     * @param siteId site id
     *
     * @return site
     *
     * @throws Exception on badnesss
     */
    public GsacSite doGetSite(String siteId) throws Exception {
        if (getSiteManager() != null) {
            return getSiteManager().getSite(siteId);
        }
        notImplemented("Derived class needs to implement doGetSite");
        return null;
    }

    /**
     * This gets called by OutputHandlers when they need all of the metadata for a site
     * If the given site object has all of its metadata already then this method just returns.
     * Else the method doGetFillSiteMetadata is called. A repository implementation can overwrite
     * this method to add the full metadata to the site
     *
     *
     * @param request _more_
     * @param gsacSite  The site
     *
     * @throws Exception On badness
     */
    public void getSiteMetadata(GsacRequest request, GsacSite gsacSite)
            throws Exception {
        int level = request.get(ARG_METADATA_LEVEL, 1);
        if (gsacSite.getMetadataLevel() >= level) {
            return;
        }
        doGetFullSiteMetadata(level, gsacSite);
        gsacSite.setMetadataLevel(level);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param gsacResource _more_
     *
     * @throws Exception _more_
     */
    public void getResourceMetadata(GsacRequest request,
                                    GsacResource gsacResource)
            throws Exception {
        int level = request.get(ARG_METADATA_LEVEL, 1);
        if (gsacResource.getMetadataLevel() >= level) {
            return;
        }
        doGetFullResourceMetadata(level, gsacResource);
        gsacResource.setMetadataLevel(level);
    }



    /**
     * Gets called to add the full metadata to the site.
     * If there is a sitemanager then this is just a pass through
     * to it.
     *
     *
     * @param level _more_
     * @param gsacSite  The site
     *
     * @throws Exception On badness
     */
    public void doGetFullSiteMetadata(int level, GsacSite gsacSite)
            throws Exception {
        if (getSiteManager() != null) {
            getSiteManager().doGetSiteMetadata(level, gsacSite);
        }
    }


    /**
     * _more_
     *
     *
     * @param level _more_
     * @param gsacResource _more_
     *
     * @throws Exception _more_
     */
    public void doGetFullResourceMetadata(int level,
                                          GsacResource gsacResource)
            throws Exception {
        if (getResourceManager() != null) {
            getResourceManager().doGetResourceMetadata(level, gsacResource);
        }
    }

    /**
     * Put the given site into the siteCache
     *
     * @param site the site to cache
     */
    public void cacheSite(GsacSite site) {
        cacheSite(site.getSiteId(), site);
    }

    /**
     * Put the given site into the siteCache with the given cache key
     *
     * @param key Key to cache with
     * @param site the site to cache
     */
    public void cacheSite(String key, GsacSite site) {
        siteCache.put(key, site);
    }

    /**
     * _more_
     *
     * @param key _more_
     *
     * @return _more_
     */
    public GsacSite getCachedSite(String key) {
        return siteCache.get(key);
    }

    /**
     * Clear the site cache
     */
    public void clearCache() {
        siteCache = new TTLCache<Object, GsacSite>(TTLCache.MS_IN_A_DAY);
        cache     = new TTLCache<String, Object>(TTLCache.MS_IN_AN_HOUR * 6);
    }

    /**
     * Are sites cachable
     *
     * @return default true
     */
    public boolean shouldCacheSites() {
        return true;
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
     * _more_
     *
     * @param request _more_
     * @param resourceId _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public GsacResource getResource(GsacRequest request, String resourceId)
            throws Exception {
        if (getResourceManager() != null) {
            return getResourceManager().getResource(resourceId);
        }
        notImplemented("Derived class needs to implement doGetResource");
        return null;
    }


    /**
     * Get the list of available site groups. This keeps the list around in a local cache.
     * If it cannot find the list then it calls doGetSiteGroups
     *
     * @return Site groups
     */
    public List<SiteGroup> getSiteGroups() {
        List<SiteGroup> siteGroups =
            (List<SiteGroup>) cache.get(PROP_SITEGROUPS);
        if (siteGroups == null) {
            siteGroups = doGetSiteGroups();
            cache.put(PROP_SITEGROUPS, siteGroups);
        }
        return siteGroups;
    }

    /**
     * Get the list of site groups. Derived classes should overwrite this method.
     *
     * @return list of site groups
     */
    public List<SiteGroup> doGetSiteGroups() {
        if (getSiteManager() != null) {
            return getSiteManager().doGetSiteGroups();
        }
        return new ArrayList<SiteGroup>();
    }


    /**
     * Get list of site types. Look in cache first. If not there then call doGetSiteTypes
     *
     * @return list of site types
     */
    public List<SiteType> getSiteTypes() {
        List<SiteType> siteTypes = (List<SiteType>) cache.get(PROP_SITETYPES);
        if (siteTypes == null) {
            siteTypes = doGetSiteTypes();
            cache.put(PROP_SITETYPES, siteTypes);
        }
        return siteTypes;
    }



    /**
     * Get the list of site types. Derived classes should overwrite this method.
     *
     * @return list of site types
     */
    public List<SiteType> doGetSiteTypes() {
        if (getSiteManager() != null) {
            return getSiteManager().doGetSiteTypes();
        }
        return new ArrayList<SiteType>();
    }



    /**
     * Get list of site statuses. Look in cache first. If not there then call doGetSiteStatuses
     *
     * @return list of site statuses
     */
    public List<SiteStatus> getSiteStatuses() {
        List<SiteStatus> siteStatuses =
            (List<SiteStatus>) cache.get(PROP_SITESTATUSES);
        if (siteStatuses == null) {
            siteStatuses = doGetSiteStatuses();
            cache.put(PROP_SITESTATUSES, siteStatuses);
        }
        return siteStatuses;
    }

    /**
     * Get the list of site statuses. Derived classes should overwrite this method.
     *
     * @return list of site statuses
     */
    public List<SiteStatus> doGetSiteStatuses() {
        if (getSiteManager() != null) {
            return getSiteManager().doGetSiteStatuses();
        }
        return new ArrayList<SiteStatus>();
    }


    /**
     * Get the list of resource types
     *
     * @return resource types
     */
    public List<ResourceType> getResourceTypes() {
        List<ResourceType> resourceTypes =
            (List<ResourceType>) cache.get(PROP_RESOURCETYPES);
        if (resourceTypes == null) {
            resourceTypes = doGetResourceTypes();
            cache.put(PROP_RESOURCETYPES, resourceTypes);
        }
        return resourceTypes;
    }


    /**
     * Get the list of resource types. Derived classes should overwrite this method.
     *
     * @return list of resource types
     */
    public List<ResourceType> doGetResourceTypes() {
        if (getResourceManager() != null) {
            return getResourceManager().doGetResourceTypes();
        }
        return new ArrayList<ResourceType>();
    }


    /**
     * get list of extra capabilities for searching on sites. This looks in the cache.
     * If not found calls doGetSiteQueryCapabilities
     *
     * @return site query capabilities
     */
    public List<Capability> getSiteQueryCapabilities() {
        List<Capability> caps =
            (List<Capability>) cache.get(PROP_SITECAPABILITIES);
        if (caps == null) {
            caps = doGetSiteQueryCapabilities();
            cache.put(PROP_SITECAPABILITIES, caps);
        }
        return caps;
    }


    /**
     * Derived classes should overwrite this to provide site query capabilities if any
     *
     * @return capabilities
     */
    public List<Capability> doGetSiteQueryCapabilities() {
        if (getSiteManager() != null) {
            return getSiteManager().doGetSiteQueryCapabilities();
        }
        return new ArrayList<Capability>();
    }



    /**
     * get list of extra capabilities for searching on resources. This looks in the cache.
     * If not found calls doGetResourceQueryCapabilities
     *
     * @return resource query capabilities
     */
    public List<Capability> getResourceQueryCapabilities() {
        List<Capability> caps =
            (List<Capability>) cache.get(PROP_RESOURCECAPABILITIES);
        if (caps == null) {
            caps = doGetResourceQueryCapabilities();
            cache.put(PROP_RESOURCECAPABILITIES, caps);
        }
        return caps;
    }


    /**
     * Derived classes should overwrite this to provide resource query capabilities
     *
     * @return capabilities
     */
    public List<Capability> doGetResourceQueryCapabilities() {
        if (getResourceManager() != null) {
            return getResourceManager().doGetResourceQueryCapabilities();
        }
        return new ArrayList<Capability>();
    }


    /**
     * add extra form elements
     *
     * @param request request
     * @param buffer buffer to append to
     */
    public void addToSiteSearchForm(GsacRequest request, Appendable buffer) {
        //e.g.:
        //        buffer.append(HtmlUtil.formEntry("City:",
        //                       HtmlUtil.input(ARG_CITY,
        //                       request.get(ARG_CITY, (String) null)));
    }




    /**
     * Override this to return the html header to use for html pages
     *
     * @param request The request
     *
     * @return html header
     */
    public String getHtmlHeader(GsacRequest request) {
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
     * _more_
     *
     * @return _more_
     */
    private Logger getErrorLogger() {
        if (LOG == null) {
            LOG = Logger.getLogger("org.gsac.gsl");
        }
        return LOG;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    private Logger getAccessLogger() {
        if (ACCESSLOG == null) {
            ACCESSLOG = Logger.getLogger("org.gsac.gsl.access");
        }
        return ACCESSLOG;
    }



    /**
     * Log the error
     *
     * @param message message
     * @param exc exception
     */
    public void logError(String message, Throwable exc) {
        if (logDirectory != null) {
            if (exc != null) {
                getErrorLogger().error(message + "\n<stack>\n" + exc + "\n"
                                       + LogUtil.getStackTrace(exc)
                                       + "\n</stack>");
            } else {
                getErrorLogger().error(message);
            }
        } else {
            System.err.println("ERROR: " + getDTTM() + ": " + message);
            if (exc != null) {
                System.err.println("<stack>");
                exc.printStackTrace();
                System.err.println("</stack>");
            }
        }
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getDTTM() {
        return new Date().toString();
    }

    /**
     * Log the info
     *
     * @param message message
     */
    public void logInfo(String message) {
        if (logDirectory != null) {
            getErrorLogger().info(message);
        } else {
            System.err.println("INFO: " + getDTTM() + ": " + message);
        }
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param what _more_
     */
    public void logAccess(GsacRequest request, String what) {
        String ip     = request.getOriginatingIP();
        String uri    = request.getRequestURI();
        String method = request.getMethod();
        int response = 200;  // always set to this in GsacResponse.startResponse()
        String message = ip + " " + uri + " " + method + " " + response;
        if (logDirectory != null) {
            getAccessLogger().info(message);
        } else {
            System.err.println("[ACCESS] " + getDTTM() + ": " + message);
        }
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
     * @param site _more_
     *
     * @return _more_
     */
    public String getRemoteSiteUrl(GsacSite site) {
        return "";
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
     * @param site _more_
     *
     * @param object _more_
     *
     * @return _more_
     */
    public String getRemoteHref(GsacObject object) {
        GsacRepositoryInfo info = object.getRepositoryInfo();
        if (info == null) {
            return "";
        }
        String icon = info.getIcon();
        if (icon == null) {
            icon = getServlet().iconUrl("/favicon.ico");
        }
        return HtmlUtil.href(getRemoteUrl(object),
                             HtmlUtil.img(icon, "View at " + info.getName()));
    }


    /**
     * _more_
     *
     * @param site _more_
     *
     * @param object _more_
     *
     * @return _more_
     */
    public String getRemoteUrl(GsacObject object) {
        if (object.getRepositoryInfo() == null) {
            return null;
        }
        List<String> pair = StringUtil.splitUpTo(object.getId(), ":", 2);
        String       id   = pair.get(1);
        return object.getRepositoryInfo().getUrl() + object.getViewUrl()
               + "?" + HtmlUtil.args(new String[] { object.getIdArg(),
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
     * @throws Exception _more_
     */
    public Object getRemoteObject(GsacRepositoryInfo info, String urlPath,
                                  String urlArgs, String output)
            throws Exception {
        try {
            return getRemoteObject(info.getUrl(), urlPath, urlArgs, output);
        } catch (Exception exc) {
            remoteRepositoryHadError(info);
            throw exc;
        }
    }




    /**
     * _more_
     *
     * @param repositoryUrl _more_
     * @param urlPath _more_
     * @param urlArgs _more_
     * @param output _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Object getRemoteObject(String repositoryUrl, String urlPath,
                                  String urlArgs, String output)
            throws Exception {
        boolean     zipit          = false;
        GsacServlet servlet        = getServlet();
        String      thisRepository = "client";
        if (servlet != null) {
            thisRepository = servlet.getAbsoluteUrl(getUrlBase());
        }
        String url = repositoryUrl + urlPath + "?" + urlArgs + "&"
                     + HtmlUtil.args(new String[] {
            ARG_REMOTEREPOSITORY, thisRepository, ARG_GZIP, zipit + "",
            ARG_OUTPUT, output
        });
        URLConnection connection  = new URL(url).openConnection();
        InputStream   inputStream = connection.getInputStream();
        if (zipit) {
            inputStream = new GZIPInputStream(inputStream);
        }
        return decodeObject(IOUtil.readContents(inputStream));
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param response _more_
     *
     * @throws Exception _more_
     */
    public void handleCapabilitiesRequest(GsacRequest request,
                                          GsacResponse response)
            throws Exception {
        response.startResponse(GsacResponse.MIME_XML);
        PrintWriter pw = response.getPrintWriter();

        if (request.get(ARG_OUTPUT, "").equals(OUTPUT_GSACXML)) {
            GsacRepositoryInfo gri = getRepositoryInfo();
            String             xml = encodeObject(gri);
            pw.print(xml);
            response.endResponse();
            return;
        }


        Document doc  = XmlUtil.makeDocument();
        Element  root = XmlUtil.create(doc, TAG_CAPABILITIES);
        for (int i = 0; i < 2; i++) {
            String           tag;
            List<Capability> capabilities;
            if (i == 0) {
                capabilities = getSiteQueryCapabilities();
                tag          = TAG_SITECAPABILITY;
            } else {
                capabilities = getResourceQueryCapabilities();
                tag          = TAG_RESOURCECAPABILITY;
            }

            for (Capability capability : capabilities) {
                Element node = XmlUtil.create(doc, tag, root, new String[] {
                    ATTR_TYPE, capability.getType(), ATTR_ID,
                    capability.getId(), ATTR_LABEL, capability.getLabel()
                });
                if (capability.isEnumeration()) {
                    for (IdLabel idLabel : capability.getEnums()) {
                        XmlUtil.create(doc, TAG_ENUMERATION, node,
                                       new String[] { ATTR_ID,
                                idLabel.getId(), ATTR_LABEL,
                                idLabel.getLabel() });
                    }
                }
            }
        }
        XmlUtil.toString(root, pw);
        response.endResponse();
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param response _more_
     *
     * @throws Exception _more_
     */
    public void handleViewRequestXml(GsacRequest request,
                                     GsacResponse response)
            throws Exception {
        response.startResponse(GsacResponse.MIME_XML);
        PrintWriter        pw  = response.getPrintWriter();
        GsacRepositoryInfo gri = getRepositoryInfo();

        pw.append(
            XmlUtil.openTag(
                TAG_REPOSITORY,
                XmlUtil.attrs(
                    ATTR_URL,
                    getServlet().getAbsoluteUrl(getUrlBase() + URL_BASE),
                    ATTR_NAME, gri.getName())));


        pw.append(XmlUtil.tag(TAG_DESCRIPTION, "",
                              XmlUtil.getCdata(gri.getDescription())));

        for (CapabilityCollection collection : gri.getCollections()) {
            collection.toXml(pw);
        }

        pw.append(XmlUtil.closeTag(TAG_REPOSITORY));
        response.endResponse();
    }


    /**
     *
     *
     * _more_
     *
     * @param request _more_
     * @param pw _more_
     * @param response _more_
     *
     * @throws Exception _more_
     */
    public void handleViewRequest(GsacRequest request, GsacResponse response)
            throws Exception {
        if (request.get(ARG_OUTPUT, "").equals(OUTPUT_XML)) {
            handleViewRequestXml(request, response);
            return;
        }

        if (request.get(ARG_OUTPUT, "").equals(OUTPUT_GSACXML)) {
            handleCapabilitiesRequest(request, response);
            return;
        }
        List<GsacRepositoryInfo> servers = getServers();
        GsacRepositoryInfo       gri     = getRepositoryInfo();
        StringBuffer             sb      = new StringBuffer();

        getServlet().getHtmlOutputHandler().initHtml(request, response, sb);
        sb.append(gri.getName());
        sb.append(HtmlUtil.br());
        sb.append(gri.getDescription());
        sb.append(HtmlUtil.p());
        sb.append(HtmlUtil.href(getServlet().getUrl(URL_REPOSITORY_VIEW)
                                + "?" + ARG_OUTPUT
                                + "=xml", msg("Repository information xml")));

        sb.append("<p>\n");

        StringBuffer contents = new StringBuffer();
        if (servers.size() > 0) {
            sb.append("Remote repositories:<ul>");
            for (GsacRepositoryInfo info : servers) {
                sb.append("<li>");
                sb.append(HtmlUtil.href(info.getUrl() + URL_SITE_FORM,
                                        info.getName()));
                showRepositoryInfo(request, sb, info, false);
            }
            sb.append("</ul>");
        } else {
            showRepositoryInfo(request, contents, gri, true);
        }

        StringBuffer tmp = new StringBuffer();
        contents.append(HtmlUtil.p());
        contents.append(getHeader(msg("Misc. Arguments")));
        tmp = new StringBuffer();

        String[] args = { ARG_LIMIT, ARG_OFFSET, ARG_GZIP };
        String[] descs = { "Number of returned sites or resources, e.g., "
                           + ARG_LIMIT + "=2000",
                           "Get next set of sites or resources, e.g., "
                           + ARG_OFFSET + "=2000",
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
        contents.append(getHeader(msg("Output types")));
        tmp.append(HtmlUtil.formTable());
        tmp.append(
            HtmlUtil.row(
                HtmlUtil.colspan(HtmlUtil.b(msg("Site Outputs")), 2)));
        for (GsacOutput output : getServlet().getSiteOutputs()) {
            if (output.getForUser()) {
                tmp.append(HtmlUtil.row(HtmlUtil.cols(output.getLabel(),
                        ARG_OUTPUT + "=" + output.getId())));
            }
        }

        tmp.append(
            HtmlUtil.row(
                HtmlUtil.colspan(HtmlUtil.b(msg("Resource Outputs")), 2)));
        for (GsacOutput output : getServlet().getResourceOutputs()) {
            if (output.getForUser()) {
                tmp.append(HtmlUtil.row(HtmlUtil.cols(output.getLabel(),
                        ARG_OUTPUT + "=" + output.getId())));
            }
        }
        tmp.append(HtmlUtil.formTableClose());
        contents.append(HtmlUtil.makeShowHideBlock("", tmp.toString(),
                false));
        sb.append(
            HtmlUtil.makeShowHideBlock(
                msg("Web Service API Documentation"),
                HtmlUtil.insetDiv(contents.toString(), 0, 20, 0, 0), false));

        getServlet().getHtmlOutputHandler().finishHtml(request, response, sb);

    }

    /**
     * _more_
     *
     * @param request _more_
     * @param pw _more_
     * @param info _more_
     * @param showList _more_
     *
     * @throws Exception _more_
     */
    private void showRepositoryInfo(GsacRequest request, Appendable pw,
                                    GsacRepositoryInfo info, boolean showList)
            throws Exception {
        int          cnt    = 0;

        String[]     urls   = { info.getUrl() };
        String[]     labels = { "Base URL" };
        StringBuffer tmp;
        pw.append(HtmlUtil.formTable());
        for (int i = 0; i < urls.length; i++) {
            pw.append(HtmlUtil.formEntry(msgLabel(labels[i]), urls[i]));
        }
        pw.append(HtmlUtil.formTableClose());


        StringBuffer sb = new StringBuffer();

        for (CapabilityCollection collection : info.getCollections()) {
            cnt = 0;
            sb  = new StringBuffer();
            sb.append("<b>" + msgLabel("URL") + "</b> "
                      + collection.getUrl());
            for (Capability capability : collection.getCapabilities()) {
                if (cnt++ == 0) {
                    pw.append(HtmlUtil.p());
                    pw.append(getHeader(msg(collection.getName())));
                    sb.append(
                        "<table width=100% cellspacing=10><tr><td><b>What</b></td><td><b>URL Argument</b></td><td><b>Type</b></td><td><b>Values</b></td></tr>");
                }
                showCapabilityInfo(sb, capability, collection.getUrl());
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
        return HtmlUtil.div(header, HtmlUtil.cssClass("formheader"));
    }

    /**
     * _more_
     *
     * @param sb _more_
     * @param capability _more_
     * @param url _more_
     *
     * @throws Exception _more_
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
            StringBuffer sb2 = new StringBuffer();
            if (capability.getAllowMultiples()) {
                sb2.append("Zero or more of:");
            } else {
                sb2.append("Zero or one of:");
            }
            sb2.append("<table border=1 cellspacing=0 cellpadding=2>");
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
                "Use any of the spatial bounds arguments. Longitude in degrees east. e.g.<br>");
            for (String[] tuple : new String[][] {
                { id + ARG_NORTH_SUFFIX + "=40.0", " latitude&le;40.0" },
                { id + ARG_SOUTH_SUFFIX + "=30.0", " latitude&ge;40.0" },
                { id + ARG_EAST_SUFFIX + "=-100.0", " longitude&le;-100.0" },
                { id + ARG_WEST_SUFFIX + "=-110.0", " longitude&ge;-110.0" }
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
        type = HtmlUtil.href(getServlet().getUrl(URL_HELP) + "/api.html#"
                             + type, type);
        sb.append(HtmlUtil.rowTop(HtmlUtil.cols(capability.getLabel(), id,
                type, message.toString())));

    }









    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    public String msg(String s) {
        return getServlet().msg(s);
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
     * @param request _more_
     *
     * @return _more_
     */
    public String getRemoteUrlArgs(GsacRequest request) {
        Hashtable<String, String> newArg     = new Hashtable<String,
                                                   String>();
        HashSet<String>           exceptArgs = new HashSet<String>();
        exceptArgs.add(ARG_REPOSITORY);
        exceptArgs.add(ARG_GZIP);
        exceptArgs.add(ARG_OUTPUT);
        return request.getUrlArgs(newArg, exceptArgs);
    }



}
