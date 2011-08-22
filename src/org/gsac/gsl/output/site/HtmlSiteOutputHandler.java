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

package org.gsac.gsl.output.site;



import org.gsac.gsl.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.output.*;
import org.gsac.gsl.util.*;

import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.TwoFacedObject;

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
public class HtmlSiteOutputHandler extends HtmlOutputHandler {

    /** output id */
    public static final String OUTPUT_SITE_HTML = "site.html";

    /** _more_ */
    private String flexigridTemplate;


    /**
     * ctor
     *
     * @param gsacRepository the repository
     * @param resourceClass _more_
     */
    public HtmlSiteOutputHandler(GsacRepository gsacRepository,
                                 ResourceClass resourceClass) {
        super(gsacRepository, resourceClass);
        getRepository().addOutput(getResourceClass(),
                                  new GsacOutput(this, OUTPUT_SITE_HTML,
                                      "Site HTML"));
        //        getRepository().addOutput(getResourceClass(), new GsacOutput(this,
        //                OUTPUT_SITE_DEFAULT, "Site Default"));
    }



    /**
     * handle the request
     *
     *
     * @param type _more_
     * @param request the request
     * @param response the response to write to
     *
     *
     * @throws Exception on badness
     */
    public void handleRequest(ResourceClass type, GsacRequest request,
                              GsacResponse response)
            throws Exception {

        StringBuffer sb = new StringBuffer();
        try {
            handleRequestInner(request, response, sb);
        } catch (IllegalArgumentException iae) {
            sb.append(
                getRepository().makeErrorDialog(
                    "An error has occurred:<br>" + iae.getMessage()));

            handleSearchForm(request, response, sb);
            finishHtml(request, response, sb);
        }
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param response _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    public void handleRequestInner(GsacRequest request,
                                   GsacResponse response, StringBuffer sb)
            throws Exception {
        if ( !initHtml(request, response, sb)) {
            return;
        }

        //        String uri = request.getRequestURI();
        String uri = request.getGsacUrlPath();
        if (checkFormSwitch(request, response, getResourceClass())) {
            return;
        }

        if (request.isGsacUrl(URL_SITE_FORM)) {
            sb.append(HtmlUtil.p());
            handleSearchForm(request, response, sb);
        } else if (request.isGsacUrl(URL_SITE_SEARCH)) {
            /*
            if(flexigridTemplate==null) {
                flexigridTemplate  = getRepository().readResource("/flexigrid.site.html");
            }
            sb.append(
                      HtmlUtil.importJS(
                                        makeHtdocsUrl("/flexigrid/flexigrid.js")));
            Hashtable<String, String> outputMap = new Hashtable<String,
                                                      String>();
            outputMap.put(ARG_OUTPUT, FlexigridSiteOutputHandler.OUTPUT_SITE_FLEXIGRID);
            String searchUrl = makeUrl(URL_SITE_SEARCH) +  "?" + request.getUrlArgs(outputMap);

            sb.append(flexigridTemplate.replace("${data.url}", searchUrl));
            */
            long t1 = System.currentTimeMillis();
            getRepository().processRequest(getResourceClass(), request,
                                           response);
            long t2 = System.currentTimeMillis();
            checkMessage(request, response, sb);
            handleSiteList(request, response, sb);
            long t3 = System.currentTimeMillis();
            System.err.println("html site request:" + (t2 - t1) + " "
                               + (t3 - t2));
        } else if (request.defined(ARG_SITE_ID)) {
            GsacSite site = (GsacSite) getRepository().getResource(request,
                                getResourceClass(),
                                request.get(ARG_SITE_ID, (String) null));
            handleSingleSite(request, response, sb, site);
        } else {
            throw new UnknownRequestException("Unknown request:" + uri);
        }
        finishHtml(request, response, sb);
    }


    /**
     * handle form
     *
     * @param request the request
     * @param response the response to write to
     * @param pw the appendable to write to
     *
     * @throws IOException on badness
     * @throws ServletException on badness
     */
    public void handleSearchForm(GsacRequest request, GsacResponse response,
                                 Appendable pw)
            throws IOException, ServletException {

        handleSearchForm(request, response, pw, getResourceClass());
        /*

        ResourceClass resourceClass = getResourceClass();
        String url = makeUrl(URL_SITE_SEARCH);
        pw.append(HtmlUtil.formPost(url,
                                    HtmlUtil.attr("name", "searchform")));;

        StringBuffer buttons = new StringBuffer("<table width=100%><tr>");

        buttons.append("<td>");
        buttons.append(HtmlUtil.submit(msg("Find Sites"), ARG_SEARCH));
        buttons.append("</td>");
        addFormSwitchButton(request, buttons, getResourceClass());
        buttons.append("</tr></table>");
        pw.append(buttons.toString());
        getSearchForm(request, pw, getResourceClass());
        getRepositorySelect(request, pw);


        StringBuffer resultsSB = new StringBuffer();
        resultsSB.append(HtmlUtil.formTable());
        getOutputSelect(getResourceClass(), request, resultsSB);
        getLimitSelect(request, resultsSB);
        getSiteSortSelect(request, resultsSB);
        resultsSB.append(HtmlUtil.formTableClose());
        pw.append(getHeader(msg("Results")));
        pw.append(HtmlUtil.makeShowHideBlock("", resultsSB.toString(),
                                             false));

        pw.append(buttons.toString());
        pw.append(HtmlUtil.formClose());
        */
    }


    /**
     * _more_
     *
     * @param request the request
     * @param response the response to write to
     * @param pw the appendable to write to
     * @param site the  site
     *
     * @throws IOException on badness
     * @throws ServletException on badness
     */
    public void handleSingleSite(GsacRequest request, GsacResponse response,
                                 Appendable pw, GsacSite site)
            throws IOException, ServletException {
        getResourceHtml(request, pw, site, true, true, false);
    }

    /**
     * _more_
     *
     * @param request the request
     * @param response the response to write to
     * @param pw the appendable to write to
     *
     * @throws IOException on badness
     * @throws ServletException on badness
     */
    public void handleSiteList(GsacRequest request, GsacResponse response,
                               Appendable pw)
            throws IOException, ServletException {
        StringBuffer formBuffer  = new StringBuffer();
        List<String> tabContents = new ArrayList<String>();
        List<String> tabTitles   = new ArrayList<String>();


        handleSearchForm(request, response, formBuffer);
        List<GsacSite> sites = response.getSites();
        if (sites.size() == 0) {
            pw.append(
                getRepository().makeInformationDialog(msg("No sites found")));
        }

        String message = response.getQueryInfo();
        if ((message != null) && (message.length() > 0)) {
            tabTitles.add(msg("Search Criteria"));
            tabContents.add(message);
        }

        StringBuffer searchLinks = makeOutputLinks(request,
                                       getResourceClass());


        tabTitles.add(msg("Search Again"));
        tabContents.add(formBuffer.toString());

        tabTitles.add(msg("Search Links"));
        tabContents.add(HtmlUtil.insetLeft(searchLinks.toString(), 10));

        StringBuffer tabs = new StringBuffer();
        makeTabs(tabs, tabTitles, tabContents);
        pw.append(HtmlUtil.makeShowHideBlock(msg("Search Info"),
                                             tabs.toString(),
                                             sites.size() == 0));

        if (sites.size() == 0) {
            return;
        }

        makeNextPrevHeader(request, response, pw);

        StringBuffer listSB = new StringBuffer();
        makeSiteHtmlTable(request, listSB, sites);
        pw.append(HtmlUtil.makeShowHideBlock(msg("Sites"), listSB.toString(),
                                             true));

        System.err.println ("GE enabled "+ isGoogleEarthEnabled(request));


        String js = createMap(request,
                              (List<GsacResource>) new ArrayList(sites), pw,
                              800, 500);
        pw.append(HtmlUtil.script(js.toString()));

    }



}
