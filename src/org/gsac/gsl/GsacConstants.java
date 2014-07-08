/*
 * Copyright 2010-2014 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
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


import org.gsac.gsl.output.file.HtmlFileOutputHandler;

import org.gsac.gsl.output.site.HtmlSiteOutputHandler;


/**
 * Interface description
 *
 *
 * @version        Enter version here..., Wed, May 19, 2010
 * @author         Jeff McWhirter
 */
public interface GsacConstants extends GsacArgs {

    /** The top-level url path */
    public static final String URL_BASE = "/gsacapi";

    /** _more_ */
    public static final String URL_STATS_BASE = URL_BASE + "/stats";

    /** Url path for site queries */
    public static final String URL_SITE_BASE = URL_BASE + "/site";

    /** _more_ */
    public static final String URL_SUFFIX_SEARCH = "/search";

    /** _more_ */
    public static final String URL_SUFFIX_FORM = "/form";

    /** _more_ */
    public static final String URL_SUFFIX_VIEW = "/view";

    /** _more_ */
    public static final String URL_SITE_SEARCH = URL_SITE_BASE + "/search";

    /** _more_ */
    public static final String URL_SITE_FORM = URL_SITE_BASE + "/form";

    /** _more_ */
    public static final String URL_SITE_VIEW = URL_SITE_BASE + "/view";

    /** Url path for file queries */
    public static final String URL_FILE_BASE = URL_BASE + "/file";

    /** _more_ */
    public static final String URL_FILE_SEARCH = URL_FILE_BASE + "/search";

    /** _more_ */
    public static final String URL_FILE_FORM = URL_FILE_BASE + "/form";

    /** _more_ */
    public static final String URL_FILE_VIEW = URL_FILE_BASE + "/view";

    /** _more_ */
    public static final String URL_BROWSE_BASE = URL_BASE + "/browse";

    /** Url path for repository queries */
    public static final String URL_REPOSITORY_BASE = URL_BASE + "/repository";

    /** _more_ */
    public static final String URL_REPOSITORY_VIEW = URL_REPOSITORY_BASE + "/view";

    /** _more_ */
    public static final String URL_HTDOCS_BASE = URL_BASE + "/htdocs";

    /** _more_ */
    public static final String URL_HELP = URL_BASE + "/help";

    /** _more_ */
    public static final String OUTPUT_XML = "xml";

    /** _more_ */
    public static final String OUTPUT_GSACXML = "gsacxml";

    /** _more_ */
    public static final int DEFAULT_LIMIT = 500;

    /**  Fran says to use 5000. */
    public static final int MAX_LIMIT = 5000;

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
    public static final String HEADER_FILE = "header.file";

    /** _more_ */
    public static final String HEADER_HELP = "header.help";

    /** _more_ */
    public static final String HEADER_INFO = "header.info";

    /** _more_ */
    public static final String RESULT_SITE = "result.site";

    /** _more_ */
    public static final String RESULT_FILE = "result.file";

    /** _more_ */
    public static final String RESULT_BROWSE = "result.browse";

    /** _more_ */
    public static final String OUTPUT_GROUP_BROWSE = "output.browse";

}
