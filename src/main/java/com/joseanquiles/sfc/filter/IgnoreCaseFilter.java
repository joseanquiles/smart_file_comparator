package com.joseanquiles.sfc.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IgnoreCaseFilter implements SFCFilter {

	@Override
	public void setParameters(Map<String, String> params) {
		// nothing to do
	}

	@Override
	public List<String> run(List<String> lines) {
		List<String> processed = new ArrayList<String>();
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			processed.add(line.toLowerCase());
		}
		return processed;
	}

}
