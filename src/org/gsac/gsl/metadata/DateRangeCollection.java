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


import ucar.unidata.util.Misc;

import java.util.ArrayList;
import java.util.List;


/**
 * Copied from the blaze_timecodes/src/org/unavco/remoting/timecode
 * package from the DAI web services
 *
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
