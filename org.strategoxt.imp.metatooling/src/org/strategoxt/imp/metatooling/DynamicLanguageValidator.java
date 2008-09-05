package org.strategoxt.imp.metatooling;

import org.eclipse.core.resources.IFile;
import org.eclipse.imp.language.LanguageValidator;

/**
 * Dynamically loaded language validator class,
 * currently the purpose of loading any such languages at startup,
 * plugging into IMP.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DynamicLanguageValidator extends LanguageValidator {

	public DynamicLanguageValidator() {
		StartupDescriptorLoader.initialize();
	}

	@Override
	public boolean validate(IFile file) {
		return false;
	}

	@Override
	public boolean validate(String buffer) {
		return false;
	}
}
