package com.joseanquiles.sfc.configuration;

import java.util.HashMap;
import java.util.Map;

/* File ocnfiguration bean */ 
class FilterConfiguration {

	String name = null;
	
	boolean enabled = true;
	
	Map<String, String> parameters = new HashMap<>();
	
}
