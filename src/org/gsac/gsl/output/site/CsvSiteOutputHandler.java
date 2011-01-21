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
import org.gsac.gsl.model.*;


import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;


/**
 * Class description
 *
 *
 * @version        Enter version here..., Wed, May 19, '10
 * @author         Enter your name here...
 */
public class CsvSiteOutputHandler extends GsacOutputHandler {

    /** output id */
    public static final String OUTPUT_SITE_CSV = "site.csv";


    /**
     * _more_
     *
     * @param gsacServlet _more_
     */
    public CsvSiteOutputHandler(GsacServlet gsacServlet) {
        super(gsacServlet);
        getServlet().addSiteOutput(new GsacOutput(this, OUTPUT_SITE_CSV,
                "Site CSV", "/sites.csv", true));
    }


    /**
     * _more_
     *
     * @param gsacRequest _more_
     * @param gsacResponse _more_
     *
     * @param request _more_
     * @param response _more_
     *
     * @throws IOException _more_
     * @throws ServletException _more_
     */
    public void handleSiteResult(GsacRequest request, GsacResponse response)
            throws IOException, ServletException {
        response.startResponse(GsacResponse.MIME_CSV);
        PrintWriter pw = response.getPrintWriter();
        try {
            pw.print(
                "#repositoryid, site code, name, latitude, longitude, elevation\n");
            for (GsacSite site : response.getSites()) {
                pw.print(site.getSiteId());
                pw.print(",");
                pw.print(site.getSiteCode());
                pw.print(",");
                pw.print(site.getName().replace(",", "\\,"));
                pw.print(",");
                pw.print(site.getLatitude());
                pw.print(",");
                pw.print(site.getLongitude());
                pw.print(",");
                pw.print(site.getElevation());
                pw.print("\n");
            }
        } finally {
            response.endResponse();
        }
    }


}
