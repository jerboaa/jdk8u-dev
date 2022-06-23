import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import test.Book;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/*
 * @test
 * @build test.Book test.package-info
 * @run junit/othervm CdataSectionTransform2
 * @summary Verifies that a OutputKeys.CDATA_SECTION_ELEMENTS is adhered to correctly
 * @bug 8288972
 */
public class CdataSectionTransform2 {

    @Test
    public void testMarshal() throws IOException, JAXBException, TransformerException, ParserConfigurationException {
	Book book = new Book();
	book.setName("Foo");
        book.setDescription("<es:thumbnail></es:thumbnail><es:details>123</es:details>");

        String str = toXmlString(book);
        assertNotNull(str);
        String expect = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
		"<book xmlns=\"http://www.example.com/foo-book\">" +
		"<name>Foo</name>" +
		"<description><![CDATA[<es:thumbnail></es:thumbnail><es:details>123</es:details>]]></description>" +
		"</book>";
        assertEquals(expect, str);
    }

    private static String toXmlString(Book book) throws TransformerException, JAXBException, ParserConfigurationException, IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
	    JAXBContext context = JAXBContext.newInstance(Book.class);
            Marshaller marshaller = context.createMarshaller();
	    marshaller.marshal(book, System.out);

            // Marshal xml into a empty dom
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            org.w3c.dom.Document document = docBuilderFactory.newDocumentBuilder().newDocument();

            marshaller.marshal(book, document);

            // transform the output, set "description" tag as a CDATA field
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, "description");

            transformer.transform(new DOMSource(document), new StreamResult(outputStream));
            return outputStream.toString();
        } catch (IOException | JAXBException | ParserConfigurationException | TransformerException e) {
            throw e;
        }
    }
}
