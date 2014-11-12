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

import com.google.gson.*;

import org.gsac.gsl.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.output.*;

import java.io.*;
import java.text.DateFormat;
import java.util.List;
import javax.servlet.*;
import javax.servlet.http.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 *  To format GSAC query results as JSON.
 *  (www.json.org/): "JSON (JavaScript Object Notation) is a lightweight data-interchange format. It is
 *  easy for humans to read and write. It is easy for machines to parse and generate."
 *
 *  This is a basic implementation.  You may add to it.  For more geodesy parameters in GSAC and how to access and format them in Java,
 *  see the other  ---OutputHandler.java files in /gsac/trunk/src/org/gsac/gsl/output/site/.
 *
 * @version        version 1 2012.
 * @author         Jeff McWhirter
 */
public class JsonSiteOutputHandler extends GsacOutputHandler {

    /** output id */
    // name the magic word site.json for use in the GSAC api to request this output format:
    public static final String OUTPUT_SITE_JSON = "site.json";


    /**
     * ctor
     *
     * @param gsacRepository the repository
     * @param resourceClass _more_
     */
    public JsonSiteOutputHandler(GsacRepository gsacRepository,
                                 ResourceClass resourceClass) {
        super(gsacRepository, resourceClass);

        getRepository().addOutput(getResourceClass(), new GsacOutput(this, OUTPUT_SITE_JSON, 
           "GSAC Sites info, JSON", "/sites.json", true));
    }


    public static String getUTCnowString() {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String utcTime = sdf.format(new Date());
        return utcTime;
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
        //long t1 = System.currentTimeMillis();
        response.startResponse(GsacResponse.MIME_JSON);
        PrintWriter pw          = response.getPrintWriter();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.setDateFormat(DateFormat.LONG);
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE);
        gsonBuilder.serializeSpecialFloatingPointValues();
        Gson           gson  = gsonBuilder.create();
        List<GsacSite> sites = response.getSites();
        String         json  = gson.toJson(sites);
        pw.print(json);
        response.endResponse();

        //long t2 = System.currentTimeMillis();
        //System.err.println("GSAC:     finished making output (json site info file) for " + sites.size()+" sites, in "+ (t2-t1)+" ms" ); // DEBUG
    }


}
