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
public class FlexigridSiteOutputHandler extends HtmlOutputHandler {

    /** output id */
    public static final String OUTPUT_SITE_FLEXIGRID = "site.flexigrid";

    public static final String TAG_ROWS = "rows";
    public static final String TAG_PAGE = "page";
    public static final String TAG_TOTAL = "total";
    public static final String TAG_ROW = "row";
    public static final String TAG_CELL = "cell";

    public static final String ATTR_ID = "id";




    /**
     * _more_
     *
     * @param gsacServlet _more_
     */
    public FlexigridSiteOutputHandler(GsacRepository gsacServlet) {
        super(gsacServlet);
        getRepository().addOutput(OUTPUT_GROUP_SITE, new GsacOutput(this, OUTPUT_SITE_FLEXIGRID,
                "Google Earth FLEXIGRID", false));
    }



    public Element makeCell(Element row, String content) throws Exception {
        Element cell = XmlUtil.create(TAG_CELL, row);
        cell.appendChild(XmlUtil.makeCDataNode(row.getOwnerDocument(), content,false));
        return cell;
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
        StringBuffer sb = new StringBuffer();
        response.startResponse("text/xml");
        getRepository().handleSiteRequest(request, response);
        List<GsacSite>             sites    = response.getSites();
        PrintWriter pw     = response.getPrintWriter();
        Element     root   = XmlUtil.getRoot(XmlUtil.tag(TAG_ROWS,"",""));
        Document doc = root.getOwnerDocument();
        XmlUtil.create(TAG_PAGE, root, "1");
        XmlUtil.create(TAG_TOTAL, root, ""+ sites.size());
        for (GsacSite site : sites) {
            Element row = XmlUtil.create(doc, TAG_ROW, root,new String[]{ATTR_ID, site.getSiteId()});
            String       href = makeSiteHref(site);
            makeCell(row, href);
            makeCell(row, site.getName());
            makeCell(row, site.getType().getName());
            makeCell(row, formatLatLon(site));
            makeCell(row, formatDate(site));
            if (getDoSiteGroup()) {
                List<SiteGroup> groups       = site.getSiteGroups();
                makeCell(row, getGroupHtml(groups, false));
            }
            /*
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
            */

        }

        XmlUtil.toString(root, pw);
        response.endResponse();
    }




}