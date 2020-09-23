package com.joseanquiles.sfc.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class IgnoreSingleLineCommentsFilter implements SFCFilter {

	private String startComment = "//";
	private String regexp  = "//.*";
	
	/*
	 * params: startComment, default = "//";
	 */
	public void setParameters(Map<String, String> params) {
		if (params.containsKey("startComment")) {
			this.startComment = params.get("startComment");
		}
		this.regexp = this.startComment + ".*";
	}

	public List<String> run(List<String> lines) {
		List<String> processed = new ArrayList<String>();
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i).replaceAll(this.regexp, "\n");
			processed.add(line);
		}
		return processed;
	}
	
}
