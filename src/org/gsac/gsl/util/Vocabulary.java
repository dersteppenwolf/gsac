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
import org.gsac.gsl.model.*;

import java.util.ArrayList;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;



/**
 * Class description
 *
 *
 */
public class Vocabulary {

    /** _more_          */
    private String id;

    /** _more_          */
    private List<IdLabel> values = new ArrayList<IdLabel>();

    /** _more_          */
    private Hashtable<String, IdLabel> idMap = new Hashtable<String,
                                                   IdLabel>();

    /** _more_          */
    private Hashtable<String, List<String>> externalToInternal =
        new Hashtable<String, List<String>>();

    /** _more_          */
    private Hashtable<String, String> internalToExternal =
        new Hashtable<String, String>();

    /**
       bean ctor
     */
    public Vocabulary() {
    }


    /**
     * _more_
     *
     * @param id _more_
     * @param values _more_
     * @param externalToInternal _more_
     * @param internalToExternal _more_
     */
    public Vocabulary(String id, List<IdLabel> values,
                      Hashtable<String, List<String>> externalToInternal,
                      Hashtable<String, String> internalToExternal) {
        this.id               = id;
        this.values           = values;
        this.externalToInternal = externalToInternal;
        this.internalToExternal = internalToExternal;
        for (IdLabel value : values) {
            idMap.put(value.getId(), value);
        }
    }


    public boolean hasValues() {
        return values.size()>0;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public List<IdLabel> getValues() {
        return values;
    }

    public static boolean isWildcard(String s) {
        return s.endsWith("*");
    }


    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    public List<String> expandValue(String s) {
        //TODO: Maybe turn the glob pattern into a regexp 
        List<String> result        = new ArrayList<String>();
        boolean      wildcardEnd   = s.endsWith("*");
        boolean      wildcardBegin = s.startsWith("*");

        if ( !wildcardEnd && !wildcardBegin) {
            result.add(s);
            return result;
        }

        if (wildcardEnd) {
            s = s.substring(0, s.length() - 1);
        }
        if (wildcardBegin) {
            s = s.substring(1);
        }

        for (IdLabel value : values) {
            if (wildcardEnd && wildcardBegin) {
                if (value.getId().indexOf(s) >= 0) {
                    result.add(value.getId());
                }
            } else if (wildcardEnd) {
                if (value.getId().startsWith(s)) {
                    result.add(value.getId());
                }
            } else {
                if (value.getId().endsWith(s)) {
                    result.add(value.getId());
                }
            }
        }

        System.err.println("results:" + result);

        return result;

    }

    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    public IdLabel getIdLabel(String id) {
        IdLabel idLabel = idMap.get(id);
        if (idLabel != null) {
            return idLabel;
        }
        return new IdLabel(id);
    }

    /**
     * _more_
     *
     * @param external _more_
     *
     * @return _more_
     */
    public List<String> externalToInternal(String external) {
        List<String> value = externalToInternal.get(external);
        if (value == null) {
            List<String> list = new ArrayList<String>();
            list.add(external);
            return list;
        }
        return value;
    }

    /**
     * _more_
     *
     * @param internal _more_
     *
     * @return _more_
     */
    public String internalToExternal(String internal) {
        String value = internalToExternal.get(internal);
        if (value == null) {
            return internal;
        }
        return value;
    }

    /**
     *  Set the Id property.
     *
     *  @param value The new value for Id
     */
    public void setId(String value) {
        id = value;
    }

    /**
     *  Get the Id property.
     *
     *  @return The Id
     */
    public String getId() {
        return id;
    }


}
