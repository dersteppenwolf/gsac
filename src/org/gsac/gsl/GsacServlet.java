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
import org.gsac.gsl.util.*;

import org.w3c.dom.*;

import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.xml.XmlUtil;

import java.io.*;


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
    private Properties properties = new Properties();

    /** _more_ */
    private String localHostname;

    /** _more_ */
    private int port = -1;


    /** _more_ */
    private boolean haveInitialized = false;


    /**
     * Make the servlet. This will look up the class of the repository to instantiate
     * from the system property gsac.repository.class
     *
     * @throws Exception on badness
     */
    public GsacServlet() throws Exception {}


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

        getRepository().logInfo("GsacServlet: running repository:"
                                + gsacRepository.getClass().getName());
        getRepository().logInfo("GsacServlet: url:"
                                + getAbsoluteUrl(gsacRepository.getUrl(URL_SITE_FORM)));
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
        gsacRepository.handleRequest(gsacRequest);
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
        GsacRequest gsacRequest = new GsacRequest(gsacRepository, request,
                                      response);
        gsacRepository.handleRequest(gsacRequest);
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
