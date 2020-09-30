package com.joseanquiles.sfc.comparator;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WordComparator implements SFCComparator {

	@Override
	public void setParameters(Map<String, String> params) {
		// nothing to do
	}

	@Override
	public List<String> run(List<String> leftLines, List<String> rightLines) {
		// No differences
		return new ArrayList<>();
	}
	
	public String extractText(InputStream in) throws Exception {
		/*
	    XWPFDocument doc = new XWPFDocument(in);
	    XWPFWordExtractor ex = new XWPFWordExtractor(doc);
	    String text = ex.getText();
	    return text;
	    */
		return "";
	}
}
