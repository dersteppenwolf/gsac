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

package org.gsac.gsl;


import org.gsac.gsl.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.util.*;


import ucar.unidata.sql.Clause;
import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.Misc;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


/**
 * Base class for site and resource managers
 *
 *
 * @author  Jeff McWhirter
 */
public abstract class GsacObjectManager extends GsacRepositoryManager {

    /** site cache */
    private TTLCache<Object, GsacObject> objectCache =
        new TTLCache<Object, GsacObject>(TTLCache.MS_IN_A_DAY);

    /** _more_          */
    private ObjectType type;

    /**
     * _more_
     *
     * @param repository _more_
     * @param type _more_
     */
    public GsacObjectManager(GsacRepository repository, ObjectType type) {
        super(repository);
        this.type = type;
    }


    /**
     * handle the request
     *
     * @param request The request
     * @param response The response
     *
     * @throws Exception on badness
     */
    public abstract void handleRequest(GsacRequest request,
                                       GsacResponse response)
     throws Exception;


    /**
     * get all of the metadata for the given site
     *
     *
     * @param level Specifies the depth of metadata that is being requeste - note: this is stupid and will change
     * @param gsacSite site
     * @param gsacObject _more_
     *
     * @throws Exception On badness
     */
    public void doGetMetadata(int level, GsacObject gsacObject)
            throws Exception {
        doGetFullMetadata(gsacObject);
    }



    /**
     * add the full metadata to the object
     *
     *
     *
     * @param gsacObject _more_
     * @throws Exception On badness
     */
    public void doGetFullMetadata(GsacObject gsacObject) throws Exception {}

    /**
     * _more_
     *
     * @param objectId _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public abstract GsacObject getObject(String objectId) throws Exception;

    /**
     * This method will first look in the local objectCache for the object.
     * If not found it calls doGetObject which should be overwritten by derived classes
     *
     * @param objectId object id
     *
     * @return The object or null if not found
     *
     */
    public GsacObject getObjectFromCache(String objectId) {
        return objectCache.get(objectId);
    }


    /**
     * _more_
     */
    public void clearCache() {
        objectCache = new TTLCache<Object, GsacObject>(TTLCache.MS_IN_A_DAY);
    }

    /**
     * Are sites cachable
     *
     * @return default true
     */
    public boolean shouldCacheObjects() {
        return true;
    }


    /**
     * Put the given object into the objectCache
     *
     *
     * @param object _more_
     */
    public void cacheObject(GsacObject object) {
        cacheObject(object.getId(), object);
    }

    /**
     * Put the given object into the objectCache with the given cache key
     *
     * @param key Key to cache with
     * @param object _more_
     */
    public void cacheObject(String key, GsacObject object) {
        String type = object.getObjectType().getType();
        objectCache.put(type + "_" + key, object);
    }

    /**
     * retrieve the site from the cache
     *
     * @param key site key
     *
     * @return site or null
     */
    public GsacObject getCachedObject(String key) {
        return objectCache.get(key);
    }




    /**
     * _more_
     *
     * @param capabilities _more_
     */
    public void addDefaultCapabilities(List<Capability> capabilities) {}

    /**
     * Get the extra site search capabilities.  Derived classes should override this
     *
     * @return site search capabilities
     */
    public List<Capability> doGetQueryCapabilities() {
        //default is to do nothing
        return new ArrayList<Capability>();
    }

    /**
     * _more_
     *
     * @param type _more_
     *
     * @return _more_
     */
    public boolean canHandleQueryCapabilities(String type) {
        if (this.type != null) {
            return this.type.getType().equals(type);
        }
        return false;
    }


    /**
     *  Set the Type property.
     *
     *  @param value The new value for Type
     */
    public void setType(ObjectType value) {
        type = value;
    }

    /**
     *  Get the Type property.
     *
     *  @return The Type
     */
    public ObjectType getType() {
        return type;
    }


}
