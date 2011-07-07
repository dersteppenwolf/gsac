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
import org.gsac.gsl.database.*;
import org.gsac.gsl.metadata.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.output.*;
import org.gsac.gsl.util.*;

import ucar.unidata.sql.Clause;
import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.StringUtil;

import ucar.unidata.xml.XmlUtil;

import java.io.InputStream;

import java.net.URL;
import java.net.URLConnection;



import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;


/**
 * Base class for the site and file managers. It provides some
 * basic facilities like formatting dates, creating the search criteria message, etc.
 *
 *
 * @author  Jeff McWhirter mcwhirter@unavco.org
 */
public abstract class GsacRepositoryManager implements GsacConstants {

    /** The repository */
    private GsacRepository repository;

    /**
     * For enumerated values that have a value and a label (e.g., file type)
     * This holds the mapping of the value to the label. It is used when creating the
     * search criteria message
     */
    private Hashtable<String, Hashtable<String, String>> labelMap =
        new Hashtable<String, Hashtable<String, String>>();


    /**
     * ctor
     *
     * @param repository the repository
     */
    public GsacRepositoryManager(GsacRepository repository) {
        this.repository = repository;
    }

    /**
     * Get the repository
     *
     * @return the repository
     */
    public GsacRepository getRepository() {
        return repository;
    }


    /**
     * get the db manager
     *
     * @return db manager
     */
    public GsacDatabaseManager getDatabaseManager() {
        return repository.getDatabaseManager();
    }


    /**
     * Format the date
     *
     * @param date the date
     *
     * @return formatted date
     */
    public String format(Date date) {
        return "" + date;
    }



    /**
     * Utility to add date range clauses
     *
     * @param clauses list of clauses to add to
     * @param column1 date column 1
     * @param column2 date column 1
     * @param dateRange The date range. Either of the fields may be null
     *
     * @throws Exception On badness
     */
    public void addDateRangeClause(List<Clause> clauses, String column1,
                                   String column2, Date[] dateRange)
            throws Exception {
        if (dateRange[0] != null) {
            clauses.add(Clause.ge(column1, dateRange[0]));
        }

        if (dateRange[1] != null) {
            clauses.add(Clause.le(column2, dateRange[1]));
        }
    }



    /**
     * If the search criteria msgBuff is non 0 length then set the message on the response
     *
     * @param response response
     * @param msgBuff message buff
     */
    public void setSearchCriteriaMessage(GsacResponse response,
                                         StringBuffer msgBuff) {
        if (msgBuff.length() > 0) {
            String message = "<table>" + msgBuff + "</table>";
            response.setQueryInfo(message);
        }
    }


    /**
     * _more_
     *
     * @param request The request
     * @param response The response
     * @param htmlBuff _more_
     */
    public void checkMessage(GsacRequest request, GsacResponse response,
                             Appendable htmlBuff) {
        String message = response.getMessage();
        if (message.length() > 0) {
            try {
                htmlBuff.append(message);
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            }
        }
    }

    /**
     * Add the mapping from key value to label for the given group
     *
     * @param group group
     * @param key key
     * @param label label
     */
    public void addLabel(String group, String key, String label) {
        Hashtable<String, String> map = labelMap.get(group);
        if (map == null) {
            map = new Hashtable<String, String>();
            labelMap.put(group, map);
        }
        map.put(key, label);
    }

    /**
     * Add the mapping from key value to label for the given group
     *
     * @param group group
     * @param labels _more_
     */
    public void addLabels(String group, List<IdLabel> labels) {
        for (IdLabel lbl : labels) {
            addLabel(group, lbl.getId(), lbl.getLabel());
        }
    }

    /**
     * _more_
     *
     * @param group _more_
     * @param labels _more_
     */
    public void addLabels(String group, String[][] labels) {
        for (String[] tuple : labels) {
            addLabel(group, tuple[0], tuple[1]);
        }
    }

    /**
     * Find the label to show the user for the given key value for the group
     *
     * @param group The group of value/label pairs
     * @param key key value
     *
     * @return label to use or key if label not found
     */
    public String getLabel(String group, String key) {
        Hashtable<String, String> map = labelMap.get(group);
        if (map == null) {
            return key;
        }
        String value = map.get(key);
        if (value == null) {
            return key;
        }
        return value;
    }



    /**
     * _more_
     *
     * @param request The request
     * @param clauses _more_
     * @param latCol _more_
     * @param lonCol _more_
     * @param msgBuff _more_
     *
     * @return _more_
     */
    public boolean addBBOXSearchCriteria(GsacRequest request,
                                         List<Clause> clauses, String latCol,
                                         String lonCol,
                                         StringBuffer msgBuff) {

        StringBuffer tmpMsgBuff = new StringBuffer();
        double       value;
        boolean      addedAny = false;
        int          cnt      = 0;
        if (request.defined(ARG_NORTH)) {
            cnt++;
            clauses.add(Clause.le(latCol,
                                  value = request.get(ARG_NORTH, 0.0)));
            appendSearchCriteria(tmpMsgBuff, "north&lt;=", "" + value);
            addedAny = true;
        }
        if (request.defined(ARG_SOUTH)) {
            cnt++;
            clauses.add(Clause.ge(latCol,
                                  value = request.get(ARG_SOUTH, 0.0)));
            appendSearchCriteria(tmpMsgBuff, "south&gt;=", "" + value);
            addedAny = true;
        }
        if (request.defined(ARG_EAST)) {
            cnt++;
            clauses.add(Clause.le(lonCol,
                                  value = request.get(ARG_EAST, 0.0)));
            appendSearchCriteria(tmpMsgBuff, "east&lt;=", "" + value);
            addedAny = true;
        }
        if (request.defined(ARG_WEST)) {
            cnt++;
            clauses.add(Clause.ge(lonCol,
                                  value = request.get(ARG_WEST, 0.0)));
            appendSearchCriteria(tmpMsgBuff, "west&gt;=", "" + value);
            addedAny = true;
        }




        return addedAny;
    }


    /**
     * append the search criteria message  to the buffer
     *
     * @param msgBuff buffer
     * @param label what is being searched for
     * @param list List of values
     *
     */
    public void addSearchCriteria(StringBuffer msgBuff, String label,
                                  List<String> list) {
        addSearchCriteria(msgBuff, label, list, null);
    }


    /**
     * append the search criteria message  to the buffer
     *
     * @param msgBuff buffer
     * @param label what is being searched for
     * @param list List of values
     * @param labelGroup The group name to lookup the label
     */
    public void addSearchCriteria(StringBuffer msgBuff, String label,
                                  List<String> list, String labelGroup) {
        int     cnt  = 0;
        HashSet seen = new HashSet();
        for (String value : list) {
            if (labelGroup != null) {
                value = getLabel(labelGroup, value);
                IdLabel idLabel =
                    getRepository().internalToExternal(labelGroup, value);
                if (idLabel != null) {
                    value = idLabel.getLabel();
                }

            }
            value = value.trim();
            if (seen.contains(value.toLowerCase())) {
                continue;
            }
            seen.add(value.toLowerCase());
            if (cnt == 0) {
                appendSearchCriteria(msgBuff, label + "=", value);
            } else {
                appendSearchCriteria(msgBuff, "", value);
            }
            cnt++;
        }
    }


    /**
     * append the name=value search criteria message
     *
     * @param msgBuff buff to append to
     * @param name search criteria name
     * @param value the value
     */
    public void appendSearchCriteria(StringBuffer msgBuff, String name,
                                     String value) {
        String line = "<tr><td align=right><b>" + name + "</b></td><td>"
                      + value + "</td></tr>\n";
        if (msgBuff.toString().indexOf(line) >= 0) {
            return;
        }
        msgBuff.append(line);
    }



    /**
     * Add the uppercase/lower case string search. This is used for site code and site name
     *
     * @param request The request
     * @param arg url arg
     * @param searchTypeArg search type arg e.g., exact, beginswith, etc
     * @param msgBuff search criteria buffer
     * @param label label for search criteria
     * @param column column to search in
     * @param clauses clauses to add to
     */
    public void addStringSearch(GsacRequest request, String arg,
                                String searchTypeArg, StringBuffer msgBuff,
                                String label, String column,
                                List<Clause> clauses) {

        addStringSearch(request, arg, searchTypeArg, true, msgBuff, label,
                        column, clauses);
    }


    /**
     * _more_
     *
     * @param request The request
     * @param arg _more_
     * @param searchTypeArg _more_
     * @param doUpperLowerCase _more_
     * @param msgBuff _more_
     * @param label _more_
     * @param column _more_
     * @param clauses _more_
     */
    public void addStringSearch(GsacRequest request, String arg,
                                String searchTypeArg,
                                boolean doUpperLowerCase,
                                StringBuffer msgBuff, String label,
                                String column, List<Clause> clauses) {

        if (request.defined(arg)) {
            String searchType = request.get(searchTypeArg, SEARCHTYPE_EXACT);
            List<Clause> valueClauses = new ArrayList<Clause>();
            List<String> values       = (List<String>) (doUpperLowerCase
                    ? request.getUpperAndLowerCaseDelimiterSeparatedList(arg)
                    : request.getDelimiterSeparatedList(arg));
            int     cnt  = 0;
            HashSet seen = new HashSet();
            for (String value : values) {
                if ( !seen.contains(value.toLowerCase())) {
                    appendSearchCriteria(msgBuff, ((cnt++ == 0)
                            ? label + "="
                            : ""), value);
                    seen.add(value.toLowerCase());
                }
                String searchTypeToUse = searchType;
                if (value.startsWith("*") && value.endsWith("*")) {
                    searchTypeToUse = SEARCHTYPE_CONTAINS;
                    value           = value.substring(1, value.length());
                    value           = value.substring(0, value.length() - 1);
                } else if (value.startsWith("*")) {
                    searchTypeToUse = SEARCHTYPE_ENDSWITH;
                    value           = value.substring(1, value.length());
                } else if (value.endsWith("*")) {
                    searchTypeToUse = SEARCHTYPE_BEGINSWITH;
                    value           = value.substring(0, value.length() - 1);
                }
                valueClauses.add(
                    GsacDatabaseManager.getStringSearchClause(
                        searchTypeToUse, column, value));

            }
            if (valueClauses.size() > 0) {
                clauses.add(Clause.or(valueClauses));
            }
        }
    }






    /**
     * _more_
     *
     * @param request The request
     * @param arg _more_
     * @param column _more_
     * @param label _more_
     * @param msgBuff _more_
     * @param clauses _more_
     */
    public void addEnumeratedSearch(GsacRequest request, String arg,
                                    String column, String label,
                                    StringBuffer msgBuff,
                                    List<Clause> clauses) {
        List<String> args =
            (List<String>) request.getDelimiterSeparatedList(arg);
        System.err.println("args:" + args);
        clauses.add(Clause.or(Clause.makeStringClauses(column, args)));
        addSearchCriteria(msgBuff, label, args, arg);
    }


    /**
     * _more_
     *
     * @param results _more_
     * @param metadataThing _more_
     * @param cols _more_
     *
     * @throws Exception On badness
     */
    public void addPropertyMetadata(ResultSet results,
                                    GsacResource metadataThing, String[] cols)
            throws Exception {
        for (String col : cols) {
            String noDotCol = SqlUtil.unDot(col);
            String prop     = results.getString(noDotCol);
            if ((prop != null) && (prop.trim().length() > 0)) {
                prop = getLabel(GsacArgs.ARG_SITE_PREFIX + col, prop);
                String label = noDotCol.replaceAll("_", " ");
                label = StringUtil.camelCase(label);
                metadataThing.addMetadata(new PropertyMetadata(noDotCol,
                        prop, label));
            }
        }
    }



    /**
     * _more_
     *
     * @param values _more_
     *
     * @return _more_
     */
    public String[][] toTuples(String[] values) {
        String[][] tuples = new String[values.length][];
        for (int i = 0; i < values.length; i++) {
            tuples[i] = new String[] { values[i] };
        }
        return tuples;
    }

    /**
     * _more_
     *
     * @param capability _more_
     * @param group _more_
     * @param desc _more_
     *
     * @return _more_
     */
    public Capability initCapability(Capability capability, String group,
                                     String desc) {
        return initCapability(capability, group, desc, null, null);
    }


    /**
     * _more_
     *
     * @param capability _more_
     * @param group _more_
     * @param desc _more_
     * @param tooltip _more_
     *
     * @return _more_
     */
    public Capability initCapability(Capability capability, String group,
                                     String desc, String tooltip) {
        return initCapability(capability, group, desc, tooltip, null);
    }

    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    public Vocabulary makeVocabulary(String id) {
        return getRepository().getVocabulary(id, true);
    }

    /**
     * _more_
     *
     * @param capability _more_
     * @param group _more_
     * @param desc _more_
     * @param tooltip _more_
     * @param vocabulary _more_
     *
     * @return _more_
     */
    public Capability initCapability(Capability capability, String group,
                                     String desc, String tooltip,
                                     Vocabulary vocabulary) {
        if (group != null) {
            capability.setGroup(group);
        }
        if (desc != null) {
            capability.setDescription(desc);
        }
        if (tooltip != null) {
            capability.setTooltip(tooltip);
        }
        if (vocabulary != null) {
            capability.setVocabulary(vocabulary);
        }
        return capability;
    }

    /**
     * _more_
     *
     * @param capabilities _more_
     * @param objects _more_
     */
    public void addCapabilities(List<Capability> capabilities,
                                Object[][] objects) {
        for (Object[] tuple : objects) {
            Capability capability = (Capability) tuple[0];
            //Skip the enums with nothing
            if (capability.isEnumeration() && !capability.hasEnumerations()) {
                continue;
            }
            capability.setGroup((String) tuple[1]);
            if (tuple.length > 2) {
                capability.setDescription((String) tuple[2]);
            }
            if (tuple.length > 3) {
                capability.setTooltip((String) tuple[3]);
            }
            if (tuple.length > 4) {
                Vocabulary vocabulary = (Vocabulary) tuple[4];
                capability.setVocabulary(vocabulary);
            }
            capabilities.add(capability);
        }
    }



    /**
     * _more_
     *
     * @param cap _more_
     * @param group _more_
     *
     * @return _more_
     */
    public Capability setGroup(Capability cap, String group) {
        cap.setGroup(group);
        return cap;
    }

    /**
     * _more_
     *
     * @param group _more_
     * @param cap _more_
     *
     * @return _more_
     */
    public Capability setGroup(String group, Capability cap) {
        cap.setGroup(group);
        return cap;
    }


    /**
     * _more_
     *
     * @param tooltip _more_
     * @param cap _more_
     *
     * @return _more_
     */
    public Capability setTooltip(String tooltip, Capability cap) {
        cap.setTooltip(tooltip);
        return cap;
    }


    /**
     * _more_
     *
     * @param info _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public Capability makeEnumeratedCapabilty(SearchInfo info)
            throws Exception {
        Statement statement =
            getDatabaseManager().select(distinct(info.getDbCol()),
                                        getTableName(info.getDbCol()));

        String[] values = SqlUtil.readString(SqlUtil.getIterator(statement),
                                             1);
        Arrays.sort(values);
        String[][] tuples = new String[values.length][];
        for (int i = 0; i < values.length; i++) {
            String id    = values[i];
            String value = id;
            tuples[i] = new String[] { id, value };
            //                    addLabel(GsacExtArgs.ARG_STATE, id, value);
        }

        Capability capability = new Capability(info.getUrlArg(),
                                    info.getLabel(), IdLabel.toList(tuples),
                                    true);
        if (info.getGroup() != null) {
            capability.setGroup(info.getGroup());
        }
        return capability;

    }

    /**
     * _more_
     *
     * @param column _more_
     *
     * @return _more_
     */
    public String getTableName(String column) {
        int idx = column.indexOf(".");
        if (idx >= 0) {
            return column.substring(0, idx);
        }
        return column;
    }





    /**
     * _more_
     *
     * @param values _more_
     *
     * @return _more_
     */
    public String[] sort(String[] values) {
        Arrays.sort(values);
        return values;
    }

    /**
     * _more_
     *
     * @param message _more_
     */
    public void notImplemented(String message) {
        throw new IllegalArgumentException(message);
    }

    /**
     * _more_
     *
     * @param what _more_
     *
     * @return _more_
     */
    public String distinct(String what) {
        return " distinct " + what;
    }




    /**
     * _more_
     *
     * @param col _more_
     * @param ascending _more_
     *
     * @return _more_
     */
    public String orderBy(String col, boolean ascending) {
        return ascending
               ? orderByAscending(col)
               : orderByDescending(col);
    }

    /**
     * _more_
     *
     * @param col _more_
     *
     * @return _more_
     */
    public String orderByAscending(String col) {
        return " ORDER BY  " + col + " ASC ";
    }

    /**
     * _more_
     *
     * @param col _more_
     *
     * @return _more_
     */
    public String orderByDescending(String col) {
        return " ORDER BY  " + col + " DESC ";
    }


    /**
     * Add basic bounding box query clauses to the list of clauses
     * and append to the search criteria msgBuff
     * This looks for the ARG_NORTH/ARG_SOUTH/ARG_EAST/ARG_WEST
     * url arguments and does a simple bounds query
     *
     * @param request The request
     * @param clauses list of clauses  to add to
     * @param latitudeColumn column name for latitude
     * @param longitudeColumn column name for longitude
     * @param msgBuff search criteria message buffer
     */
    public void addBoundingBoxSearch(GsacRequest request,
                                     List<Clause> clauses,
                                     String latitudeColumn,
                                     String longitudeColumn,
                                     StringBuffer msgBuff) {
        int          cnt        = 0;
        StringBuffer tmpMsgBuff = new StringBuffer();
        if (request.defined(ARG_NORTH)) {
            cnt++;
            clauses.add(Clause.le(latitudeColumn,
                                  request.getLatLon(ARG_NORTH, 0.0)));
            appendSearchCriteria(tmpMsgBuff, "north&lt;=",
                                 "" + request.getLatLon(ARG_NORTH, 0.0));
        }
        if (request.defined(ARG_SOUTH)) {
            cnt++;
            clauses.add(Clause.ge(latitudeColumn,
                                  request.getLatLon(ARG_SOUTH, 0.0)));
            appendSearchCriteria(tmpMsgBuff, "south&gt;=",
                                 "" + request.getLatLon(ARG_SOUTH, 0.0));
        }
        if (request.defined(ARG_EAST)) {
            cnt++;
            clauses.add(
                Clause.le(
                    longitudeColumn,
                    normalizeLongitude(request.getLatLon(ARG_EAST, 0.0))));
            appendSearchCriteria(
                tmpMsgBuff, "east&lt;=",
                "" + normalizeLongitude(request.getLatLon(ARG_EAST, 0.0)));
        }
        if (request.defined(ARG_WEST)) {
            cnt++;
            clauses.add(
                Clause.ge(
                    longitudeColumn,
                    normalizeLongitude(request.getLatLon(ARG_WEST, 0.0))));
            appendSearchCriteria(
                tmpMsgBuff, "west&gt;=",
                "" + normalizeLongitude(request.getLatLon(ARG_WEST, 0.0)));
        }
        if (cnt == 4) {
            msgBuff.append(
                "<tr valign=center><td><b>Bounds=</b></td>"
                + "<td><table border=0><tr><td colspan=2 align=center>"
                + request.get(ARG_NORTH, 0.0) + "</td></tr><tr><td>"
                + normalizeLongitude(request.get(ARG_WEST, 0.0))
                + "</td><td>"
                + normalizeLongitude(request.get(ARG_EAST, 0.0))
                + "</td></tr>" + "<tr><td colspan=2 align=center>"
                + request.get(ARG_SOUTH, 0.0) + "</td></tr></table>"
                + "</td></tr>\n");
        } else {
            msgBuff.append(tmpMsgBuff);
        }


    }



    /**
     * Normalize the longitude to lie between +/-180
     * @param lon east latitude in degrees
     * @return normalized lon
     */
    static public double normalizeLongitude(double lon) {
        if ((lon < -180.) || (lon > 180.)) {
            return Math.IEEEremainder(lon, 360.0);
        } else {
            return lon;
        }
    }


}
