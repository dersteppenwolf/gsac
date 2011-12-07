<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- An example that parses the GSAC XML to a simpler XML of just the file information. -->

	<xsl:output method="xml" indent="yes"/>

	<xsl:variable name="nl" select="codepoints-to-string((13,10))"/>
	<xsl:variable name="cur-year" select="substring($dstamp,1,4)"/>
	<xsl:variable name="cur-month" select="substring($dstamp,5,2)"/>
	<xsl:variable name="cur-day" select="substring($dstamp,7,2)"/>

	<xsl:template match="/">
		<files>
			<xsl:apply-templates select="//object[@class='org.gsac.gsl.model.FileInfo']"/>
		</files>
	</xsl:template>
	
	<xsl:template match="object[@class='org.gsac.gsl.model.FileInfo']">
		<file>
			<Url><xsl:apply-templates select="property[@name='Url']/string"/></Url>
			<Md5><xsl:apply-templates select="property[@name='Md5']/string"/></Md5>
			<FileSize><xsl:apply-templates select="property[@name='FileSize']/long"/></FileSize>
			<ShortName><xsl:apply-templates select="../../property[@name='ShortName']/string"/></ShortName>
			<PublishDate><xsl:apply-templates select="../../property[@name='PublishDate']"/></PublishDate>
		</file>
	</xsl:template>

	<!-- Remove extra space from CDATA sections -->
	<xsl:template match="string">
		<xsl:variable name="this" select="."/>
		<xsl:value-of select="normalize-space($this)"/>
	</xsl:template>
	                                     
	<xsl:template match="property[@name='PublishDate']">
		<xsl:value-of select="object/constructor/long"/>
	</xsl:template>

<!--
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
-->

</xsl:stylesheet>
