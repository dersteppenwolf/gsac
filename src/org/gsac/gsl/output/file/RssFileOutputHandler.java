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

package org.gsac.gsl.output.file;




import org.gsac.gsl.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.output.*;
import org.gsac.gsl.util.*;

import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;

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
public class RssFileOutputHandler extends GsacOutputHandler {

    /** output id */
    public static final String OUTPUT_FILE_RSS = "file.gsacrss";


    /** _more_ */
    public static final String ATTR_RSS_VERSION = "version";

    /** _more_ */
    public static final String TAG_RSS_RSS = "rss";

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
        new SimpleDateFormat("EEE dd, MMM yyyy HH:mm:ss Z");


    /**
     * ctor
     *
     * @param gsacServlet the servlet
     */
    public RssFileOutputHandler(GsacRepository gsacServlet) {
        super(gsacServlet);
        getRepository().addOutput(OUTPUT_GROUP_FILE,
                                  new GsacOutput(this, OUTPUT_FILE_RSS,
                                      "File GSAC RSS", "/files.rss",
                                      true));
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
                                  XmlUtil.attrs(ATTR_RSS_VERSION, "2.0")));
        pw.append(XmlUtil.openTag(TAG_RSS_CHANNEL));
        pw.append(XmlUtil.tag(TAG_RSS_TITLE, "",
                              getRepository().getRepositoryName()
                              + " resource results"));
        for (GsacFile resource : response.getFiles()) {
            pw.append(XmlUtil.openTag(TAG_RSS_ITEM));
            if (resource.getPublishTime() != null) {
                pw.append(
                    XmlUtil.tag(
                        TAG_RSS_PUBDATE, "",
                        rssSdf.format(resource.getPublishTime())));
            }
            String title =
                resource.getType() + " - "
                + IOUtil.getFileTail(resource.getFileInfo().getUrl());
            pw.append(XmlUtil.tag(TAG_RSS_TITLE, "", title));
            String url =
                getRepository().getAbsoluteUrl(makeResourceUrl(resource));
            pw.append(XmlUtil.tag(TAG_RSS_LINK, "", url));
            pw.append(XmlUtil.tag(TAG_RSS_GUID, "", url));

            /*
            pw.append(XmlUtil.openTag(TAG_RSS_DESCRIPTION, ""));
            XmlUtil.appendCdata(
                                pw,
                                entry.getTypeHandler().getEntryContent(
                                                                       entry, request, true, false).toString());
            pw.append(XmlUtil.closeTag(TAG_RSS_DESCRIPTION));
            */
            GsacSite site = resource.getSite();
            if (site != null) {
                EarthLocation el = site.getEarthLocation();
                if (el != null) {
                    pw.append(XmlUtil.tag(TAG_RSS_GEOLAT, "",
                                          "" + el.getLatitude()));
                    pw.append(XmlUtil.tag(TAG_RSS_GEOLON, "",
                                          "" + el.getLongitude()));
                }
                pw.append(XmlUtil.closeTag(TAG_RSS_ITEM));
            }
        }

        pw.append(XmlUtil.closeTag(TAG_RSS_CHANNEL));
        pw.append(XmlUtil.closeTag(TAG_RSS_RSS));
        response.endResponse();
    }


}
