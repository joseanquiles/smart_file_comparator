package com.joseanquiles.sfc.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class IgnoreSingleLineCommentsFilter implements SFCFilter {

	//private static final String REGEX = "//.*?\\n\",\"\\n";
	private static final String REGEX = "//.*";
	
	public void setParameters(Map<String, String> params) {
		// nothing to do
	}

	public List<String> run(List<String> lines) {
		List<String> processed = new ArrayList<String>();
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i).replaceAll(REGEX, "\n");
			processed.add(line);
		}
		return processed;
	}
	
}
