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




/**
 * Holds an array of doubles plus String names
 */
public class Tuple {

    /** _more_          */
    private double[] values;

    /** _more_          */
    private String[] names;

    /**
     * _more_
     *
     * @param values _more_
     * @param names _more_
     */
    public Tuple(double[] values, String[] names) {
        this.values = values;
        this.names  = names;
    }

    /**
     *  Set the Values property.
     *
     *  @param value The new value for Values
     */
    public void setValues(double[] value) {
        values = value;
    }

    /**
     *  Get the Values property.
     *
     *  @return The Values
     */
    public double[] getValues() {
        return values;
    }

    /**
     *  Set the Names property.
     *
     *  @param value The new value for Names
     */
    public void setNames(String[] value) {
        names = value;
    }

    /**
     *  Get the Names property.
     *
     *  @return The Names
     */
    public String[] getNames() {
        return names;
    }





}
