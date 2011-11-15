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

package org.gsac.gsl.output.site;




import org.gsac.gsl.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.output.*;
import org.gsac.gsl.util.*;

import org.w3c.dom.*;

import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.text.DateFormat;

import java.text.SimpleDateFormat;

import javax.servlet.*;
import javax.servlet.http.*;


/**
 * Class description
 *
 *
 * @version        Enter version here..., Wed, May 19, '10
 * @author         Enter your name here...
 */
public class RssSiteOutputHandler extends GsacOutputHandler {

    /** output id */
    public static final String OUTPUT_SITE_RSS = "site.gsacrss";

    /** _more_ */
    public static final String ATTR_RSS_VERSION = "version";

    /** _more_ */
    public static final String TAG_RSS_RSS = "rss";

    /** _more_ */
    public static final String ATTR_GEORSS_NS =
        "http://www.georss.org/georss";

    /** _more */
    public static final String PRFX_GEORSS_NS = "xmlns:georss";

    /** _more_ */
    public static final String TAG_RSS_GEOLAT = "georss:lat";

    /** _more_ */
    public static final String TAG_RSS_GEOLON = "georss:lon";

    /** _more_ */
    public static final String TAG_RSS_GEOBOX = "georss:box";


    /** _more_ */
    public static final String TAG_RSS_LINK = "link";

    /** _more_ */
    public static final String TAG_RSS_GUID = "guid";

    /** _more_ */
    public static final String TAG_RSS_CHANNEL = "channel";

    /** _more_ */
    public static final String TAG_RSS_ITEM = "item";

    /** _more_ */
    public static final String TAG_RSS_TITLE = "title";

    /** _more_ */
    public static final String TAG_RSS_PUBDATE = "pubDate";

    /** _more_ */
    public static final String TAG_RSS_DESCRIPTION = "description";


    /** _more_ */
    SimpleDateFormat rssSdf =
        new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");


    /**
     * ctor
     *
     * @param gsacRepository the repository
     * @param resourceClass _more_
     */
    public RssSiteOutputHandler(GsacRepository gsacRepository,
                                ResourceClass resourceClass) {
        super(gsacRepository, resourceClass);
        getRepository().addOutput(getResourceClass(),
                                  new GsacOutput(this, OUTPUT_SITE_RSS,
                                      "Site GSAC RSS", "/sites.rss", true));
    }


    /**
     * handle the request
     *
     *
     * @param request the request
     * @param response the response
     *
     *
     * @throws Exception on badness
     */
    public void handleResult(GsacRequest request, GsacResponse response)
            throws Exception {
        response.startResponse(GsacResponse.MIME_RSS);
        PrintWriter pw = response.getPrintWriter();

        pw.append(XmlUtil.XML_HEADER + "\n");
        pw.append(XmlUtil.openTag(TAG_RSS_RSS,
                                  XmlUtil.attrs(ATTR_RSS_VERSION, "2.0",
                                      PRFX_GEORSS_NS,
                                      ATTR_GEORSS_NS)) + "\n");
        pw.append(XmlUtil.openTag(TAG_RSS_CHANNEL) + "\n");
        pw.append(XmlUtil.tag(TAG_RSS_TITLE, "",
                              getRepository().getRepositoryName()
                              + " resource results"));
        for (GsacSite site : response.getSites()) {
            pw.append(XmlUtil.openTag(TAG_RSS_ITEM) + "\n");
            if (site.getToDate() != null) {
                pw.append(XmlUtil.tag(TAG_RSS_PUBDATE, "",
                                      rssSdf.format(site.getToDate())));
            }
            String title = site.getLabel();
            pw.append(XmlUtil.tag(TAG_RSS_TITLE, "", title));
            String url = getRepository().getAbsoluteUrl(request,
                             makeResourceViewUrl(site));
            pw.append(XmlUtil.tag(TAG_RSS_LINK, "", url));
            pw.append(XmlUtil.tag(TAG_RSS_GUID, "", url));
            EarthLocation el = site.getEarthLocation();
            if (el != null) {
                pw.append(XmlUtil.tag(TAG_RSS_GEOLAT, "",
                                      "" + el.getLatitude()));
                pw.append(XmlUtil.tag(TAG_RSS_GEOLON, "",
                                      "" + el.getLongitude()));
            }
            pw.append(XmlUtil.closeTag(TAG_RSS_ITEM));
        }
        pw.append(XmlUtil.closeTag(TAG_RSS_CHANNEL));
        pw.append(XmlUtil.closeTag(TAG_RSS_RSS));
        response.endResponse();
    }


}
