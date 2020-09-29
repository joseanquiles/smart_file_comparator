package com.joseanquiles.sfc.comparator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.joseanquiles.sfc.util.FileUtil;

public class XmlComparator implements SFCComparator {

	@Override
	public void setParameters(Map<String, String> params) {
		// Nothing to do
	}

	@Override
	public List<String> run(List<String> leftLines, List<String> rightLines) {
		
		List<String> result = new ArrayList<>();

		try {
		    
			String leftFile = FileUtil.writeLinesToTmpFile("left_", leftLines);
			String rightFile = FileUtil.writeLinesToTmpFile("right_", rightLines);

	        List<String> leftDoc = parseXmlFile(leftFile);
	        List<String> rightDoc = parseXmlFile(rightFile);
	        
	        // find out lines in left doc not included in right doc
	        for (int i = 0; i < leftDoc.size(); i++) {
	            String leftLine = leftDoc.get(i);
	            boolean found = false;
	            for (int j = 0; j < rightDoc.size(); j++) {
	                String rightLine = rightDoc.get(j);
	                if (leftLine.equals(rightLine)) {
	                    found = true;
	                    break;
	                }
	            }
	            if (!found) {
	            	result.add("Line [" + leftLine + "] not found in right file");
	            }
	        }

	        // find out lines in right doc not included in left doc
	        for (int i = 0; i < rightDoc.size(); i++) {
	            String rightLine = rightDoc.get(i);
	            boolean found = false;
	            for (int j = 0; j < leftDoc.size(); j++) {
	                String leftLine = leftDoc.get(j);
	                if (rightLine.equals(leftLine)) {
	                    found = true;
	                    break;
	                }
	            }
	            if (!found) {
	            	result.add("Line [" + rightLine + "] not found in left file");
	            }
	        }

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
    private List<String> parseXmlFile(String filename) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File(filename));
        document.getDocumentElement().normalize();

        List<String> elements = new ArrayList<String>();

        Element root = document.getDocumentElement();

        parseElement(elements, root, "");

        return elements;
    }

    private void parseElement(List<String> result, Element element, String currentStr) {
        // node name
    	if (currentStr != null && currentStr.length() > 0) {
            currentStr = currentStr + "." + element.getNodeName();    		
    	} else {
    		currentStr = element.getNodeName();
    	}
    	
    	// attributes
        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node attr = attrs.item(i);
            String attrName = attr.getNodeName();
            String attrValue = attr.getNodeValue();
            currentStr = currentStr + "[" + attrName + "=" + attrValue + "]";
        }
        System.out.println(currentStr);
        
    }
    
    private void parseElement2(List<String> result, Element element, String currentStr) {
        // node name
        currentStr = currentStr + "." + element.getNodeName();
        // attributes
        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node attr = attrs.item(i);
            String attrName = attr.getNodeName();
            String attrValue = attr.getNodeValue();
            result.add(currentStr + "[" + attrName + "]=" + attrValue);
        }
        // children elements
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node childNode = children.item(i);
            if (childNode.getNodeType() == Node.TEXT_NODE) {
                String textContent = childNode.getNodeValue();
                if (textContent != null && textContent.trim().length() > 0) {
                    textContent = textContent.trim();
                    result.add(currentStr + "=" + textContent);
                }
            }
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) childNode;
                parseElement(result, childElement, currentStr);
            }
        }

    }


}
