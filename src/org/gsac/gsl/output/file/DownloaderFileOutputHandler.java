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

package org.gsac.gsl.output.file;



import org.gsac.gsl.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.output.*;
import org.gsac.gsl.util.*;



import ucar.unidata.util.IOUtil;

import java.io.*;




/**
 * Class description
 *
 *
 * @version        Enter version here..., Wed, May 19, '10
 * @author         Enter your name here...
 */
public class DownloaderFileOutputHandler extends GsacOutputHandler {

    /** output id */
    public static final String OUTPUT_FILE_DOWNLOAD = "file.download";


    /**
     * ctor
     *
     * @param gsacRepository servlet
     * @param resourceClass _more_
     */
    public DownloaderFileOutputHandler(GsacRepository gsacRepository,
                                       ResourceClass resourceClass) {
        super(gsacRepository, resourceClass);
        GsacOutput output;
        getRepository().addOutput(getResourceClass(),
                                  output = new GsacOutput(this,
                                      OUTPUT_FILE_DOWNLOAD,
                                      "Download Files via Webstart",
                                      "/files.jnlp", true, "Download Files"));

    }


    /**
     * handle request
     *
     *
     *
     * @param type _more_
     * @param request the request
     * @param response the response
     *
     * @throws Exception on badness
     */
    public void handleRequest(ResourceClass type, GsacRequest request,
                              GsacResponse response)
            throws Exception {
        String path = request.getRequestURI();
        //If the path does not end with .jnlp then send a redirect

        //        System.err.println("path:" + path);
        if ( !path.endsWith(".jnlp")) {
            String redirectUrl = path + "/files.jnlp" + "?"
                                 + request.getUrlArgs();
            response.sendRedirect(redirectUrl);
            response.endResponse();
            return;
        }

        String codebase = makeUrl(request, URL_FILE_SEARCH);
        String href     = "file.jnlp?" + request.getUrlArgs();
        response.startResponse(GsacResponse.MIME_JNLP);
        InputStream inputStream = getRepository().getResourceInputStream(
                                      "/org/gsac/gsl/resources/gsac.jnlp");
        String      contents   = IOUtil.readContents(inputStream);
        GsacRequest newRequest = new GsacRequest(request);
        newRequest.put(ARG_OUTPUT, UrlFileOutputHandler.OUTPUT_FILE_URL);
        newRequest.remove(OUTPUT_FILE_DOWNLOAD);
        String dataUrl = makeUrl(request,
                                 URL_FILE_SEARCH + "?"
                                 + newRequest.getUrlArgs());
        String fullUrlRoot = getRepository().getAbsoluteUrl(request,
                                 getRepository().getUrlBase() + URL_BASE);

        //Do this a couple of times
        contents = contents.replace("${fullurlroot}", fullUrlRoot);
        contents = contents.replace("${fullurlroot}", fullUrlRoot);
        contents = contents.replace("${fullurlroot}", fullUrlRoot);
        contents = contents.replace("${resourceurl}", dataUrl);

        contents = contents.replace("${codebase}", codebase);
        contents = contents.replace("${href}", href);

        System.err.println("jnlp file:" + contents);

        PrintWriter pw = response.getPrintWriter();
        pw.append(contents);
        response.endResponse();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean shouldUrlsBeAbsolute() {
        return true;
    }

}
