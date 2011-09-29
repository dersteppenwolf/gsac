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



/**
 * Defines the core gsac api url arguments
 *
 *
 * @author  Jeff McWhirter mcwhirter@unavco.org
 */
public interface GsacArgs {

    /** _more_ */
    public static final String ARG_UNDEFINED_VALUE = "";

    /** _more_ */
    public static final String ARG_UNDEFINED_LABEL = "-Any-";

    /** _more_ */
    public static final String ARG_SORT_VALUE_SUFFIX = "sortvalue";

    /** _more_ */
    public static final String ARG_SORT_ORDER_SUFFIX = "sortorder";

    /** _more_ */
    public static final String ARG_RESOURCE_PREFIX = "resource.";

    /** _more_ */
    public static final String ARG_RESOURCE_CLASS = ARG_RESOURCE_PREFIX
                                                    + "class";

    /** _more_ */
    public static final String ARG_SITE_PREFIX = "site.";

    /** _more_ */
    public static final String ARG_SITE_ID = ARG_SITE_PREFIX + "id";

    /** _more_ */
    public static final String ARG_SITE_SORT_VALUE = ARG_SITE_PREFIX
                                                     + ARG_SORT_VALUE_SUFFIX;

    /** _more_ */
    public static final String ARG_SITE_SORT_ORDER = ARG_SITE_PREFIX
                                                     + ARG_SORT_ORDER_SUFFIX;


    /** short name or 4 letter code for sites */
    public static final String ARG_SITE_CODE = ARG_SITE_PREFIX + "code";

    /** _more_ */
    public static final String ARG_SITE_DATE = ARG_SITE_PREFIX + "date";

    /** _more_ */
    public static final String ARG_SITE_DATE_FROM = ARG_SITE_DATE + ".from";

    /** _more_ */
    public static final String ARG_SITE_DATE_TO = ARG_SITE_DATE + ".to";



    /** _more_ */
    public static final String ARG_SITE_CREATEDATE = ARG_SITE_PREFIX
                                                     + "createdate";

    /** _more_ */
    public static final String ARG_SITE_CREATEDATE_FROM = ARG_SITE_CREATEDATE
                                                          + ".from";

    /** _more_ */
    public static final String ARG_SITE_CREATEDATE_TO = ARG_SITE_CREATEDATE
                                                        + ".to";


    
    /** _more_ */
    public static final String ARG_SITE_MODIFYDATE = ARG_SITE_PREFIX + "modifydate";

    /** _more_ */
    public static final String ARG_SITE_MODIFYDATE_FROM = ARG_SITE_MODIFYDATE + ".from";

    /** _more_ */
    public static final String ARG_SITE_MODIFYDATE_TO = ARG_SITE_MODIFYDATE + ".to";

    
    

    /** _more_ */
    public static final String ARG_SITE_LOCATION = ARG_SITE_PREFIX
                                                   + "location";

    /** _more_ */
    public static final String ARG_SITE_LATITUDE = ARG_SITE_PREFIX
                                                   + "latitude";

    /** _more_ */
    public static final String ARG_SITE_LONGITUDE = ARG_SITE_PREFIX
                                                    + "longitude";

    /** _more_ */
    public static final String ARG_SITE_ELEVATION = ARG_SITE_PREFIX
                                                    + "elevation";


    /** _more_ */
    public static final String SEARCHTYPE_SUFFIX = ".searchtype";

    /** _more_ */
    public static final String ARG_SITE_CODE_SEARCHTYPE = ARG_SITE_CODE
                                                          + SEARCHTYPE_SUFFIX;

    /** _more_ */
    public static final String ARG_SITE_NAME = ARG_SITE_PREFIX + "name";

    /** _more_ */
    public static final String ARG_SITE_NAME_SEARCHTYPE = ARG_SITE_NAME
                                                          + SEARCHTYPE_SUFFIX;



    /** _more_ */
    public static final String ARG_SITENAME = ARG_SITE_NAME;

    /** _more_ */
    public static final String ARG_SITENAME_SEARCHTYPE =
        ARG_SITE_NAME_SEARCHTYPE;



    /** _more_ */
    public static final String ARG_SITECODE = ARG_SITE_CODE;

    /** _more_ */
    public static final String ARG_SITECODE_SEARCHTYPE =
        ARG_SITE_CODE_SEARCHTYPE;

    /** _more_ */
    public static final String ARG_SUFFIX_GROUP = "group";


    /** _more_ */
    public static final String ARG_SITE_GROUP = ARG_SITE_PREFIX
                                                + ARG_SUFFIX_GROUP;

    /** _more_ */
    public static final String ARG_SITE_TYPE = ARG_SITE_PREFIX + "type";

    /** _more_ */
    public static final String ARG_SITE_STATUS = ARG_SITE_PREFIX + "status";


    /** _more_ */
    public static final String[] SITE_ARGS = { ARG_SITE_CODE, ARG_SITENAME,
            ARG_SITE_TYPE, ARG_SITE_GROUP, ARG_SITE_STATUS };


    /** _more_ */
    public static final String SORT_ORDER_ASCENDING = "ascending";

    /** _more_ */
    public static final String SORT_ORDER_DESCENDING = "descending`";

    /** _more_ */
    public static final String SORT_SITE_CODE = ARG_SITE_CODE;

    /** _more_ */
    public static final String SORT_SITE_NAME = ARG_SITE_NAME;

    /** _more_ */
    public static final String SORT_SITE_TYPE = ARG_SITE_TYPE;


    /** _more_ */
    public static final String ARG_SUFFIX_ID = "id";


    /** _more_ */
    public static final String ARG_FILE_PREFIX = "file.";

    /** _more_ */
    public static final String ARG_FILE_ID = ARG_FILE_PREFIX + ARG_SUFFIX_ID;

    /** _more_ */
    public static final String ARG_FILE_SORT_VALUE = ARG_FILE_PREFIX
                                                     + ARG_SORT_VALUE_SUFFIX;

    /** _more_ */
    public static final String ARG_FILE_SORT_ORDER = ARG_FILE_PREFIX
                                                     + ARG_SORT_ORDER_SUFFIX;


    /** _more_ */
    public static final String SORT_FILE_TYPE = ARG_FILE_PREFIX + "type";

    /** _more_ */
    public static final String SORT_FILE_SIZE = ARG_FILE_PREFIX + "size";

    /** _more_ */
    public static final String SORT_FILE_PUBLISHDATE = ARG_FILE_PREFIX
                                                       + "publishdate";

    /** _more_ */
    public static final String SORT_FILE_DATADATE = ARG_FILE_PREFIX
                                                    + "datadate";



    /** _more_ */
    public static final String ARG_FILE_SIZE = ARG_FILE_PREFIX + "filesize";

    /** _more_ */
    public static final String ARG_FILE_SIZEMAX = ARG_FILE_SIZE + ".max";


    /** file size min */
    public static final String ARG_FILE_SIZEMIN = ARG_FILE_SIZE + ".min";

    /** _more_ */
    public static final String ARG_FILE_FILESIZE = ARG_FILE_PREFIX
                                                   + "filesize";

    /** file size max */
    public static final String ARG_FILESIZE_MAX = ARG_FILE_FILESIZE + ".max";

    /** file size min */
    public static final String ARG_FILESIZE_MIN = ARG_FILE_FILESIZE + ".min";


    /** _more_ */
    public static final String ARG_FILE_PUBLISHDATE = ARG_FILE_PREFIX
                                                      + "publishdate";


    /** publish date for files */
    public static final String ARG_FILE_PUBLISHDATE_FROM =
        ARG_FILE_PUBLISHDATE + ".from";


    /** publish date for files */
    public static final String ARG_FILE_PUBLISHDATE_TO = ARG_FILE_PUBLISHDATE
                                                         + ".to";

    /** publish date for files */
    public static final String ARG_FILE_DATADATE = ARG_FILE_PREFIX
                                                   + "datadate";


    /** publish date for files */
    public static final String ARG_FILE_DATADATE_FROM = ARG_FILE_DATADATE
                                                        + ".from";

    /** data date for files */
    public static final String ARG_FILE_DATADATE_TO = ARG_FILE_DATADATE
                                                      + ".to";

    /** _more_ */
    public static final String ARG_FILE_TYPE = ARG_FILE_PREFIX + "type";



    /** _more_ */
    public static final String ARG_REPOSITORY = "gsac.repository";


    /** _more_ */
    public static final String ARG_BBOX = "bbox";


    /** _more_ */
    public static final String ARG_AREA = ARG_BBOX;

    /** _more_ */
    public static final String ARG_NORTH_SUFFIX = ".north";

    /** _more_ */
    public static final String ARG_SOUTH_SUFFIX = ".south";

    /** _more_ */
    public static final String ARG_EAST_SUFFIX = ".east";

    /** _more_ */
    public static final String ARG_WEST_SUFFIX = ".west";


    /** _more_ */
    public static final String ARG_NORTH = ARG_BBOX + ARG_NORTH_SUFFIX;

    /** _more_ */
    public static final String ARG_SOUTH = ARG_BBOX + ARG_SOUTH_SUFFIX;

    /** _more_ */
    public static final String ARG_EAST = ARG_BBOX + ARG_EAST_SUFFIX;

    /** _more_ */
    public static final String ARG_WEST = ARG_BBOX + ARG_WEST_SUFFIX;


    /** _more_ */
    public static final String ARG_CAPABILITY = "capability";


    /** output type. e.g., site.html, file.csv, etc" */
    public static final String ARG_OUTPUT = "output";

    /** _more_ */
    public static final String ARG_DELIMITER = "delimiter";

    /** _more_ */
    public static final String ARG_PARAMS = "params";

    /** query offset. specifies number of row to skip */
    public static final String ARG_OFFSET = "offset";

    /** query limit. specifies how many results should be returned */
    public static final String ARG_LIMIT = "limit";

    /** _more_ */
    public static final String ARG_DECORATE = "decorate";


    /** _more_ */
    public static final String ARG_GZIP = "gzip";

    /** _more_ */
    public static final String ARG_REMOTEREPOSITORY = "remoterepository";

    /** _more_ */
    public static final String ARG_METADATA_LEVEL = "metadata.level";


    /** _more_ */
    public static final String ARG_SEARCH = "search";

    /** _more_ */
    public static final String ARG_WRAPXML = "wrapxml";


    /** _more_ */
    public static final String SEARCHTYPE_EXACT = "exact";

    /** _more_ */
    public static final String SEARCHTYPE_BEGINSWITH = "beginswith";

    /** _more_ */
    public static final String SEARCHTYPE_ENDSWITH = "endswith";

    /** _more_ */
    public static final String SEARCHTYPE_CONTAINS = "contains";


    /** _more_ */
    public static final String ARG_IP = "ip";



}
