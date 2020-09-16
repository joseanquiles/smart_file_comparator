package com.joseanquiles.sfc.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IgnoreRegularExpressionFilter implements SFCFilter {

	private String regexp;
	
	/**
	 * Params: regexp
	 */
	@Override
	public void setParameters(Map<String, String> params) {
		this.regexp = params.get("regexp");
	}

	@Override
	public List<String> run(List<String> lines) {
		List<String> processed = new ArrayList<String>();
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (!line.matches(this.regexp)) {
				processed.add(line);				
			}
		}
		return processed;
	}
	
	public static void main(String[] args) {
		String line = "@Generated(value = \"EclipseLink-2.5.2.v20140319-rNA\", date = \"2020-04-22T15:33:18\")"; 
		System.out.println(line.matches("^@Generated\\(.*"));
	}

}
