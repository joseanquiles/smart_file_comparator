package com.joseanquiles.sfc.comparator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlainComparator implements SFCComparator {

	@Override
	public void setParameters(Map<String, String> params) {
		// nothing to do
	}

	@Override
	public List<String> run(List<String> leftLines, List<String> rightLines) {
		List<String> diffs = new ArrayList<>();
		if (leftLines.size() != rightLines.size()) {
			diffs.add("Los ficheros no tienen el mismo tamaño");
			return diffs;
		}
		for (int i = 0; i < leftLines.size(); i++) {
			String line1 = leftLines.get(i);
			String line2 = rightLines.get(i);
			if (!line1.equals(line2)) {
				diffs.add("    " + line1 + "\n    " + line2);
			}
		}
		return diffs;
	}

}
