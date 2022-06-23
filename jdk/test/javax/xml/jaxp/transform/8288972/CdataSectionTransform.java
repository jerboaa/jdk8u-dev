/*
 * Copyright (c) 2018, 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Assert;
import org.junit.Test;
import javax.xml.transform.OutputKeys;

/*
 * @test
 * @run junit/othervm CdataSectionTransform
 * @summary Verifies that a OutputKeys.CDATA_SECTION_ELEMENTS is adhered to correctly
 * @bug 8288972
 */
public class CdataSectionTransform {
    final String xsl =
        "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n" +
        "    <xsl:output omit-xml-declaration=\"yes\" indent=\"no\" />\n" +
        "\n" +
        "    <xsl:template match=\"/\">\n" +
	"    <description>\n" +
        "        <xsl:value-of select=\"/source\" />\n" +
	"    </description>\n" +
        "    </xsl:template>\n" +
        "</xsl:stylesheet>\n";

    @Test
    public final void testTransformWithCdataKey() throws Exception {
        String[] xmls = prepareXML();
        Transformer t = createTransformerFromInputstream(
                new ByteArrayInputStream(xsl.getBytes(StandardCharsets.UTF_8)));
        t.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
	t.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, "description");
        StringWriter sw = new StringWriter();
        t.transform(new StreamSource(new StringReader(xmls[0])), new StreamResult(sw));
	String actual = sw.toString().replaceAll(System.lineSeparator(), "\n");
        Assert.assertEquals(xmls[1], actual);
    }

    private String[] prepareXML() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><source>&lt;es:thumbnail&gt;&lt;/es:thumbnail&gt;&lt;es:details&gt;123&lt;/es:details&gt;</source>";
        String expected = "<description><![CDATA[<es:thumbnail></es:thumbnail><es:details>123</es:details>]]></description>";
        return new String[]{xml, expected};
    }

    private Transformer createTransformerFromInputstream(InputStream xslStream)
            throws TransformerException {
        return TransformerFactory.newInstance().newTransformer(new StreamSource(xslStream));
    }
}
