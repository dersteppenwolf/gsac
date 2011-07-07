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

package org.gsac.gsl.output.file;



import org.gsac.gsl.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.output.*;
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
public class HtmlFileOutputHandler extends HtmlOutputHandler {

    /** output id */
    public static final String OUTPUT_RESOURCE_HTML = "resource.html";


    /**
     * _more_
     *
     * @param gsacServlet _more_
     */
    public HtmlFileOutputHandler(GsacRepository gsacServlet) {
        super(gsacServlet);
        getRepository().addOutput(OUTPUT_GROUP_RESOURCE,
                                  new GsacOutput(this, OUTPUT_RESOURCE_HTML,
                                      "Resource HTML"));
        //        getRepository().addOutput(OUTPUT_GROUP_RESOURCE,new GsacOutput(this,
        //                OUTPUT_RESOURCE_DEFAULT, "Resource Default"));
    }


    /**
     * _more_
     *
     *
     *
     * @param type _more_
     * @param request The request
     * @param response The response
     *
     *
     * @throws Exception On badness
     */
    public void handleRequest(ObjectType type, GsacRequest request,
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
            getRepository().processRequest(GsacFile.TYPE_RESOURCE,
                                           request, response);
            checkMessage(request, response, sb);
            handleResourceList(request, response, sb);
        } else if (request.defined(ARG_RESOURCEID)) {
            GsacFile resource =
                (GsacFile) getRepository().getResource(request,
                    GsacFile.TYPE_RESOURCE,
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
     * @param request The request
     * @param response The response
     * @param resource _more_
     * @param sb _more_
     *
     * @throws Exception On badness
     */
    public void handleSingleResource(GsacRequest request,
                                     GsacResponse response,
                                     GsacFile resource, Appendable sb)
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
                                             + site.getLabel() + " ("
                                             + site.getSiteCode() + ")"
                                             + "</a>"));
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
     * @param request The request
     * @param response The response
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

        StringBuffer buttons = new StringBuffer("<table width=100%><tr>");
        if (getDoResource()) {
            buttons.append("<td>");
            buttons.append(HtmlUtil.submit(msg("List Files"), ARG_SEARCH));
            for (GsacOutput output :
                    getRepository().getOutputs(OUTPUT_GROUP_RESOURCE)) {
                if (output.getToolbarLabel() == null) {
                    continue;
                }
                String submit = HtmlUtil.tag(HtmlUtil.TAG_INPUT,
                                             HtmlUtil.attrs(new String[] {
                    HtmlUtil.ATTR_NAME, output.getId(), HtmlUtil.ATTR_TYPE,
                    HtmlUtil.TYPE_SUBMIT, HtmlUtil.ATTR_VALUE,
                    output.getToolbarLabel(),
                    //HtmlUtil.ATTR_CLASS, "gsac-download-button",
                    HtmlUtil.ATTR_TITLE, output.getLabel()
                }));
                buttons.append(HtmlUtil.space(2));
                buttons.append(submit);
            }


            buttons.append("</td>");

        }
        if (getDoSite()) {
            buttons.append("<td align=right>");
            String switchForm =
                HtmlUtil.tag(HtmlUtil.TAG_INPUT,
                             HtmlUtil.cssClass("gsac-gobutton")
                             + HtmlUtil.attrs(new String[] {
                HtmlUtil.ATTR_NAME, ARG_SEARCH_SITES, HtmlUtil.ATTR_TYPE,
                HtmlUtil.TYPE_SUBMIT, HtmlUtil.ATTR_VALUE,
                msg("Site Search Form"), HtmlUtil.ATTR_CLASS,
                "gsac-download-button", HtmlUtil.ATTR_TITLE,
                msg("Go to the site search form"),
            }));

            buttons.append(switchForm);
            buttons.append("</td>");
        }

        buttons.append("</tr></table>");
        pw.append(buttons.toString());

        pw.append(HtmlUtil.importJS(getRepository().getUrlBase()
                                    + URL_HTDOCS_BASE + "/CalendarPopup.js"));

        CapabilityCollection resourceCollection =
            getRepository().getCapabilityCollection(CAPABILITIES_RESOURCE);
        if (resourceCollection != null) {
            addCapabilitiesToForm(request, pw, resourceCollection, false);
        }
        getSiteSearchForm(request, pw);

        StringBuffer resultsSB = new StringBuffer();
        resultsSB.append(HtmlUtil.formTable());
        getOutputSelect(OUTPUT_GROUP_RESOURCE, request, resultsSB);
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
     * @param request The request
     * @param response The response
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

            List<String> tabContents = new ArrayList<String>();
            List<String> tabTitles   = new ArrayList<String>();

            StringBuffer searchLinks = new StringBuffer();
            GsacRequest  tmpRequest  = new GsacRequest(request);
            StringBuffer toolbar     = new StringBuffer();

            for (GsacOutput output :
                    getRepository().getOutputs(OUTPUT_GROUP_RESOURCE)) {
                if (output.getId().equals(OUTPUT_RESOURCE_HTML)) {
                    continue;
                }
                tmpRequest.put(ARG_OUTPUT, output.getId());
                String suffix = output.getFileSuffix();
                String searchUrl = makeUrl(URL_RESOURCE_SEARCH)
                                   + ((suffix != null)
                                      ? suffix
                                      : "") + "?" + tmpRequest.getUrlArgs();
                if (output.getToolbarLabel() != null) {
                    String submit = HtmlUtil.tag(HtmlUtil.TAG_INPUT,
                                        HtmlUtil.attrs(new String[] {
                        HtmlUtil.ATTR_NAME, output.getId(),
                        HtmlUtil.ATTR_TYPE, HtmlUtil.TYPE_SUBMIT,
                        HtmlUtil.ATTR_VALUE, output.getToolbarLabel(),
                        HtmlUtil.ATTR_CLASS, "gsac-download-button",
                        HtmlUtil.ATTR_TITLE, output.getLabel()
                    }));
                    if (toolbar.length() > 0) {
                        toolbar.append(HtmlUtil.space(2));
                    }
                    toolbar.append(submit);

                }

                searchLinks.append(HtmlUtil.href(searchUrl,
                        output.getLabel()));
                searchLinks.append(HtmlUtil.br());
            }


            /*            formBuffer.append(
                HtmlUtil.insetLeft(
                    HtmlUtil.makeShowHideBlock(
                                               msg("Search Links"),
                        HtmlUtil.insetLeft(searchLinks.toString(), 10),
                        false), 10));
            */


            handleSearchForm(request, response, formBuffer);



            //            sb.append(HtmlUtil.makeShowHideBlock(msg("Search Again"),
            //                    formBuffer.toString(), false));

            String message = response.getQueryInfo();
            if ((message != null) && (message.length() > 0)) {
                tabTitles.add(msg("Search Criteria"));
                tabContents.add(message);
            }

            tabTitles.add(msg("Search Again"));
            tabContents.add(formBuffer.toString());

            tabContents.add(HtmlUtil.insetLeft(searchLinks.toString(), 10));
            tabTitles.add(msg("Search Links"));

            StringBuffer tabs = new StringBuffer();
            makeTabs(tabs, tabTitles, tabContents);


            sb.append(HtmlUtil.makeShowHideBlock(msg("Search Info"),
                    tabs.toString(), false));


            Hashtable<String, String> override = new Hashtable<String,
                                                     String>();
            override.put(ARG_OUTPUT, OUTPUT_RESOURCE_HTML);
            makeNextPrevHeader(request, response, sb);
            long size = 0;
            int  cnt  = 0;


            for (GsacFile resource : response.getFiles()) {
                GsacSite site = resource.getSite();
                if ((site == null) && (resource.getSiteID() != null)) {
                    site = (GsacSite) getRepository().getResource(request,
                            GsacSite.TYPE_SITE, resource.getSiteID());
                }
                if (cnt == 0) {
                    //                    pw.append(HtmlUtil.formPost(makeUrl(URL_RESOURCE_SEARCH),
                    request.remove(ARG_OUTPUT);
                    sb.append(HtmlUtil.formPost(request.getUrl(null),
                            HtmlUtil.attr("name", "searchform")));;

                    sb.append(
                        "<table border=0 cellspacing=0 cellpadding=0 width=\"100%\"><tr><td align=right><div class=gsac-toolbar>");
                    sb.append(toolbar);
                    sb.append("</div></td></tr></table>");

                    boolean includeExtraCol =
                        ((site != null)
                         && (getRepository().getRemoteHref(site).length()
                             > 0));
                    sb.append(
                        "<table class=\"gsac-result-table\" cellspacing=0 cellpadding=0 border=0 width=100%>");
                    String[] labels = new String[] {
                        (includeExtraCol
                         ? ""
                         : null), msg("Type"), msg("File"), msg("Site"),
                        msg("Date"), msg("File size")
                    };
                    String[] sortValues = new String[] {
                        (includeExtraCol
                         ? ""
                         : null), SORT_RESOURCE_TYPE, "", "",
                        SORT_RESOURCE_PUBLISHDATE, SORT_RESOURCE_SIZE
                    };
                    makeSortHeader(request, sb, ARG_RESOURCE_PREFIX, labels,
                                   sortValues);


                }
                cnt++;
                openEntryRow(sb, resource.getRepositoryId(),
                             URL_RESOURCE_VIEW, ARG_RESOURCE_ID);
                //                sb.append("<tr valign=top>");
                //                sb.append(HtmlUtil.col(""));

                String clickEvent = getEntryEventJS(resource.getId(),
                                        URL_RESOURCE_VIEW,
                                        ARG_RESOURCE_ID)[1];

                String cbx = HtmlUtil.checkbox(ARG_RESOURCE_ID,
                                 resource.getId(), true);

                //                sb.append(HtmlUtil.col(cbx));

                String remoteHref = getRepository().getRemoteHref(resource);
                if (remoteHref.length() > 0) {
                    sb.append(HtmlUtil.col(remoteHref));
                }
                sb.append("</tr></table></td>\n");
                if (resource.getType() != null) {
                    sb.append(HtmlUtil.col(resource.getType().getName(),
                                           clickEvent));
                } else {
                    sb.append(HtmlUtil.col("N/A", clickEvent));
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
                                           + site.getLabel() + " ("
                                           + site.getSiteCode() + ")"
                                           + "</a>"));
                }

                if (resource.getStartTime() == null) {
                    sb.append(HtmlUtil.col("N/A"));
                } else {
                    Date startTime = resource.getStartTime();
                    Date endTime   = resource.getEndTime();
                    if ((endTime == null) || endTime.equals(startTime)) {
                        sb.append(HtmlUtil.col(formatDate(startTime),
                                clickEvent));
                    } else {
                        sb.append(HtmlUtil.col(formatDate(startTime) + " - "
                                + formatDate(endTime), clickEvent));
                    }
                }


                if (resource.getFileInfo().getFileSize() > 0) {
                    sb.append("<td align=\"right\" class=\"gsac-filesize\" "
                              + clickEvent + ">");
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
                sb.append("<tr><td>&nbsp;</td>");
                sb.append("<td align=right>" + cnt + HtmlUtil.space(1)
                          + msg("files") + "</td>");
                sb.append("<td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td>");
                sb.append("<td align=\"right\" class=\"gsac-filesize\">");
                if (size > 0) {
                    sb.append("" + formatFileSize(size));
                }
                sb.append("</td>");
                sb.append("</tr>\n");
                sb.append("</table>\n");
                sb.append(HtmlUtil.formClose());
            }

        } catch (Exception exc) {
            throw new RuntimeException(exc);

        }
    }


}
