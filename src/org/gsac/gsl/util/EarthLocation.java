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

package org.gsac.gsl.util;




/**
 * Holds a lat/lon/alt
 */
public class EarthLocation {

    /** lat */
    private double latitude;

    /** lon */
    private double longitude;

    /** elevation */
    private double elevation;

    /**
     * ctor
     */
    public EarthLocation() {}


    /**
     * ctor
     *
     * @param latitude latitude
     * @param longitude  longitude
     * @param elevation elevation
     */
    public EarthLocation(double latitude, double longitude,
                         double elevation) {
        this.latitude  = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
    }


    /**
     * Normalize the longitude to lie between +/-180
     * @param lon east latitude in degrees
     * @return normalized lon
     */
    static public double normalizeLongitude(double lon) {
        if ((lon < -180.) || (lon > 180.)) {
            return Math.IEEEremainder(lon, 360.0);
        } else {
            return lon;
        }
    }



    /**
     *  Set the Latitude property.
     *
     *  @param value The new value for Latitude
     */
    public void setLatitude(double value) {
        latitude = value;
    }

    /**
     *  Get the Latitude property.
     *
     *  @return The Latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     *  Set the Longitude property.
     *
     *  @param value The new value for Longitude
     */
    public void setLongitude(double value) {
        longitude = value;
    }

    /**
     *  Get the Longitude property.
     *
     *  @return The Longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     *  Set the Elevation property.
     *
     *  @param value The new value for Elevation
     */
    public void setElevation(double value) {
        elevation = value;
    }

    /**
     *  Get the Elevation property.
     *
     *  @return The Elevation
     */
    public double getElevation() {
        return elevation;
    }




}
