
This is a demonstration of the GSAC-WS repository services utilizing the file.gsacxml file type.

It demonstrates how to call the web service and use the result to build a task to download files.

It can hardly be called version 0.1, but it is a start.

Tested and developed on:

ant 1.8.2
ant-commons-net.jar     Ant tasks for Apache commons, available in the full ant dist.
net-commons.jar  3.0.1       Apache commons network library from http://commons.apache.org/net


build.xml    - The main ant build file, run ant -projecthelp to see targets

build-01.xml - A file of various tests that will be incorporated or deprecated.

file-gsac.xsl - A XSLT 2.0 file to parse an ftp directory listing, part of tests. 
                Requires saxon processor.

gsac-to-ftptask.xsl - XSLT 1.0 compatible file that calls the GSAC-WS service to 
                      parse the file.gsacxml into an ant build file that will 
                      get files by ftp.