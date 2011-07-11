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

package gov.nasa.cddis.gsac;


import org.gsac.gsl.*;
import org.gsac.gsl.metadata.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.util.*;



/**
 * Holds CDDIS specific url arguments
 *
 * @author         Jeff McWhirter
 */
public interface CddisArgs {

    /** _more_ */
    public static final String ARG_VLBI_SITE_TYPE =
        GsacArgs.ARG_SITE_PREFIX + Tables.SITE_INFO_VLBI.COL_SITE_TYPE;

    /** _more_ */
    public static final String ARG_SLR_SITE_TYPE =
        GsacArgs.ARG_SITE_PREFIX + Tables.SITE_INFO_SLR.COL_SITE_TYPE;


    /** _more_ */
    public static final String ARG_MONUMENT = GsacArgs.ARG_SITE_PREFIX
                                              + "monument";

    /** _more_ */
    public static final String ARG_DOMES_NUMBER = GsacArgs.ARG_SITE_PREFIX
                                                  + "domes_number";


    /** _more_ */
    public static final String ARG_GNSS_DOMES_NUMBER =
        GsacArgs.ARG_SITE_PREFIX + "gnss_domes_number";

    /** _more_ */
    public static final String ARG_GLONASS = GsacArgs.ARG_SITE_PREFIX
                                             + "glonass";

    /** _more_ */
    public static final String ARG_HIGHRATE = GsacArgs.ARG_SITE_PREFIX
                                              + "highrate";

    /** _more_ */
    public static final String ARG_HOURLY = GsacArgs.ARG_SITE_PREFIX
                                            + "hourly";

    /** _more_ */
    public static final String ARG_SLR_DOMES_NUMBER =
        GsacArgs.ARG_SITE_PREFIX + "slr_domes_number";

    /** _more_ */
    public static final String ARG_SLR_STATION = GsacArgs.ARG_SITE_PREFIX
                                                 + "slr_station";

    /** _more_ */
    public static final String ARG_VLBI_DOMES_NUMBER =
        GsacArgs.ARG_SITE_PREFIX + "vlbi_domes_number";

    /** _more_ */
    public static final String ARG_VLBI_STATION = GsacArgs.ARG_SITE_PREFIX
                                                  + "vlbi_station";

    /** _more_ */
    public static final String ARG_DORIS_DOMES_NUMBER =
        GsacArgs.ARG_SITE_PREFIX + "doris_domes_number";


    /** _more_ */
    public static final String ARG_DORIS_SATELLITE = GsacArgs.ARG_FILE_PREFIX
                                                     + "doris.satellite";

    /** _more_ */
    public static final String ARG_SLR_SATELLITE = GsacArgs.ARG_FILE_PREFIX
                                                   + "slr.satellite";



}
