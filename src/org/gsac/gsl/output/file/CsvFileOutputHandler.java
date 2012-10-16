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

import java.io.*;


import java.util.List;
//import java.util.Date;
//import java.text.SimpleDateFormat;

import javax.servlet.*;
import javax.servlet.http.*;


/**
 * Class description
 *
 *
 * @version        Enter version here..., Wed, May 19, '10
 * @author         Enter your name here...
 */
public class CsvFileOutputHandler extends StreamingOutputHandler {

    /** output id */
    public static final String OUTPUT_FILE_CSV = "file.csv";


    /**
     * _more_
     *
     * @param gsacRepository _more_
     * @param resourceClass _more_
     */
    public CsvFileOutputHandler(GsacRepository gsacRepository,
                                ResourceClass resourceClass) {
        super(gsacRepository, resourceClass);
        getRepository().addOutput(getResourceClass(),
                                  new GsacOutput(this, OUTPUT_FILE_CSV,
                                      "File CSV", "/files.csv", true));
    }


    /**
     * _more_
     *
     * @param response The response
     * @param resource _more_
     */
    public void processResource(GsacResponse response,
                                GsacResource resource) {
        try {
            GsacFile file = (GsacFile) resource;
            FileInfo fi   = file.getFileInfo();
            //Its OK to do this every time because the response keeps track if it has started already
            boolean firstTime = !response.getHaveInitialized();
            response.startResponse(GsacResponse.MIME_CSV);

            PrintWriter pw = response.getPrintWriter();
            if (firstTime) {
                pw.print("#Id, Type, Md5, FileSize, PublishDate, Url\n");

            }
            String id = file.getId();
            if (getRepository().isRemoteResource(file)) {
                String[] pair = getRepository().decodeRemoteId(id);
                id = pair[0] + ":" + pair[1];
            }
            pw.print(id + ",");
            pw.print(file.getType().getLabel() + ",");
            pw.print(fi.getMd5() + ",");
            pw.print(fi.getFileSize() + ",");
            // TODO: Apparently the publish date does not include mm:ss
            //Date date = file.getPublishDate();
            //SimpleDateFormat sdf = makeDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
            //pw.print( sdf.format(date) + "," );
            pw.print(file.getPublishDate() + ",");
            //List<GsacResource> relatedResources = file.getRelatedResources();
            //if (relatedResources.size() == 1) {
            //    pw.print(relatedResources.get(0).getId());
            //} else {
            //    
            // }
            pw.print(fi.getUrl().replace("\\s+$", "") + "\n");
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }

}
