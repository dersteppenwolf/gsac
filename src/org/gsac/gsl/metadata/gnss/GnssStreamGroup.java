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

package org.gsac.gsl.metadata.gnss;


import org.gsac.gsl.*;

import org.gsac.gsl.metadata.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.output.HtmlOutputHandler;
import org.gsac.gsl.util.*;

import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.Misc;

import ucar.unidata.util.StringUtil;

import java.io.IOException;

import java.util.Date;
import java.util.List;


/**
 * Holds gnss equipment metadata
 *
 */
public class GnssStreamGroup extends MetadataGroup {


    /**
     * _more_
     */
    public GnssStreamGroup() {}


    /**
     * _more_
     *
     * @param request _more_
     * @param gsacResource _more_
     * @param outputHandler _more_
     * @param pw _more_
     *
     * @return _more_
     *
     * @throws IOException _more_
     */
    public boolean addHtml(GsacRequest request, GsacResource gsacResource,
                           HtmlOutputHandler outputHandler, Appendable pw)
            throws IOException {
        GsacResourceManager resourceManager =
            outputHandler.getResourceManager(gsacResource);

        int          cnt  = 0;
        StringBuffer buff = new StringBuffer(HtmlUtil.formTable());
        for (StreamMetadata streamMetadata :
                StreamMetadata.getStreamMetadata(getMetadata())) {
            cnt++;
            
            streamMetadata.addHtml(request, gsacResource, outputHandler, buff);
        }
        if (cnt > 0) {
            buff.append(HtmlUtil.formTableClose());
            pw.append(outputHandler.formEntryTop(request,
                    outputHandler.msgLabel("Streams"),
                    HtmlUtil.makeShowHideBlock("", buff.toString(), false)));
        }



        return true;
    }



}
