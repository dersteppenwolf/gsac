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


import ucar.unidata.util.TwoFacedObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;


/**
 */
public class IdLabel implements Comparable, NamedThing {

    /** _more_ */
    private String id;

    /** _more_ */
    private String label;

    /**
     * _more_
     */
    public IdLabel() {}

    /**
     * _more_
     *
     * @param that _more_
     */
    public IdLabel(IdLabel that) {
        this(that.id, that.label);
    }

    /**
     * _more_
     *
     * @param id _more_
     */
    public IdLabel(String id) {
        this(id, id);
    }


    /**
     * _more_
     *
     * @param id _more_
     * @param label _more_
     */
    public IdLabel(String id, String label) {
        this.id    = id;
        this.label = label;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean labelSameAsId() {
        return label.equals(id);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return getId() + ":" + getName();
    }

    /**
     * _more_
     *
     * @param o _more_
     *
     * @return _more_
     */
    public boolean equals(Object o) {
        if ( !this.getClass().equals(o.getClass())) {
            return false;
        }
        IdLabel that = (IdLabel) o;
        return this.id.equals(that.id);
    }


    /**
     * _more_
     *
     * @param o _more_
     *
     * @return _more_
     */
    public int compareTo(Object o) {
        if ( !(o instanceof IdLabel)) {
            throw new IllegalArgumentException("Cannot compare to:"
                    + o.getClass().getName());
        }
        IdLabel that = (IdLabel) o;
        return this.label.toLowerCase().compareTo(that.label.toLowerCase());
    }


    /**
     * _more_
     *
     * @param list _more_
     * @param value _more_
     *
     * @return _more_
     */
    public static boolean contains(List<IdLabel> list, String value) {
        for (IdLabel idLabel : list) {
            if (idLabel.getId().equals(value)) {
                return true;
            }
        }
        return false;
    }


    /**
     * _more_
     *
     * @param names _more_
     *
     * @return _more_
     */
    public static List<IdLabel> toList(String[] names) {
        List<IdLabel> list = new ArrayList<IdLabel>();
        for (String name : names) {
            if (name == null) {
                continue;
            }
            list.add(new IdLabel(name, name));
        }
        return list;
    }


    /**
     * _more_
     *
     * @param values _more_
     *
     * @return _more_
     */
    public static List<IdLabel> toList(List values) {
        List<IdLabel> list = new ArrayList<IdLabel>();
        for (Object object : values) {
            list.add((IdLabel) object);
        }
        return list;
    }


    /**
     * _more_
     *
     * @param idsAndNames _more_
     *
     * @return _more_
     */
    public static List<IdLabel> toList(String[][] idsAndNames) {
        List<IdLabel> list = new ArrayList<IdLabel>();
        for (String[] tuple : idsAndNames) {
            if ((tuple[0] == null) || (tuple[1] == null)) {
                continue;
            }
            list.add(new IdLabel(tuple[0], ((tuple.length > 1)
                                            ? tuple[1]
                                            : tuple[0])));
        }
        return list;
    }




    /**
     * _more_
     *
     * @param ids _more_
     *
     * @return _more_
     */
    public static Hashtable<String, String> toMap(List<IdLabel> ids) {
        Hashtable<String, String> map = new Hashtable<String, String>();
        for (IdLabel id : ids) {
            map.put(id.getId(), id.getName());
        }
        return map;
    }

    /**
     * _more_
     *
     * @param ids _more_
     *
     * @return _more_
     */
    public static HashSet<String> toSet(List<IdLabel> ids) {
        HashSet<String> map = new HashSet<String>();
        for (IdLabel id : ids) {
            map.add(id.getId());
        }
        return map;
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
     * _more_
     *
     * @return _more_
     */
    public String getName() {
        return label;
    }

}
