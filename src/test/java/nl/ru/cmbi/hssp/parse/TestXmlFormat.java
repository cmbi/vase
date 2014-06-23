package nl.ru.cmbi.hssp.parse;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.ru.cmbi.hssp.data.VASEDataObject;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.IOUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * Simple test using the WicketTester
 */
public class TestXmlFormat
{
	Logger log = LoggerFactory.getLogger(TestXmlFormat.class);
	
	@Test
	public void testParse() throws Exception
	{
		VASEDataObject data = VASEXMLParser.parse(this.getClass().getResourceAsStream("/1crn.xml"));

		assertTrue(data.getTable().getNumberOfRows()>0);
		assertTrue(data.getPlots().size()>0);
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		VASEXMLParser.write(data, outputStream);
		
		log.info(new String( outputStream.toByteArray(),StandardCharsets.UTF_8 ));
	}
}
