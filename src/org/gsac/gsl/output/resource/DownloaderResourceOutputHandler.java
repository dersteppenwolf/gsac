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
import org.gsac.gsl.output.*;



import org.gsac.gsl.*;
import org.gsac.gsl.model.*;
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
public class DownloaderResourceOutputHandler extends GsacOutputHandler {

    /** output id */
    public static final String OUTPUT_RESOURCE_DOWNLOAD = "resource.download";


    /**
     * ctor
     *
     * @param gsacServlet servlet
     */
    public DownloaderResourceOutputHandler(GsacRepository gsacServlet) {
        super(gsacServlet);
        getRepository().addResourceOutput(new GsacOutput(this,
                OUTPUT_RESOURCE_DOWNLOAD, "Download Files",
                "/resources.jnlp", true));
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
    public void handleResourceRequest(GsacRequest request,
                                      GsacResponse response)
            throws Exception {
        String path = request.getRequestURI();
        //If the path does not end with .jnlp then send a redirect


        //        System.err.println("path:" + path);
        if (!path.endsWith(".jnlp")) {
            String redirectUrl = path + "/resources.jnlp" + "?"
                                 + request.getUrlArgs();
            response.sendRedirect(redirectUrl);
            response.endResponse();
            return;
        }

        String codebase = makeUrl(URL_RESOURCE_SEARCH);
        String href     = "resources.jnlp?" + request.getUrlArgs();
        //        System.err.println("getting jnlp file");
        response.startResponse(GsacResponse.MIME_JNLP);
        InputStream inputStream = getRepository().getResourceInputStream(
                                      "/org/gsac/gsl/resources/gsac.jnlp");
        String      contents   = IOUtil.readContents(inputStream);
        System.err.println("read:" + contents);
        GsacRequest newRequest = new GsacRequest(request);
        newRequest.put(ARG_OUTPUT,
                       UrlResourceOutputHandler.OUTPUT_RESOURCE_URL);
        String dataUrl = makeUrl(URL_RESOURCE_SEARCH + "?"
                                 + newRequest.getUrlArgs());
        String fullUrlRoot =
            getRepository().getAbsoluteUrl(getRepository().getUrlBase()
                                        + URL_BASE);
        //Do this a couple of times
        contents = contents.replace("${fullurlroot}", fullUrlRoot);
        contents = contents.replace("${fullurlroot}", fullUrlRoot);
        contents = contents.replace("${fullurlroot}", fullUrlRoot);
        contents = contents.replace("${resourceurl}", dataUrl);

        contents = contents.replace("${codebase}", codebase);
        contents = contents.replace("${href}", href);

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
