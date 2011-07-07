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
 * Abstract base class for the output handlers.
 *
 *
 */
public abstract class GsacOutputHandler implements GsacConstants {

    /** timezone */
    public static final TimeZone TIMEZONE_DEFAULT =
        TimeZone.getTimeZone("UTC");

    /** date format */
    protected SimpleDateFormat dateSdf = makeDateFormat("yyyy-MM-dd");

    /** date format */
    protected SimpleDateFormat dateTimeSdf =
        makeDateFormat("yyyy-MM-dd HH:mm");

    /** date format */
    protected SimpleDateFormat timeSdf = makeDateFormat("HH:mm:ss z");

    /** formats */
    private DecimalFormat sizeFormat = new DecimalFormat("####0.00");

    /** formats */
    private DecimalFormat latLonFormat = new DecimalFormat("####0.###");

    /** formats */
    private DecimalFormat elevationFormat = new DecimalFormat("####0.0");


    /** the repository */
    private GsacRepository gsacRepository;


    /** flags for repository capabilities */
    private static boolean doSite = true;

    /** flags for repository capabilities */
    private static boolean doResource = true;

    /** flags for repository capabilities */
    private static boolean doSiteCode = true;

    /** flags for repository capabilities */
    private static boolean doSiteStatus = true;

    /** flags for repository capabilities */
    private static boolean doSiteGroup = true;

    /** flags for repository capabilities */
    private static boolean doSiteType = true;

    /** flags for repository capabilities */
    private static boolean doSiteDateRange = true;

    /** flags for repository capabilities */
    private static boolean doResourcePublishDate = true;

    /** flags for repository capabilities */
    private static boolean doResourceFileSize = true;


    /**
     * ctor
     *
     *
     * @param gsacRepository the repository
     */
    public GsacOutputHandler(GsacRepository gsacRepository) {
        this.gsacRepository = gsacRepository;
        doSite              = getRepository().isCapable(HEADER_SITE);
        doResource          = getRepository().isCapable(HEADER_RESOURCE);
        doSiteCode          = getRepository().isCapable(ARG_SITE_CODE);
        doSiteStatus        = getRepository().isCapable(ARG_SITE_STATUS);
        doSiteGroup         = getRepository().isCapable(ARG_SITE_GROUP);
        doSiteType          = getRepository().isCapable(ARG_SITE_TYPE);
        //        doSiteDateRange = getRepository().isCapable(ARG_SITE_TYPE);

        doResourcePublishDate =
            getRepository().isCapable(ARG_RESOURCE_PUBLISHDATE);
        doResourceFileSize = getRepository().isCapable(ARG_RESOURCE_SIZE);

    }


    /**
     * Factory method to make the response object.
     *
     * @param gsacRequest The request
     *
     * @return the response
     */
    public GsacResponse doMakeResponse(GsacRequest gsacRequest) {
        return new GsacResponse(gsacRequest);
    }

    /**
     * _more_
     *
     * @param request The request
     * @param response The response
     * @param sb _more_
     *
     * @throws Exception On badness
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
     *
     * @param type _more_
     * @param gsacRequest The request
     *
     * @throws Exception On badness
     */
    public final void handleRequest(ObjectType type, GsacRequest gsacRequest)
            throws Exception {
        handleRequest(type, gsacRequest, doMakeResponse(gsacRequest));
    }


    /**
     * _more_
     *
     *
     * @param type _more_
     * @param gsacRequest The request
     * @param gsacResponse _more_
     *
     * @throws Exception On badness
     */
    public void handleRequest(ObjectType type, GsacRequest gsacRequest,
                              GsacResponse gsacResponse)
            throws Exception {
        long t1 = System.currentTimeMillis();
        getRepository().processRequest(type, gsacRequest, gsacResponse);
        long t2 = System.currentTimeMillis();
        handleResult(gsacRequest, gsacResponse);
        long t3 = System.currentTimeMillis();
        System.err.println("GSAC.handleRequest time to read:" + (t2 - t1)
                           + " time to encode:" + (t3 - t2) + " #sites:"
                           + gsacResponse.getResources().size());
    }




    /**
     * _more_
     *
     * @param gsacRequest The request
     *
     * @throws Exception On badness
     */
    public void handleBrowseRequest(GsacRequest gsacRequest)
            throws Exception {
        handleBrowseRequest(gsacRequest, doMakeResponse(gsacRequest));
    }

    /**
     * _more_
     *
     * @param gsacRequest The request
     * @param gsacResponse _more_
     *
     * @throws Exception On badness
     */
    public void handleBrowseRequest(GsacRequest gsacRequest,
                                    GsacResponse gsacResponse)
            throws Exception {
        throw new IllegalArgumentException("not implemented");
    }


    /**
     * _more_
     *
     * @param gsacRequest The request
     * @param response The response
     *
     * @throws Exception On badness
     */
    public void handleResult(GsacRequest gsacRequest, GsacResponse response)
            throws Exception {
        throw new IllegalArgumentException(getClass().getName()
                                           + ".handleResult not implemented");
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
        synchronized (dateSdf) {
            return dateSdf.format(date);
        }
    }

    /**
     * _more_
     *
     * @param date _more_
     *
     * @return _more_
     */
    public String formatTime(Date date) {
        synchronized (timeSdf) {
            return timeSdf.format(date);
        }
    }

    /**
     * _more_
     *
     * @param date _more_
     *
     * @return _more_
     */
    public String formatDateTime(Date date) {
        synchronized (dateTimeSdf) {
            return dateTimeSdf.format(date);
        }
    }

    /**
     *  Cut and pasted from GsacRepositoryManager
     *
     * @param request The request
     * @param response The response
     * @param htmlBuff _more_
     */
    public void checkMessage(GsacRequest request, GsacResponse response,
                             Appendable htmlBuff) {
        String message = response.getMessage();
        if (message.length() > 0) {
            try {
                htmlBuff.append(message);
            } catch (Exception exc) {
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
        return getRepository().iconUrl(icon);
    }



    /**
     * _more_
     *
     * @param msg _more_
     *
     * @return _more_
     */
    public String msg(String msg) {
        return getRepository().msg(msg);
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
        return gsacRepository;
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

    /**
     * _more_
     *
     * @param resource _more_
     *
     * @return _more_
     */
    public String makeResourceUrl(GsacFile resource) {
        return makeResourceUrl(HtmlUtil.arg(ARG_RESOURCE_ID,
                                            resource.getId()));
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
            url = getRepository().getAbsoluteUrl(url);
        }
        return url;
    }


    /**
     * _more_
     *
     * @param site _more_
     *
     * @return _more_
     */
    public String formatLatLon(GsacSite site) {
        return formatLatLon(site.getLatitude()) + ","
               + formatLatLon(site.getLongitude()) + ","
               + site.getElevation();
    }


    /**
     * _more_
     *
     * @param site _more_
     *
     * @return _more_
     */
    public String formatDate(GsacSite site) {
        return formatDate(site.getFromDate()) + " - "
               + formatDate(site.getToDate());
    }



    /**
     * get flag
     *
     * @return _more_
     */
    public boolean getDoSite() {
        return doSite;
    }

    /**
     * get flag
     *
     * @return _more_
     */
    public boolean getDoResource() {
        return doResource;
    }

    /**
     * get flag
     *
     * @return _more_
     */
    public boolean getDoResourcePublishDate() {
        return doResourcePublishDate;
    }

    /**
     * _more_
     *
     * @return get flag
     */
    public boolean getDoResourceFileSize() {
        return doResourceFileSize;
    }

    /**
     * get flag
     *
     * @return _more_
     */
    public boolean getDoSiteCode() {
        return doSiteCode;
    }

    /**
     * get flag
     *
     * @return _more_
     */
    public boolean getDoSiteStatus() {
        return doSiteStatus;
    }

    /**
     * get flag
     *
     * @return _more_
     */
    public boolean getDoSiteGroup() {
        return doSiteGroup;
    }

    /**
     * get flag
     *
     * @return _more_
     */
    public boolean getDoSiteType() {
        return doSiteType;
    }





}
