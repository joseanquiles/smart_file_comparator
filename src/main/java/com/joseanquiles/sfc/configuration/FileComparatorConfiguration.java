package com.joseanquiles.sfc.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.joseanquiles.sfc.comparator.SFCComparator;
import com.joseanquiles.sfc.filter.SFCFilter;

public class FileComparatorConfiguration {

	String name = "";
	String description = "";
	String outputFile = null;
	List<String> ignoreFiles = new ArrayList<>();
	List<String> ignoreDirs = new ArrayList<>();
	List<FileTypeConfiguration> fileTypes = new ArrayList<>();
	
	public String getName() {
		return this.name;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public String getOutputFile() {
		return this.outputFile;
	}
	
	public List<String> getIgnoreFiles() {
		return this.ignoreFiles;
	}

	public List<String> getIgnoreDirs() {
		return this.ignoreDirs;
	}

	public List<SFCFilter> getFiltersForFile(File file) throws Exception {
		FileTypeConfiguration ftc = matchFileType(file);
		List<SFCFilter> result = new ArrayList<>();
		for (int i = 0; i < ftc.filters.size(); i++) {
			FilterConfiguration pc = ftc.filters.get(i);
			
			// plugin object
			Class<?> clazz = Class.forName(pc.name);
			Constructor<?> ctor = clazz.getConstructor();
			SFCFilter plugin = (SFCFilter)ctor.newInstance();
			
			// parameters
			plugin.setParameters(pc.parameters);
			
			result.add(plugin);
		}
		return result;
	}

	public List<SFCComparator> getComparatorsForFile(File file) throws Exception {
		FileTypeConfiguration ftc = matchFileType(file);
		List<SFCComparator> result = new ArrayList<>();
		for (int i = 0; i < ftc.comparators.size(); i++) {
			ComparatorConfiguration pc = ftc.comparators.get(i);
			
			// plugin object
			Class<?> clazz = Class.forName(pc.name);
			Constructor<?> ctor = clazz.getConstructor();
			SFCComparator plugin = (SFCComparator)ctor.newInstance();
			
			// parameters
			plugin.setParameters(pc.parameters);
			
			result.add(plugin);
		}
		return result;
	}

	private FileTypeConfiguration matchFileType(File file) {
		String filename = file.getName();
		for (int i = 0; filename.length() > 0 && i < this.fileTypes.size(); i++) {
			FileTypeConfiguration ftc = this.fileTypes.get(i);
			for (int j = 0; j < ftc.patterns.size(); j++) {
				if (filename.matches(ftc.patterns.get(j))) {
					return ftc;
				}
			}
		}
		// No matching found, return default configuration
		for (int i = 0; i < this.fileTypes.size(); i++) {
			FileTypeConfiguration ftc = this.fileTypes.get(i);
			for (int j = 0; j < ftc.patterns.size(); j++) {
				if ("default-conf".equalsIgnoreCase(ftc.patterns.get(j))) {
					return ftc;
				}
			}
		}
		FileTypeConfiguration ftc = new FileTypeConfiguration();
		ftc.patterns.add("default-conf");
		return ftc;
		
	}
	
	public FileComparatorConfiguration(String configFile) throws Exception {
		File file = new File(configFile);
		if (!file.exists()) {
			throw new Exception(configFile + " : does not exist");
		}
		if (!file.isFile()) {
			throw new Exception(configFile + " : is not a file");
		}
		if (!file.canRead()) {
			throw new Exception(configFile + " : cannot be read");
		}
		
		FileInputStream fis = new FileInputStream(configFile);
		
		Yaml yaml = new Yaml();
		Map<String, Object> yamlMap = null;
		try {
			yamlMap = (Map<String, Object>)yaml.load(fis);			
		} catch (Exception e) {
			throw new Exception("Error parsing configuration file: " + configFile + " : " + e.getMessage(), e);
		} finally {
			fis.close();
		}
		
		this.name = (String)yamlMap.get("name");
		this.description = (String)yamlMap.get("description");
		this.outputFile = (String)yamlMap.get("output-file");

		List<Object> ignoreList = (List<Object>)yamlMap.get("ignore-files");
		if (ignoreList != null) {
			for (int i = 0; i < ignoreList.size(); i++) {
				this.ignoreFiles.add((String)ignoreList.get(i));
			}
		}

		List<Object> ignoreDirsList = (List<Object>)yamlMap.get("ignore-dirs");
		if (ignoreDirsList != null) {
			for (int i = 0; i < ignoreDirsList.size(); i++) {
				this.ignoreDirs.add((String)ignoreDirsList.get(i));
			}
		}

		List<Object> filetypesList = (List<Object>)yamlMap.get("file-types");
		if (filetypesList != null) {
			for (int i = 0; i < filetypesList.size(); i++) {
				FileTypeConfiguration ftc = new FileTypeConfiguration();
				Map<String, Object> filetypeMap = (Map<String, Object>)filetypesList.get(i);
				List<Object> patternsList = (List<Object>)filetypeMap.get("patterns");
				if (patternsList != null) {
					for (int j = 0; j < patternsList.size(); j++) {
						ftc.patterns.add((String)patternsList.get(j));
					}
				}
				List<Object> filtersList = (List<Object>)filetypeMap.get("filters");
				if (filtersList != null) {
					for (int j = 0; filtersList != null && j < filtersList.size(); j++) {
						FilterConfiguration pc = new FilterConfiguration();
						Map<String, Object> pluginMap = (Map<String, Object>)filtersList.get(j);
						pc.name = (String)pluginMap.get("name");
						// built-in plugins 
						if (!pc.name.contains(".")) {
							pc.name = "com.joseanquiles.sfc.filter." + pc.name;
						}
						pc.enabled = pluginMap.get("enabled") != null ? (Boolean)pluginMap.get("enabled") : true;
						List<Object> parametersList = (List<Object>)pluginMap.get("parameters");
						if (parametersList != null) {
							for (int k = 0; parametersList != null && k < parametersList.size(); k++) {
								Map<String, Object> parameterMap = (Map<String, Object>)parametersList.get(k);
								pc.parameters.put((String)parameterMap.get("name"), (String)parameterMap.get("value"));
							}	
						}
						
						ftc.filters.add(pc);
					}
				}
				List<Object> comparatorsList = (List<Object>)filetypeMap.get("comparators");
				if (comparatorsList != null) {
					for (int j = 0; comparatorsList != null && j < comparatorsList.size(); j++) {
						ComparatorConfiguration pc = new ComparatorConfiguration();
						Map<String, Object> pluginMap = (Map<String, Object>)filtersList.get(j);
						pc.name = (String)pluginMap.get("name");
						// built-in plugins 
						if (!pc.name.contains(".")) {
							pc.name = "com.joseanquiles.sfc.comparator." + pc.name;
						}
						pc.enabled = pluginMap.get("enabled") != null ? (Boolean)pluginMap.get("enabled") : true;
						List<Object> parametersList = (List<Object>)pluginMap.get("parameters");
						if (parametersList != null) {
							for (int k = 0; parametersList != null && k < parametersList.size(); k++) {
								Map<String, Object> parameterMap = (Map<String, Object>)parametersList.get(k);
								pc.parameters.put((String)parameterMap.get("name"), (String)parameterMap.get("value"));
							}	
						}
						
						ftc.comparators.add(pc);
					}
				}
				this.fileTypes.add(ftc);
			}
		}
		
		// if not default configuration, create and add it
		boolean found = false;
		for (int i = 0; i < this.fileTypes.size(); i++) {
			FileTypeConfiguration ftc = this.fileTypes.get(i);
			for (int j = 0; j < ftc.patterns.size(); j++) {
				if ("default-conf".equalsIgnoreCase(ftc.patterns.get(j))) {
					found = true;
					break;
				}
			}
		}
		if (!found) {
			FileTypeConfiguration ftc = new FileTypeConfiguration();
			ftc.patterns.add("default-conf");
		}
		
	}
	
}
