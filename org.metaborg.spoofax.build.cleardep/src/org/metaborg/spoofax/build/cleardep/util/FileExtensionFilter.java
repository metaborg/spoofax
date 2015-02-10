package org.metaborg.spoofax.build.cleardep.util;

import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;

import org.sugarj.common.FileCommands;

public class FileExtensionFilter implements FileFilter {

	private HashSet<String> extensions;
	
	public FileExtensionFilter(String ext) {
		extensions = new HashSet<>();
		extensions.add(ext);
	}
	
	public FileExtensionFilter(String... exts) {
		extensions = new HashSet<>();
		for (String ext : exts) 
			extensions.add(ext);
	}
	
	@Override
	public boolean accept(File pathname) {
		String pathext = FileCommands.getExtension(pathname);
		return extensions.contains(pathext);
	}

}
