<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" indent="yes"/>
  
  <xsl:variable name="otherdoc" select="document('otherdoc.xml')"/>
  
  <xsl:template match="/">
    <result>
      <xsl:value-of select="$otherdoc/otherdoc"/>
    </result>
  </xsl:template>
</xsl:stylesheet>
