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

package org.gsac.gsl.output;


import org.gsac.gsl.*;
import org.gsac.gsl.metadata.*;
import org.gsac.gsl.metadata.gnss.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.util.*;

import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.Misc;

import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;

import java.io.*;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;

import java.util.Hashtable;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.*;
import javax.servlet.http.*;


/**
 * Class description
 *
 *
 * @version        Enter version here..., Wed, May 19, '10
 * @author         Enter your name here...
 */
public class HtmlOutputHandler extends GsacOutputHandler {

    /** _more_ */
    public static final String stringSearchHelp =
        "semi-colon separated list: p123;p456,  wildcards: p12* *123 *12* negate: !p123";

    /** _more_ */
    public static final String dateHelp =
        "e.g., yyyy-mm-dd,  now, -1 week, +3 days, etc.";

    /** _more_ */
    public static final String timeHelp = "hh:mm:ss Z, e.g. 20:15:00 MST";

    /** _more_ */
    private static String[] SITE_TABLE_LABELS;

    /** _more_ */
    private static String[] SITE_TABLE_SORTVALUES;


    /**
     * ctor
     *
     * @param gsacServlet The servlet
     */
    public HtmlOutputHandler(GsacRepository gsacServlet) {
        super(gsacServlet);
        HtmlUtil.setBlockHideShowImage(iconUrl("/minus.gif"),
                                       iconUrl("/plus.gif"));

        HtmlUtil.setInlineHideShowImage(iconUrl("/minus.gif"),
                                        iconUrl("/plus.gif"));


    }


    /**
     * _more_
     *
     * @param request The request
     * @param arg _more_
     *
     * @return _more_
     */
    public String getSearchTypeSelect(GsacRequest request, String arg) {
        String select = HtmlUtil.select(arg, toTfoList(new String[][] {
            { ARG_UNDEFINED_VALUE, ARG_UNDEFINED_LABEL },
            { SEARCHTYPE_EXACT, "Match exactly" },
            { SEARCHTYPE_BEGINSWITH, "Begins with" },
            { SEARCHTYPE_CONTAINS, "Contains" }
        }), request.get(arg, ARG_UNDEFINED_VALUE));
        return select;
    }




    /**
     * _more_
     *
     * @param request The request
     * @param pw appendable to append to
     *
     * @throws IOException On badness
     */
    public void getLimitSelect(GsacRequest request, Appendable pw)
            throws IOException {

        /*        pw.append(formEntry(request,msgLabel("Offset"),
                                     HtmlUtil.input(ARG_OFFSET, request.getOffset()+"",HtmlUtil.SIZE_5)
                                     + HtmlUtil.space(2) +
                                     msgLabel("Limit")
                                     + HtmlUtil.space(1) +
                                     HtmlUtil.input(ARG_LIMIT, request.getLimit()+"",HtmlUtil.SIZE_5)));
        */


        pw.append(formEntry(request, msgLabel("Limit"),
                            HtmlUtil.input(ARG_LIMIT,
                                           request.getLimit() + "",
                                           HtmlUtil.SIZE_5)));

    }




    /**
     * _more_
     *
     * @param request The request
     * @param pw _more_
     *
     * @throws IOException On badness
     */
    public void getRepositorySelect(GsacRequest request, Appendable pw)
            throws IOException {
        List<GsacRepositoryInfo> allServers = getRepository().getServers();
        if (allServers.size() == 0) {
            return;
        }
        List         urls = request.get(ARG_REPOSITORY, new ArrayList());
        StringBuffer sb   = new StringBuffer();
        for (GsacRepositoryInfo info : allServers) {
            String url = info.getUrl();
            sb.append(HtmlUtil.checkbox(ARG_REPOSITORY, url,
                                        urls.size() == 0
                                        | urls.contains(url)));
            sb.append(HtmlUtil.href(info.getUrl(), info.getName()));
            sb.append(HtmlUtil.br());
        }
        pw.append(getHeader(msg("Search in Repositories")));
        pw.append(HtmlUtil.makeShowHideBlock(msg(""), sb.toString(), true));
    }


    /**
     * _more_
     *
     * @param request The request
     * @param pw appendable to append to
     *
     * @throws IOException On badness
     */
    public void getSiteSortSelect(GsacRequest request, Appendable pw)
            throws IOException {
        boolean sortCapable  = getRepository().isCapable(ARG_SITE_SORT_VALUE);
        boolean orderCapable = getRepository().isCapable(ARG_SITE_SORT_ORDER);
        if ( !sortCapable) {
            return;
        }
        String[][] tuples = new String[][] {
            { ARG_UNDEFINED_VALUE, ARG_UNDEFINED_LABEL },
            { SORT_SITE_CODE, msg("Site Code") },
            { SORT_SITE_NAME, msg("Name") }, { SORT_SITE_TYPE, msg("Type") }
        };
        List<TwoFacedObject> tfos = toTfoList(tuples);
        String valueWidget = HtmlUtil.select(ARG_SITE_SORT_VALUE, tfos,
                                             request.get(ARG_SITE_SORT_VALUE,
                                                 ARG_UNDEFINED_VALUE), "");

        String orderWidget = "";
        if (orderCapable) {
            orderWidget = HtmlUtil.radio(
                ARG_SITE_SORT_ORDER, SORT_ORDER_ASCENDING,
                SORT_ORDER_ASCENDING.equals(
                    request.get(
                        ARG_SITE_SORT_ORDER, SORT_ORDER_ASCENDING))) + " "
                            + msg("Ascending") + HtmlUtil.space(2)
                            + HtmlUtil.radio(
                                ARG_SITE_SORT_ORDER, SORT_ORDER_DESCENDING,
                                SORT_ORDER_DESCENDING.equals(
                                    request.get(
                                        ARG_SITE_SORT_ORDER,
                                        SORT_ORDER_ASCENDING))) + " "
                                            + msg("Descending");
        }
        pw.append(formEntry(request, msgLabel("Order By"),
                            valueWidget + orderWidget));
    }


    /**
     * _more_
     *
     * @param request The request
     * @param pw appendable to append to
     *
     * @throws IOException On badness
     */
    public void getResourceSortSelect(GsacRequest request, Appendable pw)
            throws IOException {
        boolean sortCapable  = getRepository().isCapable(ARG_FILE_SORT_VALUE);
        boolean orderCapable = getRepository().isCapable(ARG_FILE_SORT_ORDER);
        if ( !sortCapable) {
            return;
        }
        String[][] tuples = new String[][] {
            { ARG_UNDEFINED_VALUE, ARG_UNDEFINED_LABEL },
            { SORT_FILE_TYPE, msg("Resource Type") },
            { SORT_FILE_SIZE, msg("Size") },
            { SORT_FILE_PUBLISHDATE, msg("Publish Date") }
        };
        List<TwoFacedObject> tfos = toTfoList(tuples);
        String valueWidget = HtmlUtil.select(ARG_FILE_SORT_VALUE, tfos,
                                             request.get(ARG_FILE_SORT_VALUE,
                                                 ARG_UNDEFINED_VALUE), "");

        String orderWidget = "";
        if (orderCapable) {
            orderWidget = HtmlUtil.radio(
                ARG_FILE_SORT_ORDER, SORT_ORDER_ASCENDING,
                SORT_ORDER_ASCENDING.equals(
                    request.get(
                        ARG_FILE_SORT_ORDER, SORT_ORDER_ASCENDING))) + " "
                            + msg("Ascending") + HtmlUtil.space(2)
                            + HtmlUtil.radio(
                                ARG_FILE_SORT_ORDER, SORT_ORDER_DESCENDING,
                                SORT_ORDER_DESCENDING.equals(
                                    request.get(
                                        ARG_FILE_SORT_ORDER,
                                        SORT_ORDER_ASCENDING))) + " "
                                            + msg("Descending");
        }
        pw.append(formEntry(request, msgLabel("Order By"),
                            valueWidget + orderWidget));
    }

    /**
     * _more_
     *
     *
     * @param group _more_
     *
     * @param resourceClass _more_
     * @param request The request
     * @param pw appendable to append to
     *
     * @throws IOException On badness
     */
    public void getOutputSelect(ResourceClass resourceClass,
                                GsacRequest request, Appendable pw)
            throws IOException {
        List outputs = new ArrayList();
        for (GsacOutput output : getRepository().getOutputs(resourceClass)) {
            if (output.getForUser()) {
                outputs.add(new TwoFacedObject(output.getLabel(),
                        output.getId()));
            }
        }

        pw.append(formEntry(request, msgLabel("Output"),
                            HtmlUtil.select(ARG_OUTPUT, outputs,
                                            (String) null, "")));

        pw.append(formEntry(request, "",
                            HtmlUtil.checkbox(ARG_GZIP, "true", false) + " "
                            + msg("Compress result")));
    }


    /**
     * _more_
     *
     * @param request The request
     * @param arg _more_
     * @param enums _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public String makeMultiSelect(GsacRequest request, String arg,
                                  List enums, String dflt) {
        List<TwoFacedObject> tfos     = toTfoList(enums);
        List                 selected = request.getList(arg);
        if ((selected.size() == 0) && (dflt != null)) {
            selected.add(dflt);
        }
        if (enums.size() < 6) {
            StringBuffer sb = new StringBuffer();
            for (TwoFacedObject tfo : tfos) {
                String value = tfo.getId().toString();
                if (value.length() == 0) {
                    continue;
                }
                sb.append(HtmlUtil.checkbox(arg, value,
                                            selected.contains(tfo.getId())));
                sb.append(tfo.getLabel());
                sb.append(" ");
            }
            return sb.toString();
        } else {
            return HtmlUtil.select(arg, tfos, selected,
                                   " MULTIPLE SIZE= "
                                   + Math.min(4, tfos.size()));
        }
    }

    /**
     * _more_
     *
     * @param request The request
     * @param pw appendable to append to
     *
     * @throws IOException On badness
     */
    public void getSiteSearchForm(GsacRequest request, Appendable pw)
            throws IOException {
        getRepository().addToSiteSearchForm(request, pw);
        getRepositorySelect(request, pw);
        CapabilityCollection collection =
            getRepository().getResourceManager(GsacSite.CLASS_SITE).getCapabilityCollection();
        if (collection != null) {
            addCapabilitiesToForm(request, pw, collection, true);
        }

        /*
        String[] haikus = {
            "",
            "The GSAC crashed.<br> I am the Blue Screen of Death.<br> No one hears your screams.",
            "ABORTED effort.<br> Close all that you have.<br> You ask way too much.",
            "Yesterday it worked.<br> Today it is not working.<br> Windows is like that.",
            "The code was willing,<br> It considered your request<br> But the chips were weak.",
            "A crash reduces<br> your expensive computer<br> to a simple stone.",
            "To have no errors<br> Would be life without meaning<br> No struggle, no joy",
            "Three things are certain:<br> Death, taxes, and lost data.<br> Guess which has occurred.",
            "I have gray hair now<br>Javascript can make one old<br>Damn that learning curve!",
            "Click anywhere<br>to stop the pain<br>for now."
        };
        int idx = (int) ((Math.random() * 1000) % haikus.length);
        pw.append(getFormTableHeader(msg("Haiku")));
        pw.append("<tr><td colspan=2>");
        String haiku = "<i>" + haikus[idx] + "</i>";
        pw.append(HtmlUtil.makeShowHideBlock(msg(""), haiku, false));
        pw.append("</td></tr>");
        */
    }

    /**
     * _more_
     *
     * @param request The request
     * @param pw _more_
     * @param collection _more_
     * @param forSite _more_
     *
     * @throws IOException On badness
     */
    public void addCapabilitiesToForm(GsacRequest request, Appendable pw,
                                      CapabilityCollection collection,
                                      boolean forSite)
            throws IOException {

        List<Capability> capabilities = collection.getCapabilities();
        request = new GsacRequest(request);
        request.setUseVocabulary(false);
        String capabilityGroup;
        List<String> capabilityGroups = new ArrayList<String>();
        Hashtable<String, StringBuffer> capabilityGroupMap =
            new Hashtable<String, StringBuffer>();
        StringBuffer capBuff;
        for (Capability capability : capabilities) {
            if ( !capability.enumsOk()) {
                continue;
            }
            String tooltip = capability.getTooltip();
            String arg     = capability.getId();
            String widget  = null;
            String suffix  = capability.getSuffixLabel();
            String dflt    = capability.getDefault();
            capabilityGroup = capability.getGroup();
            if (capabilityGroup == null) {
                capabilityGroup = "Advanced Query";
            }
            capBuff = capabilityGroupMap.get(capabilityGroup);
            if (capBuff == null) {
                capBuff = new StringBuffer(HtmlUtil.formTable());
                capabilityGroupMap.put(capabilityGroup, capBuff);
                capabilityGroups.add(capabilityGroup);
            }
            if (capability.getType().equals(Capability.TYPE_ENUMERATION)) {
                if (capability.getAllowMultiples()) {
                    widget = makeMultiSelect(request, arg,
                                             capability.getEnums(), dflt);
                } else {
                    List<TwoFacedObject> tfos = toTfo(capability.getEnums(),
                                                    dflt == null);
                    widget = HtmlUtil.select(arg, tfos,
                                             request.get(arg, dflt),
                                             capability.getAllowMultiples()
                                             ? " MULTIPLE SIZE=4"
                                             : "");
                }
            } else if (capability.getType().equals(
                    Capability.TYPE_NUMBERRANGE)) {
                String min = arg + ".min";
                String max = arg + ".max";
                widget =
                    " " + msgLabel("Min")
                    + HtmlUtil.input(min, request.get(min, "") + "",
                                     HtmlUtil.SIZE_5) + HtmlUtil.space(2)
                                         + msgLabel("Max")
                                         + HtmlUtil.input(max,
                                             request.get(max, "") + "",
                                             HtmlUtil.SIZE_5);

            } else if (capability.getType().equals(
                    Capability.TYPE_DATERANGE)) {
                Date   fromDate = null;
                Date   toDate   = null;
                String img      = HtmlUtil.img(iconUrl("/range.gif"));
                String dateInput1 = makeDateInput(request, arg + ".from",
                                        "searchform", fromDate, null, false);
                String dateInput2 = makeDateInput(request, arg + ".to",
                                        "searchform", toDate, null, false);
                widget = dateInput1 + HtmlUtil.space(1) + img
                         + HtmlUtil.space(1) + dateInput2;
            } else if (capability.getType().equals(Capability.TYPE_STRING)) {
                String searchType = HtmlUtil.makeToggleInline("",
                                        getSearchTypeSelect(request,
                                            arg + SEARCHTYPE_SUFFIX), false);

                widget = HtmlUtil.input(arg, request.get(arg, ((dflt != null)
                        ? dflt
                        : "")), HtmlUtil.title((tooltip != null)
                        ? tooltip
                        : stringSearchHelp) + HtmlUtil.attr(
                            HtmlUtil.ATTR_WIDTH,
                            "" + capability.getColumns())) + searchType;

            } else if (capability.getType().equals(
                    Capability.TYPE_SPATIAL_BOUNDS)) {
                widget = makeMapSelector(request, arg, true, "", "");
            } else if (capability.getType().equals(Capability.TYPE_BOOLEAN)) {
                String[][] enums = new String[][] {
                    { ARG_UNDEFINED_VALUE, ARG_UNDEFINED_LABEL },
                    { "true", "True" }, { "false", "False" },
                };

                String       booleanValue = request.get(arg, "");
                StringBuffer radioSB      = new StringBuffer();
                for (String[] tuple : enums) {
                    boolean checked = false;
                    if ( !request.defined(arg)) {
                        checked = tuple[0].equals(ARG_UNDEFINED_VALUE);
                    } else {
                        checked = tuple[0].equals(booleanValue);
                    }
                    radioSB.append(HtmlUtil.radio(arg, tuple[0], checked));
                    radioSB.append(tuple[1]);
                    radioSB.append(HtmlUtil.space(1));
                }
                /*                widget = HtmlUtil.select(arg, toTfoList(enums),
                                         request.get(arg,
                                         ARG_UNDEFINED_VALUE), "SIZE=3");*/
                widget = radioSB.toString();
            }
            if (widget != null) {
                String desc = capability.getDescription();
                if (desc == null) {
                    desc = "";
                } else {
                    desc = HtmlUtil.img(iconUrl("/help.png"), desc) + " ";
                }
                capBuff.append(formEntryTop(request,
                                            msgLabel(capability.getLabel()),
                                            widget + suffix));
            } else {
                getRepository().logError("Unknown capability:" + capability,
                                         null);
            }
        }

        int cnt = 0;
        for (String capGroup : capabilityGroups) {
            capBuff = capabilityGroupMap.get(capGroup);
            capBuff.append(HtmlUtil.formTableClose());
            pw.append(getHeader(msg(capGroup)));
            pw.append(HtmlUtil.makeShowHideBlock("", capBuff.toString(),
                    cnt == 0));
            cnt++;
        }

    }


    /**
     * _more_
     *
     * @param header _more_
     *
     * @return _more_
     */
    public String getFormTableHeader(String header) {
        return HtmlUtil.row(
            HtmlUtil.tag(
                HtmlUtil.TAG_TD,
                HtmlUtil.attr(HtmlUtil.ATTR_COLSPAN, "2")
                + HtmlUtil.cssClass("gsac-formheader"), header));
    }


    /**
     * _more_
     *
     * @param header _more_
     *
     * @return _more_
     */
    public String getHeader(String header) {
        return getRepository().getHeader(header);
    }

    /**
     * _more_
     *
     * @param values _more_
     *
     * @return _more_
     */
    public static List<TwoFacedObject> toTfo(List<IdLabel> values) {
        return toTfo(values, true);
    }

    /**
     * _more_
     *
     * @param values _more_
     * @param addAny _more_
     *
     * @return _more_
     */
    public static List<TwoFacedObject> toTfo(List<IdLabel> values,
                                             boolean addAny) {
        List<TwoFacedObject> tfos = new ArrayList<TwoFacedObject>();
        if (addAny) {
            tfos.add(new TwoFacedObject(ARG_UNDEFINED_LABEL,
                                        ARG_UNDEFINED_VALUE));
        }
        for (IdLabel nv : values) {
            tfos.add(new TwoFacedObject(nv.getLabel(), nv.getId()));
        }
        return tfos;
    }

    /** _more_ */
    private static String[] NAV_LABELS;

    /** _more_ */
    private static String[] NAV_URLS;


    /**
     * _more_
     *
     * @param request The request
     * @param response The response
     * @param sb Buffer to append to
     *
     * @throws Exception On badness
     */
    public void appendHeader(GsacRequest request, GsacResponse response,
                             Appendable sb)
            throws Exception {
        if (NAV_LABELS == null) {
            List<String> labelList = new ArrayList<String>();
            List<String> urlList   = new ArrayList<String>();

            for (GsacResourceManager resourceManager :
                    getRepository().getResourceManagers()) {
                labelList.add("Search "
                              + resourceManager.getResourceLabel(true));
                urlList.add(resourceManager.makeFormUrl());
            }


            String[] labels = { "Browse", "Information", "Help" };
            String[] urls = { URL_BROWSE_BASE, URL_REPOSITORY_VIEW,
                              URL_HELP + "/index.html" };
            String[] keys = { HEADER_BROWSE, HEADER_INFO, HEADER_HELP };
            for (int i = 0; i < labels.length; i++) {
                if (getRepository().isCapable(keys[i])) {
                    labelList.add(labels[i]);
                    urlList.add(makeUrl(urls[i]));
                }
            }
            NAV_URLS   = Misc.listToStringArray(urlList);
            NAV_LABELS = Misc.listToStringArray(labelList);
        }

        String       uri   = request.getRequestURI();
        List<String> links = new ArrayList<String>();
        for (int i = 0; i < NAV_LABELS.length; i++) {

            String url   = NAV_URLS[i];
            String label = msg(NAV_LABELS[i]);
            if (uri.equals(url)) {
                links.add(
                    HtmlUtil.span(
                        label, HtmlUtil.cssClass("gsac-header-selected")));
            } else {
                links.add(
                    HtmlUtil.href(
                        url, label,
                        HtmlUtil.cssClass("gsac-header-notselected")));
            }
        }

        sb.append(HtmlUtil.tag(HtmlUtil.TAG_CENTER,
                               HtmlUtil.cssClass("gsac-header"),
                               StringUtil.join(getHeaderSeparator(), links)));
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getHeaderSeparator() {
        return HtmlUtil.span("|", HtmlUtil.cssClass("gsac-header-separator"));
    }


    /**
     * _more_
     *
     * @param request The request
     * @param response The response
     * @param pw appendable to append to
     *
     * @throws IOException On badness
     */
    public void makeNextPrevHeader(GsacRequest request,
                                   GsacResponse response, Appendable pw)
            throws IOException {
        int limit  = request.getLimit();
        int offset = request.getOffset();
        if (offset > 0) {
            Hashtable<String, String> except = new Hashtable<String,
                                                   String>();
            int newOffset = Math.max(0, offset - limit);
            except.put(ARG_LIMIT, "" + limit);
            except.put(ARG_OFFSET, "" + newOffset);
            String args = request.getUrlArgs(except);
            pw.append(HtmlUtil.href(request.getRequestURI() + "?" + args,
                                    msg("Previous")));
            pw.append(HtmlUtil.space(2));
        }
        if (response.getExceededLimit()) {
            Hashtable<String, String> except = new Hashtable<String,
                                                   String>();
            except.put(ARG_LIMIT, "" + limit);
            except.put(ARG_OFFSET, "" + (offset + limit));
            String args = request.getUrlArgs(except);
            pw.append(HtmlUtil.href(request.getRequestURI() + "?" + args,
                                    msg("Next")));
        }
    }


    /**
     * _more_
     *
     * @param group _more_
     * @param resourceClass _more_
     *
     * @return _more_
     */
    public String getGroupSearchLink(ResourceGroup group,
                                     ResourceClass resourceClass) {
        return getSearchLink(
            group, getResourceManager(resourceClass).makeSearchUrl(),
            ARG_RESOURCE_GROUP);
    }



    /**
     * _more_
     *
     * @param thing _more_
     * @param url _more_
     * @param arg _more_
     *
     * @return _more_
     */
    public String getSearchLink(NamedThing thing, String url, String arg) {
        return HtmlUtil.href(HtmlUtil.url(url, new String[] { arg,
                thing.getId() }), thing.getName());
    }




    /**
     * _more_
     *
     * @param url _more_
     *
     * @return _more_
     */
    public String makeHtdocsUrl(String url) {
        return makeUrl(URL_HTDOCS_BASE + url);
    }

    /**
     * _more_
     *
     * @param request The request
     *
     * @return _more_
     */
    public boolean shouldDecorate(GsacRequest request) {
        return request.get(ARG_DECORATE, true);
    }

    /**
     * _more_
     *
     * @param request The request
     * @param response The response
     * @param sb Buffer to append to
     *
     *
     * @return _more_
     * @throws Exception On badness
     */
    public boolean initHtml(GsacRequest request, GsacResponse response,
                            Appendable sb)
            throws Exception {
        if (request.get(ARG_WRAPXML, false)) {
            response.startResponse(GsacResponse.MIME_XML);
            sb.append("<contents>");
            sb.append("<![CDATA[");
            return true;
        }

        response.startResponse(GsacResponse.MIME_HTML);

        boolean hasCssMacro = false;

        String  cssLink = HtmlUtil.cssLink(makeHtdocsUrl(request.isMobile()
                ? "/mobile.css"
                : "/gsac.css"));
        if (shouldDecorate(request)) {
            String header = getRepository().getHtmlHeader(request);

            header = header.replace("${gsac.name}",
                                    getRepository().getRepositoryName());
            hasCssMacro = header.indexOf("${gsac.css}") >= 0;
            if (hasCssMacro) {
                header = header.replace("${gsac.css}", cssLink);
            }
            sb.append(header);
        }
        sb.append("\n");
        if ( !hasCssMacro) {
            sb.append(cssLink);
        }
        sb.append("\n");
        sb.append(
            HtmlUtil.cssLink(
                makeHtdocsUrl(
                    "/jquery/smoothness/jquery-ui-1.8.5.custom.css")));
        sb.append("\n");
        sb.append(
            HtmlUtil.importJS(
                makeHtdocsUrl("/jquery/js/jquery-1.4.2.min.js")));
        sb.append("\n");

        sb.append(
            HtmlUtil.importJS(
                makeHtdocsUrl("/jquery/js/jquery-ui-1.8.5.custom.min.js")));
        sb.append("\n");


        sb.append(HtmlUtil.script("jQuery.noConflict();"));

        /*
          The lightbox code is conflicting with openlayers maps
        sb.append(HtmlUtil.importJS(makeHtdocsUrl("/lightbox/js/prototype.js")));
        sb.append("\n");
        sb.append(HtmlUtil.importJS(
                                    makeHtdocsUrl(
                                                  "/lightbox/js/scriptaculous.js?load=effects,builder")));
        sb.append("\n");
        sb.append(
            HtmlUtil.importJS(makeHtdocsUrl("/lightbox/js/lightbox.js")));
        sb.append("\n");
        sb.append(
            HtmlUtil.cssLink(makeHtdocsUrl("/lightbox/css/lightbox.css")));
        sb.append("\n");
        */

        sb.append(HtmlUtil.importJS(getRepository().getUrlBase()
                                    + URL_HTDOCS_BASE + "/repository.js"));
        sb.append(HtmlUtil.importJS(getRepository().getUrlBase()
                                    + URL_HTDOCS_BASE + "/CalendarPopup.js"));
        sb.append(HtmlUtil.div("",
                               HtmlUtil.id("tooltipdiv")
                               + HtmlUtil.cssClass("tooltip-outer")));
        sb.append("<div class=\"gsac-content\">");
        appendHeader(request, response, sb);

        if ( !getRepository().checkRequest(request, response, sb)) {
            finishHtml(request, response, sb);
            return false;
        }
        return true;
    }




    /**
     * _more_
     *
     * @param request The request
     * @param mapVarName _more_
     * @param sb Buffer to append to
     * @param width _more_
     * @param height _more_
     * @param forSelection _more_
     *
     * @throws IOException On badness
     */
    public void initMap(GsacRequest request, String mapVarName,
                        Appendable sb, int width, int height,
                        boolean forSelection)
            throws IOException {
        sb.append(
            HtmlUtil.cssLink(
                makeHtdocsUrl("/openlayers/theme/default/google.css")));
        sb.append(
            HtmlUtil.cssLink(
                makeHtdocsUrl("/openlayers/theme/default/style.css")));
        sb.append(
            HtmlUtil.importJS(
                "http://api.maps.yahoo.com/ajaxymap?v=3.0&appid=euzuro-openlayers"));
        //        sb.append(
        //            HtmlUtil.importJS(
        //                "http://dev.virtualearth.net/mapcontrol/mapcontrol.ashx?v=6.1"));
        //        sb.append(
        //            HtmlUtil.importJS(
        //                "http://maps.google.com/maps/api/js?v=3.2&amp;sensor=false"));
        sb.append(
            HtmlUtil.importJS(makeHtdocsUrl("/openlayers/OpenLayers.js")));
        sb.append(HtmlUtil.importJS(makeHtdocsUrl("/repositorymap.js")));
        sb.append(HtmlUtil.div("",
                               HtmlUtil.style("width:" + width
                                   + "px; height:" + height + "px") + " "
                                       + HtmlUtil.id(mapVarName)));
        sb.append(
            HtmlUtil.div(
                "",
                HtmlUtil.style(
                    "border:2px #888888 solid; width:" + width
                    + "px; height:" + height + "px") + " "
                        + HtmlUtil.id(mapVarName)));


        sb.append("\n");
        StringBuffer js = new StringBuffer();
        js.append("var " + mapVarName + " = new RepositoryMap('" + mapVarName
                  + "');\n");
        js.append("var map = " + mapVarName + ";\n");
        if ( !forSelection) {
            js.append("map.initMap(" + forSelection + ");\n");
        }
        sb.append(HtmlUtil.script(js.toString()));
        sb.append("\n");
    }


    /**
     * _more_
     *
     * @param request The request
     * @param arg _more_
     * @param popup _more_
     * @param extraLeft _more_
     * @param extraTop _more_
     *
     * @return _more_
     */
    public String makeMapSelector(GsacRequest request, String arg,
                                  boolean popup, String extraLeft,
                                  String extraTop) {
        return makeMapSelector(request, arg, popup, extraLeft, extraTop,
                               null);
    }

    /**
     * _more_
     *
     * @param request The request
     * @param arg _more_
     * @param popup _more_
     * @param extraLeft _more_
     * @param extraTop _more_
     * @param marker _more_
     *
     * @return _more_
     */
    public String makeMapSelector(GsacRequest request, String arg,
                                  boolean popup, String extraLeft,
                                  String extraTop, double[][] marker) {
        return makeMapSelector(arg, popup, extraLeft, extraTop,
                               new String[] {
                                   request.get(arg + ARG_SOUTH_SUFFIX, ""),
                                   request.get(arg + ARG_NORTH_SUFFIX, ""),
                                   request.get(arg + ARG_EAST_SUFFIX, ""),
                                   request.get(arg + ARG_WEST_SUFFIX,
                                   "") }, marker);
    }


    /**
     * _more_
     *
     * @param arg _more_
     * @param popup _more_
     * @param snew _more_
     *
     * @return _more_
     */
    public String makeMapSelector(String arg, boolean popup, String[] snew) {
        return makeMapSelector(arg, popup, "", "", snew);
    }

    /**
     * _more_
     *
     * @param arg _more_
     * @param popup _more_
     * @param extraLeft _more_
     * @param extraTop _more_
     * @param snew _more_
     *
     * @return _more_
     */
    public String makeMapSelector(String arg, boolean popup,
                                  String extraLeft, String extraTop,
                                  String[] snew) {

        return makeMapSelector(arg, popup, extraLeft, extraTop, snew, null);
    }


    /**
     * _more_
     *
     * @param arg _more_
     * @param popup _more_
     * @param extraLeft _more_
     * @param extraTop _more_
     * @param snew _more_
     * @param markerLatLons _more_
     *
     * @return _more_
     */
    public String makeMapSelector(String arg, boolean popup,
                                  String extraLeft, String extraTop,
                                  String[] snew, double[][] markerLatLons) {
        StringBuffer sb = new StringBuffer();
        String msg      =
            HtmlUtil.italics(msg("Shift-drag to select region"));
        sb.append(msg);
        sb.append(HtmlUtil.br());
        String widget;
        if (snew == null) {
            widget = HtmlUtil.makeLatLonBox(arg + ARG_SOUTH_SUFFIX,
                                            arg + ARG_NORTH_SUFFIX,
                                            arg + ARG_EAST_SUFFIX,
                                            arg + ARG_WEST_SUFFIX, "", "",
                                            "", "");
        } else if (snew.length == 4) {
            widget = HtmlUtil.makeLatLonBox(arg + ARG_SOUTH_SUFFIX,
                                            arg + ARG_NORTH_SUFFIX,
                                            arg + ARG_EAST_SUFFIX,
                                            arg + ARG_WEST_SUFFIX, snew[0],
                                            snew[1], snew[2], snew[3]);
        } else {
            widget = " Lat: "
                     + HtmlUtil.input(arg + "_lat", snew[0],
                                      HtmlUtil.SIZE_5 + " "
                                      + HtmlUtil.id(arg + "_lat")) + " Lon: "
                                          + HtmlUtil.input(arg + "_lon",
                                              snew[1],
                                                  HtmlUtil.SIZE_5 + " "
                                                      + HtmlUtil.id(arg
                                                          + "_lon"));
        }
        if ((extraLeft != null) && (extraLeft.length() > 0)) {
            widget = widget + HtmlUtil.br() + extraLeft;
        }



        String mapVarName = "mapselector" + HtmlUtil.blockCnt++;
        String rightSide  = null;
        String clearLink = HtmlUtil.mouseClickHref(mapVarName
                               + ".selectionClear();", msg("Clear"));
        String initParams = HtmlUtil.squote(arg) + "," + (popup
                ? "1"
                : "0");

        try {
            initMap((GsacRequest) null, mapVarName, sb, 500, 300, true);
        } catch (Exception exc) {}

        if (popup) {
            rightSide = makeStickyPopup(
                msg("Select"), sb.toString(),
                mapVarName + ".selectionPopupInit();") + HtmlUtil.space(2)
                    + clearLink + HtmlUtil.space(2) + HtmlUtil.space(2)
                    + extraTop;
        } else {
            rightSide = clearLink + HtmlUtil.space(2) + HtmlUtil.br()
                        + sb.toString();
        }

        StringBuffer script = new StringBuffer();
        script.append(mapVarName + ".setSelection(" + initParams + ");\n");
        if (markerLatLons != null) {
            /*
            script.append("var markerLine = new Polyline([");
            for(int i=0;i<markerLatLons[0].length;i++) {
                if(i>0)
                    script.append(",");
                script.append("new LatLonPoint(" + markerLatLons[0][i]+"," +
                              markerLatLons[1][i]+")");
            }
            script.append("]);\n");
            script.append("markerLine.setColor(\"#00FF00\");\n");
            script.append("markerLine.setWidth(3);\n");
            script.append(mapVarName +".addPolyline(markerLine);\n");
            script.append(mapVarName +".autoCenterAndZoom();\n");
            */
        }



        return HtmlUtil.table(new Object[] { widget, rightSide }) + "\n"
               + HtmlUtil.script(script.toString());

    }



    /**
     * _more_
     *
     * @param link _more_
     * @param innerContents _more_
     * @param initCall _more_
     *
     * @return _more_
     */
    public String makeStickyPopup(String link, String innerContents,
                                  String initCall) {
        boolean alignLeft = true;
        String  compId    = "menu_" + HtmlUtil.blockCnt++;
        String  linkId    = "menulink_" + HtmlUtil.blockCnt++;
        String  contents  = makeStickyPopupDiv(innerContents, compId);
        String onClick =
            HtmlUtil.onMouseClick(HtmlUtil.call("showStickyPopup",
                HtmlUtil.comma(new String[] { "event",
                HtmlUtil.squote(linkId), HtmlUtil.squote(compId), (alignLeft
                ? "1"
                : "0") })) + initCall);
        String href = HtmlUtil.href("javascript:noop();", link,
                                    onClick + HtmlUtil.id(linkId));
        return href + contents;
    }



    /**
     * _more_
     *
     * @param contents _more_
     * @param compId _more_
     *
     * @return _more_
     */
    public String makeStickyPopupDiv(String contents, String compId) {
        StringBuffer menu = new StringBuffer();
        String cLink = HtmlUtil.jsLink(
                           HtmlUtil.onMouseClick(
                               HtmlUtil.call(
                                   "hideElementById",
                                   HtmlUtil.squote(compId))), HtmlUtil.img(
                                       iconUrl("/close.gif")), "");
        contents = cLink + HtmlUtil.br() + contents;

        menu.append(HtmlUtil.div(contents,
                                 HtmlUtil.id(compId)
                                 + HtmlUtil.cssClass("popup")));
        return menu.toString();
    }



    /**
     * _more_
     *
     * @param request The request
     * @param response The response
     * @param sb Buffer to append to
     *
     * @throws Exception On badness
     */
    public void finishHtml(GsacRequest request, GsacResponse response,
                           Appendable sb)
            throws Exception {
        PrintWriter pw = response.getPrintWriter();
        if (request.get(ARG_WRAPXML, false)) {
            sb.append("]]>");
            sb.append("</contents>");
        } else {
            sb.append("</div>");
            if (shouldDecorate(request)) {
                sb.append(getRepository().getHtmlFooter(request));
                sb = getRepository().decorateHtml(request, sb);
            }
        }
        pw.append(sb.toString());
        pw.flush();
        response.endResponse();
    }


    /**
     * _more_
     *
     * @param labels _more_
     *
     * @return _more_
     */
    public String tableHeader(String[] labels) {
        StringBuffer sb = new StringBuffer();
        sb.append("<tr>");
        for (String label : labels) {
            sb.append("<td class=tableheader>");
            sb.append(label);
            sb.append("</td>");
        }
        sb.append("</tr>");
        return sb.toString();
    }

    /**
     * _more_
     *
     * @param groups _more_
     * @param resourceClass _more_
     * @param hideIfMany _more_
     *
     * @return _more_
     */
    public String getGroupHtml(List<ResourceGroup> groups,
                               ResourceClass resourceClass,
                               boolean hideIfMany) {
        StringBuffer groupSB = new StringBuffer();
        if (groups.size() > 0) {
            String firstLink = null;
            for (int i = 0; i < groups.size(); i++) {
                ResourceGroup group = groups.get(i);
                if (i > 0) {
                    groupSB.append(",");
                    groupSB.append(HtmlUtil.br());
                }
                String link = getGroupSearchLink(group, resourceClass);
                if (firstLink == null) {
                    firstLink = link;
                }
                groupSB.append(link);
            }
        }
        if (hideIfMany && (groups.size() > 1)) {
            return HtmlUtil.makeShowHideBlock("...", groupSB.toString(),
                    false);
        }

        return groupSB.toString();
    }

    /**
     * _more_
     *
     * @param resource _more_
     *
     * @return _more_
     */
    public String getIconUrl(GsacResource resource) {
        for (IconMetadata iconMetadata :
                IconMetadata.getIconMetadata(resource.getMetadata())) {
            return iconMetadata.getUrl();
        }
        return getRepository().getAbsoluteUrl(iconUrl("/site.png"));
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param label _more_
     * @param contents _more_
     *
     * @return _more_
     */
    public String formEntry(GsacRequest request, String label,
                            String contents) {
        if (request.isMobile()) {
            return "<tr><td><div class=\"formlabel\">" + label + "</div>"
                   + contents + "</td></tr>";
        } else {
            return HtmlUtil.formEntry(label, contents);
        }
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param label _more_
     * @param contents _more_
     *
     * @return _more_
     */
    public String formEntryTop(GsacRequest request, String label,
                               String contents) {
        if (request.isMobile()) {
            return "<tr><td><div class=\"formlabel\">" + label + "</div>"
                   + contents + "</td></tr>";
        } else {
            return HtmlUtil.formEntryTop(label, contents);
        }
    }





    /**
     * _more_
     *
     * @param request The request
     * @param pw appendable to append to
     * @param site _more_
     * @param fullMetadata _more_
     * @param includeMap _more_
     * @param includeLink _more_
     *
     * @throws IOException On badness
     */
    public void getSiteHtml(GsacRequest request, Appendable pw,
                            GsacSite site, boolean fullMetadata,
                            boolean includeMap, boolean includeLink)
            throws IOException {

        //Make sure the site has full metadata
        try {
            if (fullMetadata) {
                if ( !request.defined(ARG_METADATA_LEVEL)) {
                    request.put(ARG_METADATA_LEVEL, "10");
                }
                getRepository().getMetadata(request, site);
            }
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
        pw.append(HtmlUtil.formTable());
        String siteCode = (includeLink
                           ? makeResourceViewHref(site)
                           : site.getLabel());
        if (getDoSiteType()) {
            pw.append(formEntry(request, msgLabel("Site Code"), siteCode));
        }

        pw.append(formEntry(request, msgLabel("Name"), site.getName()));
        if (site.getRepositoryInfo() != null) {
            pw.append(
                formEntry(
                    request, msgLabel("Repository"),
                    getRepository().getRemoteHref(site) + " "
                    + HtmlUtil.href(
                        getRepository().getRemoteUrl(site),
                        site.getRepositoryInfo().getName())));
        }
        if (getDoSiteType() && (site.getType() != null)) {
            pw.append(formEntry(request, msgLabel("Type"),
                                site.getType().getName()));
        }
        if (getDoSiteStatus() && (site.getStatus() != null)) {
            pw.append(formEntry(request, msgLabel("Status"),
                                site.getStatus().getName()));
        }

        String js = null;
        if (includeMap) {
            StringBuffer mapSB = new StringBuffer();
            if ( !request.get(ARG_WRAPXML, false)) {
                js = createMap(request,
                               (List<GsacResource>) Misc.newList(site),
                               mapSB, 400, 200);
            }
            pw.append(formEntryTop(request, msgLabel("Location"),
                                   formatLatLon(site) + mapSB));
        }


        if (site.getFromDate() != null) {
            String dateString = formatDate(site);
            pw.append(formEntry(request, msgLabel("Date Range"), dateString));
        }

        if (getDoResourceGroup()) {
            List<ResourceGroup> groups = site.getResourceGroups();
            if (groups.size() > 0) {
                pw.append(formEntryTop(request, msgLabel((groups.size() == 1)
                        ? "Group"
                        : "Groups"), getGroupHtml(groups,
                        site.getResourceClass(), false)));
            }
        }


        processMetadata(request, pw, site, site.getMetadata(), fullMetadata,
                        new Hashtable());

        pw.append(HtmlUtil.formTableClose());
        if (js != null) {
            pw.append(HtmlUtil.script(js.toString()));
        }
    }

    /**
     * _more_
     *
     * @param request The request
     * @param pw appendable to append to
     * @param gsacResource _more_
     * @param metadataList _more_
     * @param fullMetadata _more_
     * @param state _more_
     *
     * @throws IOException On badness
     */

    public void processMetadata(GsacRequest request, Appendable pw,
                                GsacResource gsacResource,
                                List<GsacMetadata> metadataList,
                                boolean fullMetadata, Hashtable state)
            throws IOException {

        for (GsacMetadata metadata : metadataList) {
            processMetadata(request, pw, gsacResource, metadata,
                            fullMetadata, state);
        }

        if (fullMetadata) {
            int          cnt  = 0;
            StringBuffer buff = new StringBuffer(HtmlUtil.formTable());
            for (GnssEquipment equipment :
                    GnssEquipment.getMetadata(metadataList)) {
                if (cnt == 0) {
                    buff.append(tableHeader(new String[] { msg("Date"),
                            msg("Antenna"), msg("Dome"), msg("Receiver"),
                            msg("Antenna Height") }));
                }
                cnt++;

                buff.append("<tr valign=top>");
                buff.append("<td>&nbsp;");
                String dateString = formatDateTime(equipment.getFromDate())
                                    + " - "
                                    + formatDateTime(equipment.getToDate());
                if (gsacResource instanceof GsacSite) {
                    GsacSite site = (GsacSite) gsacResource;
                    dateString =
                        HtmlUtil.href(HtmlUtil.url(makeUrl(URL_FILE_FORM),
                            new String[] {
                        ARG_SITE_ID, site.getSiteId(), ARG_SITE_CODE,
                        site.getSiteCode(), ARG_FILE_DATADATE_FROM,
                        formatDateTime(equipment.getFromDate()),
                        ARG_FILE_DATADATE_TO,
                        formatDateTime(equipment.getToDate())
                    }), dateString);
                }

                buff.append(dateString);
                buff.append("&nbsp;</td>");
                equipmentRow(buff, equipment.getAntenna(),
                             equipment.getAntennaSerial());
                equipmentRow(buff, equipment.getDome(),
                             equipment.getDomeSerial());
                equipmentRow(buff, equipment.getReceiver(),
                             equipment.getReceiverSerial());
                buff.append("<td>&nbsp;");
                //                buff.append(equipment.getXyzOffset()[0] + "/"
                //                            + equipment.getXyzOffset()[1] + "/"
                //                            + equipment.getXyzOffset()[2]);

                buff.append("" + equipment.getXyzOffset()[2]);
                buff.append("&nbsp;</td>");
                buff.append("</tr>");
            }
            if (cnt > 0) {
                buff.append(HtmlUtil.formTableClose());
                pw.append(formEntryTop(request, msgLabel("Equipment"),
                                       HtmlUtil.makeShowHideBlock("",
                                           buff.toString(), false)));
            }
        }

    }


    /** _more_ */
    private int tabCnt = 0;

    /**
     * _more_
     *
     * @param request The request
     * @param pw _more_
     * @param gsacObject _more_
     * @param metadata _more_
     * @param fullMetadata _more_
     * @param state _more_
     *
     * @throws IOException On badness
     */
    private void processMetadata(GsacRequest request, Appendable pw,
                                 GsacResource gsacObject,
                                 GsacMetadata metadata, boolean fullMetadata,
                                 Hashtable state)
            throws IOException {

        if ( !metadata.getForDisplay()) {
            return;
        }

        if (metadata instanceof MetadataGroup) {
            MetadataGroup      group = (MetadataGroup) metadata;
            List<GsacMetadata> list  = group.getMetadata();
            if (group.getDisplayType().equals(MetadataGroup.DISPLAY_LIST)) {
                for (GsacMetadata childMetadata : list) {
                    processMetadata(request, pw, gsacObject, childMetadata,
                                    fullMetadata, state);
                }
                return;
            }

            if (group.getDisplayType().equals(MetadataGroup.DISPLAY_TABS)) {
                List<String> titles = new ArrayList();
                List<String> tabs   = new ArrayList();
                for (GsacMetadata childMetadata : list) {
                    StringBuffer buffer = new StringBuffer();
                    processMetadata(request, buffer, gsacObject,
                                    childMetadata, fullMetadata, state);
                    titles.add(HtmlUtil.space(1) + childMetadata.getLabel()
                               + HtmlUtil.space(1));
                    tabs.add(buffer.toString());
                }

                StringBuffer tabHtml = new StringBuffer();
                String       tabId   = "tabId" + (tabCnt++);
                tabHtml.append("\n\n");
                tabHtml.append(HtmlUtil.open(HtmlUtil.TAG_DIV,
                                             HtmlUtil.id(tabId)));
                tabHtml.append(HtmlUtil.open(HtmlUtil.TAG_UL));
                int cnt = 1;
                for (String title : titles) {
                    tabHtml.append("<li><a href=\"#" + tabId + "-" + (cnt++)
                                   + "\">" + title + "</a></li>");
                }
                tabHtml.append(HtmlUtil.close(HtmlUtil.TAG_UL));
                cnt = 1;
                for (String tabContents : tabs) {
                    tabHtml.append(HtmlUtil.div(tabContents,
                            HtmlUtil.id(tabId + "-" + (cnt++))));
                    tabHtml.append("\n");
                }

                tabHtml.append(HtmlUtil.close(HtmlUtil.TAG_DIV));
                tabHtml.append("\n");
                tabHtml.append(
                    HtmlUtil.script(
                        "\njQuery(function(){\njQuery('#" + tabId
                        + "').tabs();\n});\n"));
                tabHtml.append("\n\n");
                //                                String tabHtml =  HtmlUtil.makeTabs(titles, tabs, true);

                pw.append(formEntryTop(request, group.getLabel(),
                                       HtmlUtil.makeShowHideBlock("",
                                           tabHtml.toString(), true)));
                return;
            }

            if (group.getDisplayType().equals(
                    MetadataGroup.DISPLAY_FORMTABLE)) {
                StringBuffer buffer = new StringBuffer();
                boolean      didone = false;
                for (GsacMetadata childMetadata : list) {
                    StringBuffer tmp = new StringBuffer();
                    processMetadata(request, tmp, gsacObject, childMetadata,
                                    fullMetadata, state);
                    if (tmp.length() > 0) {
                        if ( !didone) {
                            buffer.append(HtmlUtil.formTable());
                        }
                        didone = true;
                        buffer.append(tmp);
                    }

                }
                if (didone) {
                    buffer.append(HtmlUtil.formTableClose());
                    pw.append(formEntryTop(request, group.getLabel(),
                                           HtmlUtil.makeShowHideBlock("",
                                               buffer.toString(), true)));
                }
                return;
            }

            throw new IllegalArgumentException(
                "Unknown metadata display type:" + group.getDisplayType());
        }

        if (metadata instanceof ImageMetadata) {
            ImageMetadata imageMetadata = (ImageMetadata) metadata;
            String img =
                HtmlUtil.img(imageMetadata.getUrl(), "",
                             HtmlUtil.attr(HtmlUtil.ATTR_BORDER, "0")
                             + HtmlUtil.attr(HtmlUtil.ATTR_WIDTH, "400"));
            img = HtmlUtil.href(imageMetadata.getUrl(), img,
                                " rel=\"lightbox\" ");
            pw.append(img);
            return;

        }


        if (metadata instanceof LinkMetadata) {
            LinkMetadata mtd = (LinkMetadata) metadata;
            pw.append(formEntry(request, msgLabel("Link"),
                                HtmlUtil.href(mtd.getUrl(), mtd.getLabel())));
            return;
        }

        if (metadata instanceof PropertyMetadata) {
            PropertyMetadata mtd   = (PropertyMetadata) metadata;
            String           value = mtd.getValue();
            if (value.indexOf("\n") >= 0) {
                pw.append(formEntryTop(request, mtd.getLabel() + ":",
                                       HtmlUtil.makeShowHideBlock(msg(""),
                                           "<pre>" + value + "</pre>",
                                           false)));

            } else {
                pw.append(formEntry(request, mtd.getLabel() + ":", value));
            }
            return;
        }

        metadata.addHtml(request, this, pw);


    }

    /**
     * _more_
     *
     * @param tabHtml _more_
     * @param titles _more_
     * @param tabs _more_
     */
    public void makeTabs(StringBuffer tabHtml, List<String> titles,
                         List<String> tabs) {
        String tabId = "tabId" + (tabCnt++);
        tabHtml.append(HtmlUtil.open(HtmlUtil.TAG_DIV, HtmlUtil.id(tabId)));
        tabHtml.append(HtmlUtil.open(HtmlUtil.TAG_UL));
        int cnt = 1;
        for (String title : titles) {
            tabHtml.append("<li><a href=\"#" + tabId + "-" + (cnt++) + "\">"
                           + title + "</a></li>");
        }
        tabHtml.append(HtmlUtil.close(HtmlUtil.TAG_UL));
        cnt = 1;
        for (String tabContents : tabs) {
            tabHtml.append(HtmlUtil.div(tabContents,
                                        HtmlUtil.id(tabId + "-" + (cnt++))));
            tabHtml.append("\n");
        }

        tabHtml.append(HtmlUtil.close(HtmlUtil.TAG_DIV));
        tabHtml.append("\n");
        tabHtml.append(HtmlUtil.script("\njQuery(function(){\njQuery('#"
                                       + tabId + "').tabs();\n});\n"));
        tabHtml.append("\n\n");
        //                                String tabHtml =  HtmlUtil.makeTabs(titles, tabs, true);

    }



    /**
     * _more_
     *
     * @param buff _more_
     * @param name _more_
     * @param serial _more_
     *
     * @throws IOException On badness
     */
    private void equipmentRow(Appendable buff, String name, String serial)
            throws IOException {
        buff.append("<td>&nbsp;");
        if (name != null) {
            buff.append(name);
            if (serial != null) {
                buff.append("<br><i>#" + serial + "</i>");
            }
        }
        buff.append("&nbsp;</td>");

    }

    /**
     * _more_
     *
     * @param lat _more_
     * @param lon _more_
     *
     * @return _more_
     */
    public static String jsLLP(double lat, double lon) {
        if (lat < -90) {
            lat = -90;
        }
        if (lat > 90) {
            lat = 90;
        }
        if (lon < -180) {
            lon = -180;
        }
        if (lon > 180) {
            lon = 180;
        }
        return "new OpenLayers.LonLat(" + lon + "," + lat + ")";

    }



    /**
     * _more_
     *
     * @param request The request
     * @param resources Resources to show in map
     * @param pw appendable to append to
     * @param width map width
     * @param height map height
     *
     * @return the map
     *
     * @throws IOException On badness
     */
    public String createMap(GsacRequest request,
                            List<GsacResource> resources, Appendable pw,
                            int width, int height)
            throws IOException {
        String       mapVarName = "map" + HtmlUtil.blockCnt++;
        StringBuffer mapSB      = new StringBuffer();
        initMap(request, mapVarName, mapSB, width, height, false);
        pw.append(HtmlUtil.makeShowHideBlock(msg("Map"), mapSB.toString(),
                                             false));
        StringBuffer js = new StringBuffer();
        for (GsacResource resource : resources) {
            if ( !resource.hasEarthLocation()) {
                continue;
            }
            String href    = makeResourceViewHref(resource);
            String mapInfo = href + HtmlUtil.br() + resource.getLabel();
            //Only include the full html when there are fewer than 100 resource
            if ((resources.size() < 100) && (resource instanceof GsacSite)) {
                StringBuffer mapInfoSB = new StringBuffer();
                //TODO: change this to getResourceHtml
                getSiteHtml(request, mapInfoSB, (GsacSite) resource, false,
                            false, true);
                mapInfo = mapInfoSB.toString();
            }
            mapInfo = mapInfo.replace("\r", " ");
            mapInfo = mapInfo.replace("\n", " ");
            mapInfo = mapInfo.replace("\"", "\\\"");
            mapInfo = mapInfo.replace("/script", "\\/script");
            String url = getIconUrl(resource);
            js.append("var siteInfo = \"" + mapInfo + "\";\n");
            String entryId = resource.getId();
            entryId = cleanIdForJS(entryId);
            js.append(mapVarName + ".addMarker('" + entryId + "',"
                      + jsLLP(resource.getLatitude(),
                              resource.getLongitude()) + "," + "\"" + url
                                  + "\"" + "," + "siteInfo);\n");
        }
        return js.toString();
    }




    /**
     * make a list of tfos from the 2d string array
     *
     * @param args name/id pairs or just name values
     *
     * @return tfos
     */
    public List<TwoFacedObject> toTfoList(String[][] args) {
        List<TwoFacedObject> tfos = new ArrayList<TwoFacedObject>();
        for (String[] pair : args) {
            if (pair.length == 1) {
                tfos.add(new TwoFacedObject(pair[0]));
            } else {
                tfos.add(new TwoFacedObject(pair[1], pair[0]));
            }
        }
        return tfos;
    }

    /**
     * _more_
     *
     * @param things _more_
     *
     * @return _more_
     */
    public List<TwoFacedObject> toTfoList(List things) {
        List<TwoFacedObject> tfos = new ArrayList<TwoFacedObject>();
        tfos.add(new TwoFacedObject(ARG_UNDEFINED_LABEL,
                                    ARG_UNDEFINED_VALUE));
        for (Object obj : things) {
            if (obj instanceof NamedThing) {
                NamedThing thing = (NamedThing) obj;
                tfos.add(new TwoFacedObject(thing.getName(), thing.getId()));
            } else {
                tfos.add(new TwoFacedObject(obj.toString()));
            }
        }
        return tfos;
    }



    /**
     * _more_
     *
     * @param request The request
     * @param name _more_
     * @param formName _more_
     * @param date _more_
     * @param timezone _more_
     * @param includeTime _more_
     *
     * @return _more_
     */
    public String makeDateInput(GsacRequest request, String name,
                                String formName, Date date, String timezone,
                                boolean includeTime) {

        String dateArg    = request.get(name, "");
        String timeArg    = request.get(name + ".time", "");
        String dateString = ((date == null)
                             ? dateArg
                             : formatDate(date));
        String timeString = ((date == null)
                             ? timeArg
                             : formatTime(date));

        return HtmlUtil.input(
            name, dateString,
            HtmlUtil.SIZE_10 + HtmlUtil.id(name)
            + HtmlUtil.title(dateHelp)) + getCalendarSelector(formName, name)
                                        + ( !includeTime
                                            ? ""
                                            : " T:"
                                            + HtmlUtil.input(
                                                name + ".time", timeString,
                                                    HtmlUtil.sizeAttr(6)
                                                        + HtmlUtil.attr(
                                                            HtmlUtil.ATTR_TITLE,
                                                                timeHelp)));
    }


    /**
     * make date selector
     *
     * @param formName which form
     * @param fieldName which field to use
     *
     * @return date selector
     */
    public String getCalendarSelector(String formName, String fieldName) {
        String anchorName = "anchor." + fieldName;
        String divName    = "div." + fieldName;
        String call = HtmlUtil.call("selectDate",
                                    HtmlUtil.comma(HtmlUtil.squote(divName),
        //                              "document.forms['"  + formName + "']." + fieldName, 
        "findFormElement('" + formName + "','" + fieldName
                            + "')", HtmlUtil.squote(anchorName),
                                    HtmlUtil.squote(
                                        "yyyy-MM-dd"))) + "return false;";
        return HtmlUtil
            .href("#", HtmlUtil
                .img(getRepository()
                    .iconUrl("/calendar.png"), " Choose date", HtmlUtil
                    .attr(HtmlUtil.ATTR_BORDER, "0")), HtmlUtil
                        .onMouseClick(call) + HtmlUtil
                        .attrs(HtmlUtil.ATTR_NAME, anchorName, HtmlUtil
                            .ATTR_ID, anchorName)) + HtmlUtil
                                .div("", HtmlUtil
                                    .attrs(HtmlUtil.ATTR_ID, divName, HtmlUtil
                                        .ATTR_STYLE, "position:absolute;visibility:hidden;background-color:white;layer-background-color:white;"));
    }




    /**
     * _more_
     *
     * @param request The request
     * @param sb Buffer to append to
     * @param sites _more_
     */
    public void makeSiteHtmlTable(GsacRequest request, StringBuffer sb,
                                  List<GsacSite> sites) {

        if ((sites.size() > 0) && (SITE_TABLE_LABELS == null)) {
            List<String> labels     = new ArrayList<String>();
            List<String> sortValues = new ArrayList<String>();
            String remoteHref = getRepository().getRemoteHref(sites.get(0));
            if (remoteHref.length() > 0) {
                //                labels.add("");
                //                sortValues.add("");
            }
            //            labels.add("");
            //            sortValues.add("");
            if (getDoSiteCode()) {
                labels.add(msg("Site Code").replace(" ", "&nbsp;"));
                sortValues.add(SORT_SITE_CODE);
            }
            labels.add(msg("Name"));
            sortValues.add(SORT_SITE_NAME);
            if (getDoSiteType()) {
                labels.add(msg("Type"));
                sortValues.add(SORT_SITE_TYPE);
            }
            labels.add(msg("Location") + " (lat,lon,m)");
            sortValues.add("");
            labels.add(msg("Date Range"));
            sortValues.add("");
            if (getDoResourceGroup()) {
                labels.add(msg("Groups"));
                sortValues.add("");
            }
            SITE_TABLE_LABELS     = Misc.listToStringArray(labels);
            SITE_TABLE_SORTVALUES = Misc.listToStringArray(sortValues);
        }


        int cnt = 0;
        for (GsacSite site : sites) {
            if (cnt++ == 0) {
                try {
                    String url = getResourceManager(site).makeSearchUrl();
                    sb.append(HtmlUtil.formPost(url,
                            HtmlUtil.attr("name", "searchform")));;
                    sb.append(HtmlUtil.submit(msg("View Selected Sites"),
                            ARG_SEARCH));
                    sb.append(HtmlUtil.space(2));
                    sb.append(
                        "<table class=\"gsac-result-table\" cellspacing=0 cellpadding=0 border=0 width=100%>");
                    makeSortHeader(request, sb, ARG_SITE_PREFIX,
                                   SITE_TABLE_LABELS, SITE_TABLE_SORTVALUES);
                } catch (Exception exc) {
                    throw new RuntimeException(exc);
                }
            }


            String href = makeResourceViewHref(site);
            openEntryRow(sb, site.getSiteId(), URL_SITE_VIEW, ARG_SITE_ID);
            String cbx = HtmlUtil.checkbox(ARG_SITE_ID, site.getSiteId(),
                                           false);

            String clickEvent = getEntryEventJS(site.getSiteId(),
                                    URL_SITE_VIEW, ARG_SITE_ID)[1];
            sb.append(HtmlUtil.col(cbx));
            String remoteHref = getRepository().getRemoteHref(site);
            if (remoteHref.length() > 0) {
                sb.append(HtmlUtil.col(remoteHref));
            }
            sb.append("</tr></table></td>\n");
            sb.append(HtmlUtil.col(href));
            sb.append(HtmlUtil.col(site.getName(), clickEvent));

            if (getDoSiteType()) {
                if (site.getType() != null) {
                    sb.append(HtmlUtil.col(site.getType().getName(),
                                           clickEvent));
                } else {
                    sb.append(HtmlUtil.col("&nbsp;", clickEvent));
                }
            }


            sb.append("<td " + clickEvent + ">");
            sb.append(formatLatLon(site.getLatitude()));
            sb.append(",");
            sb.append(formatLatLon(site.getLongitude()));
            sb.append(",");
            sb.append(formatElevation(site.getElevation()));
            sb.append("</td>");

            sb.append("<td " + clickEvent + ">");
            if (site.getFromDate() != null) {
                sb.append(formatDate(site.getFromDate()));
                sb.append(" - ");
                sb.append(formatDate(site.getToDate()));
            } else {
                sb.append("N/A");
            }
            sb.append("</td>");

            if (getDoResourceGroup()) {
                sb.append(HtmlUtil.col(getGroupHtml(site.getResourceGroups(),
                        site.getResourceClass(), true) + "&nbsp;"));
            }

            sb.append("</tr>\n");
        }
        if (cnt > 0) {
            sb.append("</table>");
            sb.append(HtmlUtil.formClose());
        }
    }


    /**
     * _more_
     *
     * @param sb _more_
     * @param id _more_
     * @param baseUrl _more_
     * @param urlArg _more_
     */
    public void openEntryRow(Appendable sb, String id, String baseUrl,
                             String urlArg) {
        try {
            String   rowId  = "row_" + id;
            String   divId  = "div_" + id;
            String   imgId  = "img_" + id;
            String[] events = getEntryEventJS(id, baseUrl, urlArg);
            String   event1 = events[0];
            String   event2 = events[1];
            String dartImg =
                HtmlUtil.img(iconUrl("/blank.gif"), "",
                             event2
                             + HtmlUtil.attr(HtmlUtil.ATTR_WIDTH, "10")
                             + HtmlUtil.attr(HtmlUtil.ATTR_HEIGHT, "10")
                             + HtmlUtil.id(imgId));
            sb.append("<tr valign=\"bottom\" " + HtmlUtil.id(rowId) + " "
                      + event1 + ">");

            sb.append(
                "<td  " + HtmlUtil.id(divId)
                + "><table border=0 class=\"gsac-innerresult-table\" cellpadding=0 cellspacing=0><tr>");
            sb.append(HtmlUtil.col(dartImg));
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }


    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    private String cleanIdForJS(String s) {
        s = s.replace("'", "\\'");
        return s;
    }


    /**
     * _more_
     *
     * @param entryId _more_
     * @param baseUrl _more_
     * @param urlArg _more_
     *
     * @return _more_
     */
    public String[] getEntryEventJS(String entryId, String baseUrl,
                                    String urlArg) {
        String xmlUrl = HtmlUtil.url(makeUrl(baseUrl), new String[] { urlArg,
                entryId, ARG_WRAPXML, "true", });
        entryId = cleanIdForJS(entryId);

        String event1 = HtmlUtil.onMouseOver(
                            HtmlUtil.call(
                                "entryRowOver",
                                HtmlUtil.squote(
                                    entryId))) + HtmlUtil.onMouseOut(
                                        HtmlUtil.call(
                                            "entryRowOut",
                                            HtmlUtil.squote(entryId)));

        String event2 = HtmlUtil.onMouseClick(HtmlUtil.call("entryRowClick",
                            "event," + HtmlUtil.squote(entryId) + ","
                            + HtmlUtil.squote(xmlUrl)));
        return new String[] { event1, event2 };
    }



    /**
     * _more_
     *
     * @param request The request
     * @param sb Buffer to append to
     * @param prefix _more_
     * @param labels _more_
     * @param sortValues _more_
     *
     * @throws IOException On badness
     */
    public void makeSortHeader(GsacRequest request, Appendable sb,
                               String prefix, String[] labels,
                               String[] sortValues)
            throws IOException {
        String extra = " class=\"gsac-result-header\" ";
        sb.append("<tr>");
        sb.append(HtmlUtil.col("&nbsp;", extra));
        String  valueArg     = prefix + ARG_SORT_VALUE_SUFFIX;
        String  orderArg     = prefix + ARG_SORT_ORDER_SUFFIX;
        boolean sortCapable  = getRepository().isCapable(valueArg);
        boolean orderCapable = getRepository().isCapable(orderArg);
        String  sortBy       = request.get(valueArg, "");
        for (int i = 0; i < labels.length; i++) {
            if (labels[i] == null) {
                continue;
            }
            sb.append("<td " + extra + "  nowrap>");
            if ( !sortCapable || (sortValues[i].length() == 0)) {
                sb.append(labels[i]);
                continue;
            }
            Hashtable<String, String> sortMap = new Hashtable<String,
                                                    String>();
            sortMap.put(valueArg, sortValues[i]);
            if (sortBy.equals(sortValues[i])) {
                boolean ascending = request.get(
                                        orderArg,
                                        SORT_ORDER_ASCENDING).equals(
                                            SORT_ORDER_ASCENDING);
                String img = orderCapable
                             ? iconUrl(ascending
                                       ? "/updart.png"
                                       : "/downdart.png")
                             : "";
                if (orderCapable) {
                    sortMap.put(orderArg, ascending
                                          ? SORT_ORDER_DESCENDING
                                          : SORT_ORDER_ASCENDING);
                }
                sb.append(HtmlUtil.href(request.getUrl(sortMap), labels[i])
                          + HtmlUtil.img(img));
            } else {
                sb.append(HtmlUtil.href(request.getUrl(sortMap), labels[i]));
            }
            sb.append("</td>");
        }
        sb.append("</tr>");
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param buttons _more_
     * @param resourceClass _more_
     */
    public void addFormSwitchButton(GsacRequest request,
                                    StringBuffer buttons,
                                    ResourceClass resourceClass) {
        for (GsacResourceManager gom :
                getRepository().getResourceManagers()) {
            if (gom.getResourceClass().equals(resourceClass)) {
                continue;
            }
            String arg = ARG_SEARCH + gom.getResourceClass().getName();
            buttons.append("<td align=right>");
            String switchForm =
                HtmlUtil.tag(HtmlUtil.TAG_INPUT,
                             HtmlUtil.cssClass("gsac-gobutton")
                             + HtmlUtil.attrs(new String[] {
                HtmlUtil.ATTR_NAME, arg, HtmlUtil.ATTR_TYPE,
                HtmlUtil.TYPE_SUBMIT, HtmlUtil.ATTR_VALUE,
                msg(gom.getResourceLabel(false) + " Search Form"),
                HtmlUtil.ATTR_CLASS, "gsac-download-button",
                HtmlUtil.ATTR_TITLE, msg("Switch search form"),
            }));

            buttons.append(switchForm);
            buttons.append("</td>");
        }
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param response _more_
     * @param resourceClass _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public boolean checkFormSwitch(GsacRequest request,
                                   GsacResponse response,
                                   ResourceClass resourceClass)
            throws Exception {
        for (GsacResourceManager gom :
                getRepository().getResourceManagers()) {
            String arg = ARG_SEARCH + gom.getResourceClass().getName();
            if (request.defined(arg)) {
                request.remove(ARG_OUTPUT);
                for (GsacResourceManager gom2 :
                        getRepository().getResourceManagers()) {
                    String arg2 = ARG_SEARCH
                                  + gom2.getResourceClass().getName();
                    request.remove(arg2);
                }
                String args        = request.getUrlArgs();
                String redirectUrl = gom.makeFormUrl() + "?" + args;
                response.sendRedirect(redirectUrl);
                response.endResponse();
                return true;
            }
        }
        return false;
    }

}
