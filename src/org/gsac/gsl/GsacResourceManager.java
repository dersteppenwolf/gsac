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
import org.gsac.gsl.output.*;
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
import java.util.Hashtable;
import java.util.List;


/**
 * Base class for site and file managers
 *
 *
 * @author  Jeff McWhirter
 */
public abstract class GsacResourceManager extends GsacRepositoryManager {

    /** cache */

    private TTLCache<Object, GsacResource> resourceCache =
        new TTLCache<Object, GsacResource>(TTLCache.MS_IN_A_DAY);

    /** _more_ */
    private ResourceClass resourceClass;

    /** _more_          */
    private List<GsacOutput> outputs = new ArrayList<GsacOutput>();

    /** _more_          */
    private Hashtable<String, GsacOutput> outputMap = new Hashtable<String,
                                                          GsacOutput>();


    private CapabilityCollection capabilityCollection;


    private String urlPrefix;

    /**
     * _more_
     *
     * @param repository _more_
     * @param resourceClass _more_
     */
    public GsacResourceManager(GsacRepository repository,
                               ResourceClass resourceClass) {
        super(repository);
        this.resourceClass = resourceClass;
        urlPrefix = getRepository().getUrlBase() + URL_BASE + "/"
            + getResourceClass().getName();

    }

    /**
     * _more_
     *
     * @return _more_
     */
    public List<GsacOutput> getOutputs() {
        return outputs;
    }

    /**
     * _more_
     *
     * @param output _more_
     */
    public void addOutput(GsacOutput output) {
        outputs.add(output);
        outputMap.put(output.getId(), output);
    }

    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    public GsacOutput getOutput(String id) {
        return outputMap.get(id);
    }


    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public GsacOutputHandler getOutputHandler(GsacRequest request) {
        String arg = request.get(ARG_OUTPUT, (String) null);
        for (GsacOutput output : outputs) {
            if (request.defined(output.getId())) {
                arg = output.getId();
                break;
            }
        }
        if (arg == null) {
            //See if we have an output id as a submit button name
            for (GsacOutput output : outputs) {
                if (request.defined(output.getId())) {
                    return output.getOutputHandler();
                }
            }
            arg = outputs.get(0).getId();
        }
        return getOutput(arg).getOutputHandler();
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return super.toString() + " " + getResourceLabel(false);
    }

    /**
     * _more_
     *
     * @param plural _more_
     *
     * @return _more_
     */
    public String getResourceLabel(boolean plural) {
        return (plural
                ? "Resources"
                : "Resource");
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getIdUrlArg() {
        return getResourceClass().getName() + ".id";
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String makeSearchUrl() {
        return makeResourceUrl(URL_SUFFIX_SEARCH);
    }

    public boolean canHandleUri(String uri) {
        return uri.startsWith(urlPrefix);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String makeViewUrl() {
        return makeResourceUrl(URL_SUFFIX_VIEW);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String makeFormUrl() {
        return makeResourceUrl(URL_SUFFIX_FORM);
    }

    /**
     * _more_
     *
     * @param suffix _more_
     *
     * @return _more_
     */
    public String makeResourceUrl(String suffix) {
        System.err.println ("url prefix:" + urlPrefix + " suffix:" + suffix);
        return   urlPrefix + suffix;
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
     * get all of the metadata for the given resource
     *
     *
     * @param level Specifies the depth of metadata that is being requeste - note: this is stupid and will change
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
     * Is  cachable
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
        String resourceClass = resource.getResourceClass().getName();
        resourceCache.put(resourceClass + "_" + key, resource);
    }

    /**
     * retrieve the resource from the cache
     *
     * @param key key
     *
     * @return resource or null
     */
    public GsacResource getCachedResource(String key) {
        return resourceCache.get(key);
    }





    public CapabilityCollection getCapabilityCollection() {
        if(capabilityCollection == null) {
            capabilityCollection = 
                new CapabilityCollection(
                                         getResourceClass(), 
                                         getResourceLabel(false) +  "  Query",
                                         getRepository().getServlet().getAbsoluteUrl(makeSearchUrl()), 
                                         doGetQueryCapabilities());

        }
        return capabilityCollection;
    }



    /**
     * _more_
     *
     * @param capabilities _more_
     */
    public void addDefaultCapabilities(List<Capability> capabilities) {}

    /**
     * Get the extra search capabilities.  Derived classes should override this
     *
     * @return search capabilities
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
        if (this.resourceClass != null) {
            return this.resourceClass.getName().equals(type);
        }
        return false;
    }



    /**
     *  Get the ResourceClass property.
     *
     *  @return The ResourceClass
     */
    public ResourceClass getResourceClass() {
        return resourceClass;
    }


    /**
     * return the list of ResourceGroups. This is only used by the {@link #addDefaultSiteCapabilities}
     *
     * @return list of site groups
     */
    public List<ResourceGroup> doGetResourceGroups() {
        return new ArrayList<ResourceGroup>();
    }


}
