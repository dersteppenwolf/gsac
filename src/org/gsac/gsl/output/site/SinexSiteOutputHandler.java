/*
 * Copyright 2012 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
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
 * DUMMY for Class making the output format SINEX
 * This version has station.info formatting code in handleresult () which needs to be all rewritten to write SINEX format.
 *
 * @version     Nov 21, 2012  
 * @author      SKW
 */
public class SinexSiteOutputHandler extends GsacOutputHandler {

    /** output id */
    public static final String OUTPUT_SITE_SINEX = "site.sinex";


    /**
     *  class to compose the SINEX formatted result
     *
     * @param gsacRepository _more_
     * @param resourceClass _more_
     */
    public SinexSiteOutputHandler(GsacRepository gsacRepository,
                                 ResourceClass resourceClass) {
        super(gsacRepository, resourceClass);
        getRepository().addOutput(getResourceClass(),
                                  new GsacOutput(this, OUTPUT_SITE_SINEX,
                                      "SINEX", "/sites.sinex", true));
                                    // "SINEX" is a label for the Results choice box in site search form on web page.
                                    // the sites.sinex is used for names of files of results.   
    }


    /**
     *  method to compose the SINEX formatted result
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
                                    : "  ");
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
            /* header lines */
            pw.print("*           \n");
            pw.print("*          \n");
            pw.print("*          \n");
            pw.print("*          \n");

            /* csv file header style:
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
            */

            //     template to see values in draft csv format:       pw.print("#repositoryid, site code, name, latitude, longitude, elevation\n");

            int siteCnt = 0;
            for (GsacSite site : response.getSites()) {
                siteCnt++;
                colCnt = 0;
                for (String param : params) {
                    if (0 == colCnt ) {
                        pw.print(" ");
                    }
                    colCnt++;
                    /* site.getShortName appears to be the same as ARG_SITE_ID)
                    if (param.equals(ARG_SITE_CODE)) {
                        pw.print("sn="+site.getShortName());
                        pw.print(delimiter);
                    }  else   
                    */
                    /*  ARG_SITE_NAME is a long name like "Palmer Station, Antarctica";  is here cut to 16 chars max*/
                    if (param.equals(ARG_SITE_NAME)) {
                        String lnStr = site.getLongName();
                        pw.print(lnStr.substring(0,16));
                        pw.print(delimiter);
                    } 
                    /* next is 4 char ID like PALM */
                    else if (param.equals(ARG_SITE_ID)) {
                        String id = site.getId();
                        if (getRepository().isRemoteResource(site)) {
                            String[] pair =
                                getRepository().decodeRemoteId(id);
                            id = pair[0] + ":" + pair[1];
                        }
                        pw.print(id);
                        pw.print(delimiter);
                    } 

                    /*  lat long info
                     else if (param.equals(ARG_SITE_LATITUDE)) {
                        pw.print(site.getLatitude());
                    } else if (param.equals(ARG_SITE_LONGITUDE)) {
                        pw.print(site.getLongitude());
                    } else if (param.equals(ARG_SITE_ELEVATION)) {
                        pw.print(site.getElevation());
                    } else if (param.equals(ARG_SITE_LOCATION)) {
                        pw.print(site.getLatitude());
                        pw.print(delimiter);
                        pw.print(site.getLongitude());
                        pw.print(delimiter);
                        pw.print(site.getElevation());
                    } 
                    else if (param.equals(ARG_SITE_TYPE)) {
                        pw.print("type="+site.getType().getId());
                        pw.print(delimiter);
                    }
                    */
                     /* don't worry about some param which SINEX format does not use
                      else {
                        throw new IllegalArgumentException(
                            "Unknown parameter:" + param);
                    }
                    */
                }
                /* line return at end of line with all parms for a site */
                pw.print("\n");
            }
        } finally {
            response.endResponse();
        }
    }


    /**
     *  get rid of commas in field values, for csv formatting which gives comma special meaning as delimiter
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
