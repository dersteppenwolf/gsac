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

import ucar.unidata.util.IOUtil;


import java.io.*;

import java.net.URL;
import java.net.URLConnection;

import java.util.HashSet;

import java.util.zip.*;

import javax.servlet.*;
import javax.servlet.http.*;


/**
 * Class description
 *
 *
 * @version        Enter version here..., Wed, May 19, '10
 * @author         Enter your name here...
 */
public class ZipFileOutputHandler extends HtmlOutputHandler {


    /** _more_ */
    public static final String OUTPUT_FILE_ZIP = "file.zip";

    /** _more_ */
    public static final String PROP_MAXSIZE = OUTPUT_FILE_ZIP + ".maxsize";

    /** 100 MB limit */
    public static final long SIZE_THRESHOLD = 1000000 * 100;

    /**
     * _more_
     *
     * @param gsacServlet _more_
     */
    public ZipFileOutputHandler(GsacRepository gsacServlet) {
        super(gsacServlet);
        getRepository().addOutput(OUTPUT_GROUP_FILE,
                                  new GsacOutput(this, OUTPUT_FILE_ZIP,
                                      "Zip Files", "/files.zip", true,
                                      null /*"Zip Files"*/));

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
    public void handleResult(GsacRequest request, GsacResponse response)
            throws Exception {

        //Check the size
        long totalSize = 0;
        long maxSize = getRepository().getProperty(PROP_MAXSIZE,
                           SIZE_THRESHOLD);
        for (GsacFile resource : response.getFiles()) {
            if (resource.getFileInfo().getType() != FileInfo.CLASS_FILE) {
                continue;
            }
            totalSize += resource.getFileInfo().getFileSize();
            if (totalSize > maxSize) {
                StringBuffer sb = new StringBuffer();
                initHtml(request, response, sb);
                sb.append(
                    getRepository().makeErrorDialog(
                        "Requested # bytes &gt; " + SIZE_THRESHOLD));
                finishHtml(request, response, sb);
                return;
            }
        }

        response.startResponse(GsacResponse.MIME_ZIP);
        response.setReturnFilename("gsacresults.zip");
        ZipOutputStream zos = new ZipOutputStream(
                                  new BufferedOutputStream(
                                      request.getOutputStream(), 10000));
        HashSet seen = new HashSet();
        for (GsacFile resource : response.getFiles()) {
            if (resource.getFileInfo().getType() != FileInfo.CLASS_FILE) {
                continue;
            }
            String      urlPath    = resource.getFileInfo().getUrl();
            String      tail       = IOUtil.getFileTail(urlPath);
            InputStream fileStream = null;
            try {
                fileStream = resource.getFileInfo().getInputStream();
            } catch (Exception exc) {
                //Catch if its not a url
                continue;
            }
            if (fileStream == null) {
                continue;
            }
            int    cnt = 1;
            String tmp = tail;
            while (seen.contains(tmp)) {
                tmp = "v" + cnt + "_" + tail;
                cnt++;
            }
            zos.putNextEntry(new ZipEntry("files/" + tmp));
            //            System.err.println("write to:" + tail);
            IOUtil.writeTo(fileStream, zos);
            //            System.err.println("done write to:" + tail);
            zos.closeEntry();
        }
        IOUtil.close(zos);
        response.endResponse();
    }

}
