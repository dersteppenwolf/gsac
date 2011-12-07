<?xml version="1.0" encoding="utf-8" standalone="yes"?>

<!-- $Id: gsac-to-ftptask.xsl 319 2011-12-05 02:35:35Z griffinwerks $ -->

<!--
	This script creates an ant build file.
-->

<xsl:stylesheet
  version="2.0"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="xml" indent="yes"/>

	<xsl:variable name="nl" select="codepoints-to-string((13,10))"/>
	
	<xsl:variable name="classpath" select="'${ant.home}/lib/saxon9he.jar'"/>
	
	<!-- It may look clumsy, but you can perform date/duration calculations and date formating --> 
	<xsl:variable name="datadate.to" select="format-date( current-date(), '[Y]-[M01]-[D01]' )"/>
	<xsl:variable name="datadate.from"
		select="format-date(xs:date(current-date() - xs:dayTimeDuration('P7D')),'[Y]-[M01]-[D01]')"/>
	<xsl:variable name="site.group" select="/properties/site/group"/>
	<xsl:variable name="file.type" select="/properties/file/type"/>
	<xsl:variable name="service-url" select="concat(
		/properties/archive/host, /properties/service/endpoint, '?',
		'output=file.gsacxml',
		'&amp;file.datadate.from=', $datadate.from,
		'&amp;file.datadate.to=', $datadate.to,
		'&amp;site.group=', $site.group,
		'&amp;file.type=', $file.type )"/>
	<xsl:variable name="service.result.file" select="concat( 'q-', $datadate.to, '.xml')"/>
	<xsl:variable name="target.dir" select="/properties/target/dir"/>
	<xsl:variable name="userid" select="/properties/userid"/>
	<xsl:variable name="password" select="/properties/password"/>
	
	<!-- Notice that Ant variable look like this: ${variable} whereas
	     XSLT Attribute Value Templates look like {$variable} -->
		
	<xsl:template match="/">
<project name="gsacws-get" default="gsacws-get" basedir=".">

	<target name="gsacws-get" description="Query Archive REST Service for Files">
		<echo>Querying GSAC-WS archive...</echo>
		<get src="{$service-url}" dest="{$service.result.file}"/>
		<echo>Parsing GSAC XML <xsl:value-of select="$service.result.file"/> to Ant FTP task</echo>
		<xslt style="gsac-to-ftptask2.xsl" in="{$service.result.file}" out="build-ftp2.xml"
			classpath="${{ant.home}}/lib/saxon9he.jar">
			<factory name="net.sf.saxon.TransformerFactoryImpl"/>
			<param name="userid" expression="{$userid}"/>
			<param name="password" expression="{$password}"/>
			<param name="dir" expression="{$target.dir}"/>
		</xslt>
		<mkdir dir="{$target.dir}"/>
		<echo>Calling Ant FTP task...</echo>
		<!-- ant antfile="build-ftp2.xml" / -->
	</target>
	
</project>
	</xsl:template>
	
</xsl:stylesheet>
