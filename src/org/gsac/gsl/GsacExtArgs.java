/*
 * Copyright 2012 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
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

package org.gsac.gsl;



/**
 * Interface description: defines GSAC variables; declares names of them (such as "site....") to use making API queries and in making results returned.
 *
 *
 * @version  Aug 29, 2013 
 * @author   JM; SKW; 2010-2013.
 */
public interface GsacExtArgs {

    /** _more_ */
    public static final String ARG_ANTENNA = GsacArgs.ARG_SITE_PREFIX + "antenna";

    /** _more_ */
    public static final String ARG_ANTENNA_SN = GsacArgs.ARG_SITE_PREFIX + "antennaSN";

    /** _more_ */
    public static final String ARG_ANTENNA_HT = GsacArgs.ARG_SITE_PREFIX + "antenna.ht";

    /** HtCod is for GAMIT station.info format */
    public static final String ARG_ANTENNA_HTCOD = GsacArgs.ARG_SITE_PREFIX + "antenna.htcod";

    /** _more_ */
    public static final String ARG_REGION = GsacArgs.ARG_SITE_PREFIX + "region";

    /** dome name or type, NOT the "IERSDOMES" value  */
    public static final String ARG_DOME = GsacArgs.ARG_SITE_PREFIX + "dome";

    /** _more_ */
    public static final String ARG_RECEIVER = GsacArgs.ARG_SITE_PREFIX + "receiver";

    /** _more_ */
    public static final String ARG_MONUMENT = GsacArgs.ARG_SITE_PREFIX + "monument";

    /** _more_ */
    public static final String ARG_SAMPLE_INTERVAL = GsacArgs.ARG_SITE_PREFIX + "sampleinterval";

    /** _more_ */
    public static final String ARG_HAS_METPACK = GsacArgs.ARG_SITE_PREFIX + "hasmetpack";

    /** _more_ */
    public static final String ARG_CITY = GsacArgs.ARG_SITE_PREFIX + "city";

    /** _more_ */
    public static final String ARG_COUNTRY = GsacArgs.ARG_SITE_PREFIX + "country";

    /** _more_ */
    public static final String ARG_STATE = GsacArgs.ARG_SITE_PREFIX + "state";

    public static final String ARG_NETWORK = GsacArgs.ARG_SITE_PREFIX + "network";

    /** Name of station's tectonic plate.  */
    public static final String ARG_TECTONICPLATE = GsacArgs.ARG_SITE_PREFIX + "tectonicplate";

    public static final String SITE_METADATA_MONUMENTINSCRIPTION = "site.metadata.monumentinscription";

    public static final String SITE_METADATA_IDENTIFICATIONMONUMENT = "site.metadata.identificationmonument";

    public static final String SITE_METADATA_MONUMENTDESCRIPTION = "site.metadata.monumentdescription";

    public static final String SITE_METADATA_IERDOMES = "site.metadata.ierdomes";

    public static final String SITE_METADATA_INDIVIDUALCALIBRATION = "site.metadata.calibration";

    public static final String SITE_METADATA_CDPNUM  = "site.metadata.cdpnum";

    public static final String SITE_METADATA_NAMEAGENCY  = "site.metadata.nameagency";

    public static final String SITE_METADATA_NAMEMETPACK = "site.metadata.namemetpack";

    public static final String SITE_METADATA_FREQUENCYSTANDARD = "metadata.frequencystandard";

    public static final String SITE_PHOTO_URL = "site.metadata.photoURL";

    /**  the x,y,z coordinate values of a location in a geodetic terrestrial reference frame (TRF): */
    public static final String SITE_TRF_X = "site.metadata.x";
    public static final String SITE_TRF_Y = "site.metadata.y";
    public static final String SITE_TRF_Z = "site.metadata.z";

}
