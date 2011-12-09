<?xml version="1.0" encoding="utf-8" standalone="yes"?>

<!-- $Id$ -->

<!--
     This script parses GSAC XML to find the file information.
     It is an example of using an Ant task to process the GSAC 
     service request, create a build file that will download the 
     files in the query result.  
-->

<xsl:stylesheet
  version="2.0"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:param name="dstamp"/>
	<xsl:param name="host"/>
	<xsl:param name="userid"/>
	<xsl:param name="password"/>
	<xsl:param name="dir"/>

	<xsl:output method="xml" indent="yes"/>

	<xsl:variable name="nl" select="codepoints-to-string((13,10))"/>
	<!-- get the date from the param or from current date? -->
	<xsl:variable name="datadate.to" select="format-date( current-date(), '[Y]-[M01]-[D01]' )"/>
	<xsl:variable name="datadate.from"
		select="format-date(xs:date(current-date() - xs:dayTimeDuration('P7D')),'[Y]-[M01]-[D01]')"/>
	
 
 	<xsl:template match="/">
<project name="gsacws" default="get-files" basedir=".">

	<fileset id="ftp-files" dir="{$dir}">
		<xsl:apply-templates select="//object[@class='org.gsac.gsl.model.FileInfo']"/>
	</fileset>

	<target name="get-files">
		<mkdir dir="{$dir}"/>
		<ftp server="{$host}" action="get" remotedir="/"
					userid="{$userid}"
					password="{$password}"
					verbose="yes"
					preserveLastModified="true">
				<fileset refid="ftp-files"/>
			</ftp>
	
	</target>

</project>
	</xsl:template>
	
	<xsl:template match="object[@class='org.gsac.gsl.model.FileInfo']">
		<xsl:variable name="fname" select="normalize-space(../../property[@name='ShortName']/string)"/>
		<include name="{$fname}"/>
	</xsl:template>

<!--
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
-->

</xsl:stylesheet>
