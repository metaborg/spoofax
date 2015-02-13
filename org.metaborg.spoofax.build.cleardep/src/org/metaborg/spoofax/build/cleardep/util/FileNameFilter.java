package org.metaborg.spoofax.build.cleardep.util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class FileNameFilter implements FileFilter {

	private List<Pattern> alternativePatterns;
	
	public FileNameFilter(String regex) {
		alternativePatterns = new ArrayList<>();
		alternativePatterns.add(Pattern.compile(regex));
	}
	
	public FileNameFilter(String... regexes) {
		this.alternativePatterns = new ArrayList<>();
		for (String regex : regexes) 
			this.alternativePatterns.add(Pattern.compile(regex));
	}
	
	@Override
	public boolean accept(File path) {
		String pathname = path.getName();
		for (Pattern pat : alternativePatterns)
			if (pat.matcher(pathname).matches())
				return true;
		return false;
	}

}
