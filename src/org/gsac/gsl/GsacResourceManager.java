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
public abstract class GsacResourceManager extends GsacRepositoryManager {

    /** site cache */

    private TTLCache<Object, GsacResource> resourceCache =
        new TTLCache<Object, GsacResource>(TTLCache.MS_IN_A_DAY);

    /** _more_ */
    private ResourceClass type;

    /**
     * _more_
     *
     * @param repository _more_
     * @param type _more_
     */
    public GsacResourceManager(GsacRepository repository,
                               ResourceClass type) {
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
     * @param gsacResource _more_
     *
     * @throws Exception On badness
     */
    public void doGetMetadata(int level, GsacResource gsacResource)
            throws Exception {
        doGetFullMetadata(gsacResource);
    }



    /**
     * add the full metadata to the resource
     *
     *
     *
     * @param gsacResource _more_
     * @throws Exception On badness
     */
    public void doGetFullMetadata(GsacResource gsacResource)
            throws Exception {}

    /**
     * _more_
     *
     * @param resourceId _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public abstract GsacResource getResource(String resourceId)
     throws Exception;

    /**
     * This method will first look in the local resourceCache for the resource.
     * If not found it calls doGetResource which should be overwritten by derived classes
     *
     * @param resourceId resource id
     *
     * @return The resource or null if not found
     *
     */
    public GsacResource getResourceFromCache(String resourceId) {
        return resourceCache.get(resourceId);
    }


    /**
     * _more_
     */
    public void clearCache() {
        resourceCache = new TTLCache<Object,
                                     GsacResource>(TTLCache.MS_IN_A_DAY);
    }

    /**
     * Are sites cachable
     *
     * @return default true
     */
    public boolean shouldCacheResources() {
        return true;
    }


    /**
     * Put the given resource into the resourceCache
     *
     *
     * @param resource _more_
     */
    public void cacheResource(GsacResource resource) {
        cacheResource(resource.getId(), resource);
    }

    /**
     * Put the given resource into the resourceCache with the given cache key
     *
     * @param key Key to cache with
     * @param resource _more_
     */
    public void cacheResource(String key, GsacResource resource) {
        String type = resource.getResourceClass().getType();
        resourceCache.put(type + "_" + key, resource);
    }

    /**
     * retrieve the site from the cache
     *
     * @param key site key
     *
     * @return site or null
     */
    public GsacResource getCachedResource(String key) {
        return resourceCache.get(key);
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
    public void setType(ResourceClass value) {
        type = value;
    }

    /**
     *  Get the Type property.
     *
     *  @return The Type
     */
    public ResourceClass getType() {
        return type;
    }


}
