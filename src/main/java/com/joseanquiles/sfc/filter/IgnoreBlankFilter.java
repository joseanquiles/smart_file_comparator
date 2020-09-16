package com.joseanquiles.sfc.filter;

/**
 * Filtra espacios al principio y/o al final de cada línea
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IgnoreBlankFilter implements SFCFilter {

	private boolean leftBlanks = true;
	private boolean rightBlanks = true;
	
	@Override
	public void setParameters(Map<String, String> params) {
		String lb = params.get("leftBlanks");
		if (lb != null) {
			if ("false".equalsIgnoreCase(lb)) {
				leftBlanks = false;
			} else if ("no".equalsIgnoreCase(lb)) {
				leftBlanks = false;
			} else if ("0".equalsIgnoreCase(lb)) {
				leftBlanks = false;
			}
		}
		String rb = params.get("rightBlanks");
		if (rb != null) {
			if ("false".equalsIgnoreCase(rb)) {
				rightBlanks = false;
			} else if ("no".equalsIgnoreCase(rb)) {
				rightBlanks = false;
			} else if ("0".equalsIgnoreCase(rb)) {
				rightBlanks = false;
			}
		}
	}

	@Override
	public List<String> run(List<String> lines) {
		
		List<String> processed = new ArrayList<String>();
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (leftBlanks && rightBlanks) {
				line = line.trim();
			} else if (!leftBlanks) {
				line = rtrim(line);
			} else if (!rightBlanks) {
				line = ltrim(line);
			}
			if (line.length() > 0) {
				processed.add(line);
			}
		}
		return processed;
	}
	
	private static String ltrim(String s) {
	    int i = 0;
	    while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
	        i++;
	    }
	    return s.substring(i);
	}

	private static String rtrim(String s) {
	    int i = s.length()-1;
	    while (i >= 0 && Character.isWhitespace(s.charAt(i))) {
	        i--;
	    }
	    return s.substring(0,i+1);
	}

}
