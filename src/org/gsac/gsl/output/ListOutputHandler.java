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
import org.gsac.gsl.model.*;
import org.gsac.gsl.util.*;

import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.util.ArrayList;


import java.util.Hashtable;
import java.util.List;

import javax.servlet.*;
import javax.servlet.http.*;


/**
 * Class description
 *
 *
 * @version        Enter version here..., Wed, May 19, '10
 * @author         Enter your name here...
 */
public class ListOutputHandler extends HtmlOutputHandler {

    /** url arg of what we are listing */
    public static final String ARG_BROWSE_WHAT = "what";

    /** url argument for first letter search or browse */
    public static final String ARG_LETTER = "letter";

    /** _more_ */
    public static final String WHAT_TYPES = "types";

    /** what are we listing */
    public static final String WHAT_SITE_CODES = "site.code";


    /** what are we listing */
    public static final String WHAT_SITE_GROUPS = "site.group";

    /** what are we listing */
    public static final String WHAT_SITE_TYPES = "site.type";

    /** what are we listing */
    public static final String WHAT_SITE_STATUS = "site.status";

    /** xml tag */
    public static final String TAG_GROUPS = "groups";

    /** xml tag */
    public static final String TAG_GROUP = "group";

    /** xml attribute */
    public static final String ATTR_ID = "id";

    /** xml attribute */
    public static final String ATTR_NAME = "name";

    /** output id */
    public static final String OUTPUT_LIST_HTML = "list.html";

    /** csv listing */
    public static final String OUTPUT_LIST_CSV = "list.csv";

    /** xml listing */
    public static final String OUTPUT_LIST_XML = "list.xml";



    /** _more_ */
    private static String[] WHATS;

    /** _more_ */
    private static String[] LABELS;



    /**
     * ctor
     *
     * @param gsacServlet the servlet
     */
    public ListOutputHandler(GsacServlet gsacServlet) {
        super(gsacServlet);
        checkInit();


        getServlet().addListOutput(new GsacOutput(this, OUTPUT_LIST_HTML,
                "HTML"));
        getServlet().addListOutput(new GsacOutput(this, OUTPUT_LIST_DEFAULT,
                "Default"));
        getServlet().addListOutput(new GsacOutput(this, OUTPUT_LIST_CSV,
                "CSV"));
        getServlet().addListOutput(new GsacOutput(this, OUTPUT_LIST_XML,
                "XML"));
    }

    /**
     * _more_
     */
    private void checkInit() {
        if (WHATS == null) {
            List<String> whats  = new ArrayList<String>();
            List<String> labels = new ArrayList<String>();
            if (getDoSiteCode()) {
                whats.add(WHAT_SITE_CODES);
                labels.add("Codes");
            }
            LABELS = Misc.listToStringArray(labels);
            WHATS  = Misc.listToStringArray(whats);
        }
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param response _more_
     * @param output _more_
     *
     * @throws Exception _more_
     */
    public void handleTypesRequest(GsacRequest request,
                                   GsacResponse response, String output)
            throws Exception {

        List things = new ArrayList();

        for (int i = 0; i < WHATS.length; i++) {
            String id = WHATS[i];
            //Skip site codes
            if (id.equals(WHAT_SITE_CODES)) {
                continue;
            }
            String label = msg(LABELS[i]);
            things.add(new IdLabel(id, label));
        }
        List<Capability> capabilities =
            getRepository().getSiteQueryCapabilities();

        for (Capability capability : capabilities) {
            if ( !capability.isEnumeration()) {
                continue;
            }
            String id    = capability.getId();
            String label = capability.getLabel();
            things.add(new IdLabel(id, label));
        }

        if (output.equals(OUTPUT_LIST_CSV)) {
            handleCsvRequest(request, response, WHAT_TYPES, things);
        } else {
            handleXmlRequest(request, response, WHAT_TYPES, things);
        }
    }


    /**
     * Handle the request
     * @param request the request
     * @param response the response
     *
     *
     * @throws Exception on badness
     */
    public void handleListRequest(GsacRequest request, GsacResponse response)
            throws Exception {
        List   things    = null;
        String what      = request.get(ARG_BROWSE_WHAT, WHAT_SITE_TYPES);
        String searchArg = null;
        String output    = request.get(ARG_OUTPUT, OUTPUT_LIST_DEFAULT);
        if (what.equals(WHAT_SITE_GROUPS)) {
            things    = getRepository().getSiteGroups();
            searchArg = ARG_SITE_GROUP;
        } else if (what.equals(WHAT_TYPES)) {
            handleTypesRequest(request, response, output);
            return;
        } else if (what.equals(WHAT_SITE_CODES)) {
            handleSiteCodeRequest(request, response, output);
            return;
        } else if (what.equals(WHAT_SITE_TYPES)) {
            things    = getRepository().getSiteTypes();
            searchArg = ARG_SITE_TYPE;
        } else if (what.equals(WHAT_SITE_STATUS)) {
            things    = getRepository().getSiteStatuses();
            searchArg = ARG_SITE_STATUS;
        } else {
            List<Capability> capabilities =
                getRepository().getSiteQueryCapabilities();
            for (Capability capability : capabilities) {
                if ( !capability.isEnumeration()) {
                    continue;
                }
                String arg = capability.getId();
                if (arg.equals(what)) {
                    searchArg = arg;
                    things    = capability.getEnums();
                    break;
                }
            }
        }

        if (output.equals(OUTPUT_LIST_HTML)
                || output.equals(OUTPUT_LIST_DEFAULT)) {
            handleHtmlRequest(request, response, what, things, searchArg);
        } else if (output.equals(OUTPUT_LIST_CSV)) {
            handleCsvRequest(request, response, what, things);
        } else if (output.equals(OUTPUT_LIST_XML)) {
            handleXmlRequest(request, response, what, things);
        } else {
            throw new IllegalArgumentException("Unknown output type:"
                    + output);
        }
    }


    /**
     * handle the site listing by site code letter
     *
     * @param request the request
     * @param response the response
     * @param output output type
     *
     * @throws Exception on badness
     */
    public void handleSiteCodeRequest(GsacRequest request,
                                      GsacResponse response, String output)
            throws Exception {
        String       letter = request.get(ARG_LETTER, "A").toUpperCase();
        StringBuffer html   = new StringBuffer();
        initHtml(request, response, html);
        html.append(getHeader(request, WHAT_SITE_CODES));
        String[] letters = {
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
            "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
        };

        List<String> links = new ArrayList<String>();
        for (String l : letters) {
            if (letter.equals(l)) {
                links.add(HtmlUtil.span(HtmlUtil.b(l),
                                        HtmlUtil.cssClass("firstletternav")));
            } else {
                String url = getServlet().getUrl(URL_LIST_BASE) + "?"
                             + ARG_BROWSE_WHAT + "=" + WHAT_SITE_CODES + "&"
                             + ARG_LETTER + "=" + l;
                links.add(HtmlUtil.href(url, l,
                                        HtmlUtil.cssClass("firstletternav")));
            }
        }

        html.append(
            HtmlUtil.tag(
                HtmlUtil.TAG_CENTER, HtmlUtil.cssClass("navheader"),
                StringUtil.join(
                    "<span class=\"separator\">&nbsp;|&nbsp;</span>",
                    links)));
        Hashtable<String, String> outputMap = new Hashtable<String, String>();

        GsacRequest               searchRequest = new GsacRequest(request);
        searchRequest.put(ARG_SITE_CODE, letter);
        searchRequest.put(ARG_SITE_CODE_SEARCHTYPE, SEARCHTYPE_BEGINSWITH);
        searchRequest.put(ARG_LIMIT, 10000 + "");
        getRepository().handleSiteRequest(searchRequest, response);
        List<GsacSite> sites = response.getSites();
        if (sites.size() == 0) {
            html.append(
                getServlet().makeInformationDialog(msg("No sites found")));
        } else {
            makeSiteHtmlTable(request, html, sites);
        }
        finishHtml(request, response, html);
    }

    /**
     * get the nav header
     *
     * @param request the request
     * @param what what are we viewing
     *
     * @return nav header
     */
    public String getHeader(GsacRequest request, String what) {
        List<String> links = new ArrayList<String>();

        for (int i = 0; i < WHATS.length; i++) {
            String id    = WHATS[i];
            String label = msg(LABELS[i]);
            if (what.equals(id)) {
                String lbl = HtmlUtil.b(label);
                links.add(lbl);
            } else {
                links.add(HtmlUtil.href(getServlet().getUrl(URL_LIST_BASE)
                                        + "?" + ARG_BROWSE_WHAT + "="
                                        + id, label));
            }
        }


        List<Capability> capabilities =
            getRepository().getSiteQueryCapabilities();
        for (Capability capability : capabilities) {
            if ( !capability.isEnumeration()) {
                continue;
            }
            String id    = capability.getId();
            String label = capability.getLabel();
            if (what.equals(id)) {
                String lbl = HtmlUtil.b(label);
                links.add(lbl);
            } else {
                links.add(HtmlUtil.href(getServlet().getUrl(URL_LIST_BASE)
                                        + "?" + ARG_BROWSE_WHAT + "="
                                        + id, label));
            }
        }

        String navHeader = HtmlUtil.tag(HtmlUtil.TAG_CENTER,
                                        HtmlUtil.cssClass("navheader"),
                                        StringUtil.join("&nbsp;|&nbsp;",
                                            links));

        String baseUrl = getServlet().getUrl(URL_LIST_BASE);
        if ( !what.equals(WHAT_SITE_CODES)) {
            String suffix = "/" + what.replace(".", "_");
            String urls = HtmlUtil.href(
                              baseUrl + suffix + ".csv" + "?"
                              + request.getUrlArgs(
                                  ARG_OUTPUT, OUTPUT_LIST_CSV), HtmlUtil.img(
                                  getServlet().iconUrl("/xls.png"),
                                  msg("Download CSV file"))) + " "
                                      + HtmlUtil.href(
                                          baseUrl + suffix + ".xml" + "?"
                                          + request.getUrlArgs(
                                              ARG_OUTPUT,
                                              OUTPUT_LIST_XML), HtmlUtil.img(
                                                  getServlet().iconUrl(
                                                      "/xml.png"), msg(
                                                      "Download XML file")));

            navHeader += urls;
        }

        return navHeader;
    }


    /**
     * make the html page
     *
     * @param request the request
     * @param response the response
     * @param what what are  we listing
     * @param things the named things we are listing
     * @param arg the url arg that we do a site search on
     *
     * @throws Exception on badness
     */
    public void handleHtmlRequest(GsacRequest request, GsacResponse response,
                                  String what, List things, String arg)
            throws Exception {

        StringBuffer sb          = new StringBuffer();
        String       firstLetter = null;
        List<String> header      = new ArrayList<String>();
        String       url         = makeUrl(URL_SITE_SEARCH);
        sb.append(HtmlUtil.form(url, HtmlUtil.attr("name", "searchform")));;
        sb.append(HtmlUtil.submit(msg("Search"), ARG_SEARCH));
        sb.append(HtmlUtil.br());
        StringBuffer letterBuffer = new StringBuffer();
        for (NamedThing thing : (List<NamedThing>) things) {
            String thingName = thing.getName();
            if ((thingName == null) || (thingName.trim().length() == 0)) {
                continue;
            }
            String thingFirstLetter = thing.getName().substring(0,
                                          1).toUpperCase();
            if ((firstLetter == null)
                    || !thingFirstLetter.equals(firstLetter)) {
                firstLetter = thingFirstLetter;
                header.add("<a class=\"firstletternav\" href=\"#"
                           + firstLetter + "\">" + firstLetter + "</a>");
                if (things.size() > 10) {
                    sb.append("<a name=\"" + firstLetter + "\">\n");
                    sb.append(HtmlUtil.p());
                    sb.append(HtmlUtil.bold(firstLetter));
                    sb.append(HtmlUtil.br());
                }
            }
            sb.append(HtmlUtil.checkbox(arg, thing.getId(), false));
            sb.append(HtmlUtil.space(2));
            sb.append(getSearchLink(thing, arg));
            sb.append(HtmlUtil.br());
        }
        StringBuffer html = new StringBuffer();
        initHtml(request, response, html);
        html.append(getHeader(request, what));
        if ((things.size() > 10) && (header.size() > 4)) {
            String headerHtml =
                StringUtil.join(
                    "<span class=\"separator\">&nbsp;|&nbsp;</span>", header);
            html.append(HtmlUtil.tag(HtmlUtil.TAG_CENTER,
                                     HtmlUtil.cssClass("navheader"),
                                     headerHtml));
        }

        sb.append(HtmlUtil.submit(msg("Search"), ARG_SEARCH));

        sb.append(HtmlUtil.formClose());

        html.append(sb);


        finishHtml(request, response, html);
    }


    /**
     * list the results as csv
     *
     * @param request the request
     * @param response the response
     * @param what what are we  showing
     * @param things things to show
     *
     * @throws Exception on badness
     */
    public void handleCsvRequest(GsacRequest request, GsacResponse response,
                                 String what, List things)
            throws Exception {
        StringBuffer sb = new StringBuffer();
        response.startResponse(GsacResponse.MIME_CSV);
        for (NamedThing thing : (List<NamedThing>) things) {
            sb.append(thing.getId());
            sb.append(",");
            sb.append(thing.getName());
            sb.append("\n");
        }
        finishResponse(request, response, sb);
    }

    /**
     * list the results as xml
     *
     * @param request the request
     * @param response the response
     * @param what what are we  showing
     * @param things things to show
     *
     * @throws Exception on badness
     */
    public void handleXmlRequest(GsacRequest request, GsacResponse response,
                                 String what, List things)
            throws Exception {
        StringBuffer sb      = new StringBuffer();
        String       tagName = what.replace("site.", "").replace(".", "_");
        response.startResponse(GsacResponse.MIME_XML);
        sb.append(XmlUtil.openTag(tagName + "_list", ""));


        for (NamedThing thing : (List<NamedThing>) things) {
            sb.append(XmlUtil.tag(tagName,
                                  XmlUtil.attrs(ATTR_ID, thing.getId(),
                                      ATTR_NAME, thing.getName())));
        }
        sb.append(XmlUtil.closeTag(tagName + "_list"));
        finishResponse(request, response, sb);
    }



}
