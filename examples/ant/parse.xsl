<?xml version="1.0" standalone="yes"?>

<!-- $Id: -->

<!-- 

  This is an XSLT 2.0 script that parses an ftp directory list.
  The idea is that a script can get a directory listing and 
  compare with local files to determine what to download.
  There may be features in the ant ftp task that also 
  accomplish this....

-->
 
<xsl:stylesheet
  version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="xml" indent="yes"/>

	<xsl:variable name="nl"><xsl:text>
</xsl:text></xsl:variable>
	<xsl:variable name="month-names" select="('jan','feb','mar','apr','may','jun','jul','aug','sep','oct','nov','dec')"/>

	
	<xsl:template match="/">
		<xsl:value-of select="$nl"/>
		<files xmlns=""><xsl:value-of select="$nl"/>
			<xsl:apply-templates/>
		</files><xsl:value-of select="$nl"/>
	</xsl:template>
	
	<xsl:template match="document">

		<!-- unix ftp parser -->
<!--
      <mon>Apr</mon>
      <day>05</day>
      <yt>2001</yt>
-->
		<xsl:variable name="lines" select="tokenize(.,'\n')"/>
		<xsl:for-each select="$lines">
			<xsl:if test="string-length(.) &gt; 0">
			<xsl:variable name="attrs" select="tokenize(.,'\s+')"/>
			<xsl:variable name="mon" select="$attrs[6]"/>
			<xsl:variable name="day" select="$attrs[7]"/>
			<xsl:variable name="yt"  select="$attrs[8]"/>
			<xsl:variable name="iso-date">
				<xsl:variable name="mo">
					<xsl:call-template name="month-of">
						<xsl:with-param name="mon" select="$mon"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:variable name="year" select="format-date(current-date(),'[Y]')"/>
				<xsl:analyze-string select="$yt" regex="^(\d\d):(\d\d)">
					<xsl:matching-substring>
						<xsl:value-of select="concat($year,'-',$mo,'-',$day,'T',.,':00Z')"/>
					</xsl:matching-substring>
					<xsl:non-matching-substring>
						<xsl:value-of select="concat($year,'-',$mo,'-',$day,'T00:00:00Z')"/>
					</xsl:non-matching-substring>	
				</xsl:analyze-string>
			</xsl:variable>
			<file>
				<perms><xsl:value-of select="$attrs[1]"/></perms>
				<dirs><xsl:value-of select="$attrs[2]"/></dirs>
				<gid><xsl:value-of select="$attrs[3]"/></gid>
				<uid><xsl:value-of select="$attrs[4]"/></uid>
				<size><xsl:value-of select="$attrs[5]"/></size>
				<mon><xsl:value-of select="$attrs[6]"/></mon>
				<day><xsl:value-of select="$attrs[7]"/></day>
				<yt><xsl:value-of select="$yt"/></yt>
				<name><xsl:value-of select="$attrs[9]"/></name>
				<dt><xsl:value-of select="$iso-date"/></dt>
				<!-- <xsl:value-of select="."/> -->
			</file>
			<xsl:value-of select="$nl"/>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="month-of">
		<xsl:param name="mon"/>
		<xsl:choose>
			<xsl:when test="lower-case($mon) = 'jan'"><xsl:value-of select="'01'"/></xsl:when>
			<xsl:when test="lower-case($mon) = 'feb'"><xsl:value-of select="'02'"/></xsl:when>
			<xsl:when test="lower-case($mon) = 'mar'"><xsl:value-of select="'03'"/></xsl:when>
			<xsl:when test="lower-case($mon) = 'apr'"><xsl:value-of select="'04'"/></xsl:when>
			<xsl:when test="lower-case($mon) = 'may'"><xsl:value-of select="'05'"/></xsl:when>
			<xsl:when test="lower-case($mon) = 'jun'"><xsl:value-of select="'06'"/></xsl:when>
			<xsl:when test="lower-case($mon) = 'jul'"><xsl:value-of select="'07'"/></xsl:when>
			<xsl:when test="lower-case($mon) = 'aug'"><xsl:value-of select="'08'"/></xsl:when>
			<xsl:when test="lower-case($mon) = 'sep'"><xsl:value-of select="'09'"/></xsl:when>
			<xsl:when test="lower-case($mon) = 'oct'"><xsl:value-of select="'10'"/></xsl:when>
			<xsl:when test="lower-case($mon) = 'nov'"><xsl:value-of select="'11'"/></xsl:when>
			<xsl:when test="lower-case($mon) = 'dec'"><xsl:value-of select="'12'"/></xsl:when>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="unix-ftp-date">
		<xsl:param name="yt"/>
		<xsl:param name="mon"/>
		<xsl:param name="day"/>

	</xsl:template>

<!--	
	<xsl:template name="parse-ftp">
		<xsl:param name="lines"/>
		<xsl:variable name="data" select="substring-before($lines,'\n')" />
		<xsl:choose>
			<xsl:when test="string-length($data) > 0" >
				<value>
					<xsl:value-of select="$data" />
				</value>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
-->	

</xsl:stylesheet>

