package com.joseanquiles.sfc.comparator;

import java.util.List;
import java.util.Map;

public interface SFCComparator {

	public void setParameters(Map<String, String> params);
	
	public List<String> run(List<String> leftLines, List<String> rightLines);
}
