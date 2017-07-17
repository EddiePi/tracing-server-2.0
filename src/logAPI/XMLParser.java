package logAPI;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eddie on 2017/7/17.
 */
public class XMLParser {
    private static Document doc;

    public static List<MessageMark> parse(String path) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        List<MessageMark> messageMarkList = new ArrayList<>();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();

            doc = db.parse(path);

            Element root = doc.getDocumentElement();
            if (!root.getNodeName().equals("rules")) {
                System.out.printf("unrecognized root element: %s\n", root.getNodeName());
            }

            NodeList nl = root.getElementsByTagName("rule");

            if (nl.getLength() == 0) {
                System.out.printf("no rule is specified in file: %s\n", path);
            }

            for (int i = 0; i < nl.getLength(); i++) {
                Element rule = (Element) nl.item(i);
                MessageMark mm = new MessageMark();
                mm.isFinishMark = getBooleanValue(rule, "isFinish");
                mm.name = getTextValue(rule, "name");
                mm.regex = getTextValue(rule, "regex");
                if(checkSanity(mm)) {
                    messageMarkList.add(mm);
                }
            }

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return messageMarkList;
    }

    private static String getTextValue(Element elem, String tagName){
        String textValue;
        NodeList nl = elem.getElementsByTagName(tagName);
        if(nl.getLength() == 0) {
            System.out.printf("no tag called: %s\n", tagName);
            return null;
        }
        Element el = (Element)nl.item(0);
        textValue = el.getFirstChild().getNodeValue();
        if(textValue.matches("\\s*")) {
            return null;
        }
        return textValue;
    }

    private static boolean getBooleanValue(Element elem, String tagName){
        return Boolean.parseBoolean(getTextValue(elem,tagName));
    }

    private static boolean checkSanity(MessageMark mm) {
        if(mm.regex != null && mm.name != null) {
            return true;
        }
        return false;
    }
}
