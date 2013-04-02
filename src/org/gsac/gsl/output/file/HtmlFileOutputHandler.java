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
    public static final String OUTPUT_FILE_HTML = "file.html";


    /**
     * _more_
     *
     * @param gsacRepository _more_
     * @param resourceClass _more_
     */
    public HtmlFileOutputHandler(GsacRepository gsacRepository,
                                 ResourceClass resourceClass) {
        super(gsacRepository, resourceClass);
        getRepository().addOutput(getResourceClass(),
                                  new GsacOutput(this, OUTPUT_FILE_HTML,
                                      "File HTML"));
        //        getRepository().addOutput(OUTPUT_GROUP_FILE,new GsacOutput(this,
        //                OUTPUT_FILE_DEFAULT, "File Default"));
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
    public void handleRequest(ResourceClass type, GsacRequest request,
                              GsacResponse response)
            throws Exception {

        StringBuffer sb = new StringBuffer();
        try {
            handleRequestInner(request, response, sb);
        } catch (IllegalArgumentException iae) {
            getRepository().getLogManager().logError("Error handling site request", iae);
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


        if (checkFormSwitch(request, response, getResourceClass())) {
            return;
        }


        String uri = request.getRequestURI();
        if (request.isGsacUrl(URL_FILE_FORM)) {
            if ( !initHtml(request, response, sb, msg("File Search Form"))) {
                return;
            }

            handleSearchForm(request, response, sb);
        } else if (request.isGsacUrl(URL_FILE_SEARCH)) {
            if ( !initHtml(request, response, sb,
                           msg("File Search Results"))) {
                return;
            }
            getRepository().processRequest(getResourceClass(), request,
                                           response);
            checkMessage(request, response, sb);
            handleFileList(request, response, sb);
        } else if (request.defined(ARG_FILE_ID)) {
            if ( !initHtml(request, response, sb, msg("File View"))) {
                return;
            }
            GsacFile resource =
                (GsacFile) getRepository().getResource(request,
                    getResourceClass(), request.get(ARG_FILE_ID, ""));
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

        List<GsacResource> relatedResources = resource.getRelatedResources();
        for (GsacResource relatedResource : relatedResources) {
            String resourceLabel =
                getResourceManager(relatedResource).getResourceLabel(false);
            if (relatedResource.getId() == null) {
                sb.append(HtmlUtil.formEntry(msgLabel(resourceLabel),
                                             relatedResource.getLongLabel()));
            } else {
                String resourceUrl = makeResourceViewUrl(relatedResource);
                sb.append(HtmlUtil.formEntry(msgLabel(resourceLabel),
                                             "<a href=\"" + resourceUrl
                                             + "\">"
                                             + relatedResource.getLongLabel()
                                             + "</a>"));
            }
        }


        Date publishTime = resource.getPublishDate();
        Date startTime   = resource.getFromDate();
        Date endTime     = resource.getToDate();

        if (publishTime != null) {

            sb.append(formEntry(request, msgLabel("Publish Date"),
                                formatDate(publishTime)));
        }


        if (startTime != null) {
            if ((endTime == null) || endTime.equals(startTime)) {
                sb.append(formEntry(request, msgLabel("Start Date"),
                                    formatDate(startTime)));
            } else {
                sb.append(formEntry(request, msgLabel("Date Range"),
                                    formatDate(resource)));
            }
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
        handleSearchForm(request, response, pw, getResourceClass());
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
    public void handleFileList(GsacRequest request, GsacResponse response,
                               Appendable sb)
            throws Exception {

        try {
            StringBuffer formBuffer  = new StringBuffer();

            List<String> tabContents = new ArrayList<String>();
            List<String> tabTitles   = new ArrayList<String>();

            StringBuffer toolbar     = new StringBuffer();

            StringBuffer searchLinks = makeOutputLinks(request,
                                           getResourceClass());

            for (GsacOutput output :
                    getRepository().getOutputs(getResourceClass())) {
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
            }

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

            tabTitles.add(msg("Search Links"));
            tabContents.add(HtmlUtil.insetLeft(searchLinks.toString(), 10));


            StringBuffer tabs = new StringBuffer();
            makeTabs(tabs, tabTitles, tabContents);
            List<GsacFile> files       = response.getFiles();
            boolean        openSection = (files.size() == 0);
            sb.append(HtmlUtil.makeShowHideBlock(msg("Search Information"),
                    tabs.toString(), openSection));


            Hashtable<String, String> override = new Hashtable<String,
                                                     String>();
            override.put(ARG_OUTPUT, OUTPUT_FILE_HTML);
            makeNextPrevHeader(request, response, sb);
            long size = 0;
            int  cnt  = 0;

            for (GsacFile resource : files) {
                List<GsacResource> relatedResources =
                    resource.getRelatedResources();
                String relatedLabel = "";
                if (relatedResources.size() > 0) {
                    relatedLabel = getResourceManager(
                        relatedResources.get(0)).getResourceLabel(false);
                }
                if (cnt == 0) {
                    request.remove(ARG_OUTPUT);
                    sb.append(HtmlUtil.formPost(request.getUrl(null),
                            HtmlUtil.attr("name", "searchform")));;

                    sb.append(
                        "<table border=0 cellspacing=0 cellpadding=0 width=\"100%\"><tr><td align=right><div class=gsac-toolbar>");
                    sb.append(toolbar);
                    sb.append("</div></td></tr></table>");
                    boolean includeExtraCol =
                        getRepository().getRemoteHref(resource).length() > 0;
                    includeExtraCol = false;

                    sb.append(
                        "<table class=\"gsac-result-table\" cellspacing=0 cellpadding=0 border=0 width=100%>");

                    String[] labels = null; 
                    labels = new String[] {
                        (includeExtraCol ? "&nbsp;" : null), msg("File"), msg("Type"), msg(relatedLabel), msg("Date"), msg("File size")
                    };

                    String[] sortValues = new String[] {
                        (includeExtraCol
                         ? ""
                         : null), "", SORT_FILE_TYPE, "",
                        SORT_FILE_PUBLISHDATE, SORT_FILE_SIZE
                    };
                    makeSortHeader(request, sb, ARG_FILE_PREFIX, labels,
                                   sortValues);
                }
                cnt++;
                openEntryRow(sb, resource.getId(), URL_FILE_VIEW,
                             ARG_FILE_ID);
                //                sb.append("<tr valign=top>");
                //                sb.append(HtmlUtil.col(""));

                String clickEvent = getEntryEventJS(resource.getId(),
                                        URL_FILE_VIEW, ARG_FILE_ID)[1];

                String cbx = HtmlUtil.checkbox(ARG_FILE_ID, resource.getId(),
                                 true);
                //                sb.append(HtmlUtil.col(cbx));
                String remoteHref = getRepository().getRemoteHref(resource);
                if (remoteHref.length() > 0) {
                    sb.append(HtmlUtil.col(remoteHref));
                }
                sb.append("</tr></table></td>\n");
                String href = makeResourceViewHref(resource);
                //                sb.append(HtmlUtil.col(href));
                String url = resource.getFileInfo().getUrl();
                if (url != null) {
                    String downloadHref = HtmlUtil.href(
                                              url,
                                              HtmlUtil.img(
                                                  iconUrl(
                                                      "/down_arrow.gif")));
                    String tmp = downloadHref + " " + href;
                    sb.append(HtmlUtil.col(tmp));
                } else {
                    sb.append(HtmlUtil.col("N/A"));
                }

                if (resource.getType() != null) {
                    sb.append(HtmlUtil.col(resource.getType().getName(),
                                           clickEvent));
                } else {
                    sb.append(HtmlUtil.col("N/A", clickEvent));
                }


              // Note: if you have no "related content" omit this section
              if (resource.getRelatedResources() != null) {
                StringBuffer relatedContent = new StringBuffer();
                for (int relatedIdx = 0; relatedIdx < relatedResources.size();
                        relatedIdx++) {
                    GsacResource relatedResource =
                        relatedResources.get(relatedIdx);
                    if (relatedIdx > 0) {
                        relatedContent.append("<br>");
                    }
                    if (relatedResource.getId() != null) {
                        String relatedUrl =
                            makeResourceViewUrl(relatedResource);
                        relatedContent.append("<a href=\"" + relatedUrl
                                + "\">" + relatedResource.getLongLabel()
                                + "</a>");
                    } else {
                        relatedContent.append(relatedResource.getLongLabel());
                    }
                }
                if (relatedResources.size() == 0) {
                    relatedContent.append("NA");
                }
                sb.append(HtmlUtil.col(relatedContent.toString()));
              }

                Date publishTime = resource.getPublishDate();
                Date startTime   = resource.getFromDate();
                Date endTime     = resource.getToDate();

                if (startTime == null) {
                    sb.append(HtmlUtil.col(formatDate(publishTime)));
                } else {
                    if ((endTime == null) || endTime.equals(startTime)) {
                        sb.append(HtmlUtil.col(formatDate(startTime),
                                clickEvent));
                    } else {
                        sb.append(HtmlUtil.col(formatDate(startTime) + " - "
                                + formatDate(endTime), clickEvent));
                    }
                }


              if (resource.getRelatedResources() != null) {
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
