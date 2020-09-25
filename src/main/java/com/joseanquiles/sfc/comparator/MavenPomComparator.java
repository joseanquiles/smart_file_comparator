package com.joseanquiles.sfc.comparator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import garcia.herrero.elena.model.PomComparation;
import garcia.herrero.elena.service.PomService;

public class MavenPomComparator implements SFCComparator {

	@Override
	public void setParameters(Map<String, String> params) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<String> run(List<String> leftLines, List<String> rightLines) {
		
		List<String> result = new ArrayList<>();

		try {
		    
			String leftFile = writeLinesToTmpFile("left_", leftLines);
			String rightFile = writeLinesToTmpFile("right_", rightLines);
		    		
			PomService pomService = new PomService();
		    Map<String, String> sortedDependenciesForPom1 = pomService.getSortedDependencies(new FileReader(leftFile));
		    Map<String, String> sortedDependenciesForPom2 = pomService.getSortedDependencies(new FileReader(rightFile));
	        Map<String, PomComparation> comparePoms = pomService.comparePoms(sortedDependenciesForPom1,
	                sortedDependenciesForPom2);
	        String versions = pomService.getComparatiosAsString(comparePoms);
	        
	        String[] lines = versions.split("\n");
	        for (String line : lines) {
	            String[] words = line.split("\t");
	            if (words.length >= 3 && !"DEPENDENCY".equalsIgnoreCase(words[0].trim())) {
	            	if (!words[1].equals(words[2])) {
	            		result.add(words[0].trim() + " : " + words[1].trim() + " != " + words[2].trim());
	            	}
	            }
	        }			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	private static String writeLinesToTmpFile(String prefix, List<String> lines) throws IOException {
	    // create temporal file
	    File file = File.createTempFile(prefix, ".tmp");

	    FileOutputStream fos = new FileOutputStream(file);
	    
	    for (int i = 0; i < lines.size(); i++) {
	    	fos.write(lines.get(i).getBytes());
	    	fos.write("\n".getBytes());
	    }
	    
	    fos.close();
	    
	    return file.toString();
	}

}
