package com.joseanquiles.sfc.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IgnoreMultilineCommentsFilter implements SFCFilter {
	
	private static final String REGEX = "(?s)/\\*.*?\\*/";
	
	public void setParameters(Map<String, String> params) {
	}

	public List<String> run(List<String> lines) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < lines.size(); i++) {
			sb.append(lines.get(i)).append("\n");
		}
		String text = sb.toString();
		// remove comments
		text = text.replaceAll(REGEX, "");
		// convert to lines again
		String[] splitted = text.split("\\r?\\n");
		List<String> processed = new ArrayList<String>();
		for (int i = 0; i < splitted.length; i++) {
			processed.add(splitted[i]);
		}
		return processed;
	}
	
}
