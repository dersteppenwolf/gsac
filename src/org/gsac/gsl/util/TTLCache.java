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
 * Supports a cache that holds time limited entries (time to live)
 * Note: this only removes items from the cache when a get is performed and the item has expired
 *
 * @author     Jeff McWhirter (jeffmc@unavco.org)
 *
 * @param <KEY>
 * @param <VALUE>
 */
public class TTLCache<KEY, VALUE> {

    /** helper for ttl */
    public static long MS_IN_A_MINUTE = 1000 * 60;

    /** _more_ */
    public static long MS_IN_AN_HOUR = 1000 * 60 * 60;

    /** helper for ttl */
    public static long MS_IN_A_DAY = MS_IN_AN_HOUR * 24;

    /** the cache */
    private Hashtable<KEY, CacheEntry<VALUE>> cache =
        new Hashtable<KEY, CacheEntry<VALUE>>();

    /** how long should the objects be in the cache */
    private long timeThreshold;

    /** should we update the time when a get is performed */
    private boolean updateTimeOnGet = false;

    /** how big should the cache become until its cleared */
    private int sizeLimit = -1;


    /**
     * default ctor. 1 hour in cache. No time reset. No size limit
     */
    public TTLCache() {
        this(MS_IN_AN_HOUR, -1, false);
    }

    /**
     * ctor. No time reset. No size limit
     *
     *
     * @param timeThresholdInMilliseconds time in cache
     */
    public TTLCache(long timeThresholdInMilliseconds) {
        this(timeThresholdInMilliseconds, -1, false);
    }

    /**
     * ctor. No time reset.
     *
     *
     * @param timeThresholdInMilliseconds time in cache
     * @param sizeLimit cache size limit
     */
    public TTLCache(long timeThresholdInMilliseconds, int sizeLimit) {
        this(timeThresholdInMilliseconds, sizeLimit, false);
    }


    /**
     * ctor. No time reset.
     *
     *
     * @param timeThresholdInMilliseconds time in cache
     * @param sizeLimit cache size limit
     * @param updateTimeOnGet if true then on a get reset the time to current time
     */
    public TTLCache(long timeThresholdInMilliseconds, int sizeLimit,
                    boolean updateTimeOnGet) {
        this.timeThreshold   = timeThresholdInMilliseconds;
        this.sizeLimit       = sizeLimit;
        this.updateTimeOnGet = updateTimeOnGet;
    }

    /**
     * _more_
     *
     * @param t _more_
     */
    public void setTimeThreshold(long t) {
        this.timeThreshold = t;
    }

    /**
     * put the value
     *
     * @param key key
     * @param value value
     */
    public void put(KEY key, VALUE value) {
        if ((sizeLimit > 0) && (cache.size() > sizeLimit)) {
            cache = new Hashtable<KEY, CacheEntry<VALUE>>();
        }
        cache.put(key, new CacheEntry<VALUE>(value));
    }


    /**
     * get the value
     *
     * @param key key
     *
     * @return value or null if not in cache or entry has expired
     */
    public VALUE get(Object key) {
        CacheEntry cacheEntry = cache.get(key);
        if (cacheEntry == null) {
            return null;
        }
        Date now      = new Date();
        long timeDiff = now.getTime() - cacheEntry.time;
        if (timeDiff > timeThreshold) {
            cache.remove(key);
            return null;
        }
        if (updateTimeOnGet) {
            cacheEntry.resetTime();
        }
        return (VALUE) cacheEntry.object;
    }




    /**
     * Class description
     *
     *
     * @author     Jeff McWhirter (jeffmc@unavco.org)
     *
     * @param <VALUE> Type of object
     */
    private class CacheEntry<VALUE> {

        /** time put in cache */
        long time;

        /** the object */
        VALUE object;

        /**
         * ctor
         *
         * @param object the object
         */
        public CacheEntry(VALUE object) {
            this.object = object;
            resetTime();
        }

        /**
         * reset time in cache
         */
        public void resetTime() {
            this.time = new Date().getTime();
        }


    }


}
