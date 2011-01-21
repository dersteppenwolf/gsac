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

package org.gsac.gsl.output;


import org.gsac.gsl.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.util.*;


import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.Misc;

import java.io.*;

import java.text.DecimalFormat;

import java.text.SimpleDateFormat;

import java.util.Date;

import java.util.List;
import java.util.TimeZone;

import javax.servlet.*;
import javax.servlet.http.*;


/**
 * Class description
 *
 *
 */
public abstract class GsacOutputHandler implements GsacConstants {

    /** _more_ */
    public static final TimeZone TIMEZONE_DEFAULT =
        TimeZone.getTimeZone("UTC");

    /** _more_ */
    protected SimpleDateFormat dateSdf = makeDateFormat("yyyy-MM-dd");

    protected SimpleDateFormat dateTimeSdf = makeDateFormat("yyyy-MM-dd HH:mm");

    /** _more_ */
    protected SimpleDateFormat timeSdf = makeDateFormat("HH:mm:ss z");



    /** _more_ */
    private GsacServlet gsacServlet;

    /** _more_ */
    private DecimalFormat sizeFormat = new DecimalFormat("####0.00");

    /** _more_ */
    private DecimalFormat latLonFormat = new DecimalFormat("####0.###");

    /** _more_ */
    private DecimalFormat elevationFormat = new DecimalFormat("####0.0");


    /** _more_ */
    private static boolean doSite = true;

    /** _more_ */
    private static boolean doResource = true;

    /** _more_ */
    private static boolean doSiteCode = true;

    /** _more_ */
    private static boolean doSiteStatus = true;

    /** _more_ */
    private static boolean doSiteGroup = true;

    /** _more_ */
    private static boolean doSiteType = true;

    /** _more_ */
    private static boolean doSiteDateRange = true;

    /** _more_ */
    private static boolean doResourcePublishDate = true;

    /** _more_ */
    private static boolean doResourceFileSize = true;


    /**
     * _more_
     *
     * @param gsacServlet _more_
     */
    public GsacOutputHandler(GsacServlet gsacServlet) {
        this.gsacServlet = gsacServlet;

        doSite           = getRepository().isCapable(HEADER_SITE);
        doResource       = getRepository().isCapable(HEADER_RESOURCE);
        doSiteCode       = getRepository().isCapable(ARG_SITE_CODE);
        doSiteStatus     = getRepository().isCapable(ARG_SITE_STATUS);
        doSiteGroup      = getRepository().isCapable(ARG_SITE_GROUP);
        doSiteType       = getRepository().isCapable(ARG_SITE_TYPE);
        //        doSiteDateRange = getRepository().isCapable(ARG_SITE_TYPE);

        doResourcePublishDate =
            getRepository().isCapable(ARG_RESOURCE_PUBLISHDATE);
        doResourceFileSize = getRepository().isCapable(ARG_RESOURCE_SIZE);

    }



    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getDoSite() {
        return doSite;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getDoResource() {
        return doResource;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getDoResourcePublishDate() {
        return doResourcePublishDate;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getDoResourceFileSize() {
        return doResourceFileSize;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getDoSiteCode() {
        return doSiteCode;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getDoSiteStatus() {
        return doSiteStatus && (getRepository().getSiteStatuses().size() > 0);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getDoSiteGroup() {
        return doSiteGroup && (getRepository().getSiteGroups().size() > 0);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getDoSiteType() {
        return doSiteType && (getRepository().getSiteTypes().size() > 0);
    }




    /**
     * _more_
     *
     * @param gsacRequest _more_
     *
     * @return _more_
     */
    public GsacResponse doMakeResponse(GsacRequest gsacRequest) {
        return new GsacResponse(gsacRequest);
    }


    /**
     * _more_
     *
     * @param formatString _more_
     *
     * @return _more_
     */
    public static SimpleDateFormat makeDateFormat(String formatString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        dateFormat.setTimeZone(TIMEZONE_DEFAULT);
        dateFormat.applyPattern(formatString);
        return dateFormat;
    }


    /**
     * _more_
     *
     * @param latlon _more_
     *
     * @return _more_
     */
    public String formatLatLon(double latlon) {
        return latLonFormat.format(latlon);
    }

    /**
     * _more_
     *
     * @param elevation _more_
     *
     * @return _more_
     */
    public String formatElevation(double elevation) {
        return elevationFormat.format(elevation);
    }

    /**
     * _more_
     *
     * @param date _more_
     *
     * @return _more_
     */
    public String formatDate(Date date) {
        synchronized(dateSdf) {
            return dateSdf.format(date);
        }
    }

    public String formatTime(Date date) {
        synchronized(timeSdf) {
            return timeSdf.format(date);
        }
    }

    public String formatDateTime(Date date) {
        synchronized(dateTimeSdf) {
            return dateTimeSdf.format(date);
        }
    }

    /**
       Cut and pasted from GsacRepositoryManager
     */
    public void checkMessage(GsacRequest request, GsacResponse response, Appendable htmlBuff) {
        String message = response.getMessage();
        if(message.length()>0) {
            try {
                htmlBuff.append(message);
            } catch(Exception exc) {
                throw new RuntimeException(exc);
            }
        }
    }



    /**
     * _more_
     *
     * @param icon _more_
     *
     * @return _more_
     */
    public String iconUrl(String icon) {
        return getServlet().iconUrl(icon);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param response _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    public void finishResponse(GsacRequest request, GsacResponse response,
                               StringBuffer sb)
            throws Exception {
        PrintWriter pw = response.getPrintWriter();
        pw.append(sb.toString());
        response.endResponse();
    }

    /**
     * _more_
     *
     * @param msg _more_
     *
     * @return _more_
     */
    public String msg(String msg) {
        return getServlet().msg(msg);
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
     * @return _more_
     */
    public GsacRepository getRepository() {
        return gsacServlet.getRepository();
    }

    /**
     * _more_
     *
     * @param gsacRequest _more_
     *
     * @throws Exception _more_
     */
    public void handleSiteRequest(GsacRequest gsacRequest) throws Exception {
        handleSiteRequest(gsacRequest, doMakeResponse(gsacRequest));
    }


    /**
     * _more_
     *
     * @param gsacRequest _more_
     * @param gsacResponse _more_
     *
     * @throws Exception _more_
     */
    public void handleSiteRequest(GsacRequest gsacRequest,
                                  GsacResponse gsacResponse)
            throws Exception {
        getRepository().handleSiteRequest(gsacRequest, gsacResponse);
        handleSiteResult(gsacRequest, gsacResponse);
    }


    /**
     * _more_
     *
     * @param gsacRequest _more_
     *
     * @throws Exception _more_
     */
    public void handleResourceRequest(GsacRequest gsacRequest)
            throws Exception {
        handleResourceRequest(gsacRequest, doMakeResponse(gsacRequest));
    }

    /**
     * _more_
     *
     * @param gsacRequest _more_
     * @param gsacResponse _more_
     *
     * @throws Exception _more_
     */
    public void handleResourceRequest(GsacRequest gsacRequest,
                                      GsacResponse gsacResponse)
            throws Exception {
        getRepository().handleResourceRequest(gsacRequest, gsacResponse);
        handleResourceResult(gsacRequest, gsacResponse);
    }


    /**
     * _more_
     *
     * @param gsacRequest _more_
     *
     * @throws Exception _more_
     */
    public void handleListRequest(GsacRequest gsacRequest) throws Exception {
        handleListRequest(gsacRequest, doMakeResponse(gsacRequest));
    }

    /**
     * _more_
     *
     * @param gsacRequest _more_
     * @param gsacResponse _more_
     *
     * @throws Exception _more_
     */
    public void handleListRequest(GsacRequest gsacRequest,
                                  GsacResponse gsacResponse)
            throws Exception {
        throw new IllegalArgumentException("not implemented");
    }

    /**
     * _more_
     *
     * @param gsacRequest _more_
     * @param response _more_
     *
     * @throws IOException _more_
     * @throws ServletException _more_
     *
     * @throws Exception _more_
     */
    public void handleResourceResult(GsacRequest gsacRequest,
                                     GsacResponse response)
            throws Exception {
        throw new IllegalArgumentException(
            getClass().getName() + ".handleResourceResult not implemented");
    }


    /**
     * _more_
     *
     * @param gsacRequest _more_
     * @param response _more_
     *
     * @throws Exception _more_
     */
    public void handleSiteResult(GsacRequest gsacRequest,
                                 GsacResponse response)
            throws Exception {
        throw new IllegalArgumentException(
            getClass().getName() + ".handleSiteResult not implemented");
    }

    /**
     * _more_
     *
     * @param bytes _more_
     *
     * @return _more_
     */
    public String formatFileSize(long bytes) {
        if (bytes < 1000) {
            return "" + bytes;
        }
        if (bytes < 1000000) {
            return sizeFormat.format(bytes / 1000.0) + "&nbsp;KB";
        }
        if (bytes < 1000000000) {
            return sizeFormat.format(bytes / 1000000.0) + "&nbsp;MB";
        }
        return sizeFormat.format(bytes / 1000000000.0) + "&nbsp;GB";
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public GsacServlet getServlet() {
        return gsacServlet;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean shouldUrlsBeAbsolute() {
        return false;
    }


    /**
     * _more_
     *
     * @param args _more_
     *
     * @return _more_
     */
    public String makeSiteUrl(String args) {
        return makeUrl(URL_SITE_VIEW + "?" + args);
    }

    /**
     * _more_
     *
     * @param site _more_
     *
     * @return _more_
     */
    public String makeSiteUrl(GsacSite site) {
        return makeSiteUrl(HtmlUtil.arg(ARG_SITEID, site.getSiteId()));
    }



    /**
     * _more_
     *
     * @param site _more_
     *
     * @return _more_
     */
    public String makeSiteHref(GsacSite site) {
        String siteUrl = makeSiteUrl(site);
        return HtmlUtil.href(siteUrl, site.getLabel());
    }

    /**
     * _more_
     *
     * @param args _more_
     *
     * @return _more_
     */
    public String makeResourceUrl(String args) {
        return makeUrl(URL_RESOURCE_VIEW + "?" + args);
    }

    public String makeResourceUrl(GsacResource resource) {
        return makeResourceUrl(HtmlUtil.arg(ARG_RESOURCE_ID, resource.getId()));
    }


    /**
     * _more_
     *
     * @param suffix _more_
     *
     * @return _more_
     */
    public String makeUrl(String suffix) {
        String url = getRepository().getUrlBase() + suffix;
        if (shouldUrlsBeAbsolute()) {
            url = gsacServlet.getAbsoluteUrl(url);
        }
        return url;
    }





}
