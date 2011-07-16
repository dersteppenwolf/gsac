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

package org.gsac.gsl.output;


import org.gsac.gsl.*;
import org.gsac.gsl.model.*;


import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;


/**
 * abstract base class for output handlers who want to stream the results.
 * to use this overwrite processResource
 *
 */
public abstract class StreamingOutputHandler extends GsacOutputHandler {

    /**
     * _more_
     *
     * @param gsacRepository _more_
     * @param resourceClass _more_
     */
    public StreamingOutputHandler(GsacRepository gsacRepository,
                                  ResourceClass resourceClass) {
        super(gsacRepository, resourceClass);
    }

    /**
     * _more_
     *
     * @param request The request
     *
     * @return _more_
     */
    public GsacResponse doMakeResponse(GsacRequest request) {
        return new GsacResponse(request) {
            public void handleNewResource(GsacResource resource) {
                processResource(this, resource);
            }
        };
    }

    /**
     * _more_
     *
     * @param response The response
     * @param resource _more_
     */
    public abstract void processResource(GsacResponse response,
                                         GsacResource resource);


    /**
     * _more_
     *
     * @param gsacRequest The request
     * @param gsacResponse _more_
     *
     * @throws IOException On badness
     * @throws ServletException On badness
     */
    public void handleResult(GsacRequest gsacRequest,
                             GsacResponse gsacResponse)
            throws IOException, ServletException {
        gsacResponse.endResponse();
    }


}
