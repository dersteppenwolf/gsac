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

//Note: we should put this in a util package but where?
package org.gsac.gsl.util;


import java.util.Date;
import java.util.Hashtable;



/**
 * Keep the given object in memory for only a time threshold
 *
 * @author     Jeff McWhirter (jeffmc@unavco.org)
 *
 *
 * @param <VALUE>
 */
public class TTLObject<VALUE> {

    /** holds the object */
    private TTLCache<String, VALUE> cache;


    /**
     * ctor
     *
     * @param timeThresholdInMilliseconds time to live
     */
    public TTLObject(long timeThresholdInMilliseconds) {
        this(null, timeThresholdInMilliseconds);
    }

    /**
     * default ctor. 1 hour in cache. No time reset. No size limit
     *
     * @param object object to store
     */
    public TTLObject(VALUE object) {
        this(object, 1000 * 60 * 60);
    }

    /**
     * ctor.
     *
     * @param object object to store
     * @param timeThresholdInMilliseconds time in cache
     */
    public TTLObject(VALUE object, long timeThresholdInMilliseconds) {
        cache = new TTLCache<String, VALUE>(timeThresholdInMilliseconds);
        if (object != null) {
            put(object);
        }
    }

    /**
     * store a new object
     *
     * @param value new object_
     */
    public void put(VALUE value) {
        cache.put("", value);
    }

    /**
     * _more_
     *
     * @param t _more_
     */
    public void setTimeThreshold(long t) {
        cache.setTimeThreshold(t);
    }

    /**
     * get the object or null if its expired
     *
     * @return object
     */
    public VALUE get() {
        return cache.get("");
    }

}
