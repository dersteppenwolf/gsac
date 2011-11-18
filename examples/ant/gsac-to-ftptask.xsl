<?xml version="1.0" encoding="utf-8" standalone="yes"?>

<!-- $Id$ -->

<!--
     This script parses GSAC XML to find the file information.
     It is an example of using an Ant task to process the GSAC 
     service request, create a build file that will download the 
     files in the query result.  
-->

<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:param name="dstamp"/>
	<xsl:param name="server"/>
	<xsl:param name="userid"/>
	<xsl:param name="password"/>
	<xsl:param name="remote-dir"/>
	<xsl:param name="dir"/>

	<xsl:output method="xml" indent="yes"/>

	<xsl:variable name="nl"><xsl:text>
</xsl:text></xsl:variable>

	<variable name="cur-year" select="substring($dstamp,1,4)"/>
	<variable name="cur-month" select="substring($dstamp,5,2)"/>
	<variable name="cur-day" select="substring($dstamp,7,2)"/>

	<xsl:template match="/">
<project name="gsacws" default="get-files" basedir=".">

	<fileset id="ftp-files" dir="{$dir}">
		<xsl:apply-templates select="//object[@class='org.gsac.gsl.model.FileInfo']"/>
	</fileset>

	<target name="get-files">
		<mkdir dir="{$dir}"/>
		<ftp server="{$server}" action="get" remotedir="{$remote-dir}"
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
