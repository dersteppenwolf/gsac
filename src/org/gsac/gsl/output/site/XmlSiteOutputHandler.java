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

import java.io.*;

import java.text.DateFormat;

import javax.servlet.*;
import javax.servlet.http.*;


/**
 * Class description
 *
 *
 * @version        Enter version here..., Wed, May 19, '10
 * @author         Enter your name here...
 */
public class XmlSiteOutputHandler extends GsacOutputHandler {

    /** output id */
    public static final String OUTPUT_SITE_XML = "site.gsacxml";


    /**
     * ctor
     *
     * @param gsacRepository the repository
     * @param resourceClass _more_
     */
    public XmlSiteOutputHandler(GsacRepository gsacRepository,
                                ResourceClass resourceClass) {
        super(gsacRepository, resourceClass);
        getRepository().addOutput(getResourceClass(),
                                  new GsacOutput(this, OUTPUT_SITE_XML,
                                      "GSAC Site XML", "/gsacsites.xml", true));
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
        response.startResponse(GsacResponse.MIME_XML);
        PrintWriter pw  = response.getPrintWriter();
        // FIX this works for the UNAVCO GSAC but fails for RING GSAC: String      xml = getRepository().encodeObject(response.getSites());
        // see gsl/GsacRepository.java for encodeObject
        String      xml = getRepository().encodeObject(response.getSites());
/*

        try {
                    String      xml = getRepository().encodeObject(response.getSites());
                    System.err.println("   XmlSiteOutputHandler string of sites xml=" + xml); 
        } catch {

        }
*/
        // List<GsacSite> sites = response.getSites();

        pw.print(xml);
        response.endResponse();
    }


}
