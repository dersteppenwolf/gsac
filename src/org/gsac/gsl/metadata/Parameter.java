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

package org.gsac.gsl.metadata;




/**
 * Holds an array of doubles plus String names
 */
public class Parameter {

    /** _more_ */
    private String id;

    /** _more_ */
    private String name;

    /** _more_ */
    private String unit;



    /**
     * _more_
     *
     * @param values _more_
     * @param names _more_
     *
     * @param id _more_
     * @param name _more_
     * @param unit _more_
     */
    public Parameter(String id, String name, String unit) {
        this.id   = id;
        this.name = name;
        this.unit = unit;
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
     *  Set the Name property.
     *
     *  @param value The new value for Name
     */
    public void setName(String value) {
        name = value;
    }

    /**
     *  Get the Name property.
     *
     *  @return The Name
     */
    public String getName() {
        return name;
    }

    /**
     *  Set the Unit property.
     *
     *  @param value The new value for Unit
     */
    public void setUnit(String value) {
        unit = value;
    }

    /**
     *  Get the Unit property.
     *
     *  @return The Unit
     */
    public String getUnit() {
        return unit;
    }


}
