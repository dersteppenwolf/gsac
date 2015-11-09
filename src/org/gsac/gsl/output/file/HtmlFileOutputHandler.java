/*
 * Copyright 2015 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
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
import java.text.SimpleDateFormat;
import javax.servlet.*;
import javax.servlet.http.*;


/**
 * Class description not given by original class author.
 *
 * @author         Jeff McWhirter
 * @author S K Wier Jan 2013 - 12 Nov 2013; 19 Feb 2015 some special code for UNR case
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
        getRepository().addOutput(getResourceClass(), new GsacOutput(this, OUTPUT_FILE_HTML, "File HTML"));

        // getRepository().addOutput(OUTPUT_GROUP_FILE,new GsacOutput(this,   OUTPUT_FILE_DEFAULT, "File Default"));
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
            sb.append( getRepository().makeErrorDialog( "An error has occurred:<br>" + iae.getMessage()));
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
            sb.append(formEntry(request, msgLabel("Publish Date"), formatDate(publishTime)));
        }

        if (startTime != null) {
            if ((endTime == null) || endTime.equals(startTime)) {
                sb.append(formEntry(request, msgLabel("Start Date"), formatDate(startTime)));
            } else {
                sb.append(formEntry(request, msgLabel("Date Range"), formatDate(resource)));
            }
        }

        if (endTime != null) {
            sb.append(formEntry(request, msgLabel("End Date"), formatDate(endTime)));
        }


        if (resource.getFileInfo().getFileSize() > 0) {
            sb.append(
                HtmlUtil.formEntry( msgLabel("File Size"), formatFileSize(resource.getFileInfo().getFileSize())));

        }

        processMetadata(request, sb, resource, resource.getMetadata(), true, new Hashtable());

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

            tabTitles.add(msg("Search Form"));
            tabContents.add(formBuffer.toString());

            // original, with ambiguous meaning to many: tabTitles.add(msg("Search Links"));
            tabTitles.add(msg("Result Formats"));
            tabContents.add(HtmlUtil.insetLeft(searchLinks.toString(), 10));


            StringBuffer tabs = new StringBuffer();
            makeTabs(tabs, tabTitles, tabContents);
            List<GsacFile> files       = response.getFiles();
            boolean        openSection = (files.size() == 0);
            sb.append(HtmlUtil.makeShowHideBlock(msg("Search Information"), tabs.toString(), openSection));

            String reqstr=request.toString();
            sb.append(HtmlUtil.makeShowHideBlock(msg("API request"), "Base URL + "+reqstr, false));

            Hashtable<String, String> override = new Hashtable<String, String>();

            override.put(ARG_OUTPUT, OUTPUT_FILE_HTML);

            makeNextPrevHeader(request, response, sb);
            long filesizesum = 0;
            //float sampint = 0.0f;
            int  cnt  = 0;
            String relatedLabel = null;

            //System.err.println("   HTML file list 1 ");

            // NOTE in this main loop the "resource" is a GsacFile
            for (GsacFile resource : files) {
                relatedLabel = null;

                List<GsacResource> relatedResources = resource.getRelatedResources();

                if (relatedResources.size() > 0) {
                    relatedLabel = getResourceManager( relatedResources.get(0)).getResourceLabel(false);
                    // related content is for example site name 
                }


                // LABEL THE TOP OF THE HTML TABLE OF FILES.  first row in table is labels
                
                if (cnt == 0) {
                    request.remove(ARG_OUTPUT);
                    sb.append(HtmlUtil.formPost(request.getUrl(null), HtmlUtil.attr("name", "searchform")));;
                    sb.append(
                        "<table border=0 cellspacing=0 cellpadding=0 width=\"100%\"><tr><td align=right><div class=gsac-toolbar>");
                    sb.append(toolbar);
                    sb.append("</div></td></tr></table>");
                    
                    boolean includeExtraCol = getRepository().getRemoteHref(resource).length() > 0;
                    includeExtraCol = false;

                    sb.append( "<table class=\"gsac-result-table\" cellspacing=0 cellpadding=0 border=0 width=100%>");

                    String[] labels = null;
                    if (  relatedLabel != null)
                    { 
                       // unavco gsac server has this case, since it lacks some file metadata values easily available in the prototype GSAC code
                       // but does have the "related" stuff for the UNAVCO GSAC (only).
                           //(includeExtraCol ? "&nbsp;" : null), msg("File"), msg("Type"), msg(relatedLabel), msg("Pub. Date"), msg("File size")
                           //                                     for the UNAVCO GSAC relatedLabel is "Site"
                       labels = new String[] {
                           (includeExtraCol ? "&nbsp;" : null), msg("File & URL link"), msg("File Type"),msg("Time range of data"), msg("Published"), msg("File size")
                           };
                    }
                    else {  
                       // Standard GSAC and prototype GSAC:
                       //    note that the HTML term for the Greek letter delta is &Delta;    use delta T in place of the longer label 'sample time.'
                       labels = new String[] {                  msg("File & URL link"), msg("File type"),msg("Time range of data"), msg("&Delta;t"), msg("MD5 check sum"), msg("File size") };
                       // CHANGEME special, only for UNR:
                       //labels = new String[] {  msg("File URL to download"), msg("File type"),msg("Time range of data") };
                    }

                    String[] sortValues = new String[] {
                        (includeExtraCol ? "" : null), "", SORT_FILE_TYPE, "", SORT_FILE_PUBLISHDATE, SORT_FILE_SIZE
                    };

                    makeSortHeader(request, sb, ARG_FILE_PREFIX, labels, sortValues);

                    /* 
                    request.remove(ARG_OUTPUT);
                    sb.append(HtmlUtil.formPost(request.getUrl(null), HtmlUtil.attr("name", "searchform")));;
                    sb.append( "<table border=0 cellspacing=0 cellpadding=0 width=\"100%\"><tr><td align=right><div class=gsac-toolbar>");
                    sb.append(toolbar);
                    sb.append("</div></td></tr></table>");
                    boolean includeExtraCol = getRepository().getRemoteHref(resource).length() > 0;
                    sb.append( "<table class=\"gsac-result-table\" cellspacing=0 cellpadding=0 border=0 width=100%>");
                    String[] labels = null; 
                    labels = new String[] {  msg("File for download"), msg("File type"),msg("Time range of data"), msg("&Delta;t"), msg("MD5 check sum"), msg("File size") };
                    // * to handle "related content"; and also see below
                    if ( relatedLabel.equals("") || relatedLabel == null)
                    {
                        labels = new String[] {  msg("File for download"), msg("File type"),msg("Time range of data"), msg("&Delta;t"), msg("MD5"), msg("File size") };
                        labels = new String[] {  msg("File for download"), msg("File type"),msg("Time range of data"), msg("&Delta;t"), msg("MD5"), msg("File size") };
                        //labels = new String[] { (includeExtraCol ? "&nbsp;" : null), msg("File"), msg("Type"),                    msg("Date"), msg("File size") };
                        //labels = new String[] {  msg("File"), msg("Type"),                    msg("Date"), msg("File size") };
                    }
                    else {
                        labels = new String[] {  msg("File for download"), msg("File type"),msg("Time range of data"), msg("&Delta;t"), msg("MD5"), msg("File size") };
                    }
                    // * /
                    
                    String[]   sortValues = new String[] { (includeExtraCol ? "" : null), "", SORT_FILE_TYPE, "", SORT_FILE_PUBLISHDATE, SORT_FILE_SIZE };
                    //String[] sortValues = new String[] {  "",                             SORT_FILE_TYPE, "", SORT_FILE_PUBLISHDATE, SORT_FILE_SIZE };
                    makeSortHeader(request, sb, ARG_FILE_PREFIX, labels, sortValues);

                     */
                }
                cnt++;

                openEntryRow(sb, resource.getId(), URL_FILE_VIEW, ARG_FILE_ID);
                // what for? sb.append("<tr valign=top>");
                // sb.append(HtmlUtil.col(""));

                // putting this thing in the column entries with +clickEvent below means that if the user clicks in the table, except the file URL, he sees a popup 'resource' box.
                String clickEvent = getEntryEventJS(resource.getId(), URL_FILE_VIEW, ARG_FILE_ID)[1];

                // what does this do?
                // String cbx = HtmlUtil.checkbox(ARG_FILE_ID, resource.getId(), true);
                //                sb.append(HtmlUtil.col(cbx));
                /* this is not the mystery link in the first column of file search results table   
                String remoteHref = getRepository().getRemoteHref(resource);
                if (remoteHref.length() > 0) {
                    sb.append(HtmlUtil.col(remoteHref));
                }
                */

                sb.append("</tr></table></td>\n");

                // show link to the url, the complete URL to download one file 
                String url = resource.getFileInfo().getUrl();  
                if (url != null) {
                    String downloadHref = HtmlUtil.href ( url, IOUtil.getFileTail(url) );
                                                      //( url, url); // show complete text of the url for the link on the page
                                                      //( url, HtmlUtil.img( iconUrl("/down_arrow.gif"))); // shows a little arrow
                    String tmp = downloadHref;  // this looks pointless but is necessary
                    sb.append(HtmlUtil.col(tmp));
                } else {
                    sb.append(HtmlUtil.col("no file/URL"));
                }


                /* original code which shows, in the table of file-search results, in the "File" column (labeled above), 
                   a down arrow which is a link to the file for single file download, and a link (a highlighted file name) 
                   to a "resource page" which in the typical geodesy archive of data files, has no more information than the table of files-found results.
                   This is deprecated a geodesy archive.  Why click to get a new page with some of the same information you are looking at?
                // this href serves for a link to the "resource page about the file," not to the file.
                // onscreen it looks like text like a file name " soph0330.13d.Z"
                String href = makeResourceViewHref(resource);
                //                sb.append(HtmlUtil.col(href)); // do this when you want this thing in a column by itself
                String url = resource.getFileInfo().getUrl();  // url is the ...
                if (url != null) {
                    // note ! this 'href' is not the href above 
                    String downloadHref = HtmlUtil.href(
                                              url,
                                              HtmlUtil.img( iconUrl("/down_arrow.gif")));
                    // so this "File" column will show BOTH these items in this order
                    String tmp = downloadHref + " " + href;
                    sb.append(HtmlUtil.col(tmp));
                } else {
                    sb.append(HtmlUtil.col("N/A"));
                }
                */

                // Show file type for this file, such as 'GNSS RINEX Meteorology'
                if (resource.getType() != null) {
                    String filetype = resource.getType().getName();
                    // for full large font size   sb.append(HtmlUtil.col( filetype));
                    sb.append("<td align=\"left\" class=\"gsac-md5\" " + ">");
                    sb.append( "<font size=-2>" + filetype +"</font>");
                    sb.append("</td>");
                } else {
                    //sb.append(HtmlUtil.col("file type not specified", clickEvent));
                    sb.append(HtmlUtil.col("file type not specified"));
                }


                /* skip; not of any use:
                // show "related" content for this file, which is so far oNLY for the UNAVCO GSAC and which is only the special UNAVDCO-only full site ID like "Anguilla_BVI2012 CN02"
                //  If you have no "related content", skip this
                //  which gets rid of the mystery original "NA" column in table of file search results
                //if ( ( !relatedLabel.equals("") || relatedLabel != null) )   //&& resource.getRelatedResources() != null) 
                if ( relatedLabel!=null && relatedResources!=null && relatedLabel.length() > 0) {
                  StringBuffer relatedContent = new StringBuffer();
                  for (int relatedIdx = 0; relatedIdx < relatedResources.size(); relatedIdx++) {
                    GsacResource relatedResource = relatedResources.get(relatedIdx);
                    if (relatedIdx > 0) {
                        relatedContent.append("<br>");
                    }
                    if (relatedResource.getId() != null) {
                        String relatedUrl = makeResourceViewUrl(relatedResource);

                        relatedContent.append("<a href=\"" + relatedUrl + "\">" + relatedResource.getLongLabel() + "</a>");
                    } else {
                        relatedContent.append(relatedResource.getLongLabel());
                    }
                  }
                  if (relatedResources.size() == 0) {
                    relatedContent.append("NA");
                  }
                  sb.append(HtmlUtil.col(relatedContent.toString()));
                    System.err.println("    relatedContent.toString= " +relatedContent.toString() ); // debug only
                }
                end skip */

                SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                Date startTime   = resource.getFromDate();
                Date endTime     = resource.getToDate();
                Date publishTime = resource.getPublishDate();
                //System.err.println("   stop    time  =" +            sdfDate.format(endTime));       // has time of day from unav GSAC
                //System.err.println("   publish time  =" +            sdfDate.format(publishTime) );  

                //  to fix a bug original code with GsacOutputHandler:formatDateTime gives a wrong date-time value, hours later than input date-time
                String start   = sdfDate.format(startTime);
                String end     = sdfDate.format(endTime);
                String publish = sdfDate.format(publishTime);
                // if publish ends in 00:00:00, cut that part off the end:
                /*if ( publish.indexOf( "00:00:00") != -1) {
                       publish = publish.substring(0,10) ;
                } *.


                /* skip since relatedresource value is of no real use; and is in the UNAVCO GSAC:
                // any real "relatedResources" is, so far, from the UNAVCO GSAC instances, not the Prototype GSAC for general use.
                if (relatedResources.size() > 0) {  
                    //System.err.println("   HTML file list for UNAVCO GSAC ");
                    // original code to preserve legacy output format from the unavco gsac server
                    if (startTime == null) {
                        sb.append(HtmlUtil.col(formatDate(publishTime)));
                        System.err.println("  no start time so show Pub. Time  " + formatDate(publishTime) ); // debug only

                    } 
                    else {
                        if ((endTime == null) || endTime.equals(startTime)) {
                            sb.append(HtmlUtil.col(formatDate(startTime),
                                    clickEvent));
                        } else {
                            sb.append(HtmlUtil.col(formatDate(startTime) + " - "
                                    + formatDate(endTime), clickEvent));
                        }
                    }
                }
                else 
                end of skip */

                // show Time range of  etc., and publish time
                {
                    // more complete info in prototype gsac:

                    if (startTime == null && endTime != null) {
                           //end = sdfDate.format(endTime);
                           sb.append(HtmlUtil.col("unknown start - " + end));
                    }
                    else if (endTime == null && startTime != null) {
                           //start = sdfDate.format(startTime);
                           sb.append(HtmlUtil.col( start + " - end unknown"));
                    }
                    else if (endTime == null && startTime == null) { 
                           sb.append(HtmlUtil.col(  "start and end dates unknown"));
                    }
                    else {
                           //start = sdfDate.format(startTime);
                           //end = sdfDate.format(endTime);
                           //System.err.println(start + " - " + end);
                           // OK full size font sb.append(HtmlUtil.col(start + " - " + end));
                           sb.append("<td align=\"left\" class=\"gsac-times\" " + ">");
                           sb.append( "<font size=-2>" +  start + " - " + end +"</font>");
                           sb.append("</td>");
                    }

                    // for unavco gsac:
                    //if (relatedResources.size() > 0) {  
                       if (publishTime != null) {
                           //sb.append(HtmlUtil.col( publish ));
                           sb.append("<td align=\"left\" class=\"gsac-ptime\" " + ">");
                           sb.append( "<font size=-2>" + publish +"</font>");
                           sb.append("</td>");
                           // debug System.err.println( "publish time in files html table ="+ publish +"_");
                       } else {
                           sb.append("<td align=\"left\" class=\"gsac-ptime\" " + ">");
                           sb.append( "<font size=-2>N/A</font>");
                           sb.append("</td>");
                       }
                    //}
                }

                //  if not  unavco gsac, show sample interval and the MD5 check sum
                if (relatedResources.size() > 0) {  
                   ; // is the UNAVCO GSAC
                }
                else {
                    // show sample rate for this file;  (prototype gsac servers).  note the column label coded above as the Delta-symbol  in HTML
                    if ( /*resource.getFileInfo().getSampleInterval()!=null && */ resource.getFileInfo().getSampleInterval() > 0) {
                        //sb.append("<td align=\"left\" class=\"gsac-sampint\" " + clickEvent + ">");
                        sb.append("<td align=\"left\" class=\"gsac-sampint\" " +  ">");
                        //sampint       += resource.getFileInfo().getSampleInterval();
                        sb.append( "" +  resource.getFileInfo().getSampleInterval() );
                        sb.append("</td>");
                    } else {
                        //sb.append(HtmlUtil.col(" unknown"));
                        // CHANGEME or for UNR:
                        ;
                    }

                    // show MD5 value for this file if any (prototype gsac servers)
                    if (resource.getFileInfo().getMd5()!= null && resource.getFileInfo().getMd5() != "") {
                        sb.append("<td align=\"left\" class=\"gsac-md5\" " + ">");
                        sb.append( "<font size=-2>" +  resource.getFileInfo().getMd5() +"</font>");
                        sb.append("</td>");
                    } else {
                        sb.append(HtmlUtil.col("N/A"));
                        // CHANGEME  for UNR:
                        ;
                    }
                }

                // show file size value if any is available
                if (resource.getRelatedResources() != null) {
                    if ( /*resource.getFileInfo().getFileSize() !=null &&*/ resource.getFileInfo().getFileSize() > 0) {
                        //sb.append("<td align=\"left\" class=\"gsac-filesize\" " + clickEvent + ">");
                        filesizesum += resource.getFileInfo().getFileSize(); // computing for final line, shown next below

                        //sb.append("<td align=\"left\" class=\"gsac-filesize\" "+ ">");
                        //sb.append( ""+ formatFileSize( resource.getFileInfo().getFileSize()));
                        //sb.append("</td>");
                        sb.append("<td align=\"left\" class=\"gsac-md5\" " + ">");
                        sb.append( "<font size=-2>" +    resource.getFileInfo().getFileSize() +"</font>");
                        sb.append("</td>");
                    } else {
                        sb.append(HtmlUtil.col("N/A"));
                        // CHANGEME omit showing file size since none available
                    }
                 }

                sb.append("</tr>\n");
            } // end of loop on data files for (GsacFile resource : files)

            // show lower line of table with count of files found, and sum of all sizes found
            if (cnt == 0) {
                sb.append( getRepository().makeInformationDialog( msg("No files found")));
            } else {
                sb.append("<tr><td>&nbsp;</td>");

                sb.append("<td align=left>" + cnt + HtmlUtil.space(1) + msg("files") + "</td>");

                sb.append("<td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td>");

                if (  relatedLabel == null) // need extra column, empty, for table bottom row, if not the unavco server
                   { sb.append("<td>&nbsp;</td>"); }

                sb.append("<td align=\"left\" class=\"gsac-filesize\">");
                if (filesizesum > 0) {
                    sb.append("" + formatFileSize(filesizesum));
                }
                else {
                    sb.append(HtmlUtil.col(" "));  // no total file sizes sum made
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
