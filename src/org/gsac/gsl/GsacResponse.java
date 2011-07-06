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

import ucar.unidata.util.IOUtil;

import java.io.*;

import java.util.ArrayList;

import java.util.List;

import java.util.zip.*;

import javax.servlet.*;
import javax.servlet.http.*;


/**
 * Class description
 *
 *
 * @version        Enter version here..., Wed, May 19, '10
 * @author         Enter your name here...
 */
public class GsacResponse {

    /** _more_ */
    public static final String MIME_CSV = "text/csv";

    /** _more_ */
    public static final String MIME_HTML = "text/html";

    /** _more_ */
    public static final String MIME_XML = "text/xml";

    /** _more_ */
    public static final String MIME_RSS = "application/rss+xml";

    /** _more_ */
    public static final String MIME_ATOM = "application/atom+xml";

    /** _more_ */
    public static final String MIME_TEXT = "text";

    /** _more_ */
    public static final String MIME_ZIP = "application/zip";

    /** _more_ */
    public static final String MIME_KML =
        "application/vnd.google-earth.kml+xml";

    /** _more_ */
    public static final String MIME_JSON = "application/json";

    /** _more_ */
    public static final String MIME_JNLP = "application/x-java-jnlp-file";


    /** _more_ */
    private GsacRequest request;

    /** _more_ */
    private boolean haveInitialized = false;

    /** _more_ */
    private List<GsacObject> objects = new ArrayList<GsacObject>();


    /** _more_ */
    private PrintWriter printWriter;

    /** _more_ */
    private GZIPOutputStream zos;

    /** _more_ */
    private boolean exceededLimit = false;

    /** _more_ */
    public StringBuffer message = new StringBuffer();

    /** _more_ */
    public StringBuffer queryInfo = new StringBuffer();

    /**
     * _more_
     */
    public GsacResponse() {}


    /**
     * _more_
     *
     * @param request The request
     */
    public GsacResponse(GsacRequest request) {
        this.request = request;
    }


    /**
     * _more_
     *
     * @param mimeType _more_
     */
    public void startResponse(String mimeType) {
        if (haveInitialized) {
            return;
        }
        haveInitialized = true;
        HttpServletResponse response = request.getHttpServletResponse();
        if (request.get(GsacArgs.ARG_GZIP, false)) {
            mimeType = MIME_ZIP;
        }
        response.setContentType(mimeType);
        response.setStatus(200);
    }

    /**
     * _more_
     *
     * @param filename _more_
     */
    public void setReturnFilename(String filename) {
        HttpServletResponse response = request.getHttpServletResponse();
        response.setHeader("Content-disposition",
                           "attachment; filename=" + filename);
    }



    /**
     * _more_
     *
     * @param message _more_
     */
    public void setMessage(String message) {
        this.message = new StringBuffer(message);
    }

    /**
     * _more_
     *
     * @param message _more_
     */
    public void appendMessage(String message) {
        this.message.append(message);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getMessage() {
        return message.toString();
    }


    /**
     * _more_
     *
     * @param QueryInfo _more_
     */
    public void setQueryInfo(String QueryInfo) {
        this.queryInfo = new StringBuffer(QueryInfo);
    }

    /**
     * _more_
     *
     * @param QueryInfo _more_
     */
    public void appendQueryInfo(String QueryInfo) {
        this.queryInfo.append(QueryInfo);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getQueryInfo() {
        return queryInfo.toString();
    }



    /**
     * _more_
     *
     * @param url _more_
     *
     * @throws IOException On badness
     */
    public void sendRedirect(String url) throws IOException {
        HttpServletResponse response = request.getHttpServletResponse();
        response.sendRedirect(url);
    }


    /**
     * _more_
     *
     * @throws IOException On badness
     */
    public void endResponse() throws IOException {
        if (printWriter != null) {
            printWriter.close();
            printWriter = null;
        }
        if (zos != null) {
            zos.flush();
            zos.close();
        }
        HttpServletResponse response = request.getHttpServletResponse();
        response.getOutputStream().flush();
        response.getOutputStream().close();
    }

    /**
     * _more_
     */
    public void setExceededLimit() {
        exceededLimit = true;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getExceededLimit() {
        return exceededLimit;
    }


    /**
     * _more_
     *
     * @return _more_
     *
     * @throws IOException On badness
     */
    public PrintWriter getPrintWriter() throws IOException {
        if (printWriter == null) {
            OutputStream os = request.getOutputStream();
            if (request.get(GsacArgs.ARG_GZIP, false)) {
                os = zos = new GZIPOutputStream(os);
            }
            printWriter = new PrintWriter(os);
        }
        return printWriter;
    }

    /**
     *  Set the HaveInitialized property.
     *
     *  @param value The new value for HaveInitialized
     */
    public void setHaveInitialized(boolean value) {
        haveInitialized = value;
    }

    /**
     *  Get the HaveInitialized property.
     *
     *  @return The HaveInitialized
     */
    public boolean getHaveInitialized() {
        return haveInitialized;
    }


    /**
     * _more_
     *
     * @param sites _more_
     *
     * @param objects _more_
     */
    public void addObjects(List<GsacObject> objects) {
        for (GsacObject object : objects) {
            addObject(object);
        }
    }


    /**
     * _more_
     *
     * @param site _more_
     *
     * @param object _more_
     */
    public final synchronized void addObject(GsacObject object) {
        handleNewObject(object);
    }


    /**
     * _more_
     *
     * @param site _more_
     *
     * @param object _more_
     */
    public void handleNewObject(GsacObject object) {
        objects.add(object);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public List<GsacObject> getObjects() {
        return objects;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public List<GsacSite> getSites() {
        List<GsacSite> sites = new ArrayList<GsacSite>();
        for (GsacObject object : objects) {
            sites.add((GsacSite) object);
        }
        return sites;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public List<GsacResource> getResources() {
        List<GsacResource> resources = new ArrayList<GsacResource>();
        for (GsacObject object : objects) {
            resources.add((GsacResource) object);
        }
        return resources;
    }


    /**
     *  Set the Request property.
     *
     *  @param value The new value for Request
     */
    public void setRequest(GsacRequest value) {
        request = value;
    }

    /**
     *  Get the Request property.
     *
     *  @return The Request
     */
    public GsacRequest getRequest() {
        return request;
    }



}
