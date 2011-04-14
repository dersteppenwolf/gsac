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

package org.gsac.gsl.output.resource;



import com.google.gson.*;

import org.gsac.gsl.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.output.*;
import org.gsac.gsl.util.*;

import java.io.*;

import java.lang.reflect.Type;


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
public class JsonResourceOutputHandler extends GsacOutputHandler {

    /** output id */
    public static final String OUTPUT_RESOURCE_JSON = "resource.json";


    /**
     * ctor
     *
     * @param gsacServlet servlet
     */
    public JsonResourceOutputHandler(GsacRepository gsacServlet) {
        super(gsacServlet);
        getRepository().addOutput(OUTPUT_GROUP_RESOURCE,
                                  new GsacOutput(this, OUTPUT_RESOURCE_JSON,
                                      "Resource JSON", "/resources.json",
                                      true));
    }


    /**
     * handle request
     *
     *
     * @param request the request
     * @param response the response
     *
     * @throws Exception on badness
     */
    public void handleResourceResult(GsacRequest request,
                                     GsacResponse response)
            throws Exception {
        response.startResponse(GsacResponse.MIME_JSON);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(GsacSite.class,
                                        new GsonSiteAdapter());
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.setDateFormat(DateFormat.LONG);
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE);
        Gson        gson = gsonBuilder.create();
        PrintWriter pw   = response.getPrintWriter();
        gson.toJson(response.getResources(), pw);
        response.endResponse();
    }

    /**
     * Special serializer for sites
     *
     *
     */
    public class GsonSiteAdapter implements JsonSerializer<GsacSite> {

        /**
         * serialize the object
         *
         * @param src site to serialize
         * @param typeOfSrc type
         * @param context  context
         *
         * @return the json thing
         */
        public JsonElement serialize(GsacSite src, Type typeOfSrc,
                                     JsonSerializationContext context) {
            return new JsonPrimitive(src.getSiteId());
        }
    }

}
