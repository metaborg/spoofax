package org.metaborg.spoofax.build.cleardep.util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class FileNameFilter implements FileFilter {

	private List<String> alternativeNameParts;
	
	public FileNameFilter(String namePart) {
		alternativeNameParts = new ArrayList<>();
		alternativeNameParts.add(namePart);
	}
	
	public FileNameFilter(String... alternativeNameParts) {
		this.alternativeNameParts = new ArrayList<>();
		for (String ext : alternativeNameParts) 
			this.alternativeNameParts.add(ext);
	}
	
	@Override
	public boolean accept(File pathname) {
		String pathext = pathname.getName();
		for (String part : alternativeNameParts)
			if (pathext.contains(part))
				return true;
		return false;
	}

}
