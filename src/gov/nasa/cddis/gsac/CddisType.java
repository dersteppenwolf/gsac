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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Example site manager.
 *
 * @author         Jeff McWhirter
 */
public class CddisType {

    /** _more_ */
    public static final String ID_DELIMITER = "_";

    /** _more_ */
    public static final String ESCDELIM = "ESCDELIM";

    /** _more_ */
    public static final String TYPE_NAME_GNSS = "gnss";

    /** _more_ */
    public static final String TYPE_NAME_SLR = "slr";

    /** _more_ */
    public static final String TYPE_NAME_VLBI = "vlbi";

    /** _more_ */
    public static final String TYPE_NAME_DORIS = "doris";


    /** _more_ */
    private static List<String> TYPE_LIST = new ArrayList<String>();

    /** _more_ */
    public static final CddisType TYPE_GNSS =
        new CddisType(TYPE_NAME_GNSS, Tables.SITE_INFO_GNSS.table,
                      Tables.SITE_INFO_GNSS.COL_MONUMENT_NAME,
                      Tables.SITE_INFO_GNSS.COL_DOMES_NUMBER,
                      Tables.GPS_TRACKING2009.table,
                      new String[] { Tables.GPS_TRACKING2009.COL_START_DATE,
                                     Tables.GPS_TRACKING2009.COL_END_DATE });

    /** _more_ */
    public static final CddisType TYPE_DORIS =
        new CddisType(TYPE_NAME_DORIS, Tables.SITE_INFO_DORIS.table,
                      Tables.SITE_INFO_DORIS.COL_DORIS,
                      Tables.SITE_INFO_DORIS.COL_DOMES_NUMBER,
                      Tables.DORIS_2009.table,
                      new String[] { Tables.DORIS_2009.COL_S_DATE,
            Tables.DORIS_2009.COL_E_DATE });


    /** _more_ */
    public static final CddisType TYPE_SLR =
        new CddisType(TYPE_NAME_SLR, Tables.SITE_INFO_SLR.table,
                      Tables.SITE_INFO_SLR.COL_SLR,
                      Tables.SITE_INFO_SLR.COL_DOMES_NUMBER,
                      Tables.SATELLITESQL_2009.table,
                      new String[] { Tables.SATELLITESQL_2009.COL_S_DATE,
            Tables.SATELLITESQL_2009.COL_E_DATE });


    /** _more_ */
    public static final CddisType TYPE_VLBI =
        new CddisType(TYPE_NAME_VLBI, Tables.SITE_INFO_VLBI.table,
                      Tables.SITE_INFO_VLBI.COL_VLBI_NAME, 
                      Tables.SITE_INFO_VLBI.COL_DOMES_NUMBER,
                      null, null);


    /** This is the list of types that we deal with */
    //J--
    public static final CddisType[] TYPES = { 
        TYPE_GNSS,
        TYPE_DORIS,
        TYPE_SLR,
        TYPE_VLBI};
    //j++


    /** _more_ */
    private String type;

    /** _more_ */
    private Tables siteTable;

    /** _more_ */
    private String siteCodeColumn;

    private String domesNumberColumn;

    private String[] resourceDateColumns;

    /** _more_ */
    private Tables resourceTable;


    /**
     * _more_
     *
     * @param type _more_
     * @param siteTable _more_
     * @param siteCodeColumn _more_
     * @param resourceTable _more_
     */
    public CddisType(String type, Tables siteTable, String siteCodeColumn, 
                     String domesNumberColumn,
                     Tables resourceTable,
                     String[] resourceDateColumns) {
        this.type            = type;
        this.siteTable       = siteTable;
        this.siteCodeColumn  = siteCodeColumn;
        this.domesNumberColumn  = domesNumberColumn;
        this.resourceTable   = resourceTable;
        this.resourceDateColumns = resourceDateColumns;
    }

    /**
     * _more_
     *
     * @param that _more_
     */
    public CddisType(CddisType that) {
        this.type            = that.type;
        this.siteTable       = that.siteTable;
        this.siteCodeColumn  = that.siteCodeColumn;
        this.domesNumberColumn  = that.domesNumberColumn;
        this.resourceTable   = that.resourceTable;
        this.resourceDateColumns = that.resourceDateColumns;
    }


    /**
     * _more_
     *
     * @param type _more_
     *
     * @return _more_
     */
    public static CddisType getType(String type) {
        for (int i = 0; i < CddisType.TYPES.length; i++) {
            if (type.equals(CddisType.TYPES[i].getType())) {
                return new CddisType(CddisType.TYPES[i]);
            }
        }
        return null;
        //        throw new IllegalArgumentException("Unknown type:" + type);
    }

    public boolean isGnss() {
        return isType(TYPE_NAME_GNSS);
    }

    public boolean isDoris() {
        return isType(TYPE_NAME_DORIS);
    }

    public boolean isSlr() {
        return isType(TYPE_NAME_SLR);
    }

    public boolean isVlbi() {
        return isType(TYPE_NAME_VLBI);
    }

    /**
     * _more_
     *
     * @param fields _more_
     *
     * @return _more_
     */
    public String makeId(String[] fields) {
        StringBuffer sb = new StringBuffer();
        sb.append(getType());
        for (String field : fields) {
            if (field == null) {
                continue;
            }
            sb.append(ID_DELIMITER);
            sb.append(cleanNameForId(field));
        }
        return sb.toString();
    }


    /**
     * _more_
     *
     * @param name _more_
     *
     * @return _more_
     */
    public static final String cleanNameForId(String name) {
        name = name.replaceAll(ID_DELIMITER, ESCDELIM);
        return name;
    }


    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public static List<String> getTypeAndFields(String id) throws Exception {
        List<String> tuple  = StringUtil.split(id, ID_DELIMITER, true, true);
        List<String> result = new ArrayList<String>();
        result.add(tuple.get(0));
        for (int i = 1; i < tuple.size(); i++) {
            result.add(tuple.get(i).replaceAll(ESCDELIM, ID_DELIMITER));
        }
        return result;
    }


    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public static List<CddisType> getSiteTypes(GsacRequest request) {
        if(TYPE_LIST.size()==0) {
            for (CddisType type : CddisType.TYPES) {
                TYPE_LIST.add(type.getType());
            }
        }
        List<String> types = (List<String>) request.getDelimiterSeparatedList(
                                 GsacArgs.ARG_SITE_TYPE);
        if (types.size() == 0) {
            types = TYPE_LIST;
        }
        List<CddisType> siteTypes = new ArrayList<CddisType>();
        for (String t : types) {
            CddisType type = CddisType.getType(t);
            if(type!=null) {
                siteTypes.add(type);
            }
        }
        return siteTypes;
    }





    /**
     * _more_
     *
     * @param t _more_
     *
     * @return _more_
     */
    public boolean isType(String t) {
        return this.type.equals(t);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return type;
    }


    /**
     *  Get the Type property.
     *
     *  @return The Type
     */
    public String getType() {
        return type;
    }



    /**
     *  Get the SiteTable property.
     *
     *  @return The SiteTable
     */
    public String getSiteTable() {
        return siteTable.getName();
    }

    /**
     *  Get the SiteCodeColumn property.
     *
     *  @return The SiteCodeColumn
     */
    public String getSiteCodeColumn() {
        return siteCodeColumn;
    }

    public String getDomesNumberColumn() {
        return domesNumberColumn;
    }


    /**
     *  Get the SiteColumns property.
     *
     *  @return The SiteColumns
     */
    public String getSiteColumns() {
        return siteTable.getColumns();
    }


    /**
     *  Get the ResourceTable property.
     *
     *  @return The ResourceTable
     */
    public String getResourceTable() {
        if(resourceTable==null) return null;
        return resourceTable.getName();
    }



    /**
     *  Get the ResourceColumns property.
     *
     *  @return The ResourceColumns
     */
    public String getResourceColumns() {
        if(resourceTable==null) return null;
        return resourceTable.getColumns();
    }

    public String[] getResourceDateColumns() {
        return resourceDateColumns;
    }

}



