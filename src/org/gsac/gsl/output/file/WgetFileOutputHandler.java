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


import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;


/**
 * Class description
 *
 *
 * @version        Enter version here..., Wed, May 19, '10
 * @author         Enter your name here...
 */
public class WgetFileOutputHandler extends StreamingOutputHandler {

    /** _more_ */
    public static final String OUTPUT_FILE_WGET = "file.wget";

    /**
     * _more_
     *
     * @param gsacRepository _more_
     */
    public WgetFileOutputHandler(GsacRepository gsacRepository, ResourceClass resourceClass) {
        super(gsacRepository, resourceClass);
        getRepository().addOutput(getResourceClass(),
                                  new GsacOutput(this, OUTPUT_FILE_WGET,
                                      "Wget Script", "/files.sh", true,
                                      "Wget Script"));

    }


    /**
     * _more_
     *
     * @param response The response
     * @param resource _more_
     * @param resource _more_
     */
    public void processResource(GsacResponse response,
                                GsacResource resource) {
        try {
            GsacFile file = (GsacFile) resource;
            //Its OK to do this every time because the response keeps track if it has started already
            boolean firstTime = !response.getHaveInitialized();
            if (firstTime) {
                response.startResponse("application/x-sh");
                response.setReturnFilename("gsacwget.sh");
            }

            PrintWriter pw = response.getPrintWriter();
            pw.print("wget ");
            pw.print(file.getFileInfo().getUrl());
            pw.print("\n");
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }

}
