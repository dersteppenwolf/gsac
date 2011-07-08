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
public class BrowseOutputHandler extends HtmlOutputHandler {

    /** url arg of what we are listing */
    public static final String ARG_BROWSE_WHAT = "what";

    /** url argument for first letter search or browse */
    public static final String ARG_LETTER = "letter";

    /** _more_ */
    public static final String WHAT_TYPES = "types";


    /** xml attribute */
    public static final String ATTR_ID = "id";

    /** xml attribute */
    public static final String ATTR_NAME = "name";

    /** output id */
    public static final String OUTPUT_BROWSE_HTML = "browse.html";

    /** csv listing */
    public static final String OUTPUT_BROWSE_CSV = "browse.csv";

    /** xml listing */
    public static final String OUTPUT_BROWSE_XML = "browse.xml";

    /** _more_ */
    private List<Capability> browseCapabilities;


    /**
     * ctor
     *
     * @param gsacServlet the servlet
     */
    public BrowseOutputHandler(GsacRepository gsacServlet) {
        super(gsacServlet);
        checkInit();

        getRepository().addOutput(OUTPUT_GROUP_BROWSE,
                                  new GsacOutput(this, OUTPUT_BROWSE_HTML,
                                      "HTML"));
        getRepository().addOutput(OUTPUT_GROUP_BROWSE,
                                  new GsacOutput(this, OUTPUT_BROWSE_CSV,
                                      "CSV"));
        getRepository().addOutput(OUTPUT_GROUP_BROWSE,
                                  new GsacOutput(this, OUTPUT_BROWSE_XML,
                                      "XML"));
    }

    /**
     * _more_
     */
    private void checkInit() {
        if (browseCapabilities == null) {
            List<Capability>   tmp = new ArrayList<Capability>();
            GsacRepositoryInfo gri = getRepository().getRepositoryInfo();
            for (CapabilityCollection collection : gri.getCollections()) {
                for (Capability capability : collection.getCapabilities()) {
                    if (capability.isEnumeration()
                            || capability.getBrowse()) {
                        tmp.add(capability);
                    }
                }
            }
            browseCapabilities = tmp;
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
    public void handleBrowseRequest(GsacRequest request,
                                    GsacResponse response)
            throws Exception {
        List   things    = null;
        String what      = request.get(ARG_BROWSE_WHAT, "");
        String searchArg = null;
        String output    = request.get(ARG_OUTPUT, OUTPUT_BROWSE_HTML);
        for (Capability capability : browseCapabilities) {
            if (what.equals(capability.getId())) {
                if (capability.isEnumeration()) {
                    handleEnumerationRequest(request, response, capability);
                } else {
                    handleStringRequest(request, response, capability);
                }
                return;
            }
        }

        StringBuffer html = new StringBuffer();
        initHtml(request, response, html);
        html.append(getHeader(request, null));
        finishHtml(request, response, html);
    }


    /**
     * handle the listing by beginning of string  letter
     *
     * @param request the request
     * @param response the response
     * @param capability _more_
     *
     * @throws Exception on badness
     */
    public void handleStringRequest(GsacRequest request,
                                    GsacResponse response,
                                    Capability capability)
            throws Exception {
        String       letter = request.get(ARG_LETTER, "").toUpperCase();
        StringBuffer html   = new StringBuffer();
        initHtml(request, response, html);
        html.append(getHeader(request, capability));
        String[] letters = {
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
            "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"
        };
        String       id    = capability.getId();

        List<String> links = new ArrayList<String>();
        for (String l : letters) {
            if (letter.equals(l)) {
                links.add(
                    HtmlUtil.span(
                        HtmlUtil.b(l),
                        HtmlUtil.cssClass("gsac-firstletternav")));
            } else {
                String url = getRepository().getUrl(URL_BROWSE_BASE) + "?"
                             + ARG_BROWSE_WHAT + "=" + id + "&" + ARG_LETTER
                             + "=" + l;
                links.add(
                    HtmlUtil.href(
                        url, l, HtmlUtil.cssClass("gsac-firstletternav")));
            }
        }

        html.append(
            HtmlUtil.tag(
                HtmlUtil.TAG_CENTER, HtmlUtil.cssClass("gsac-navheader"),
                StringUtil.join(
                    "<span class=\"gsac-separator\">&nbsp;|&nbsp;</span>",
                    links)));
        Hashtable<String, String> outputMap = new Hashtable<String, String>();

        if (letter.length() > 0) {
            GsacRequest searchRequest = new GsacRequest(request);
            searchRequest.put(capability.getId(), letter + "*");
            //        searchRequest.put(ARG_SITE_CODE_SEARCHTYPE, SEARCHTYPE_BEGINSWITH);
            searchRequest.put(ARG_LIMIT, 10000 + "");

            getRepository().processRequest(GsacSite.CLASS_SITE, searchRequest,
                                           response);
            List<GsacSite> sites = response.getSites();
            if (sites.size() == 0) {
                html.append(
                    getRepository().makeInformationDialog(
                        msg("No results found")));
            } else {
                makeSiteHtmlTable(request, html, sites);
            }
        }
        finishHtml(request, response, html);
    }

    /**
     * get the nav header
     *
     * @param request the request
     * @param capability _more_
     *
     * @return nav header
     */
    public String getHeader(GsacRequest request, Capability capability) {
        List<String> links = new ArrayList<String>();

        for (Capability cap : browseCapabilities) {
            String id    = cap.getId();
            String label = cap.getLabel();
            if ((capability != null)
                    && capability.getId().equals(cap.getId())) {
                links.add(
                    HtmlUtil.span(
                        label, HtmlUtil.cssClass("gsac-header2-selected")));
            } else {
                String url = getRepository().getUrl(URL_BROWSE_BASE) + "?"
                             + ARG_BROWSE_WHAT + "=" + id;
                links.add(
                    HtmlUtil.href(
                        url, label,
                        HtmlUtil.cssClass("gsac-header2-notselected")));
            }
        }


        String sep =
            HtmlUtil.span("|", HtmlUtil.cssClass("gsac2-header-separator"));
        String navHeader = HtmlUtil.tag(HtmlUtil.TAG_CENTER,
                                        HtmlUtil.cssClass("gsac-header2"),
                                        StringUtil.join(sep, links));
        return navHeader;
    }


    /**
     * make the html page
     *
     * @param request the request
     * @param response the response
     * @param capability _more_
     *
     * @throws Exception on badness
     */
    public void handleEnumerationRequest(GsacRequest request,
                                         GsacResponse response,
                                         Capability capability)
            throws Exception {

        List things = capability.getEnums();
        java.util.Collections.sort(things);
        StringBuffer sb          = new StringBuffer();
        String       firstLetter = null;
        List<String> header      = new ArrayList<String>();
        String       url         =
            capability.getCollection().getRelativeUrl();
        String       id          = capability.getId();

        sb.append(HtmlUtil.form(url, HtmlUtil.attr("name", "searchform")));;
        sb.append(HtmlUtil.submit(msg("Search"), ARG_SEARCH));
        sb.append("<div style=\"margin:10px;margin-left:15px;\">");
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
                header.add("<a class=\"gsac-firstletternav\" href=\"#"
                           + firstLetter + "\">" + firstLetter + "</a>");
                if (things.size() > 10) {
                    sb.append("<a name=\"" + firstLetter + "\">\n");
                    sb.append(HtmlUtil.p());
                    sb.append(HtmlUtil.bold(firstLetter));
                    sb.append(HtmlUtil.br());
                }
            }
            sb.append(HtmlUtil.checkbox(id, thing.getId(), false));
            sb.append(HtmlUtil.space(2));
            sb.append(getSearchLink(thing, url, id));
            sb.append(HtmlUtil.br());
        }
        sb.append("</div>");
        StringBuffer html = new StringBuffer();
        initHtml(request, response, html);
        html.append(getHeader(request, capability));
        if ((things.size() > 10) && (header.size() > 4)) {
            String headerHtml =
                StringUtil.join(
                    "<span class=\"gsac-separator\">&nbsp;|&nbsp;</span>",
                    header);
            html.append(HtmlUtil.tag(HtmlUtil.TAG_CENTER,
                                     HtmlUtil.cssClass("gsac-navheader"),
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
