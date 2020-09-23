package com.joseanquiles.sfc.engine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SFCResult {

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
		}
		return sb.toString();
	}
	
}
