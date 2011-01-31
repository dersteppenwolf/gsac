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


import org.gsac.gsl.output.resource.HtmlResourceOutputHandler;

import org.gsac.gsl.output.site.HtmlSiteOutputHandler;


/**
 * Interface description
 *
 *
 * @version        Enter version here..., Wed, May 19, '10
 * @author         Enter your name here...
 */
public interface GsacConstants extends GsacArgs {






    /** _more_ */
    public static final String PROP_REPOSITORY_CLASS =
        "gsac.repository.class";

    /** _more_ */
    public static final String PROP_BASEURL = "gsac.baseurl";

    /** _more_ */
    public static final String PROP_GSACDIRECTORY = "gsac.directory";

    /** _more_ */
    public static final String PROP_REPOSITORY_NAME = "gsac.repository.name";

    /** _more_ */
    public static final String PROP_REPOSITORY_DESCRIPTION =
        "gsac.repository.description";

    /** The top-level url path */
    public static final String URL_BASE = "/gsacapi";

    /** _more_ */
    public static final String URL_STATS_BASE = URL_BASE + "/stats";


    /** Url path for site queries */
    public static final String URL_SITE_BASE = URL_BASE + "/site";

    /** _more_ */
    public static final String URL_SITE_SEARCH = URL_SITE_BASE + "/search";

    /** _more_ */
    public static final String URL_SITE_FORM = URL_SITE_BASE + "/form";

    /** _more_ */
    public static final String URL_SITE_VIEW = URL_SITE_BASE + "/view";

    /** Url path for resource queries */
    public static final String URL_RESOURCE_BASE = URL_BASE + "/resource";

    /** _more_ */
    public static final String URL_RESOURCE_SEARCH = URL_RESOURCE_BASE
                                                     + "/search";

    /** _more_ */
    public static final String URL_RESOURCE_FORM = URL_RESOURCE_BASE
                                                   + "/form";

    /** _more_ */
    public static final String URL_RESOURCE_VIEW = URL_RESOURCE_BASE
                                                   + "/view";


    /** _more_ */
    public static final String URL_BROWSE_BASE = URL_BASE + "/browse";


    /** Url path for repository queries */
    public static final String URL_REPOSITORY_BASE = URL_BASE + "/repository";


    /** _more_ */
    public static final String URL_REPOSITORY_VIEW = URL_REPOSITORY_BASE
                                                     + "/view";

    /** _more_ */
    public static final String URL_HTDOCS_BASE = URL_BASE + "/htdocs";

    /** _more_ */
    public static final String URL_HELP = URL_BASE + "/help";

    /** _more_ */
    public static final String OUTPUT_SITE_DEFAULT =
        HtmlSiteOutputHandler.OUTPUT_SITE_HTML;

    /** _more_ */
    public static final String OUTPUT_BROWSE_DEFAULT = "browse.default";

    /** _more_ */
    public static final String OUTPUT_RESOURCE_DEFAULT =
        HtmlResourceOutputHandler.OUTPUT_RESOURCE_HTML;

    /** _more_ */
    public static final String OUTPUT_XML = "xml";

    /** _more_ */
    public static final String OUTPUT_GSACXML = "gsacxml";



    /** _more_ */
    public static final int DEFAULT_LIMIT = 1000;

    /** _more_ */
    public static final int MAX_LIMIT = 10000;



    /** _more_ */
    public static final String TAG_CAPABILITIES = "capabilities";

    /** _more_ */
    public static final String TAG_ENUMERATION = "enum";

    /** _more_ */
    public static final String ATTR_TYPE = "type";

    /** _more_ */
    public static final String ATTR_ID = "id";

    /** _more_ */
    public static final String ATTR_LABEL = "label";

    /** _more_ */
    public static final String ATTR_VALUE = "value";

    /** _more_ */
    public static final String HEADER_SITE = "header.site";

    /** _more_ */
    public static final String HEADER_BROWSE = "header.browse";

    /** _more_ */
    public static final String HEADER_RESOURCE = "header.resource";

    /** _more_ */
    public static final String HEADER_HELP = "header.help";

    /** _more_ */
    public static final String HEADER_INFO = "header.info";




    /** _more_ */
    public static final String RESULT_SITE = "result.site";

    /** _more_ */
    public static final String RESULT_RESOURCE = "result.resource";

    /** _more_ */
    public static final String RESULT_BROWSE = "result.browse";

    /** _more_ */
    public static final String[] URL_BASES = { URL_SITE_BASE,
            URL_RESOURCE_BASE, URL_BROWSE_BASE };


    /** _more_ */
    public static final String CAPABILITIES_SITE = "site";

    /** _more_ */
    public static final String CAPABILITIES_RESOURCE = "resource";

}
