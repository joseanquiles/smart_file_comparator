package com.joseanquiles.sfc.engine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SFCResult {

	public static final boolean SHOW_DIFFS = true;
	
	public File leftFile;
	public File rightFile;
	public SFCStatus status = SFCStatus.UNKNOWN;
	List<String> differences = new ArrayList<>();
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (this.status == SFCStatus.LEFT) {
			sb.append(">>> ");
			sb.append(this.leftFile);
		} else if (this.status == SFCStatus.RIGHT) {
			sb.append("<<< ");
			sb.append(this.rightFile);			
		} else if (this.status == SFCStatus.DIFFERENT) {
			sb.append("### ");
			sb.append(this.leftFile);
			sb.append(" != ");
			sb.append(this.rightFile);
			if (SHOW_DIFFS) {
				for (int i = 0; i < this.differences.size(); i++) {
					sb.append("\n    ");
					sb.append(this.differences.get(i));
				}
			}
		}
		return sb.toString();
	}
	
}
