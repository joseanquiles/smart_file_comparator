package com.joseanquiles.sfc.engine;

public enum SFCStatus {
	EQUAL,      // they are equal in both sides
	DIFFERENT,  // they are different
	LEFT,       // it exists in left but not in right
	RIGHT,      // it exists in right but not in left
}
