/*
 * Copyright 2010-2013 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
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
import org.gsac.gsl.util.*;  // for EarthLocation

import ucar.unidata.util.StringUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.*;
import javax.servlet.http.*;
import java.text.DecimalFormat;


/**
 * Class description: formats query results to write a csv file format, with only name and location.
 *
 * This produces a SHORT report with few values. FB wants to keep it (legacy issue).   Noted Feb 25 2013.
 *
 * 26 feb 2013: output shows huge number of digits in latitude and longitude. Reformatted same.
 * 20 Nov 2013:
 * 1. first column in output removed: in the unavco gsac is such as "13922_AHUP_268"  an internal identifier in the UNAVCO GSAC code, and of no use to any geodesy user
 * or simply repeats second column in prototype gsac
 * 2. replace top header line with standard csv format style.
 *
 * sample of formatted results:
 * old:  
    #site.id,site.code,site.name,site.latitude,site.longitude,site.elevation
    13922_AHUP_268,AHUP,Ahua,19.3791,-155.2661,1104.8471
    17048_AINP_467,AINP,Ainapo,19.3727,-155.458,1568.1289
    18823_ALAL_937,ALAL,Alala Lava Flow,19.3815,-155.5915,3203.6935
    18380_ALEP_678,ALEP,ALEA Permanent,19.5412,-155.6441,2922.0759
    18825_ANIP_938,ANIP,Anipeahi,19.3956,-155.5173,2599.3176 
 * new:
 *
 * 
 *      Note the "elevation" value is in fact  ellipsoid height, depending in the metadata available.
 *
 * @author  Jeff McWhirter
 * @author SK Wier 26 Feb 2013.  Format lat longi height to avoid huge number of non-significant digits. 20 Nov 2013; see above.
 */
public class TextSiteOutputHandler extends GsacOutputHandler {
    String latitude ="";
    String longitude ="";
    String ellipsoidalheight ="";

    /** used to format numerical lat longi values */
    private DecimalFormat latLonFormat = new DecimalFormat("####0.#####");

    /**  to format elevation OR ellipsoidal height values sometimes called elevation in GSAC code.  */
    private DecimalFormat elevationFormat = new DecimalFormat("####0.###");

    /** GSAC output id */
    public static final String OUTPUT_SITE_CSV = "site.csv";

    /**
     * ctor; provides label in GSAC GUI "Output:" box, and output file name extension for this class.
     *
     * @param gsacRepository _more_
     * @param resourceClass _more_
     */
    public TextSiteOutputHandler(GsacRepository gsacRepository,
                                 ResourceClass resourceClass) {
        super(gsacRepository, resourceClass);
        getRepository().addOutput(getResourceClass(), new GsacOutput(this, OUTPUT_SITE_CSV,
                                      "GSAC station info, csv (short)", "/sites.csv", true));
    }


    /**
     * get and format the results to output 
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

        String       delimiter   = (request.defined(ARG_DELIMITER) ? request.get(ARG_DELIMITER, " ") : ",");
        String       paramString = request.get(ARG_PARAMS, (String) null);

        List<String> params      = ((paramString == null)
                                    ? new ArrayList<String>()
                                    : StringUtil.split(paramString, ",", true, true));
        if (params.size() == 0) {
            params.add(ARG_SITE_ID);  // note this is a odd thing like 18825_ANIP_938  in the unavco gsac code
            params.add(ARG_SITE_CODE);
            params.add(ARG_SITE_NAME);
            params.add(ARG_SITE_LATITUDE);
            params.add(ARG_SITE_LONGITUDE);
            params.add(ARG_SITE_ELEVATION);
        }
        try {
            int colCnt = 0;
            // #fields=ID[type='string'],station name[type='string'],latitude,longitude,ellip. height[unit='m']
            String l1= "#fields=ID[type='string'],station name[type='string'],latitude,longitude,ellipsoid height[unit='m']";
            pw.print(l1);
            /* old: 
            for (String param : params) {
                if (colCnt > 1) {
                    pw.print(",");
                } else {
                    pw.print("#");
                }
                colCnt++;
                pw.print(param);
            }
            */
            pw.print("\n");

            int siteCnt = 0;
            for (GsacSite site : response.getSites()) {
                siteCnt++;
                colCnt = 0;
                int parmCnt = 0;

                EarthLocation el = site.getEarthLocation();
                latitude =formatLocation(el.getLatitude())  ;
                longitude =formatLocation(el.getLongitude()) ;
                ellipsoidalheight =elevationFormat.format(el.getElevation()) ;

                // for each parm in the List
                for (String param : params) {
                    if (colCnt > 1) {
                        pw.print(delimiter); // add a ,
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
                        ; /* no longer wanted 20 Nov 2013
                        String id = site.getId();
                        if (getRepository().isRemoteResource(site)) {
                            String[] pair =
                                getRepository().decodeRemoteId(id);
                            id = pair[0] + ":" + pair[1];
                        }
                        pw.print(cleanString(id, delimiter));
                        //System.out.println("  3 " + id + "\n");
                        */
                    } else if (param.equals(ARG_SITE_LATITUDE)) {
                        pw.print(latitude);
                        //System.out.println("  4 " + site.getLatitude() + "\n");
                    } else if (param.equals(ARG_SITE_LONGITUDE)) {
                        pw.print(longitude);
                        //System.out.println("  5 " + site.getLongitude() + "\n");
                    } else if (param.equals(ARG_SITE_ELEVATION)) {
                        pw.print(site.getElevation());
                        //System.out.println("  6 " + site.getElevation() + "\n");
                    } else if (param.equals(ARG_SITE_LOCATION)) {
                        pw.print(latitude);
                        //pw.print(site.getLatitude());
                        pw.print(delimiter);
                        pw.print(longitude);
                        //pw.print(site.getLongitude());
                        pw.print(delimiter);
                        pw.print(ellipsoidalheight);
                        //pw.print(site.getElevation());
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

    /**
     * format a double value (such as lat and longi) to 4 sig figs or as per latLonFormat definition.  0.0001 latitude res is about 1/100 of a km or 10 meters.
     *
     * @param v  a double number
     *
     * @return a String 
     */
    private String formatLocation(double v) {
        v = (double) Math.round(v * 10000) / 10000;
        String s = latLonFormat.format(v);
        return s;
    }

}
