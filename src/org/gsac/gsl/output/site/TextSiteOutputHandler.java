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

import ucar.unidata.util.StringUtil;

import java.io.*;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.*;
import javax.servlet.http.*;


/**
 * Class description: formats query results to write a csv file format
 *
 * This produces a SHORT report with few values. FB wants to keep (restore) it.   Noted Feb 25 2013.
 *
 * @version        29 Nov 2012 SKW;
 * @author         JM, SKW;
 */
public class TextSiteOutputHandler extends GsacOutputHandler {

    /** output id */
    public static final String OUTPUT_SITE_CSV = "site.csv";

    /**
     * ctor
     *
     * @param gsacRepository _more_
     * @param resourceClass _more_
     */
    public TextSiteOutputHandler(GsacRepository gsacRepository,
                                 ResourceClass resourceClass) {
        super(gsacRepository, resourceClass);
        getRepository().addOutput(getResourceClass(),
                                  new GsacOutput(this, OUTPUT_SITE_CSV,
                                      "Site CSV file", "/sites.csv", true));
    }


    /**
     * _more_
     *
     * @param request The request
     * @param response The response
     *
     * @throws IOException On badness
     * @throws ServletException On badness
     */
    public void handleResult(GsacRequest request, GsacResponse response)
            throws IOException, ServletException {
        response.startResponse(GsacResponse.MIME_CSV);
        PrintWriter  pw          = response.getPrintWriter();
        String       delimiter   = (request.defined(ARG_DELIMITER)
                                    ? request.get(ARG_DELIMITER, " ")
                                    : ",");
        String       paramString = request.get(ARG_PARAMS, (String) null);

        List<String> params      = ((paramString == null)
                                    ? new ArrayList<String>()
                                    : StringUtil.split(paramString, ",", true,
                                        true));
        if (params.size() == 0) {
            params.add(ARG_SITE_ID);
            params.add(ARG_SITE_CODE);
            params.add(ARG_SITE_NAME);
            params.add(ARG_SITE_LATITUDE);
            params.add(ARG_SITE_LONGITUDE);
            params.add(ARG_SITE_ELEVATION);
        }
        try {
            int colCnt = 0;
            for (String param : params) {
                if (colCnt > 0) {
                    pw.print(",");
                } else {
                    pw.print("#");
                }
                colCnt++;
                pw.print(param);
            }
            pw.print("\n");

            //            pw.print("#repositoryid, site code, name, latitude, longitude, elevation\n");
            int siteCnt = 0;
            for (GsacSite site : response.getSites()) {
                siteCnt++;
                colCnt = 0;
                int parmCnt = 0;
                // for each parm in the List
                for (String param : params) {
                    if (colCnt > 0) {
                        pw.print(delimiter);
                    }
                    colCnt++;
                    //System.out.println(" parm #"+ parmCnt +  "   \n");
                    parmCnt++;
                    if (param.equals(ARG_SITE_CODE)) {
                        pw.print(cleanString(site.getShortName(), delimiter));
                        //System.out.println("  1 " + site.getShortName() + "\n");
                    } else if (param.equals(ARG_SITE_NAME)) {
                        pw.print(cleanString(site.getLongName(), delimiter));
                        //System.out.println("  2 " + site.getLongName() + "\n");
                    } else if (param.equals(ARG_SITE_ID)) {
                        String id = site.getId();
                        if (getRepository().isRemoteResource(site)) {
                            String[] pair =
                                getRepository().decodeRemoteId(id);
                            id = pair[0] + ":" + pair[1];
                        }
                        pw.print(cleanString(id, delimiter));
                        //System.out.println("  3 " + id + "\n");
                    } else if (param.equals(ARG_SITE_LATITUDE)) {
                        pw.print(site.getLatitude());
                        //System.out.println("  4 " + site.getLatitude() + "\n");
                    } else if (param.equals(ARG_SITE_LONGITUDE)) {
                        pw.print(site.getLongitude());
                        //System.out.println("  5 " + site.getLongitude() + "\n");
                    } else if (param.equals(ARG_SITE_ELEVATION)) {
                        pw.print(site.getElevation());
                        //System.out.println("  6 " + site.getElevation() + "\n");
                    } else if (param.equals(ARG_SITE_LOCATION)) {
                        pw.print(site.getLatitude());
                        pw.print(delimiter);
                        //System.out.println("  4b " + site.getLatitude() + "\n");
                        pw.print(site.getLongitude());
                        pw.print(delimiter);
                        //System.out.println("  5b " + site.getLongitude() + "\n");
                        pw.print(site.getElevation());
                        //System.out.println("  6b " + site.getElevation() + "\n");
                    } else if (param.equals(ARG_SITE_TYPE)) {
                        pw.print(site.getType().getId());
                        //System.out.println("  7 " + site.getType().getId() + "\n");
                    } else {
                        //System.out.println("  8 unknown  \n");
                        throw new IllegalArgumentException(
                            "Unknown parameter:" + param);
                    }
                }
                pw.print("\n");
            }
        } finally {
            response.endResponse();
        }
    }


    /**
     * _more_
     *
     * @param s _more_
     * @param delimiter _more_
     *
     * @return _more_
     */
    private String cleanString(String s, String delimiter) {
        s = s.replaceAll(",", "_COMMA_");
        s = s.replaceAll(delimiter, "\\" + delimiter);

        return s;
    }


}
