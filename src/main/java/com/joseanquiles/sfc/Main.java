package com.joseanquiles.sfc;

import java.util.List;
import java.util.Map;

import com.joseanquiles.sfc.engine.SFCEngine;
import com.joseanquiles.sfc.engine.SFCResult;
import com.joseanquiles.sfc.util.ArgsUtil;


public class Main {

	private static void printSyntax() {
		System.out.println(Main.class.getName() + " [-h] -c configFile -l left -r right [-o output]");
		System.out.println("    -h : show this help");
		System.out.println("    -c : configuration file (mandatory)");
		System.out.println("    -s : source directory or file (mandatory)");
		System.out.println("    -t : target directory or file (mandatory)");
		System.out.println("    -o : output file (default console)");
	}

	public static void main(String[] args) {
		
		try {
			
			Map<String, String> argsMap = ArgsUtil.parseArgs(args);
			
			if (argsMap.containsKey("h")) {
				printSyntax();
			}

			String configFile = null;
			if (!argsMap.containsKey("c")) {
				System.err.println("-c argument is mandatory");
				printSyntax();
				System.exit(1);
			} else {
				configFile = argsMap.get("c");
			}

			String left = null;
			if (!argsMap.containsKey("l")) {
				System.err.println("-l argument is mandatory");
				printSyntax();
				System.exit(1);
			} else {
				left = argsMap.get("l");
			}

			String right = null;
			if (!argsMap.containsKey("r")) {
				System.err.println("-r argument is mandatory");
				printSyntax();
				System.exit(1);
			} else {
				right = argsMap.get("r");
			}

			String output = null;
			if (argsMap.containsKey("o")) {
				output = argsMap.get("o");
			}
			
			SFCEngine engine = new SFCEngine(configFile, left, right, null);
			List<SFCResult> result = engine.run();
			for (int i = 0; i < result.size(); i++) {
				System.out.println(result.get(i));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
