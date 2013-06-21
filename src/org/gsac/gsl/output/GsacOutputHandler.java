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


    /** http://stackoverflow.com/questions/5054132/how-to-change-the-decimal-separator-of-decimalformat-from-comma-to-dot-point **/
    /** http://stackoverflow.com/questions/4738853/java-decimal-format-parse-to-return-double-value-with-specified-number-of-deci/4739381#4739381 */

    /** formats */
    private DecimalFormat sizeFormat = new DecimalFormat("####0.00");

    /** formats */
    private DecimalFormat latLonFormat = new DecimalFormat("####0.####");

    /** formats */
    private DecimalFormat elevationFormat = new DecimalFormat("####0.0");

    /** the repository */
    private GsacRepository gsacRepository;

    /** flags for repository capabilities */
    private static boolean doResourcePublishDate = true;

    /** flags for repository capabilities */
    private static boolean doResourceFileSize = true;

    /** _more_ */
    private ResourceClass resourceClass;

    /**
     * ctor
     *
     *
     * @param gsacRepository the repository
     */
    public GsacOutputHandler(GsacRepository gsacRepository) {
        this.gsacRepository   = gsacRepository;
        doResourcePublishDate =
            getRepository().isCapable(ARG_FILE_PUBLISHDATE);
        doResourceFileSize = getRepository().isCapable(ARG_FILE_SIZE);
    }


    /**
     * _more_
     *
     * @param gsacRepository _more_
     * @param resourceClass _more_
     */
    public GsacOutputHandler(GsacRepository gsacRepository,
                             ResourceClass resourceClass) {
        this.gsacRepository = gsacRepository;
        this.resourceClass  = resourceClass;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public ResourceClass getResourceClass() {
        return resourceClass;
    }

    /**
     * _more_
     *
     * @param resourceClass _more_
     *
     * @return _more_
     */
    public GsacResourceManager getResourceManager(
            ResourceClass resourceClass) {
        return getRepository().getResourceManager(resourceClass);
    }


    /**
     * _more_
     *
     * @param resource _more_
     *
     * @return _more_
     */
    public GsacResourceManager getResourceManager(GsacResource resource) {
        return getResourceManager(resource.getResourceClass());
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
     *
     * @return _more_
     * @throws Exception On badness
     */
    public final GsacResponse handleRequest(ResourceClass type,
                                            GsacRequest gsacRequest)
            throws Exception {
        GsacResponse response = doMakeResponse(gsacRequest);
        handleRequest(type, gsacRequest, response);

        return response;
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
    public void handleRequest(ResourceClass type, GsacRequest gsacRequest,
                              GsacResponse gsacResponse)
            throws Exception {

        //long t1 = System.currentTimeMillis();

        getRepository().processRequest(type, gsacRequest, gsacResponse);

        // long t2 = System.currentTimeMillis();

        handleResult(gsacRequest, gsacResponse);

        //long t3 = System.currentTimeMillis();
        //System.err.println("GSAC.handleRequest time to read:" + (t2 - t1) + " time to encode:" + (t3 - t2) + 
        //  " #resources:" + gsacResponse.getResources().size());
    }



    /**
     * _more_
     *
     * @param gsacRequest The request
     * @param gsacResponse _more_
     *
     *
     * @return _more_
     * @throws Exception On badness
     */
    public ResourceClass handleRequestBrowse(GsacRequest gsacRequest,
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
        return formatDate(date, null);
    }


    /**
     * _more_
     *
     * @param date _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public String formatDate(Date date, String dflt) {
        if (date == null) {
            return dflt;
        }
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
        return formatDateTime(date, null);
    }


    /**
     * _more_
     *
     * @param date _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public String formatDateTime(Date date, String dflt) {
        if (date == null) {
            return dflt;
        }
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
     *
     * @param resourceClass _more_
     * @param args _more_
     *
     * @return _more_
     */
    public String makeResourceViewUrl(ResourceClass resourceClass,
                                      String args) {
        return getResourceManager(resourceClass).makeViewUrl() + "?" + args;
    }

    /**
     * _more_
     *
     * @param resource _more_
     *
     * @return _more_
     */
    public String makeResourceViewUrl(GsacResource resource) {
        return makeResourceViewUrl(resource.getResourceClass(),
                                   HtmlUtil
                                       .arg(getResourceManager(resource
                                           .getResourceClass())
                                               .getIdUrlArg(), resource
                                                   .getId()));
    }



    /**
     * _more_
     *
     * @param resource _more_
     *
     * @return _more_
     */
    public String makeResourceViewHref(GsacResource resource) {
        String resourceUrl = makeResourceViewUrl(resource);

        return HtmlUtil.href(resourceUrl, resource.getLabel());
    }



    /**
     * _more_
     *
     * @param suffix _more_
     *
     * @return _more_
     */
    public String makeUrl(String suffix) {
        return makeUrl(null, suffix);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param suffix _more_
     *
     * @return _more_
     */
    public String makeUrl(GsacRequest request, String suffix) {
        String url = getRepository().getUrlBase() + suffix;
        if (shouldUrlsBeAbsolute()) {
            url = getRepository().getAbsoluteUrl(request, url);
        }

        return url;
    }


    /**
     * _more_
     *
     *
     * @param resource _more_
     * @return _more_
     */
    public String formatLatLon(GsacResource resource) {
        return formatLatLon(resource.getLatitude()) + ","
               + formatLatLon(resource.getLongitude()) + ","
               + resource.getElevation();
    }

    /**
     * separate values of lat, long, and ellipsoid height with &nbsp; spaces for HTML output.
     * to avoid confusion in Europe where latLonFormat appears to use commas for decimal points.
     *
     * so far, is used only in HTML site page
     *
     * @param resource _more_
     * @return _more_
     */
    public String formatLatLonNoCommas(GsacResource resource) {
        return "latitude "+formatLatLon(resource.getLatitude()) + "  &nbsp; &nbsp; longitude "

               // About ellipsoidal height:
               // use this label:
               //+ formatLatLon(resource.getLongitude()) + " &nbsp; &nbsp; ellipsoid height "
               // or this:
               + formatLatLon(resource.getLongitude()) + " &nbsp; &nbsp; elevation "

               + resource.getElevation();
    }


    /**
     * _more_
     *
     *
     * @param resourcee _more_
     * @return _more_
     */
    public String formatDate(GsacResource resourcee) {
        Date fromDate = resourcee.getFromDate();
        Date toDate   = resourcee.getToDate();
        if ((fromDate != null) && (toDate != null)) {
            return formatDate(fromDate) + " - " + formatDate(toDate);
        } else if (fromDate != null) {
            return formatDate(fromDate);
        } else if (toDate != null) {
            return formatDate(toDate);
        } else {
            return "NA";
        }
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
     * _more_
     *
     * @param tail _more_
     *
     * @return _more_
     */
    public String fileUrl(String tail) {
        return getRepository().getUrlBase() + URL_HTDOCS_BASE + tail;
    }


}
