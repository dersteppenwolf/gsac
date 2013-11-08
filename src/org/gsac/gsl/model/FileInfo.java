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

package org.gsac.gsl.model;


import org.gsac.gsl.util.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import java.net.URL;
import java.net.URLConnection;

import java.util.Date;


/**
 */
public class FileInfo {

    /** _more_ */
    public static final int CLASS_FILE = 0;

    /** _more_ */
    public static final int TYPE_NONFILE = 1;

    /** _more_ */
    private int type = CLASS_FILE;

    /** _more_ */
    private File localFile;

    /** _more_ */
    private String url="";

    /** _more_ */
    private long fileSize = 0;

    /** _more_ */
    private String md5="";

    /**  sample rate in seconds of instrument making this geodesy data file */
    private float sample_interval=0.0f;

    /**
     * ctor
     */
    public FileInfo() {}

    /**
     * _more_
     *
     * @param url _more_
     */
    public FileInfo(String url) {
        this.url = url;
    }


    /**
     * _more_
     *
     * @param url _more_
     * @param fileSize _more_
     * @param md5 _more_
     */
    public FileInfo(String url, long fileSize, String md5) {
        this(url, fileSize, md5, CLASS_FILE);
    }

    /**
     * _more_
     *
     * @param url _more_
     * @param fileSize _more_
     * @param md5 _more_
     * @param type _more_
     */
    public FileInfo(String url, long fileSize, String md5, int type) {
        this(url, fileSize, md5, type, null);
    }



    /**
     * _more_
     *
     * @param url _more_
     * @param fileSize _more_
     * @param md5 _more_
     * @param type _more_
     * @param localFile _more_
     */
    public FileInfo(String url, long fileSize, String md5, int type, File localFile) {
        this.url       = url;
        this.fileSize  = fileSize;
        this.md5       = md5;
        this.type      = type;
        this.localFile = localFile;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return this.url;
    }

    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public InputStream getInputStream() throws Exception {
        if (type != CLASS_FILE) {
            return null;
        }
        if (localFile != null) {
            return new FileInputStream(localFile);
        }
        URLConnection connection = new URL(url).openConnection();

        return connection.getInputStream();
    }






    /**
     *  Set the Url property.
     *
     *  @param value The new value for Url
     */
    public void setUrl(String value) {
        url = value;
    }

    /**
     *  Get the Url property.
     *
     *  @return The Url
     */
    public String getUrl() {
        return url;
    }

    /**
     *  Set the FileSize property.
     *
     *  @param value The new value for FileSize
     */
    public void setFileSize(long value) {
        fileSize = value;
    }

    /**
     *  Get the FileSize property.
     *
     *  @return The FileSize
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * Set the sample_interval property.
     *
     * @param value The new value for 
     */
    public void  setSampleInterval(float val )  {
       sample_interval=val;
    }

    /**
     *  Get the property.
     *
     *  @return the 
     */
    public float getSampleInterval() {
        return sample_interval;
    }

    /**
     *  Set the Md5 property.
     *
     *  @param value The new value for Md5
     */
    public void setMd5(String value) {
        md5 = value;
    }

    /**
     *  Get the Md5 property.
     *
     *  @return The Md5
     */
    public String getMd5() {
        return md5;
    }

    /**
     *  Set the Type property.
     *
     *  @param value The new value for Type
     */
    public void setType(int value) {
        type = value;
    }

    /**
     *  Get the Type property.
     *
     *  @return The Type
     */
    public int getType() {
        return type;
    }


    /**
     *  Set the LocalFile property.
     *
     *  @param value The new value for LocalFile
     */
    public void setLocalFile(File value) {
        localFile = value;
    }

    /**
     *  Get the LocalFile property.
     *
     *  @return The LocalFile
     */
    public File getLocalFile() {
        return localFile;
    }


}
