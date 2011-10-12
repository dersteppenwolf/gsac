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

    /** _more_          */
    private double x = Double.NaN;

    /** _more_          */
    private double y = Double.NaN;

    /** _more_          */
    private double z = Double.NaN;

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


    public boolean hasXYZ() {
        return x==x && y==y && z==z;
    }


    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     * @param z _more_
     */
    public void setXYZ(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }


    /**
     *  Set the X property.
     *
     *  @param value The new value for X
     */
    public void setX(double value) {
        x = value;
    }

    /**
     *  Get the X property.
     *
     *  @return The X
     */
    public double getX() {
        return x;
    }

    /**
     *  Set the Y property.
     *
     *  @param value The new value for Y
     */
    public void setY(double value) {
        y = value;
    }

    /**
     *  Get the Y property.
     *
     *  @return The Y
     */
    public double getY() {
        return y;
    }

    /**
     *  Set the Z property.
     *
     *  @param value The new value for Z
     */
    public void setZ(double value) {
        z = value;
    }

    /**
     *  Get the Z property.
     *
     *  @return The Z
     */
    public double getZ() {
        return z;
    }



}
