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

package org.gsac.gsl.output.resource;
import org.gsac.gsl.output.*;



import org.gsac.gsl.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.util.*;

import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;

import java.io.*;

import java.util.ArrayList;

import java.util.Date;
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
public class HtmlResourceOutputHandler extends HtmlOutputHandler {

    /** output id */
    public static final String OUTPUT_RESOURCE_HTML = "resource.html";


    /**
     * _more_
     *
     * @param gsacServlet _more_
     */
    public HtmlResourceOutputHandler(GsacRepository gsacServlet) {
        super(gsacServlet);
        getRepository().addResourceOutput(new GsacOutput(this,
                OUTPUT_RESOURCE_HTML, "Resource HTML"));
        //        getRepository().addResourceOutput(new GsacOutput(this,
        //                OUTPUT_RESOURCE_DEFAULT, "Resource Default"));
    }


    /**
     * _more_
     *
     *
     * @param request _more_
     * @param response _more_
     *
     *
     * @throws Exception On badness
     */
    public void handleResourceRequest(GsacRequest request,
                                      GsacResponse response)
            throws Exception {
        StringBuffer sb = new StringBuffer();
        if(!initHtml(request, response, sb)) return;

        String uri = request.getRequestURI();

        if (request.defined(ARG_SEARCH_SITES)) {
            request.remove(ARG_OUTPUT);
            request.remove(ARG_SEARCH_RESOURCES);
            String args        = request.getUrlArgs();
            String redirectUrl = makeUrl(URL_SITE_FORM) + "?" + args;
            response.sendRedirect(redirectUrl);
            response.endResponse();
            return;
        }

        if (request.isGsacUrl(URL_RESOURCE_FORM)) {
            handleSearchForm(request, response, sb);
        } else if (request.isGsacUrl(URL_RESOURCE_SEARCH)) {
            getRepository().handleResourceRequest(request, response);
            checkMessage(request, response, sb);
            handleResourceList(request, response, sb);
        } else if (request.defined(ARG_RESOURCEID)) {
            GsacResource resource = getRepository().getResource(request,
                                        request.get(ARG_RESOURCEID, ""));
            handleSingleResource(request, response, resource, sb);
        } else {
            throw new UnknownRequestException("Unknown request:" + uri);
        }
        finishHtml(request, response, sb);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param response _more_
     * @param resource _more_
     * @param sb _more_
     *
     * @throws Exception On badness
     */
    public void handleSingleResource(GsacRequest request,
                                     GsacResponse response,
                                     GsacResource resource, Appendable sb)
            throws Exception {
        if (resource == null) {
            sb.append("Could not find resource");
            return;
        }
        sb.append(HtmlUtil.formTable());

        if (resource.getType() != null) {
            sb.append(HtmlUtil.formEntry(msgLabel("Type"),
                                         resource.getType().getName()));
        }

        String url = resource.getFileInfo().getUrl();
        if (url != null) {
            sb.append(HtmlUtil.formEntry(msgLabel("URL"),
                                         "<a href=\"" + url + "\">"
                                         + IOUtil.getFileTail(url) + "</a>"));

        }

        GsacSite site = resource.getSite();

        if (site != null) {
            if (site.getSiteId() == null) {
                sb.append(HtmlUtil.formEntry(msgLabel("Site"),
                                             site.getSiteCode()));
            } else {
                String siteUrl = makeSiteUrl(ARG_SITEID + "="
                                             + site.getSiteId());
                sb.append(HtmlUtil.formEntry(msgLabel("Site"),
                                             "<a href=\"" + siteUrl + "\">"
                                             + site.getName() + " "
                                             + site.getLabel() + "</a>"));
            }
        }


        if (resource.getStartTime() != null) {
            sb.append(HtmlUtil.formEntry(msgLabel("Date"),
                                         "" + resource.getStartTime()));
        }

        if (resource.getFileInfo().getFileSize() > 0) {
            sb.append(
                HtmlUtil.formEntry(
                    msgLabel("File Size"),
                    formatFileSize(resource.getFileInfo().getFileSize())));

        }

        processMetadata(request, sb, resource, resource.getMetadata(), true,
                        new Hashtable());

        sb.append(HtmlUtil.formTableClose());
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param response _more_
     * @param pw _more_
     *
     * @throws IOException On badness
     * @throws ServletException On badness
     */
    public void handleSearchForm(GsacRequest request, GsacResponse response,
                                 Appendable pw)
            throws IOException, ServletException {

        pw.append(HtmlUtil.formPost(makeUrl(URL_RESOURCE_SEARCH),
                                    HtmlUtil.attr("name", "searchform")));;

        String blankImg = iconUrl("/blank.gif");
        //Put a blank image submit input here so any Enter key pressed does not
        //default to a site submit button search
        pw.append(HtmlUtil.submitImage(blankImg, ARG_SEARCH));

        StringBuffer buttons = new StringBuffer();
        if (getDoResource()) {
            buttons.append(HtmlUtil.submit(msg("Find Files"), ARG_SEARCH));
        }
        if (getDoSite()) {
            buttons.append(HtmlUtil.space(8));
            buttons.append(HtmlUtil.submit(msg("Site Search Form"), ARG_SEARCH_SITES));
        }

        pw.append(buttons.toString());

        pw.append(HtmlUtil.importJS(getRepository().getUrlBase()
                                             + URL_HTDOCS_BASE
                                             + "/CalendarPopup.js"));

        addCapabilitiesToForm(request, pw, getRepository().getResourceQueryCapabilities(), false);
        getSiteSearchForm(request, pw);

        StringBuffer resultsSB = new StringBuffer();
        resultsSB.append(HtmlUtil.formTable());
        getResourceOutputSelect(request, resultsSB);
        getLimitSelect(request, resultsSB);
        getResourceSortSelect(request, resultsSB);
        resultsSB.append(HtmlUtil.formTableClose());
        pw.append(getHeader(msg("Results")));
        pw.append(HtmlUtil.makeShowHideBlock("", resultsSB.toString(),
                                             false));

        pw.append(buttons.toString());
        pw.append(HtmlUtil.formClose());
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param response _more_
     * @param sb _more_
     *
     *
     * @throws Exception On badness
     */
    public void handleResourceList(GsacRequest request,
                                   GsacResponse response, Appendable sb)
            throws Exception {

        try {
            StringBuffer formBuffer  = new StringBuffer();

            StringBuffer searchLinks = new StringBuffer();
            GsacRequest  tmpRequest  = new GsacRequest(request);
            for (GsacOutput output : getRepository().getResourceOutputs()) {
                if (output.getId().equals(OUTPUT_RESOURCE_HTML)) {
                    continue;
                }
                tmpRequest.put(ARG_OUTPUT, output.getId());
                String suffix = output.getFileSuffix();
                String searchUrl = makeUrl(URL_RESOURCE_SEARCH)
                                   + ((suffix != null)
                                      ? suffix
                                      : "") + "?" + tmpRequest.getUrlArgs();
                searchLinks.append(HtmlUtil.href(searchUrl,
                        output.getLabel()));
                searchLinks.append(HtmlUtil.br());
            }

            formBuffer.append(
                HtmlUtil.insetLeft(
                    HtmlUtil.makeShowHideBlock(
                        msg("Search Links"),
                        HtmlUtil.insetLeft(searchLinks.toString(), 10),
                        false), 10));


            handleSearchForm(request, response, formBuffer);
            String message = response.getQueryInfo();
            if (message.length() > 0) {
                sb.append(message);
            }
            sb.append(HtmlUtil.makeShowHideBlock(msg("Search Again"),
                    formBuffer.toString(), false));

            Hashtable<String, String> override = new Hashtable<String,
                                                     String>();
            override.put(ARG_OUTPUT, OUTPUT_RESOURCE_DEFAULT);
            makeNextPrevHeader(request, response, sb);
            long size = 0;
            int  cnt  = 0;
            for (GsacResource resource : response.getResources()) {
                GsacSite site = resource.getSite();
                if (cnt == 0) {
                    sb.append(
                        "<table class=\"result-table\" cellspacing=0 cellpadding=4 border=0 xwidth=100%>");
                    String[] labels = new String[] {
                        "", msg("Type"), msg("File"), msg("Site"),
                        msg("Date"), msg("File size")
                    };
                    String[] sortValues = new String[] {
                        "", SORT_RESOURCE_TYPE, "", "",
                        SORT_RESOURCE_PUBLISHDATE, SORT_RESOURCE_SIZE
                    };
                    makeSortHeader(request, sb, ARG_RESOURCE_PREFIX, labels,
                                   sortValues);


                }
                cnt++;
                if ((site == null) && (resource.getSiteID() != null)) {
                    site = getRepository().getSite(request,
                            resource.getSiteID());
                }
                openEntryRow(sb, resource.getRepositoryId(),
                             URL_RESOURCE_VIEW, ARG_RESOURCE_ID);
                //                sb.append("<tr valign=top>");
                //                sb.append(HtmlUtil.col(""));

                sb.append(
                    HtmlUtil.col(getRepository().getRemoteHref(resource)));
                if (resource.getType() != null) {
                    sb.append(HtmlUtil.col(resource.getType().getName()));
                } else {
                    sb.append(HtmlUtil.col("N/A"));
                }

                String url = resource.getFileInfo().getUrl();
                if (url != null) {
                    sb.append(HtmlUtil.col("<a href=\"" + url + "\">"
                                           + IOUtil.getFileTail(url)
                                           + "</a>"));

                } else {
                    sb.append(HtmlUtil.col("N/A"));
                }


                if (site == null) {
                    sb.append(HtmlUtil.col("N/A"));
                } else if (site.getSiteId() == null) {
                    sb.append(HtmlUtil.col(site.getSiteCode()));
                } else {
                    String siteUrl = makeSiteUrl(ARG_SITEID + "="
                                         + site.getSiteId());
                    sb.append(HtmlUtil.col("<a href=\"" + siteUrl + "\">"
                                           + site.getName() + " "
                                           + site.getLabel() + "</a>"));
                }

                if (resource.getStartTime() == null) {
                    sb.append(HtmlUtil.col("N/A"));
                } else {
                    Date startTime = resource.getStartTime();
                    Date endTime   = resource.getEndTime();
                    if ((endTime == null) || endTime.equals(startTime)) {
                        sb.append(HtmlUtil.col(formatDate(startTime)));
                    } else {
                        sb.append(HtmlUtil.col(formatDate(startTime) + " - "
                                + formatDate(endTime)));
                    }
                }



                if (resource.getFileInfo().getFileSize() > 0) {
                    sb.append("<td align=\"right\" class=\"filesize\">");
                    size += resource.getFileInfo().getFileSize();
                    sb.append(
                        "" + formatFileSize(
                            resource.getFileInfo().getFileSize()));

                    sb.append("</td>");
                } else {
                    sb.append(HtmlUtil.col("N/A"));
                }

                sb.append("</tr>\n");
            }
            if (cnt == 0) {
                sb.append(
                    getRepository().makeInformationDialog(
                        msg("No files found")));
            } else {
                sb.append("<tr><td colspan=2></td>");
                sb.append("<td align=right>" + cnt + HtmlUtil.space(1)
                          + msg("files") + "</td>");
                sb.append("<td colspan=3></td>");
                sb.append("<td align=\"right\" class=\"filesize\">");
                if (size > 0) {
                    sb.append("" + formatFileSize(size));
                }
                sb.append("</td>");
                sb.append("</tr>\n");
                sb.append("</table>\n");
            }

        } catch (Exception exc) {
            throw new RuntimeException(exc);

        }
    }


}
