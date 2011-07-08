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

package org.gsac.ramadda;


import org.ramadda.repository.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;


import org.w3c.dom.*;


import ucar.unidata.sql.Clause;
import ucar.unidata.sql.SqlUtil;


import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;

import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;




import java.io.File;



import java.sql.Statement;


import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;


/**
 *
 *
 * @version $Revision: 1.3 $
 */
public class GsacSiteTypeHandler extends GenericTypeHandler {

    /** _more_ */
    public static final String TABLE_GSACSITE = "gsacsite";

    /** _more_ */
    public static final String GSAC_COL_ID = TABLE_GSACSITE + "."
                                             + GenericTypeHandler.COL_ID;

    /** _more_ */
    public static final String GSAC_COL_SITEID = TABLE_GSACSITE + ".siteid";

    /** _more_ */
    public static final String GSAC_COL_STATUS = TABLE_GSACSITE + ".status";

    /** _more_ */
    public static final String GSAC_COL_SOURCE = TABLE_GSACSITE + ".source";

    /** _more_ */
    public static final String GSAC_COL_WHOLESALER = TABLE_GSACSITE
                                                     + ".wholesaler";


    /** _more_ */
    public static final String CLASS_SITE = "gsacsite";

    /** _more_ */
    public static final String COL_SITEID = "siteid";

    /** _more_ */
    public static final String COL_WHOLESALER = "wholesaler";


    /**
     * _more_
     *
     * @param repository _more_
     * @param node _more_
     * @throws Exception _more_
     */
    public GsacSiteTypeHandler(Repository repository, Element node)
            throws Exception {
        super(repository, node);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param parent _more_
     * @param newEntry _more_
     *
     * @throws Exception _more_
     */
    public void initializeEntryFromForm(Request request, Entry entry,
                                        Entry parent, boolean newEntry)
            throws Exception {
        super.initializeEntryFromForm(request, entry, parent, newEntry);
        if ( !newEntry) {
            return;
        }
        //        initializeNewEntry(entry);
    }

    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    public Entry createEntry(String id) {
        return new Entry(id, this, true);
    }


    /**
     * _more_
     *
     * @param siteId _more_
     * @param wholesaler _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry findSiteEntry(String siteId, String wholesaler)
            throws Exception {

        Clause clause = Clause.and(Clause.eq(COL_SITEID, siteId),
                                   Clause.eq(COL_WHOLESALER, wholesaler));
        String[] ids = SqlUtil.readString(
                           getDatabaseManager().getIterator(
                               getDatabaseManager().select(
                                   GenericTypeHandler.COL_ID, TABLE_GSACSITE,
                                   clause)));
        if (ids.length == 0) {
            return null;
        }
        return getRepository().getEntryManager().getEntry(null, ids[0]);
    }

}
