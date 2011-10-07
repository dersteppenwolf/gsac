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

package org.gsac.gsl.metadata.gnss;


import org.gsac.gsl.metadata.*;


import org.gsac.gsl.util.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 *
 */
public class GnssStream extends GsacMetadata {

    /** _more_ */
    public static final String TYPE_GNSS_STREAM = "gnss.stream";



    /**
     * _more_
     */
    public GnssStream() {
        super(TYPE_GNSS_STREAM);
    }

    /**
     * _more_
     *
     * @param metadata _more_
     *
     * @return _more_
     */
    public static List<GnssStream> getMetadata(
            List<GsacMetadata> metadata) {
        return (List<GnssStream>) findMetadata(metadata,
                GnssStream.class);
    }


}
