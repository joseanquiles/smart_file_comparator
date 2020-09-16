package com.joseanquiles.sfc.filter;

import java.util.List;
import java.util.Map;

public interface SFCFilter {

	public void setParameters(Map<String, String> params);
	
	public List<String> run(List<String> lines);
	
}
