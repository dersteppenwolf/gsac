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

package org.gsac.gsl.downloader;


import ucar.unidata.util.FileManager;
import ucar.unidata.util.GuiUtils;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import java.awt.*;


import java.io.*;

import java.net.URL;
import java.net.URLConnection;

import java.text.DecimalFormat;


import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Vector;


import javax.swing.*;


/**
 * A webstart file downloader
 * @author Jeff McWhirter mcwhirter@unavco.org
 */
public class FileDownloader {


    /** command line args */
    private String[] args;

    /** Where we read the resource urls */
    private String resourceUrl;

    /** am currently downloading */
    private boolean downloading = false;

    /** Stop downloads by changing this */
    private int downloadTimeStamp = 0;

    /** where the urls are shown */
    private JTextArea urlList;

    /** list of urls */
    private List<String> urls = new ArrayList<String>();

    /** where we write files to */
    private File destDir;

    /** _more_ */
    private DecimalFormat sizeFormat = new DecimalFormat("####0.00");

    /** shows progress */
    private JProgressBar progressBar;

    /** shows messages */
    private JLabel message1Lbl;

    /** shows messages */
    private JLabel message2Lbl;

    /** button */
    private JButton downloadBtn;

    /** _more_ */
    private JButton reloadBtn;


    /** shows the dest dir */
    private JTextField fileFld;

    /** overwrite files widget */
    private JCheckBox overwriteCbx;

    /** _more_ */
    private JCheckBox multiThreadsCbx;

    /** widget */
    private JCheckBox keepPathsCbx;



    /**
     * ctor
     *
     * @param args command line args
     */
    public FileDownloader(String[] args) {
        this.args = args;
        if (args.length > 0) {
            resourceUrl = args[0];
        } else {
            resourceUrl =
                "http://localhost:8080/gsacws/resource/search/resources.txt?resource.sortvalue=resource.size&output=resource.url&search=Search+Files&site.code=p123&site.code.searchtype=exact&limit=50&resource.sortorder=ascending&site.name.searchtype=exact";
        }
        init();
    }




    /**
     * make the gui
     */
    private void init() {
        urlList = new JTextArea(10, 50);
        urlList.setEditable(false);
        JComponent fileComponent =
            GuiUtils.leftCenter(GuiUtils.makeButton("Destination Directory:",
                this, "chooseFile"), fileFld = new JTextField(""));
        fileFld.setEditable(false);

        keepPathsCbx = new JCheckBox("Make local directories", true);
        keepPathsCbx.setToolTipText(
            "Make local directories from FTP directories");

        overwriteCbx = new JCheckBox("Overwrite files", false);
        overwriteCbx.setToolTipText("Overwrite existing files?");
        multiThreadsCbx = new JCheckBox("Download in parallel", false);
        multiThreadsCbx.setToolTipText("Download multiple files at once");


        JComponent buttons =
            GuiUtils.doLayout(new JComponent[] {
                downloadBtn =
                    GuiUtils.makeButton("Download", this, "doDownload"),
                GuiUtils.makeButton("Quit", this, "quit") }, 2,
                    GuiUtils.WT_N, GuiUtils.WT_N);

        JComponent bottom = GuiUtils.leftRight(GuiUtils.hbox(keepPathsCbx,
                                overwriteCbx /*, multiThreadsCbx*/), buttons);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);
        progressBar.setStringPainted(true);
        JComponent contents =
            GuiUtils.topCenterBottom(
                GuiUtils.inset(
                    GuiUtils.leftRight(
                        new JLabel("Files to download"),
                        reloadBtn =
                            GuiUtils.makeButton(
                                "Reload", this, "fetchFileList")), new Insets(
                                    2, 2, 0, 2)), GuiUtils.makeScrollPane(
                                        urlList, 100, 50), bottom);
        message1Lbl = new JLabel(" ");
        message2Lbl = GuiUtils.rLabel(" ");
        JComponent statusBar =
            GuiUtils.vbox(GuiUtils.leftRight(message1Lbl, message2Lbl),
                          GuiUtils.centerRight(progressBar,
                              GuiUtils.filler(1, 40)));
        contents = GuiUtils.topCenterBottom(fileComponent, contents,
                                            statusBar);
        contents = GuiUtils.inset(contents, new Insets(2, 2, 2, 2));
        contents.setBorder(BorderFactory.createRaisedBevelBorder());

        JFrame f = new JFrame("GSAC File Downloader");

        f.getContentPane().add(contents);
        f.pack();
        f.setLocation(200, 200);
        f.setVisible(true);
        Misc.run(this, "fetchFileList");
    }

    /**
     * _more_
     *
     * @param bytes _more_
     *
     * @return _more_
     */
    public String formatFileSize(long bytes) {
        if (bytes < 1000) {
            return "" + bytes;
        }
        if (bytes < 1000000) {
            return sizeFormat.format(bytes / 1000.0) + " KB";
        }
        if (bytes < 1000000000) {
            return sizeFormat.format(bytes / 1000000.0) + " MB";
        }

        return sizeFormat.format(bytes / 1000000000.0) + " GB";
    }



    /**
     * Select a dest dir
     */
    public void chooseFile() {
        File f = FileManager.getDirectory(((destDir != null)
                                           ? destDir.toString()
                                           : ""), "Please choose a destination directory");
        if (f != null) {
            destDir = f;
            fileFld.setText(destDir.toString());
        }
    }

    /**
     * start the download
     */
    public void doDownload() {
        if ((destDir == null) || !destDir.exists()) {
            chooseFile();
        }
        if ((destDir == null) || !destDir.exists()) {
            return;
        }
        Misc.run(this, "doDownloadInner");
    }


    /**
     * actually do the download
     */
    public void doDownloadInner() {

        if (downloading) {
            downloadBtn.setText("Stopping");
            downloading = false;
            downloadTimeStamp++;

            return;
        }
        downloadBtn.setText("  Stop  ");
        final int myTimeStamp = ++downloadTimeStamp;
        downloading = true;
        progressBar.setValue(0);
        progressBar.setMinimum(0);
        progressBar.setMaximum(urls.size());
        progressBar.setVisible(true);
        clearMessages();
        final long[]       totalSize   = { 0 };
        final int[]        cnt         = { 0 };
        final List<String> myUrls      = new ArrayList<String>(urls);

        final int[]        runnableCnt = { 0 };
        Runnable           runnable    = new Runnable() {
            public void run() {
                try {
                    while (myUrls.size() > 0) {
                        String f;
                        synchronized (myUrls) {
                            if (myUrls.size() == 0) {
                                break;
                            }
                            f = myUrls.remove(0);
                            urls.remove(f);
                            updateUrlList();
                            cnt[0]++;
                            progressBar.setValue(cnt[0]);
                            message1("Fetching: " + f);
                        }
                        if ( !canContinue(myTimeStamp)) {
                            break;
                        }
                        try {
                            URL           url        = new URL(f);
                            String        tail       = IOUtil.getFileTail(f);
                            URLConnection connection = url.openConnection();
                            InputStream   is = connection.getInputStream();
                            File          newDest    = destDir;
                            if (keepPathsCbx.isSelected()) {
                                String urlPath = url.getPath();
                                if (urlPath.indexOf("..") >= 0) {
                                    //Make sure there isn't anything funny here
                                    urlPath = "";
                                }
                                List<String> toks = StringUtil.split(urlPath,
                                                        "/", true, true);
                                toks.remove(toks.size() - 1);
                                String newPath = StringUtil.join("/", toks);
                                newDest = new File(IOUtil.joinDir(newDest,
                                        newPath));
                                //System.err.println ("newDest:" + newDest);
                                IOUtil.makeDirRecursive(newDest);
                            }

                            File newFile = new File(IOUtil.joinDir(newDest,
                                               tail));
                            if (newFile.exists()
                                    && !overwriteCbx.isSelected()) {
                                //                    message1("Skipping file:" + tail);
                                continue;
                            }

                            int numBytes = (int)IOUtil.writeTo(
                                               is,
                                               new BufferedOutputStream(
                                                   new FileOutputStream(
                                                       newFile), 8000));
                            synchronized (totalSize) {
                                totalSize[0] += numBytes;
                                message2("Downloaded " + cnt[0]
                                         + " files  Total size: "
                                         + formatFileSize(totalSize[0]));
                            }
                        } catch (Exception exc) {
                            logError("Downloading files", exc);
                        }
                        if ( !canContinue(myTimeStamp)) {
                            break;
                        }
                    }
                } finally {
                    synchronized (runnableCnt) {
                        runnableCnt[0]--;
                    }
                }
            }
        };

        int numThreads = (multiThreadsCbx.isSelected()
                          ? 4
                          : 1);
        runnableCnt[0] = numThreads;
        for (int i = 0; i < numThreads; i++) {
            Misc.run(runnable);
        }



        while (true) {
            Misc.sleep(10);
            synchronized (runnableCnt) {
                if (runnableCnt[0] <= 0) {
                    break;
                }
            }
        }

        downloadBtn.setText("Download");
        message1("Done downloading");
        progressBar.setVisible(false);
        downloading = false;

    }



    /**
     * _more_
     *
     * @param myTimeStamp _more_
     *
     * @return _more_
     */
    private boolean canContinue(int myTimeStamp) {
        return downloading && (myTimeStamp == downloadTimeStamp);
    }

    /**
     * exit
     */
    public void quit() {
        System.exit(0);
    }

    /**
     * get the list of files
     */
    public void fetchFileList() {
        Misc.run(this, "fetchFileListInner");
    }

    /**
     * really get the list of files
     */
    public void fetchFileListInner() {
        try {
            progressBar.setIndeterminate(true);
            progressBar.setVisible(true);
            reloadBtn.setEnabled(false);
            downloadBtn.setEnabled(false);
            message1("Loading files");
            String files = IOUtil.readContents(resourceUrl);
            urls = StringUtil.split(files, "\n", true, true);
            updateUrlList();
            clearMessages();
        } catch (Exception exc) {
            logError("Trying to read files from url:" + resourceUrl, exc);
        }
        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);
        reloadBtn.setEnabled(true);
        downloadBtn.setEnabled(true);
    }

    /**
     * set the url list
     */
    private void updateUrlList() {
        urlList.setText(StringUtil.join("\n", urls));
    }

    /**
     * _more_
     */
    public void clearMessages() {
        message1("");
        message2("");
    }

    /**
     * show user message
     *
     * @param message message
     */
    public void message1(String message) {
        message1Lbl.setText(message + " ");
    }


    /**
     * _more_
     *
     * @param message _more_
     */
    public void message2(String message) {
        progressBar.setString(message);
        //        message2Lbl.setText(message + " ");
    }

    /**
     * show error
     *
     * @param msg message
     * @param exc exception
     */
    public void logError(String msg, Exception exc) {
        urlList.setText("Error:" + msg + "\n" + exc);
    }

    /**
     * main
     *
     * @param args args
     *
     * @throws Exception On badness
     */
    public static void main(String[] args) throws Exception {
        FileDownloader fileDownloader = new FileDownloader(args);
    }




}
