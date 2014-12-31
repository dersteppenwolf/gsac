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

package org.gsac.gsl.util;


import org.gsac.gsl.*;

// import org.ramadda.sql.SqlUtil;
import org.gsac.gsl.ramadda.sql.SqlUtil;


import java.util.ArrayList;
import java.util.List;



/**
 * Defines a query capability. Takes an id, query type (e.g., enumeration, boolean, string), a label, etc.
 */
public class SearchInfo {

    /** _more_ */
    private String urlArg;

    /** _more_ */
    private String dbCol;

    /** _more_ */
    private String label;

    /** _more_ */
    private String group;

    /** _more_ */
    private String type = Capability.TYPE_ENUMERATION;


    /**
     * _more_
     *
     * @param urlArg _more_
     * @param dbCol _more_
     * @param label _more_
     * @param type _more_
     * @param group _more_
     */
    public SearchInfo(String urlArg, String dbCol, String label, String type,
                      String group) {
        this.urlArg = urlArg;
        this.dbCol  = dbCol;
        this.label  = label;
        this.group  = group;
        this.type   = type;


    }


    /**
     *  Set the UrlArg property.
     *
     *  @param value The new value for UrlArg
     */
    public void setUrlArg(String value) {
        urlArg = value;
    }

    /**
     *  Get the UrlArg property.
     *
     *  @return The UrlArg
     */
    public String getUrlArg() {
        return urlArg;
    }

    /**
     *  Set the DbCol property.
     *
     *  @param value The new value for DbCol
     */
    public void setDbCol(String value) {
        dbCol = value;
    }

    /**
     *  Get the DbCol property.
     *
     *  @return The DbCol
     */
    public String getDbCol() {
        return dbCol;
    }

    /**
     *  Set the Label property.
     *
     *  @param value The new value for Label
     */
    public void setLabel(String value) {
        label = value;
    }

    /**
     *  Get the Label property.
     *
     *  @return The Label
     */
    public String getLabel() {
        return label;
    }

    /**
     *  Set the Group property.
     *
     *  @param value The new value for Group
     */
    public void setGroup(String value) {
        group = value;
    }

    /**
     *  Get the Group property.
     *
     *  @return The Group
     */
    public String getGroup() {
        return group;
    }

    /**
     *  Set the Type property.
     *
     *  @param value The new value for Type
     */
    public void setType(String value) {
        type = value;
    }

    /**
     *  Get the Type property.
     *
     *  @return The Type
     */
    public String getType() {
        return type;
    }





}
