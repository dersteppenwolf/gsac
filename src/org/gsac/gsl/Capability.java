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


import org.gsac.gsl.util.*;

import ucar.unidata.xml.XmlUtil;

import java.io.PrintWriter;


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;



/**
 * Defines a query capability. Takes an id, query type (e.g., enumeration, boolean, string), a label, etc.
 */
public class Capability {


    /** _more_ */
    public static final String TAG_CAPABILITY = "capability";

    /** _more_ */
    public static final String TAG_DESCRIPTION = "description";

    /** _more_ */
    public static final String TAG_ENUMERATIONS = "enumerations";

    /** _more_ */
    public static final String TAG_VALUE = "value";

    /** _more_ */
    public static final String ATTR_ALLOWMULTIPLES = "allowMultiples";

    /** _more_ */
    public static final String ATTR_COLUMNS = "columns";

    public static final String ATTR_BROWSE = "browse";

    /** _more_ */
    public static final String ATTR_GROUP = "group";

    /** _more_ */
    public static final String ATTR_ID = "id";

    /** _more_ */
    public static final String ATTR_NAME = "name";

    /** _more_ */
    public static final String ATTR_TYPE = "type";

    /** _more_ */
    public static final String ATTR_LABEL = "label";

    /** _more_ */
    public static final String ATTR_URL = "url";



    /** type */
    public static final String TYPE_ENUMERATION = "enumeration";

    /** type */
    public static final String TYPE_STRING = "string";

    /** type */
    public static final String TYPE_NUMERIC = "numeric";

    /** type */
    public static final String TYPE_BOOLEAN = "boolean";

    /** _more_ */
    public static final String TYPE_DATERANGE = "date_range";


    public static final String TYPE_NUMBERRANGE = "number_range";

    /** _more_ */
    public static final String TYPE_SPATIAL_BOUNDS = "spatial_bounds";

    /** The vocabulary used. May be null */
    Vocabulary vocabulary;

    /** the id of the search capability */
    private String id;

    /** label */
    private String label;

    /** type of capability */
    private String type;

    /** Enumerated values */
    private List<IdLabel> enums;

    /** allows multiple selects for enumerated values */
    private boolean allowMultiples = false;

    /** _more_ */
    private int columns = 30;

    private boolean browse = false;

    /** _more_ */
    private String group;

    /** _more_ */
    private String tooltip;

    /** _more_ */
    private String description;

    /** _more_ */
    private List<GsacRepositoryInfo> repositories =
        new ArrayList<GsacRepositoryInfo>();

    /** _more_          */
    private String suffixLabel = "";


    /**
     * _more_
     */
    public Capability() {}

    /**
     * ctor
     *
     * @param id the capability id
     * @param label the label
     * @param enums enumerated values
     * @param allowMultiples allows multiple selects for enumerated values
     */
    public Capability(String id, String label, String[] enums,
                      boolean allowMultiples) {
        this(id, label, TYPE_ENUMERATION, IdLabel.toList(enums),
             allowMultiples);
    }



    /**
     * ctor
     *
     * @param id the capability id
     * @param label the label
     * @param enums enumerated values
     * @param allowMultiples allows multiple selects for enumerated values
     */
    public Capability(String id, String label, List<IdLabel> enums,
                      boolean allowMultiples) {
        this(id, label, TYPE_ENUMERATION, enums, allowMultiples);
    }


    public Capability(String id, String label, Vocabulary vocabulary,
                      boolean allowMultiples) {
        this(id, label, TYPE_ENUMERATION, vocabulary.getValues(), allowMultiples);
        this.vocabulary = vocabulary;
    }


    /**
     * ctor
     *
     * @param id the capability id
     * @param label the label
     * @param type The capability type
     */
    public Capability(String id, String label, String type) {
        this(id, label, type, null, false);
    }


    /**
     * ctor
     *
     * @param id the capability id
     * @param label the label
     * @param type The capability type
     * @param enums enumerated values
     * @param allowMultiples allows multiple selects for enumerated values
     */
    public Capability(String id, String label, String type,
                      List<IdLabel> enums, boolean allowMultiples) {
        this.id             = id;
        this.label          = label;
        this.type           = type;
        this.enums          = enums;
        this.allowMultiples = allowMultiples;
    }


    /**
       Set the Vocabulary property.

       @param value The new value for Vocabulary
    **/
    public void setVocabulary (Vocabulary value) {
        //Only set the vocabulary if it has values
        if(value.hasValues()) {
            vocabulary = value;
        }
    }

    /**
       Get the Vocabulary property.

       @return The Vocabulary
    **/
    public Vocabulary getVocabulary () {
	return vocabulary;
    }



    public void printDescription(PrintWriter pw) {
	if(type.equals(TYPE_DATERANGE)) {
	    pw.println(label +": "+  id+".from" + " " + id+".to");
	} else {
	    pw.println(label +": "+  id);
	}
    }



    /**
     * Set the SuffixLabel property.
     *
     * @param value The new value for SuffixLabel
     */
    public void setSuffixLabel(String value) {
        suffixLabel = value;
    }

    /**
     * Get the SuffixLabel property.
     *
     * @return The SuffixLabel
     */
    public String getSuffixLabel() {
        return suffixLabel;
    }





    public boolean enumsOk() {
        if (isEnumeration()) {
            return getEnums().size()>0;
        }
        return true;
    }

    /**
     * _more_
     *
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    public void toXml(Appendable sb) throws Exception {
        StringBuffer attrs = new StringBuffer(XmlUtil.attrs(ATTR_ID, id,
                                                            ATTR_TYPE, type, ATTR_LABEL, label));

        if (group != null) {
            attrs.append(XmlUtil.attrs(ATTR_GROUP, group));
        }
        if(columns!=0) {
            attrs.append(XmlUtil.attrs(ATTR_COLUMNS,
                                       "" + columns));
        }
        if(browse) {
            attrs.append(XmlUtil.attrs(ATTR_BROWSE,
                                       "" + browse));
        }
        if (isEnumeration()) {
            attrs.append(XmlUtil.attrs(ATTR_ALLOWMULTIPLES,
                                       "" + allowMultiples));
        }
        sb.append(XmlUtil.openTag(TAG_CAPABILITY, attrs.toString()));
        sb.append("\n");
        if (isEnumeration()) {
            for (IdLabel object : enums) {
                String valueAttrs = XmlUtil.attrs(ATTR_ID, object.getId());
                if ( !object.labelSameAsId()) {
                    valueAttrs += XmlUtil.attrs(ATTR_LABEL,
                                                object.getLabel());
                }
                sb.append(XmlUtil.tag(TAG_VALUE, valueAttrs));
                sb.append("\n");
            }
        }
        if ((description != null) && (description.length() > 0)) {
            sb.append(XmlUtil.tag(TAG_DESCRIPTION, "",
                                  XmlUtil.getCdata(description)));
        }
        sb.append(XmlUtil.closeTag(TAG_CAPABILITY));
        sb.append("\n");
    }


    /**
     * _more_
     *
     * @param capabilities _more_
     * @param used _more_
     *
     * @return _more_
     */
    public static List<Capability> mergeCapabilities(
                                                     List<Capability> capabilities,
                                                     Hashtable<String, Capability> used) {

        List<Capability> results = new ArrayList<Capability>();
        for (Capability capability : capabilities) {
            results.add(mergeCapability(capability, used));
        }
        return results;
    }

    /**
     * _more_
     *
     * @param capability _more_
     * @param used _more_
     *
     * @return _more_
     */
    public static Capability mergeCapability(Capability capability,
                                             Hashtable<String,
                                             Capability> used) {
        Capability existingCapability = used.get(capability.getId());
        if (existingCapability != null) {
            if ( !capability.getType().equals(existingCapability.getType())) {
                throw new IllegalArgumentException(
                                                   "Capability type mismatch:" + existingCapability + " != "
                                                   + capability);
            }
            if (existingCapability.getType().equals(TYPE_ENUMERATION)) {
                System.err.println("Merging " + existingCapability.getId());
                existingCapability.mergeEnums(capability.getEnums());
            }
            return existingCapability;
        }
        used.put(capability.getId(), capability);
        return capability;
    }

    /**
     * _more_
     *
     * @param enumsToMerge _more_
     */
    private void mergeEnums(List<IdLabel> enumsToMerge) {
        List<IdLabel> newEnums = new ArrayList<IdLabel>(enums);
        for (IdLabel object : enumsToMerge) {
            if ( !enums.contains(object)) {
                newEnums.add(object);
            }
        }
        enums = newEnums;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public List<GsacRepositoryInfo> getRepositories() {
        return repositories;
    }


    /**
     * _more_
     *
     * @param repository _more_
     */
    public void addRepository(GsacRepositoryInfo repository) {
        if ( !repositories.contains(repository)) {
            repositories.add(repository);
        }
    }



    /**
     * _more_
     *
     * @param id _more_
     * @param label _more_
     *
     * @return _more_
     */
    public static Capability makeBooleanCapability(String id, String label) {
        return new Capability(id, label, TYPE_BOOLEAN);
    }


    /**
     * _more_
     *
     * @param info _more_
     *
     * @return _more_
     */
    public static Capability makeBooleanCapability(SearchInfo info) {
        return Capability.makeBooleanCapability(info.getUrlArg(),
                                                info.getLabel());
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public boolean hasEnumerations() {
        return ((enums != null) && (enums.size() > 0));
    }

    /**
     * is this of type enumeration
     *
     * @return is enumeration
     */
    public boolean isEnumeration() {
        return type.equals(TYPE_ENUMERATION);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return id + " " + label + " " + type + " reps:" + repositories;
    }

    /**
     *  Set the Id property.
     *
     *  @param value The new value for Id
     */
    public void setId(String value) {
        id = value;
    }

    /**
     *  Get the Id property.
     *
     *  @return The Id
     */
    public String getId() {
        return id;
    }

    /**
     *  Set the Label property.
     *
     *  @param value The new value for Label
     */
    public void setLabel(String value) {
        label = value;
    }

    /**
     *  Get the Label property.
     *
     *  @return The Label
     */
    public String getLabel() {
        return label;
    }

    /**
     *  Set the Type property.
     *
     *  @param value The new value for Type
     */
    public void setType(String value) {
        type = value;
    }

    /**
     *  Get the Type property.
     *
     *  @return The Type
     */
    public String getType() {
        return type;
    }



    /**
     *  Set the Enums property.
     *
     *  @param value The new value for Enums
     */
    public void setEnums(List<IdLabel> value) {
        enums = value;
    }


    /**
     *  Get the Enums property.
     *
     *  @return The Enums
     */
    public List<IdLabel> getEnums() {
        if(vocabulary!=null) {
            System.err.println("Got vocab:" +vocabulary.getValues());
            return vocabulary.getValues();
        }

        return enums;
    }


    /**
     * Set the AllowMultiples property.
     *
     * @param value The new value for AllowMultiples
     */
    public void setAllowMultiples(boolean value) {
        allowMultiples = value;
    }

    /**
     * Get the AllowMultiples property.
     *
     * @return The AllowMultiples
     */
    public boolean getAllowMultiples() {
        return allowMultiples;
    }

    /**
       Set the Browse property.

       @param value The new value for Browse
    **/
    public void setBrowse (boolean value) {
	browse = value;
    }

    /**
       Get the Browse property.

       @return The Browse
    **/
    public boolean getBrowse () {
	return browse;
    }



    /**
     *  Set the Columns property.
     *
     *  @param value The new value for Columns
     */
    public void setColumns(int value) {
        columns = value;
    }

    /**
     *  Get the Columns property.
     *
     *  @return The Columns
     */
    public int getColumns() {
        return columns;
    }

    /**
     *  Set the Group property.
     *
     *  @param value The new value for Group
     */
    public void setGroup(String value) {
        group = value;
    }

    /**
     *  Get the Group property.
     *
     *  @return The Group
     */
    public String getGroup() {
        return group;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean hasGroup() {
        return (group != null) && (group.length() > 0);
    }

    /**
     *  Set the Tooltip property.
     *
     *  @param value The new value for Tooltip
     */
    public void setTooltip(String value) {
        tooltip = value;
    }

    /**
     *  Get the Tooltip property.
     *
     *  @return The Tooltip
     */
    public String getTooltip() {
        return tooltip;
    }


    /**
     *  Set the Description property.
     *
     *  @param value The new value for Description
     */
    public void setDescription(String value) {
        description = value;
    }

    /**
     *  Get the Description property.
     *
     *  @return The Description
     */
    public String getDescription() {
        return description;
    }


}
