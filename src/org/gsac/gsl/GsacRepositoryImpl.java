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

package org.gsac.gsl;


import org.gsac.gsl.database.*;


import org.gsac.gsl.model.*;


import org.gsac.gsl.util.*;

import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;

import java.io.InputStream;

import java.util.ArrayList;
import java.util.Date;

import java.util.Hashtable;
import java.util.List;
import java.util.Properties;


/**
 * Class description
 *
 *
 * @version        $version$, Thu, Oct 28, '10
 * @author         Enter your name here...    
 */
public class GsacRepositoryImpl extends GsacRepository {

    /**
     * _more_
     */
    public GsacRepositoryImpl() {}

    /**
     * Constructor
     *
     * @param servlet the servlet
     */
    public GsacRepositoryImpl(GsacServlet servlet) {
        super(servlet);
    }


}
