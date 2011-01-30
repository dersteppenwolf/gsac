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
import org.gsac.gsl.output.*;



import org.gsac.gsl.*;
import org.gsac.gsl.metadata.*;
import org.gsac.gsl.model.*;

import org.w3c.dom.*;

import ucar.unidata.data.gis.KmlUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.TwoFacedObject;

import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.util.ArrayList;

import java.util.Hashtable;
import java.util.List;

import javax.servlet.*;
import javax.servlet.http.*;


/**
 * Class description
 *
 *
 * @version        Enter version here..., Wed, May 19, '10
 * @author         Enter your name here...
 */
public class KmlSiteOutputHandler extends HtmlOutputHandler {

    /** output id */
    public static final String OUTPUT_SITE_KML = "site.kml";

    /**
     * _more_
     *
     * @param gsacServlet _more_
     */
    public KmlSiteOutputHandler(GsacRepository gsacServlet) {
        super(gsacServlet);
        getRepository().addSiteOutput(new GsacOutput(this, OUTPUT_SITE_KML,
                "Google Earth KML", "/sites.kml", true));
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean shouldUrlsBeAbsolute() {
        return true;
    }


    /**
     * _more_
     *
     * @param request the request
     * @param response the response to write to
     *
     *
     * @throws Exception on badness
     */
    public void handleSiteRequest(GsacRequest request, GsacResponse response)
            throws Exception {
        String path = request.getRequestURI();
        //If the path does not end with .kml then send a redirect
        if ( !path.endsWith(".kml")) {
            path = path + "/sites.kml";
            String redirectUrl = path + "?" + request.getUrlArgs();
            response.sendRedirect(redirectUrl);
            response.endResponse();
            return;
        }


        StringBuffer sb = new StringBuffer();
        response.startResponse(GsacResponse.MIME_KML);
        getRepository().handleSiteRequest(request, response);
        PrintWriter pw     = response.getPrintWriter();
        Element     root   = KmlUtil.kml("Site Search");
        Element     doc    = KmlUtil.document(root, "Sites", true);


        Element     folder = doc;
        //        Element folder = KmlUtil.folder(doc, "Site Groups", false);
        List<GsacSite>             sites    = response.getSites();
        Hashtable<String, Element> groupMap = new Hashtable<String,
                                                  Element>();
        Hashtable<String, Element> iconMap = new Hashtable<String, Element>();
        for (GsacSite site : sites) {
            String       href = makeSiteHref(site);
            StringBuffer html = new StringBuffer();
            html.append(href);
            getSiteHtml(request, html, site, false, false, true);
            Element         groupElement = folder;
            List<SiteGroup> groups       = site.getSiteGroups();
            if (groups.size() > 0) {
                SiteGroup firstGroup = groups.get(0);
                groupElement = groupMap.get(firstGroup.getId());
                if (groupElement == null) {
                    groupElement = KmlUtil.folder(folder,
                            firstGroup.getName(), false);
                    groupMap.put(firstGroup.getId(), groupElement);
                }
            }

            String  url          = getIconUrl(site);
            Element styleElement = iconMap.get(url);
            String  styleId;
            if (styleElement == null) {
                styleId      = "style" + iconMap.size();
                styleElement = KmlUtil.iconstyle(doc, styleId, url, 0.5);
                iconMap.put(url, styleElement);
            } else {
                styleId = XmlUtil.getAttribute(styleElement, "id");
            }


            Element placemark = KmlUtil.placemark(groupElement,
                                    site.getLabel(), html.toString(),
                                    site.getLatitude(), site.getLongitude(),
                                    0, styleId);
            KmlUtil.snippet(placemark, site.getName());
        }
        //      System.err.println("xml:" +  XmlUtil.toString(root));
        XmlUtil.toString(root, pw);
        response.endResponse();
    }




}
