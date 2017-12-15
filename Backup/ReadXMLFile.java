
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ReadXMLFile {

	public Map<String, String> getDeficiencyMapFromXML(String xmlFileName) {

		final Map<String, String> def_map = new HashMap<String, String>();
		try {

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			DefaultHandler handler = new DefaultHandler() {

				boolean bdefCode = false;
				boolean bdefDesc = false;
				String def_code = "";
				String def_desc = "";

				public void startElement(String uri, String localName, String qName, Attributes attributes)
						throws SAXException {

					if (qName.equalsIgnoreCase("DeficiencyNumber")) {
						bdefCode = true;
					}

					if (qName.equalsIgnoreCase("DeficiencyDescription")) {
						bdefDesc = true;
					}

				}

				public void endElement(String uri, String localName, String qName) throws SAXException {

					if (qName.equalsIgnoreCase("row")) {
						def_map.put(def_code, def_desc);
						def_code = "";
						def_desc = "";
					}

				}

				public void characters(char ch[], int start, int length) throws SAXException {

					if (bdefCode) {
						def_code = new String(ch, start, length);

						bdefCode = false;
					}

					if (bdefDesc) {
						def_desc = new String(ch, start, length);

						bdefDesc = false;
					}

				}

			};

			saxParser.parse(xmlFileName, handler);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return def_map;

	}

}
