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


import org.gsac.gsl.util.*;

import java.util.ArrayList;
import java.util.List;


/**
 * General purpose property metadata. Has a name (id), label and value.
 * Can also have a type
 *
 */
public class PropertyMetadata extends GsacMetadata {

    /** the name */
    private String name;

    /** the value */
    private String value;


    /**
     * _more_
     */
    public PropertyMetadata() {}

    /**
     * _more_
     *
     * @param name _more_
     * @param value _more_
     */
    public PropertyMetadata(String name, String value) {
        this(name, value, null);
    }



    /**
     * ctor
     *
     * @param name the name
     * @param label _more_
     * @param value the value
     */
    public PropertyMetadata(String name, String value, String label) {
        super(TYPE_PROPERTY, label);
        this.name  = name;
        this.value = value;
    }

    /**
     * _more_
     *
     * @param metadata _more_
     *
     * @return _more_
     */
    public static List<PropertyMetadata> getMetadata(
            List<GsacMetadata> metadata) {
        return (List<PropertyMetadata>) findMetadata(metadata,
                PropertyMetadata.class);
    }

    /**
     *  Set the Name property.
     *
     *  @param value The new value for Name
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     *  Get the Name property.
     *
     *  @return The Name
     */
    public String getName() {
        return this.name;
    }


    /**
     *  Set the Value property.
     *
     *  @param value The new value for Value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     *  Get the Value property.
     *
     *  @return The Value
     */
    public String getValue() {
        return this.value;
    }



}
