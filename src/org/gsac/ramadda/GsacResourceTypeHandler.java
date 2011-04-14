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
public class GsacResourceTypeHandler extends GenericTypeHandler {

    /** _more_          */
    public static final String TYPE_STREAM = "gsacstream";



    /**
     * _more_
     *
     * @param repository _more_
     * @param node _more_
     * @throws Exception _more_
     */
    public GsacResourceTypeHandler(Repository repository, Element node)
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
     * @param request _more_
     * @param column _more_
     * @param formBuffer _more_
     * @param entry _more_
     * @param values _more_
     * @param state _more_
     *
     * @throws Exception _more_
     */
    public void addColumnToEntryForm(Request request, Column column,
                                     StringBuffer formBuffer, Entry entry,
                                     Object[] values, Hashtable state)
            throws Exception {
        if ( !column.getName().equals("siteid")) {
            super.addColumnToEntryForm(request, column, formBuffer, entry,
                                       values, state);
            return;
        }

        String id     = column.getFullName();
        String siteId = column.toString(values, column.getOffset());
        if (siteId == null) {
            siteId = "";
        }
        formBuffer.append(HtmlUtil.formEntry(msgLabel(column.getLabel()),
                                             "XX"
                                             + HtmlUtil.input(id, siteId,
                                                 HtmlUtil.SIZE_10)));
    }

    /** _more_          */
    public static final String COL_SITEID = "siteid";

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param column _more_
     * @param tmpSb _more_
     * @param values _more_
     *
     * @throws Exception _more_
     */
    public void formatColumnHtmlValue(Request request, Entry entry,
                                      Column column, StringBuffer tmpSb,
                                      Object[] values)
            throws Exception {
        if (column.getName().equals(COL_SITEID)) {
            String siteId = (String) values[column.getOffset()];
            if ((siteId == null) || (siteId.length() == 0)) {
                tmpSb.append(msg("Undefined"));
                return;
            }
            Entry siteEntry = getEntryManager().getEntry(request, siteId);
            if (siteEntry == null) {
                tmpSb.append(msg("Undefined"));
                return;
            }
            tmpSb.append(
                HtmlUtil.href(
                    request.entryUrl(
                        getRepository().URL_ENTRY_SHOW,
                        siteEntry), siteEntry.getName()));
        }
    }




}
