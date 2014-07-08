/*
 * Copyright 2010; 2014 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
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

import org.gsac.gsl.output.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;

import java.io.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;


/**
 * Handles logging
 *
 * @author  Jeff McWhirter mcwhirter@unavco.org
 * @author SKW improved time tags and messages    8 Jul 2014
 */
public class GsacLogManager extends GsacManager {

    /** apache style log macro */
    public static final String LOG_MACRO_IP = "%h";

    /** apache style log macro */
    public static final String LOG_MACRO_REQUEST = "%r";

    /** apache style log macro */
    public static final String LOG_MACRO_USERAGENT = "%{User-agent}i";

    /** apache style log macro */
    public static final String LOG_MACRO_REFERER = "%{Referer}i";

    /** apache style log macro */
    public static final String LOG_MACRO_USER = "%u";

    /** apache style log macro */
    public static final String LOG_MACRO_TIME = "%t";

    /** apache style log macro */
    public static final String LOG_MACRO_METHOD = "%m";

    /** apache style log macro */
    public static final String LOG_MACRO_PATH = "%U";

    /** apache style log macro */
    public static final String LOG_MACRO_PROTOCOL = "%H";

    /** quote */
    public static final String QUOTE = "\"";

    /** The template to use for logging */
    public static final String LOG_TEMPLATE = LOG_MACRO_IP + " " + "["
                                              + LOG_MACRO_TIME + "] " + QUOTE
                                              + LOG_MACRO_REQUEST + QUOTE
                                              + " " + QUOTE
                                              + LOG_MACRO_REFERER + QUOTE
                                              + " " + QUOTE
                                              + LOG_MACRO_USERAGENT + QUOTE;

    /** info logger */
    private Logger LOG;

    /** access logger */
    private Logger ACCESSLOG;

    /** Points to where to write logs to. May be null */
    private File logDirectory;

    /**
     * _more_
     *
     * @param repository _more_
     */
    public GsacLogManager(GsacRepository repository) {
        super(repository);
    }

    /**
     * initialize the log directory. This will make a /logs sub-directory of the gsacDir.
     * It will then write the log4j.properties from the resources java dir
     *
     * @param gsacDir the gsac dir
     */
    public void initLogDir(File gsacDir) {
        logDirectory = new File(gsacDir.toString() + "/logs");
        System.err.println("GSAC: logging to " + logDirectory + "/gsac.log, and/or /usr/local/apachetomcat/logs/catalina.out");
        if ( !logDirectory.exists()) {
            System.err.println("GSAC: Making log dir:" + logDirectory);
            logDirectory.mkdir();
            if ( !logDirectory.exists()) {
                System.err.println("GSAC: failed to created log directory:" + logDirectory);
                System.err.println("GSAC: are permissions OK?");
                return;
            }
        }

        File   log4JFile = new File(logDirectory + "/" + "log4j.properties");
        String contents  = getRepository().readResource("/log4j.properties");
        //Always write out the log4j file
        //Note: If the installer modified their file this would overwrite it
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
     * @param request the request
     * @param what _more_
     * @param count _more_
     */
    public void logAccess(GsacRequest request, String what, int count) {
        String ip        = request.getOriginatingIP();
        String uri       = request.getRequestURI();
        String method    = request.getMethod();
        String userAgent = request.getUserAgent("none");
        String time      = GsacOutputHandler.makeDateFormat("dd/MMM/yyyy:HH:mm:ss Z").format(new Date());
        int response = 200;  // always set to this in GsacResponse.startResponse()
        String requestPath = method + " " + uri + " "
                             + request.getHttpServletRequest().getProtocol();
        String referer = request.getHttpServletRequest().getHeader("referer");
        if (referer == null) {
            referer = "-";
        }
        String message = getLogTemplate();

        message = message.replace(LOG_MACRO_IP, ip);
        message = message.replace(LOG_MACRO_TIME, time);
        message = message.replace(LOG_MACRO_METHOD, method);
        message = message.replace(LOG_MACRO_PATH, uri);
        message =
            message.replace(LOG_MACRO_PROTOCOL,
                            request.getHttpServletRequest().getProtocol());
        message = message.replace(LOG_MACRO_REQUEST, requestPath);
        message = message.replace(LOG_MACRO_USERAGENT, userAgent);
        message = message.replace(LOG_MACRO_REFERER, referer);
        message = message.replace(LOG_MACRO_USER, "-");
        message = message + " " + what;
        if (count >= 0) {
            message = message + " " + count;
        }

        if (logDirectory != null) {
            getAccessLogger().info(message);
        } else {
            // gives like  GSAC REQUEST:127.0.1.1 [27/Mar/2013:16:40:01 +0000] "POST /gsacring/gsacapi/site/search HTTP/1.1" "http://swierd:8080/gsacring/gsacapi/site/form" "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.2.13) Gecko/20101206 Ubuntu/10.04 (lucid) Firefox/3.6.13" site 7
            ; 
        }
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
            System.err.println("GSAC ERROR: " + getDTTM() + ": " + message);
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
            System.err.println("GSAC: Starting this GSAC server, at " + getUTCnowString() );
        }
    }


    public static String getUTCnowString() {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String utcTime = sdf.format(new Date());
        return utcTime;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getLogTemplate() {
        return LOG_TEMPLATE;
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

}
