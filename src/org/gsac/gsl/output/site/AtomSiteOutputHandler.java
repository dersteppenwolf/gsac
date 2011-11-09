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

import org.gsac.gsl.util.AtomUtil;

import org.w3c.dom.*;

import ucar.unidata.xml.XmlUtil;

import java.io.*;


import java.text.DateFormat;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
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
public class AtomSiteOutputHandler extends GsacOutputHandler {

    /** output id */
    public static final String OUTPUT_SITE_ATOM = "site.gsacatom";


    /**
     * ctor
     *
     * @param gsacRepository the repository
     * @param resourceClass _more_
     */
    public AtomSiteOutputHandler(GsacRepository gsacRepository,
                                 ResourceClass resourceClass) {
        super(gsacRepository, resourceClass);
        getRepository().addOutput(getResourceClass(),
                                  new GsacOutput(this, OUTPUT_SITE_ATOM,
                                      "Site GSAC ATOM", "/sites.atom", true));
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
        response.setReturnFilename("sites.xml");
        response.startResponse(GsacResponse.MIME_ATOM);
        PrintWriter pw = response.getPrintWriter();
        pw.append(AtomUtil.openFeed(getRepository().getAbsoluteUrl(request,
                request.getRequestURI())));
        pw.append(AtomUtil.makeTitle(getRepository().getRepositoryName()
                                     + " ATOM Site Feed"));
        pw.append(AtomUtil.makeLink(getRepository().getAbsoluteUrl(request,
                request.toString())));
        for (GsacSite site : response.getSites()) {
            String url = getRepository().getAbsoluteUrl(request,
                             makeResourceViewUrl(site));
            EarthLocation el = site.getEarthLocation();
            /*
            //TODO: add georss
            if(el!=null) {
                pw.append(XmlUtil.tag(TAG_RSS_GEOLAT, "",
                                      "" + el.getLatitude()));
                pw.append(XmlUtil.tag(TAG_RSS_GEOLON, "",
                                      "" + el.getLongitude()));
                                      }*/
            List<AtomUtil.Link> links = new ArrayList<AtomUtil.Link>();
            links.add(new AtomUtil.Link(AtomUtil.REL_ALTERNATE, url));
            pw.append(AtomUtil.makeEntry(site.getShortName(), url,
                                         site.getPublishDate(),
                                         site.getToDate(), site.getLabel(),
                                         site.getShortName(), "gsac",
                                         "http://www.unavco.org", links,
                                         null));
        }
        pw.append(AtomUtil.closeFeed());
        response.endResponse();
    }


}
