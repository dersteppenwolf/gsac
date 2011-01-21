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

package gov.nasa.cddis.gsac;


import org.gsac.gsl.*;
import org.gsac.gsl.metadata.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.util.*;


import ucar.unidata.sql.Clause;
import ucar.unidata.sql.SqlUtil;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;


import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;


/**
 * Class description
 *
 *
 * @version        $version$, Tue, Oct 5, '10
 * @author         Enter your name here...
 */
public class CddisSearchInfo extends SearchInfo {

    /** _more_ */
    private String siteType;

    /**
     * _more_
     *
     * @param siteType _more_
     * @param urlArg _more_
     * @param dbCol _more_
     * @param label _more_
     * @param searchType _more_
     * @param group _more_
     */
    public CddisSearchInfo(String siteType, String urlArg, String dbCol,
                           String label, String searchType, String group) {
        super(urlArg, dbCol, label, searchType, group);
        this.siteType = siteType;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getSiteType() {
        return siteType;
    }

}
