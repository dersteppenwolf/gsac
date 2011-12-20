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

	<xsl:variable name="nl">
</xsl:variable>  <!-- select="codepoints-to-string((13,10))"/> -->
	<!-- get the date from the param or from current date? -->
	<xsl:variable name="datadate.to" select="format-date( current-date(), '[Y]-[M01]-[D01]' )"/>
	<xsl:variable name="datadate.from"
		select="format-date(xs:date(current-date() - xs:dayTimeDuration('P7D')),'[Y]-[M01]-[D01]')"/>
	
 	<xsl:template match="/">
<project name="gsacws" default="get-files" basedir=".">

	<!-- Create the filesets to be used by the ftp tasks. -->
	<xsl:for-each-group select="/object/method[@name='add'][object[@class='org.gsac.gsl.model.GsacFile']]"
		group-by="object/property[@name='PublishTime']/object/constructor/long">
			
		<xsl:variable name="id" select="concat('f-',position())"/>
		<xsl:variable name="url" select="normalize-space(object/property[@name='FileInfo']/object/property[@name='Url']/string)"/>
		<!-- Extract the directory by tokenizing the string on the directory separator. -->
		<xsl:variable name="pseq" select="tokenize($url,'/')"/>
		<!-- Ignore the hostname and top most directory. Based on a priori knowledge of the directory.... -->
		<xsl:variable name="rdir" select="string-join( (for $i in (5 to (count($pseq) - 1)) return $pseq[$i], ''), '/')"/>

		<fileset id="{$id}" dir="{$dir}">
			<xsl:for-each select="current-group()">
				<xsl:variable name="furl" select="normalize-space(object/property[@name='FileInfo']/object/property[@name='Url']/string)"/>
				<xsl:variable name="fseq" select="tokenize($furl,'/')"/>
				<xsl:variable name="fname" select="concat($rdir,$fseq[last()])"/>
				<include name="{$fname}"/>
			</xsl:for-each>
		</fileset>
	
	</xsl:for-each-group>

	<target name="get-files">
		<mkdir dir="{$dir}"/>
		<parallel>
	
			<!-- Create the ftp tasks in parallel. -->
			<xsl:for-each-group select="/object/method[@name='add'][object[@class='org.gsac.gsl.model.GsacFile']]"
				group-by="object/property[@name='PublishTime']/object/constructor/long">
				
				<xsl:variable name="id" select="concat('f-',position())"/>
				<xsl:variable name="url" select="normalize-space(object/property[@name='FileInfo']/object/property[@name='Url']/string)"/>
				<xsl:variable name="seq" select="tokenize($url,'/')"/>
				<xsl:variable name="ftp-host" select="$seq[3]"/>
				<xsl:variable name="pub-dir" select="$seq[4]"/>
	
					<ftp server="{$ftp-host}" action="get" remotedir="{$pub-dir}"
								userid="{$userid}"
								password="{$password}"
								verbose="yes"
								preserveLastModified="true">
						<fileset refid="{$id}"/>
					</ftp>
		
			</xsl:for-each-group>
	
		</parallel>
	
	</target>

</project>
	</xsl:template>
	
</xsl:stylesheet>
