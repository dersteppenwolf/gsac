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

package org.gsac.gsl.metadata;


import org.gsac.gsl.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.output.*;

import ucar.unidata.util.HtmlUtil;


import ucar.unidata.util.Misc;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Copied from the blaze_timecodes/src/org/unavco/remoting/timecode
 * package from the DAI web services
 * @author Jeff McWhirter
 */
public class DateRangeCollection extends GsacMetadata {

    /** _more_ */
    private List<long[]> dateRanges = new ArrayList<long[]>();

    /**
     * _more_
     */
    public DateRangeCollection() {}

    /**
     *  Set the StartDate property.
     *
     * @param date1 start date
     * @param date2 end date
     */
    public void addDateRange(long date1, long date2) {
        dateRanges.add(new long[] { date1, date2 });
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param gsacResource _more_
     * @param outputHandler _more_
     * @param pw _more_
     *
     * @return _more_
     *
     * @throws IOException _more_
     */
    public boolean addHtml(GsacRequest request, GsacResource gsacResource,
                           HtmlOutputHandler outputHandler, Appendable pw)
            throws IOException {
        GsacRepository repository = outputHandler.getRepository();
        if (dateRanges.size() == 0) {
            System.err.println("  addHtml:  date ranges size is 0");
            return true;
        }
        StringBuffer buff = new StringBuffer(HtmlUtil.formTable());
        long         min  = dateRanges.get(0)[0];
        long         max  = dateRanges.get(0)[1];
        for (long[] tuple : dateRanges) {
            min = Math.min(min, tuple[0]);
            max = Math.max(max, tuple[1]);
        }

        // print date ranges data time ranges values:    Data Date Ranges Data Time Ranges
        //System.err.println("# date ranges: size=" + dateRanges.size());
        //System.err.println("  date ranges:  min=" + min);
        //System.err.println("  date ranges:  max=" + max);

        int rowWidth = 1000;

        //        buff.append(HtmlUtil.row(HtmlUtil.cols(new String[] {
        //            HtmlUtil.b("From"),
        //            HtmlUtil.b("To") })));
        buff.append("<table border=1 cellspacing=0 cellpadding=0><tr>");
        long lastTime = 0;
        for (int i = 0; i < dateRanges.size(); i++) {
            long[] tuple              = dateRanges.get(i);
            String from = outputHandler.formatDate(new Date(tuple[0]));
            String to = outputHandler.formatDate(new Date(tuple[1]));
            double timeDelta          = (double) (tuple[1] - tuple[0]);
            double percentOfTimeRange = timeDelta / (max - min);
            int    width              = (int) (rowWidth * percentOfTimeRange);

            if (i > 0) {
                double percentOfTimeRange2 = (tuple[0] - lastTime)
                                             / (double) (max - min);
                int width2 = (int) (rowWidth * percentOfTimeRange2);
                buff.append("<td>");
                buff.append(
                    HtmlUtil.img(
                        repository.iconUrl("/blank.gif"), "",
                        HtmlUtil.attr(HtmlUtil.ATTR_WIDTH, "" + width2)
                        + HtmlUtil.attr(HtmlUtil.ATTR_HEIGHT, "10")));

                buff.append("</td>");
            }
            lastTime = tuple[1];
            //System.err.println(from + " " + to + " width:" + width + " %:" + percentOfTimeRange);
            buff.append("<td bgcolor=red>");

            buff.append( HtmlUtil.img( repository.iconUrl("/blank.gif"), "", HtmlUtil.attr(HtmlUtil.ATTR_WIDTH, "" + width) + HtmlUtil.attr(HtmlUtil.ATTR_HEIGHT, "10")));

            buff.append("</td>");
            //            buff.append(HtmlUtil.row(HtmlUtil.cols(new String[] { from,
            //                    to })));
        }
        buff.append("</table>");
        buff.append(HtmlUtil.formTableClose());
        pw.append(
            outputHandler.formEntryTop(
                request, outputHandler.msgLabel("Data Availability"),
                HtmlUtil.makeShowHideBlock("", buff.toString(), false)));

        return true;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public List<long[]> getDateRanges() {
        return dateRanges;
    }

    /**
     * _more_
     *
     * @param ranges _more_
     */
    public void setDateRanges(List<long[]> ranges) {
        dateRanges = ranges;
    }


}
