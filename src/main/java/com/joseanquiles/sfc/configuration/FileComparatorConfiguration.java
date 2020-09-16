package com.joseanquiles.sfc.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.joseanquiles.sfc.filter.SFCFilter;
import com.joseanquiles.sfc.util.FileUtil;

public class FileComparatorConfiguration {

	String name = "";
	String description = "";
	String source = "";
	String target = "";
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
	
	public String getSource() {
		return this.source;
	}
	
	public String getTarget() {
		return this.target;
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

	public List<SFCFilter> getPluginsForFile(File file) throws Exception {
		FileTypeConfiguration ftc = matchFileType(file);
		List<SFCFilter> result = new ArrayList<>();
		for (int i = 0; i < ftc.plugins.size(); i++) {
			PluginConfiguration pc = ftc.plugins.get(i);
			
			// plugin object
			Class<?> clazz = Class.forName(pc.pluginName);
			Constructor<?> ctor = clazz.getConstructor();
			SFCFilter plugin = (SFCFilter)ctor.newInstance();
			
			// parameters
			plugin.setParameters(pc.parameters);
			
			result.add(plugin);
		}
		return result;
	}
	
	private FileTypeConfiguration matchFileType(File file) {
		String ext = FileUtil.getFileExtension(file.getName());
		for (int i = 0; ext.length() > 0 && i < this.fileTypes.size(); i++) {
			FileTypeConfiguration ftc = this.fileTypes.get(i);
			for (int j = 0; j < ftc.extensions.size(); j++) {
				if (ext.equalsIgnoreCase(ftc.extensions.get(j))) {
					return ftc;
				}
			}
		}
		// No matching found, return default configuration
		for (int i = 0; i < this.fileTypes.size(); i++) {
			FileTypeConfiguration ftc = this.fileTypes.get(i);
			for (int j = 0; j < ftc.extensions.size(); j++) {
				if ("default-conf".equalsIgnoreCase(ftc.extensions.get(j))) {
					return ftc;
				}
			}
		}
		FileTypeConfiguration ftc = new FileTypeConfiguration();
		ftc.extensions.add("default-conf");
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
		this.source = (String)yamlMap.get("source");
		this.target = (String)yamlMap.get("target");
		this.outputFile = (String)yamlMap.get("output-file");

		if (this.source == null) {
			throw new Exception("source is mandatory");
		}
		if (this.target == null) {
			throw new Exception("target is mandatory");
		}

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
				List<Object> extensionsList = (List<Object>)filetypeMap.get("extensions");
				if (extensionsList != null) {
					for (int j = 0; j < extensionsList.size(); j++) {
						ftc.extensions.add((String)extensionsList.get(j));
					}
				}
				List<Object> pluginsList = (List<Object>)filetypeMap.get("plugins");
				if (pluginsList != null) {
					for (int j = 0; pluginsList != null && j < pluginsList.size(); j++) {
						PluginConfiguration pc = new PluginConfiguration();
						Map<String, Object> pluginMap = (Map<String, Object>)pluginsList.get(j);
						pc.pluginName = (String)pluginMap.get("name");
						// built-in plugins 
						if (!pc.pluginName.contains(".")) {
							pc.pluginName = "com.joseanquiles.comparator.plugin." + pc.pluginName;
						}
						pc.enabled = pluginMap.get("enabled") != null ? (Boolean)pluginMap.get("enabled") : true;
						List<Object> parametersList = (List<Object>)pluginMap.get("parameters");
						if (parametersList != null) {
							for (int k = 0; parametersList != null && k < parametersList.size(); k++) {
								Map<String, Object> parameterMap = (Map<String, Object>)parametersList.get(k);
								pc.parameters.put((String)parameterMap.get("name"), (String)parameterMap.get("value"));
							}	
						}
						
						ftc.plugins.add(pc);
					}
				}
				this.fileTypes.add(ftc);
			}
		}
		
		// if not default configuration, create and add it
		boolean found = false;
		for (int i = 0; i < this.fileTypes.size(); i++) {
			FileTypeConfiguration ftc = this.fileTypes.get(i);
			for (int j = 0; j < ftc.extensions.size(); j++) {
				if ("default-conf".equalsIgnoreCase(ftc.extensions.get(j))) {
					found = true;
					break;
				}
			}
		}
		if (!found) {
			FileTypeConfiguration ftc = new FileTypeConfiguration();
			ftc.extensions.add("default-conf");
		}
		
	}
	
}
