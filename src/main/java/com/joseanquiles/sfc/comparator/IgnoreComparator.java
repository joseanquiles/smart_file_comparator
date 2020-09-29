package com.joseanquiles.sfc.comparator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IgnoreComparator implements SFCComparator {

	@Override
	public void setParameters(Map<String, String> params) {
		// nothing to do
	}

	@Override
	public List<String> run(List<String> leftLines, List<String> rightLines) {
		// No differences
		return new ArrayList<>();
	}

}
