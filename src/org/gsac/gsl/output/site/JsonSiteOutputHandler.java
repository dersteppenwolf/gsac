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

import org.gsac.gsl.metadata.*;
//import org.gsac.gsl.metadata.gnss.*;
//import org.gsac.gsl.util.*;


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

        // this line labels the choice in the Results > Outout formats choice box, and ...?
        getRepository().addOutput(getResourceClass(), new GsacOutput(this, OUTPUT_SITE_JSON, 
           "GSAC Sites info, JSON", "/sites.json", true));
    }


    /* not used
    public static String getUTCnowString() {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String utcTime = sdf.format(new Date());
        return utcTime;
    }
    */

    /**
     *  from the input GSAC 'site' object, extract the value of the named field or API argument.
     *
     * @param site _more_
     * @param propertyId _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    private String getProperty(GsacResource site, String propertyId,
                               String dflt) {
        List<GsacMetadata> propertyMetadata =
            (List<GsacMetadata>) site.findMetadata(
                new GsacMetadata.ClassMetadataFinder(PropertyMetadata.class));
        for (int i = 0; i < propertyMetadata.size(); i++) {
            PropertyMetadata metadata =
                (PropertyMetadata) propertyMetadata.get(i);
            if (metadata.getName().equals(propertyId)) {
                return metadata.getValue();
            }
        }
        return "";
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

        //Get all the sites in the results (response) from the GSAC site query made by the user: 
        String Xstr ;
        String Ystr;
        String Zstr;
        String mondesc;
        String sampIntstr ;
        List<GsacSite> sites = response.getSites();
        // /* how to put  samp int, monu, xyz  values in the site?  
        for (GsacSite site : sites) {
            Xstr       =getProperty(site, GsacExtArgs.SITE_TRF_X, "");
            Ystr       =getProperty(site, GsacExtArgs.SITE_TRF_Y, "");
            Zstr       =getProperty(site, GsacExtArgs.SITE_TRF_Z, "");
            mondesc    =getProperty(site, GsacExtArgs.SITE_METADATA_MONUMENTDESCRIPTION, "");
            sampIntstr =getProperty(site, GsacExtArgs.ARG_SAMPLE_INTERVAL, "");  // station not datafile sample interval
            System.out.println("   Json   sample int="+sampIntstr + "   monum="+mondesc+"   zstr="+Zstr);
            //System.err.println("GSAC: x y z = "+ Xstr +"  "+ Ystr  +"  "+ Zstr );
            //site.earthlocation.addXYZ( - - -  );
        } 

        response.startResponse(GsacResponse.MIME_JSON);
        PrintWriter pw          = response.getPrintWriter();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();

        // original time format for for Java before 1.7:
        //gsonBuilder.setDateFormat(DateFormat.LONG);
        // new 2015:
        // based on SimpleDateFormat sdf8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss" );  
        // note that "yyyy-MM-dd'T'HH:mm:ss.SSS" in SimpleDateFormat makes a time string like 2001-07-04T12:08:56.235 WITHOUT the '' around the T.

        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE);
        gsonBuilder.serializeSpecialFloatingPointValues();
        Gson           gson  = gsonBuilder.create();


        String         json  = gson.toJson(sites);
        pw.print(json);
        response.endResponse();
        //long t2 = System.currentTimeMillis();
        // typically 500 ms for 6 sites System.err.println("GSAC: made the json output file for " + sites.size()+" sites in "+ (t2-t1)+" ms" ); // DEBUG
    }


}
