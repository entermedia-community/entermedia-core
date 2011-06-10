<?xml version="1.0"?>

<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="http://www.w3.org/1999/xhtml">
  <xsl:output method="html" indent="yes"/>

  <xsl:template match="/user">
<html>
<head>
	<title>WSPublisher Change Notifications</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
</head>

<body>

<table borderColor="#FFFFFF" cellSpacing="0" cellPadding="0" width="80%" bgColor="#ffffff" border="0">
  <tbody> 
<tr valign="top">
    <td colspan="2">
<img border="0" src="http://www.wspublisher.com/dev/wsplogo-web.jpg" hspace="0" alt="WSPublisher" />

<xsl:if test="properties/property[@name='firstName']">
    <p>Dear <xsl:value-of select="properties/property[@name='firstName']"/>,</p>
</xsl:if>

<p>Here are the current changes you might be interested in</p>
<hr/>
</td>		
</tr>
</tbody>
</table>
<br/>
<xsl:apply-templates select="ChangeLog"/>

        <br />
<p align="center"><font face="Arial" color="#666666" size="1">Thank you</font></p>
</body>

</html>
  </xsl:template>
  
  <xsl:template match="Change"> 
  <table width="60%" border="1">
  	<tr>    		
  	<td><b>Change type</b></td><td><xsl:value-of select="@type" /></td>
  	</tr>
  	<tr>
  	<td><b>Date</b></td><td><xsl:value-of select="Date" /></td>
  	</tr>
  	<tr>
  	<td><b>Author</b></td><td><xsl:value-of select="Author" /></td>	
  	</tr>
  	<tr>
  	<td><b>Link</b></td><td><a><xsl:value-of select="Url" /></a></td>		
	</tr>
	</table>
  </xsl:template>
  
</xsl:stylesheet>

