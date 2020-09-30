package com.joseanquiles.sfc.comparator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

		String leftFile = null;
		String rightFile = null;
				
		try {
		    
			leftFile = FileUtil.writeLinesToTmpFile("left_", leftLines);
			rightFile = FileUtil.writeLinesToTmpFile("right_", rightLines);

			XmlNode leftDoc = parseXml(leftFile);
			XmlNode rightDoc = parseXml(rightFile);
			
			// compare
			
			Status compareResult = compare(leftDoc, rightDoc, result);
			
			if (compareResult == Status.EQUAL) {
				// Find nodes not processed in right tree
				XmlNode noProcessed = findNoProcessedNode(rightDoc);
				if (noProcessed != null) {
					result.add(noProcessed.toString());
					compareResult = Status.RIGHT;
				}
			}
			
			if (compareResult == Status.LEFT) {
				result.add(0, "LEFT");
			} else if (compareResult == Status.RIGHT) {
				result.add(0, "RIGHT");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (leftFile != null) {
				new File(leftFile).delete();
			}
			if (rightFile != null) {
				new File(rightFile).delete();
			}
		}
		
		return result;
	}
	
	private static void resetTree(XmlNode node) {
		node.processed = false;
		for (int i = 0; i < node.children.size(); i++) {
			XmlNode child = node.children.get(i);
			child.processed = false;
			resetTree(child);
		}
	}
	
	private static Status compare(XmlNode left, XmlNode right, List<String> diffs) {
		if (!left.equals(right)) {
			if (left.parent == null) {
				diffs.add(left.toString());
			}
			return Status.LEFT;
		} else {
			left.processed = true;
			right.processed = true;
		}
		
		// check left tree
		
		for (int i = 0; i < left.children.size(); i++) {
			XmlNode child1 = left.children.get(i);
			if (child1.processed) {
				continue;
			}
			XmlNode foundChild = null;
			for (int j = 0; j < right.children.size(); j++) {
				XmlNode child2 = right.children.get(j);
				if (child2.processed) {
					continue;
				}
				Status c = compare(child1, child2, diffs);
				if (c == Status.EQUAL) {
					foundChild = child2;
					child1.processed = true;
					child2.processed = true;
					break;					
				}
			}
			if (foundChild == null) {
				diffs.add(child1.toString());
				return Status.LEFT;
			}
		}
		
		return Status.EQUAL;
	
	}
	
	private static XmlNode findNoProcessedNode(XmlNode from) {
		if (!from.processed) {
			return from;
		} else {
			for (int i = 0; i < from.children.size(); i++) {
				if (!from.children.get(i).processed) {
					return from.children.get(i);
				} else {
					XmlNode n = findNoProcessedNode(from.children.get(i));
					if (n != null) {
						return n;
					}
				}
			}
		}
		return null;
	}
	
    private static XmlNode parseXml(String filename) throws Exception {
    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File(filename));
        document.getDocumentElement().normalize();
        Element rootElement = document.getDocumentElement();
        XmlNode rootNode = parseElement(rootElement, null);
        return rootNode;
    }
    
    private static XmlNode parseElement(Element element, XmlNode parent) {
    	XmlNode node = new XmlNode();
    	
    	// parent
    	node.parent = parent;
    	
    	// name
    	node.name = element.getNodeName();
    	
    	// namespace
    	node.namespace = element.getNamespaceURI();
    	
    	// attributes
        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node attr = attrs.item(i);
            String attrName = attr.getNodeName();
            String attrValue = attr.getNodeValue();
            node.attributes.put(attrName, attrValue);
        }
        
        // children
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node childNode = children.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) childNode;
                XmlNode child = parseElement(childElement, node);
                node.children.add(child);
            } else if (childNode.getNodeType() == Node.TEXT_NODE) {
            	String textContent = childNode.getNodeValue();
                if (textContent != null && textContent.trim().length() > 0) {
                    node.text += textContent.trim();
                }
            }
        }
        
        if (node.name == null) {
        	node.name = "";
        }
        if (node.namespace == null) {
        	node.namespace = "";
        }
        if (node.text == null) {
        	node.text = "";
        }
        
        return node;
        
    }

	public static void main(String[] args) throws Exception {
		String filename = "./src/test/resources/test1/left/xsd/types.xsd";
		XmlNode node = parseXml(filename);
		System.out.println(node.toStringRecursive(0));
	}

}

enum Status {
	EQUAL,
	LEFT,
	RIGHT,
}

class XmlNode {
	String name = "";
	String namespace = "";
	Map<String, String> attributes = new HashMap<>();
	String text = "";
	List<XmlNode> children = new ArrayList<>();
	XmlNode parent = null;
	boolean processed = false;
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof XmlNode) {
			XmlNode otherNode = (XmlNode)other;
			if (!name.equals(otherNode.name)) {
				return false;
			}
			if (!namespace.equals(otherNode.namespace)) {
				return false;
			}
			if (!text.equals(otherNode.text)) {
				return false;
			}
			Iterator<String> it = attributes.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				String value = attributes.get(key);
				String value2 = otherNode.attributes.get(key);
				if (value2 == null || !value2.equals(value)) {
					return false;
				}
			}
			it = otherNode.attributes.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				String value = otherNode.attributes.get(key);
				String value2 = attributes.get(key);
				if (value2 == null || !value2.equals(value)) {
					return false;
				}
			}			
			return true;
		} else {
			return false;
		}
	}
		
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (namespace != null && namespace.length() > 0) {
			sb.append(namespace).append(":");
		}
		sb.append(name);
		if (attributes.size() > 0) {
			sb.append(attributes.toString());			
		}
		if (text != null && text.length() > 0) {
			sb.append("[").append(text).append("]");
		}
		return sb.toString();
	}
	
	public String toStringRecursive(int tabs) {
		StringBuilder sb = new StringBuilder();
		sb.append(toString()).append('\n');
		tabs += 4;
		for (int i = 0; i < children.size(); i++) {
			for (int t = 0; t < tabs; t++) {
				sb.append(' ');
			}
			sb.append(children.get(i).toStringRecursive(tabs));
		}
		return sb.toString();
	}
	
	boolean isLeaf() {
		return children.size() == 0;
	}

}



