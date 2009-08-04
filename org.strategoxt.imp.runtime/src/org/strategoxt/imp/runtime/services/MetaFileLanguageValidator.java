package org.strategoxt.imp.runtime.services;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.language.LanguageValidator;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 *
 */
public class MetaFileLanguageValidator extends LanguageValidator {
	
	private static boolean isLanguageRegistryPatchEnabled;

	@Override
	public boolean validate(IFile file) {
		isLanguageRegistryPatchEnabled = true;
		//String name = file.getName();
		//IPath location = file.getLocation().removeLastSegments(1);
		return true;
	}

	@Override
	public boolean validate(String buffer) {
		// String path = currentPath();
		if (!isLanguageRegistryPatchEnabled) {
			System.err.println("LanguageRegistry patch not enabled; cannot use .meta files");
			isLanguageRegistryPatchEnabled = true;
		}
			
		return false;
	}
}