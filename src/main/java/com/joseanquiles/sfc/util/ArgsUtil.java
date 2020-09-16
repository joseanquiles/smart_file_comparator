package com.joseanquiles.sfc.util;

import java.util.HashMap;
import java.util.Map;

public class ArgsUtil {

	public static Map<String, String> parseArgs(String[] args) {
		Map<String, String> parsed = new HashMap<String, String>();
		for (int i = 0; i < args.length; ) {
			String argName = args[i];
			if (argName.startsWith("-")) {
				argName = argName.substring(1);
			}
			parsed.put(argName, "");
			i++;
			if (i >= args.length) {
				break;
			}
			String argValue = args[i];
			if (argValue.startsWith("-")) {
				continue;
			}
			i++;
			parsed.put(argName, argValue);
		}
		return parsed;
	}
	
	public static void main(String[] args) {
		String[] theArgs = {
				"-1", "data/original.txt",
				"-2", "data/revised.txt",
				"-c", "config.yaml",
				"-ignore",
				"-plugin", "ignoreCase"
		};
		System.out.println(parseArgs(theArgs));
	}

}
