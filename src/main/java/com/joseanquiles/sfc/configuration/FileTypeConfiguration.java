package com.joseanquiles.sfc.configuration;

import java.util.ArrayList;
import java.util.List;

class FileTypeConfiguration {

	String name = "";
	
	List<String> patterns = new ArrayList<>();
	
	List<FilterConfiguration> filters = new ArrayList<>();
	
	List<ComparatorConfiguration> comparators = new ArrayList<>();

}