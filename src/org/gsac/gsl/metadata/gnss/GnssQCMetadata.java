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
import org.gsac.gsl.metadata.*;

import org.gsac.gsl.GsacRequest;
import org.gsac.gsl.output.HtmlOutputHandler;
import org.gsac.gsl.util.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Metadata class to hold qc data from unavco
 *
 */
public class GnssQCMetadata extends GsacMetadata {

    /** _more_ */
    public static final String TYPE_GNSS_QC = "gnss.qc";

    /**
     * _more_
     */
    public GnssQCMetadata() {
        super(TYPE_GNSS_QC);
    }



    public void addHtml(GsacRequest request, 
			HtmlOutputHandler outputHandler,
			Appendable pw) 
	throws IOException {
	//NOOP
	//Assume this is in a  2 column table. First column is
	//the label. Second is the html content.
	//Use outputHandler.formEntry (or formEntryTop)
	//because this adds to the table or if the request is
	//from an iphone then it handles the layout differently
	pw.append(outputHandler.formEntry(request,
					  outputHandler.msgLabel("Label"),
					  "html goes here"));
    }










}
