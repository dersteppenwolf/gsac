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


import org.gsac.gsl.model.*;
import org.gsac.gsl.output.*;
import org.gsac.gsl.output.resource.*;
import org.gsac.gsl.output.site.*;
import org.gsac.gsl.util.*;

import org.w3c.dom.*;

import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.lang.management.*;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.servlet.*;
import javax.servlet.http.*;


/**
 * Main entry point into the gsacws services
 *
 *
 * @author Jeff McWhirter mcwhirter@unavco.org
 */
public class GsacServlet extends HttpServlet implements GsacConstants {

    /** The default repository class */
    private static final String DFLT_REPOSITORY_CLASS =
        "org.gsac.gsl.DummyRepository";

    /** The repository that does the work */
    private GsacRepository gsacRepository;

    /** _more_ */
    private List<GsacOutput> siteOutputs = new ArrayList<GsacOutput>();

    /** site output handlers */
    private Hashtable<String, GsacOutput> siteOutputMap =
        new Hashtable<String, GsacOutput>();

    /** _more_ */
    private List<GsacOutput> resourceOutputs = new ArrayList<GsacOutput>();

    /** resource output handlers */
    private Hashtable<String, GsacOutput> resourceOutputMap =
        new Hashtable<String, GsacOutput>();


    /** _more_ */
    private List<GsacOutput> listOutputs = new ArrayList<GsacOutput>();

    /** list output handlers */
    private Hashtable<String, GsacOutput> listOutputMap =
        new Hashtable<String, GsacOutput>();

    /** _more_ */
    private Properties properties = new Properties();

    /** _more_ */
    private String localHostname;

    /** _more_ */
    private int port = -1;

    /** _more_ */
    private HtmlOutputHandler htmlOutputHandler;

    /** _more_ */
    private boolean haveInitialized = false;

    /** _more_          */
    private int numConnections = 0;

    /** _more_          */
    private Date startDate = new Date();

    /**
     * Make the servlet. This will look up the class of the repository to instantiate
     * from the system property gsac.repository.class
     *
     * @throws Exception on badness
     */
    public GsacServlet() throws Exception {
        System.err.println("GsacServlet.ctor");
    }


    /**
     * _more_
     *
     * @param port _more_
     * @param properties _more_
     *
     * @throws Exception On badness
     */
    public GsacServlet(int port, Properties properties) throws Exception {
        this.properties = properties;
        this.port       = port;
    }


    /**
     * Make the servlet with the given repository
     *
     * @param gsacRepository the repository to use
     * @param port _more_
     * @param properties _more_
     *
     * @throws Exception On badness
     */
    public GsacServlet(GsacRepository gsacRepository, int port,
                       Properties properties)
            throws Exception {
        this.gsacRepository = gsacRepository;
        this.port           = port;
        this.properties     = properties;
        if (this.gsacRepository != null) {
            this.gsacRepository.initServlet(this);
        }
    }


    /**
     * _more_
     *
     * @param gsacRepository _more_
     * @param port _more_
     *
     * @throws Exception On badness
     */
    public GsacServlet(GsacRepository gsacRepository, int port)
            throws Exception {
        this.port           = port;
        this.gsacRepository = gsacRepository;
        if (this.gsacRepository != null) {
            this.gsacRepository.initServlet(this);
        }
    }


    /**
     * _more_
     *
     * @throws javax.servlet.ServletException _more_
     */
    public void init() throws javax.servlet.ServletException {
        System.err.println("GsacServlet.init");
        super.init();
        try {
            initServlet();
        } catch (Exception exc) {
            System.err.println("GsacServlet.init: error " + exc);
            throw new RuntimeException(exc);
        }
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public int getPort() {
        if (port >= 0) {
            return port;
        }
        return gsacRepository.getPort();
    }


    /**
     * initialize
     *
     * @throws Exception On badness
     */
    public void initServlet() throws Exception {
        try {
            if (haveInitialized) {
                return;
            }
            haveInitialized = true;


            ServletContext context = getServletContext();

            if (context != null) {
                //Load properties from the war
                String      propertyFile = "/WEB-INF/gsac.properties";
                InputStream is = context.getResourceAsStream(propertyFile);
                if (is != null) {
                    properties.load(is);
                }

                //Load properties from the web.xml
                for (Enumeration params = context.getInitParameterNames();
                        params.hasMoreElements(); ) {
                    String paramName = (String) params.nextElement();
                    String paramValue =
                        getServletContext().getInitParameter(paramName);
                    properties.put(paramName, paramValue);
                }
            }
        } catch (NullPointerException npe) {
            //      System.err.println("**** error:" + npe);
            //      npe.printStackTrace();
            //I know this is a hack but getServletContext is throwing an NPE when we aren't running
            //in a servlet container
        }

        //Create the repository by reflection if needed
        if (gsacRepository == null) {
            gsacRepository = createRepositoryViaReflection();
        }


        //Init the output handlers
        //TODO: put these in a properties file

        htmlOutputHandler = new HtmlSiteOutputHandler(this);
        new KmlSiteOutputHandler(this);
        new CsvSiteOutputHandler(this);
        new RssSiteOutputHandler(this);
        new AtomSiteOutputHandler(this);
        new JsonSiteOutputHandler(this);
        new XmlSiteOutputHandler(this);


        new HtmlResourceOutputHandler(this);
        new CsvResourceOutputHandler(this);
        new WgetResourceOutputHandler(this);
        new UrlResourceOutputHandler(this);
        new JsonResourceOutputHandler(this);
        new DownloaderResourceOutputHandler(this);
        new ZipResourceOutputHandler(this);
        new ListOutputHandler(this);
        new RssResourceOutputHandler(this);
        new XmlResourceOutputHandler(this);

        getRepository().logInfo("GsacServlet: running repository:"
                                + gsacRepository.getClass().getName());
        getRepository().logInfo("GsacServlet: url:"
                                + getAbsoluteUrl(getUrl(URL_SITE_FORM)));
    }


    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private GsacRepository createRepositoryViaReflection() throws Exception {
        String className =
            (String) properties.get(GsacConstants.PROP_REPOSITORY_CLASS);
        if (className == null) {
            className =
                System.getProperty(GsacConstants.PROP_REPOSITORY_CLASS,
                                   (String) null);
        }
        if (className == null) {
            className = DFLT_REPOSITORY_CLASS;
            System.err.println(
                "No repository class name defined. Using the Example repository.\nTo use your own repository set the system property:\n"
                + " java -D" + GsacConstants.PROP_REPOSITORY_CLASS
                + "=your.gsac.repository.class");
        }
        System.err.println("GsacServlet:initServlet: making repository:"
                           + className);
        Class c = Class.forName(className);
        this.gsacRepository = (GsacRepository) c.newInstance();
        gsacRepository.initServlet(this);
        return gsacRepository;
    }

    /**
     * _more_
     *
     * @param name _more_
     *
     * @return _more_
     */
    public String getProperty(String name) {
        String value = gsacRepository.getProperty(name);
        if (value == null) {
            //            System.out.println("#" +name +"=");
        }
        return value;
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
     * _more_
     *
     * @param name _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public long getProperty(String name, long dflt) {
        String prop = getProperty(name);
        if (prop != null) {
            return new Long(prop).longValue();
        }
        return dflt;
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
     * preprend the url base to  the given path
     *
     * @param path path
     *
     * @return full url
     */
    public String getUrl(String path) {
        return getRepository().getUrlBase() + path;
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
        return HtmlUtil.url(getRepository().getUrlBase() + path, args);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getLocalHostname() {
        if (localHostname == null) {
            try {
                java.net.InetAddress localMachine =
                    java.net.InetAddress.getLocalHost();
                localHostname = localMachine.getHostName();
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            }
        }
        return localHostname;
    }


    /**
     * _more_
     *
     * @param path _more_
     *
     * @return _more_
     */
    public String getAbsoluteUrl(String path) {
        String hostname = getRepository().getHostname();
        if (hostname == null) {
            hostname = getLocalHostname();
        }
        int port = getPort();
        if (port == 80) {
            return "http://" + hostname + path;
        } else {
            return "http://" + hostname + ":" + port + "" + path;
        }
    }






    /**
     * Add output type for sites
     *
     * @param output site output type
     */
    public void addSiteOutput(GsacOutput output) {
        if (getProperty(output.getProperty("enabled"), true)) {
            siteOutputMap.put(output.getId(), output);
            siteOutputs.add(output);
        }
    }



    /**
     * add output type for resources
     *
     * @param output resource output type
     */
    public void addResourceOutput(GsacOutput output) {
        if (getProperty(output.getProperty("enabled"), true)) {
            resourceOutputMap.put(output.getId(), output);
            resourceOutputs.add(output);
        }
    }

    /**
     * add output type for listing
     *
     * @param output output type
     */
    public void addListOutput(GsacOutput output) {
        if (getProperty(output.getProperty("enabled"), true)) {
            listOutputMap.put(output.getId(), output);
            listOutputs.add(output);
        }
    }






    /**
     * servlet destroy
     */
    public void destroy() {
        super.destroy();
    }


    /**
     * Get the repository
     *
     * @return the repository
     */
    public GsacRepository getRepository() {
        if (gsacRepository == null) {
            try {
                gsacRepository = createRepositoryViaReflection();
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            }
        }
        return gsacRepository;
    }

    /**
     * Find the site output handler
     *
     *
     * @param output output type
     * @param map map to use
     *
     * @return site output handler
     */
    public GsacOutputHandler getOutputHandler(String output,
            Hashtable<String, GsacOutput> map) {
        GsacOutput gsacOutput = map.get(output);
        if (gsacOutput == null) {
            throw new IllegalArgumentException("Unknown output type:"
                    + output);
        }
        return gsacOutput.getOutputHandler();
    }



    /**
     * _more_
     *
     * @param request the request
     *
     * @return _more_
     */
    public GsacOutputHandler getSiteOutputHandler(GsacRequest request) {
        return getOutputHandler(request.get(ARG_OUTPUT, OUTPUT_SITE_DEFAULT),
                                siteOutputMap);
    }

    /**
     * _more_
     *
     * @param request the request
     *
     * @return _more_
     */
    public GsacOutputHandler getResourceOutputHandler(GsacRequest request) {
        return getOutputHandler(
            request.get(ARG_OUTPUT, OUTPUT_RESOURCE_DEFAULT),
            resourceOutputMap);
    }

    /**
     * _more_
     *
     * @param request the request
     *
     * @return _more_
     */
    public GsacOutputHandler getListOutputHandler(GsacRequest request) {
        return getOutputHandler(request.get(ARG_OUTPUT, OUTPUT_LIST_DEFAULT),
                                listOutputMap);
    }


    /**
     * process get request
     *
     * @param request the request
     * @param response the response
     *
     * @throws IOException On badness
     * @throws ServletException On badness
     */
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {
        GsacRequest gsacRequest = new GsacRequest(gsacRepository, request,
                                      response);
        doGet(gsacRequest);
    }


    List conns = new ArrayList();

    /**
     * Main entry point
     *
     *
     * @param gsacRequest the request
     *
     * @throws IOException On badness
     * @throws ServletException On badness
     */
    public void doGet(GsacRequest gsacRequest)
            throws IOException, ServletException {
        String uri = gsacRequest.getRequestURI();

        //TODO: What to do with a head request
        if (gsacRequest.getMethod().toUpperCase().equals("HEAD")) {
            System.err.println("****** head:" + uri);
            return;
        }

        try {
            boolean serviceRequest = uri.indexOf(URL_HTDOCS_BASE) < 0;
            if (serviceRequest) {
                numConnections++;
                //                getRepository().logInfo("start url:" + uri);
            }
            String what = "other";



            if (uri.indexOf(URL_SITE_BASE) >= 0) {
                what = URL_SITE_BASE;
                handleSiteRequest(gsacRequest);
                //            } else if(uri.indexOf("connections")) {
                //                System.err.println ("getting connection");
                //            for(int i=0;i<30;i++)
                //                conns.add(getRepository().getDatabaseManager().getConnection());
            } else if (uri.indexOf(URL_RESOURCE_BASE) >= 0) {
                what = URL_RESOURCE_BASE;
                handleResourceRequest(gsacRequest);

            } else if (uri.indexOf("/stats") >= 0) {
                handleStatsRequest(gsacRequest,
                                   new GsacResponse(gsacRequest));
            } else if (uri.indexOf(URL_HELP) >= 0) {
                handleHelpRequest(gsacRequest, new GsacResponse(gsacRequest));
            } else if (uri.indexOf(URL_LIST_BASE) >= 0) {
                handleListRequest(gsacRequest);
            } else if (uri.indexOf(URL_HTDOCS_BASE) >= 0) {
                handleHtdocsRequest(gsacRequest);
            } else if (uri.indexOf(URL_REPOSITORY_VIEW) >= 0) {
                getRepository().handleViewRequest(gsacRequest,
                        new GsacResponse(gsacRequest));
            } else {
                throw new UnknownRequestException("");
                //                getRepository().logError("Unknown request:" + uri, null);
            }
            //Only log the access if its actuall a service request (as opposed to htdocs requests)
            if (serviceRequest) {
                getRepository().logAccess(gsacRequest, what);
                //                System.err.println (getRepository().getDatabaseManager().getPoolStats());
                //                System.out.println("http://${server}" + gsacRequest.toString());
            }
        } catch (UnknownRequestException exc) {
            getRepository().logError("Unknown request:" + uri + "?"
                                     + gsacRequest.getUrlArgs(), null);
            gsacRequest.sendError(HttpServletResponse.SC_NOT_FOUND,
                                  "Unknown request:" + uri);
        } catch (java.net.SocketException sexc) {
            //Ignore the client closing the connection
        } catch (Exception exc) {
            Throwable thr = LogUtil.getInnerException(exc);
            getRepository().logError("Error processing request:" + uri + "?"
                                     + gsacRequest.getUrlArgs(), thr);
            try {
                gsacRequest.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "An error occurred:" + thr);
            } catch (Exception ignoreThisOne) {}
        }
    }



    /**
     * process post
     *
     * @param request the request
     * @param response the response
     *
     * @throws IOException On badness
     * @throws ServletException On badness
     */
    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {
        doGet(request, response);
    }


    /**
     * handle a site request
     *
     * @param gsacRequest the request
     *
     * @throws Exception On badness
     */
    public void handleSiteRequest(GsacRequest gsacRequest) throws Exception {
        GsacOutputHandler outputHandler = getSiteOutputHandler(gsacRequest);
        outputHandler.handleSiteRequest(gsacRequest);
    }


    /**
     * handle a resource request
     *
     * @param gsacRequest the request
     *
     * @throws Exception On badness
     */
    public void handleResourceRequest(GsacRequest gsacRequest)
            throws Exception {
        GsacOutputHandler outputHandler =
            getResourceOutputHandler(gsacRequest);
        outputHandler.handleResourceRequest(gsacRequest);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param response _more_
     *
     * @throws Exception _more_
     */
    public void handleHelpRequest(GsacRequest request, GsacResponse response)
            throws Exception {
        String path = request.getGsacUrlPath().substring(URL_HELP.length());
        if (path.length() == 0) {
            path = "/index.html";
        }

        InputStream inputStream = getResourceInputStream("/org/gsac/gsl/help"
                                      + path);
        if (inputStream == null) {
            //TODO:         inputStream = getRepository().getResourceInputStream(path);
        }
        if ( !path.endsWith(".html")) {
            OutputStream outputStream = request.getOutputStream();
            IOUtil.writeTo(inputStream, outputStream);
            IOUtil.close(outputStream);
            IOUtil.close(inputStream);
            return;
        }

        StringBuffer sb = new StringBuffer();
        htmlOutputHandler.initHtml(request, response, sb);
        String contents = "Could not read file:" + path;
        if (inputStream != null) {
            contents = IOUtil.readContents(inputStream);
            inputStream.close();
            contents = contents.replace("${urlroot}",
                                        getRepository().getUrlBase()
                                        + URL_BASE);
            contents =
                contents.replace("${fullurlroot}",
                                 getAbsoluteUrl(getRepository().getUrlBase()
                                     + URL_BASE));
        }
        sb.append(contents);
        htmlOutputHandler.finishHtml(request, response, sb);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param response _more_
     *
     * @throws Exception _more_
     */
    public void handleStatsRequest(GsacRequest request, GsacResponse response)
            throws Exception {
        response.startResponse(GsacResponse.MIME_TEXT);
        request.put(ARG_DECORATE, "false");
        StringBuffer sb = new StringBuffer();
        htmlOutputHandler.initHtml(request, response, sb);

        sb.append(HtmlUtil.formTable());
        DecimalFormat fmt         = new DecimalFormat("#0");

        double        totalMemory = (double) Runtime.getRuntime().maxMemory();
        double        freeMemory  =
            (double) Runtime.getRuntime().freeMemory();
        double highWaterMark = (double) Runtime.getRuntime().totalMemory();
        double        usedMemory  = (highWaterMark - freeMemory);
        totalMemory = totalMemory / 1000000.0;
        usedMemory  = usedMemory / 1000000.0;
        sb.append(HtmlUtil.formEntry("Total Memory Available:",
                                     fmt.format(totalMemory) + " (MB)"));
        sb.append(HtmlUtil.formEntry("Used Memory:",
                                     fmt.format(usedMemory) + " (MB)"));

        sb.append(HtmlUtil.formEntry("# Requests:", "" + numConnections));
        sb.append(HtmlUtil.formEntry("Start Time:", "" + startDate));


        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        sb.append(HtmlUtil.formEntry("Up Time:",
                                     fmt.format((double) (uptime / 1000
                                         / 60)) + " " + msg("minutes")));

        getRepository().addStats(sb);
        sb.append(HtmlUtil.formTableClose());

        sb.append(LogUtil.getStackDump(true));
        htmlOutputHandler.finishHtml(request, response, sb);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    HtmlOutputHandler getHtmlOutputHandler() {
        return htmlOutputHandler;
    }




    /**
     * handle a resource request
     *
     * @param request the request
     *
     * @throws Exception On badness
     */
    public void handleListRequest(GsacRequest request) throws Exception {
        GsacOutputHandler outputHandler = getListOutputHandler(request);
        outputHandler.handleListRequest(request);
    }





    /**
     * _more_
     *
     * @param request the request
     *
     * @throws Exception On badness
     */
    public void handleHtdocsRequest(GsacRequest request) throws Exception {
        String      uri         = request.getRequestURI();
        int idx = uri.indexOf(URL_HTDOCS_BASE) + URL_HTDOCS_BASE.length();
        String      path        = uri.substring(idx);

        InputStream inputStream = null;
        String[] paths = new String[] {
                             getRepository().getLocalHtdocsPath(path),
                             "/org/gsac/gsl/htdocs" + path };

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
        if (uri.endsWith(".js") || uri.endsWith(".jnlp")) {
            String content = IOUtil.readContents(inputStream);
            inputStream.close();
            content = content.replace("${urlroot}",
                                      getRepository().getUrlBase()
                                      + URL_BASE);
            content =
                content.replace("${fullurlroot}",
                                getAbsoluteUrl(getRepository().getUrlBase()
                                    + URL_BASE));
            inputStream = new ByteArrayInputStream(content.getBytes());
        }
        OutputStream outputStream = request.getOutputStream();
        IOUtil.writeTo(inputStream, outputStream);
        IOUtil.close(outputStream);
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
     * @return _more_
     */
    public List<GsacOutput> getSiteOutputs() {
        return siteOutputs;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public List<GsacOutput> getResourceOutputs() {
        return resourceOutputs;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public List<GsacOutput> getListOutputs() {
        return listOutputs;
    }



    /**
     * _more_
     *
     * @param h _more_
     *
     * @return _more_
     */
    public String makeInformationDialog(String h) {
        return makeDialog(h, "/information.png", true);
    }

    /**
     * _more_
     *
     * @param h _more_
     *
     * @return _more_
     */
    public String makeWarningDialog(String h) {
        return makeDialog(h, "/warning.png", true);
    }

    /**
     * _more_
     *
     * @param h _more_
     *
     * @return _more_
     */
    public String makeErrorDialog(String h) {
        return makeDialog(h, "/error.png", true);
    }

    /**
     * _more_
     *
     * @param h _more_
     * @param icon _more_
     * @param showClose _more_
     *
     * @return _more_
     */
    public String makeDialog(String h, String icon, boolean showClose) {
        String html =
            HtmlUtil.jsLink(HtmlUtil.onMouseClick("hide('messageblock')"),
                            HtmlUtil.img(iconUrl("/close.gif")));
        if ( !showClose) {
            html = "&nbsp;";
        }
        h = "<div class=\"innernote\"><table cellspacing=\"0\" cellpadding=\"0\" border=\"0\"><tr><td valign=\"top\">"
            + HtmlUtil.img(iconUrl(icon)) + HtmlUtil.space(2)
            + "</td><td valign=\"bottom\"><span class=\"notetext\">" + h
            + "</span></td></tr></table></div>";
        return "\n<table border=\"0\" id=\"messageblock\"><tr><td><div class=\"note\"><table><tr valign=top><td>"
               + h + "</td><td>" + html + "</td></tr></table>"
               + "</div></td></tr></table>\n";
    }


    /**
     * _more_
     *
     * @param msg _more_
     *
     * @return _more_
     */
    public String msg(String msg) {
        String newMsg = gsacRepository.translatePhrase(msg);
        if (newMsg != null) {
            return newMsg;
        }
        return msg;
    }




    /**
     * main
     *
     * @param args args
     *
     * @throws Exception On badness
     */
    public static void main(String[] args) throws Exception {
        GsacServlet servlet = new GsacServlet();
    }




}
