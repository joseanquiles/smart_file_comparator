package com.joseanquiles.sfc.comparator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;

public class PlainComparator implements SFCComparator {

	@Override
	public void setParameters(Map<String, String> params) {
		// nothing to do
	}

	@Override
	public List<String> run(List<String> leftLines, List<String> rightLines) {
		List<String> diffs = new ArrayList<>();
		try {
			Patch<String> patch = DiffUtils.diff(leftLines, rightLines);
			List<AbstractDelta<String>> deltas = patch.getDeltas();
			for (int i = 0; i < deltas.size(); i++) {
				
				diffs.add(deltas.get(i).toString());
				
				/*
				System.out.println(deltas.get(i));
				AbstractDelta<String> delta = deltas.get(i);
				AbstractDelta<String> newDelta = null;
				int sourcePosition = delta.getSource().getPosition() < leftLines.size() ? delta.getSource().getPosition() : leftLines.size()-1;
				int targetPosition = delta.getTarget().getPosition() < rightLines.size() ? delta.getTarget().getPosition() : rightLines.size()-1;
				Chunk<String> sourceChunk = new Chunk<String>(leftLines.get(sourcePosition).getLinenumber(), delta.getSource().getLines());
				Chunk<String> targetChunk = new Chunk<String>(rightLines.get(targetPosition).getLinenumber(), delta.getTarget().getLines());
				
				switch (delta.getType()) {
				case DELETE:
					newDelta = new DeleteDelta<String>(sourceChunk, targetChunk);
					break;
				case CHANGE:
					newDelta = new ChangeDelta<String>(sourceChunk, targetChunk);
					break;
				case INSERT:
					newDelta = new InsertDelta<String>(sourceChunk, targetChunk);
					break;
				default:
					break;
				}
				*/
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return diffs;
		/*
		if (leftLines.size() != rightLines.size()) {
			diffs.add("Los ficheros no tienen el mismo tamaño");
			return diffs;
		}
		for (int i = 0; i < leftLines.size(); i++) {
			String line1 = leftLines.get(i);
			String line2 = rightLines.get(i);
			if (!line1.equals(line2)) {
				diffs.add("[" + line1 + "] != [" + line2 + "]");
			}
		}
		return diffs;
		*/
	}

}
