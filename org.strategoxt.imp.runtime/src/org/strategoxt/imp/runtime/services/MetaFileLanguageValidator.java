package org.strategoxt.imp.runtime.services;

import org.eclipse.core.resources.IFile;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.eclipse.imp.language.LanguageValidator;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;

/**
 * Validates files by their (optional) .meta file,
 * picking the right editor for the job.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class MetaFileLanguageValidator extends LanguageValidator {
	
	private static boolean isLanguageRegistryPatchEnabled;
	
	private Descriptor descriptor;
	
	public MetaFileLanguageValidator(Descriptor descriptor) {
	    this.descriptor = descriptor;
	}
	
	protected MetaFileLanguageValidator() {
		// Lazily initializes the descriptor
	}
	
	public Descriptor getDescriptor() {
		return descriptor;
	}

	@Override
	public boolean validate(IFile file) {
		isLanguageRegistryPatchEnabled = true;
		String metaFileName = file.getFullPath().removeFileExtension().addFileExtension("meta").toOSString();
		MetaFile metaFile = MetaFile.read(metaFileName);
		
		return metaFile == null || validateByLanguage(file, metaFile.getLanguage());
	}
	
	public boolean validateByLanguage(IFile file, String languageName) {
		try {
			Language language = getDescriptor().getLanguage();
				
			if (languageName.equals(language.getName()))
				return true;
			
			if (isExactMatchAvailable(languageName))
				return false; // better alternative exists
			
			if (isExtensionOf(language, languageName))
				return true;
			
			if (getDescriptor().isUsedForUnmanagedParseTable(languageName))
				return true;
			
			if (isUnmanagedMatchAvailable(languageName))
				return false; // better alternatives exist
			
			if (!isExtensionOfAvailable(languageName) && validateByExtension(file))
				return true;

			return true;
		} catch (BadDescriptorException e) {
			Environment.logException(e);
			return false;
		}
	}
	
	private boolean validateByExtension(IFile file) {
		try {
			return getDescriptor().getLanguage().hasExtension(file.getFileExtension());
		} catch (BadDescriptorException e) {
			Environment.logException(e);
			return false;
		}
	}
	
	private boolean isExactMatchAvailable(String languageName) throws BadDescriptorException {
		Language myLanguage = getDescriptor().getLanguage(); 
		for (Language language : LanguageRegistry.getLanguages()) {
			if (language != myLanguage && languageName.equals(language.getName()))
				return true;
		}
		return false;
	}
	
	private boolean isUnmanagedMatchAvailable(String languageName) throws BadDescriptorException {
		for (Language language : LanguageRegistry.getLanguages()) {
			Descriptor descriptor = Environment.getDescriptor(language);
			if (descriptor != null && descriptor.isUsedForUnmanagedParseTable(languageName))
				return true;
		}
		return false;
	}
	
	private boolean isExtensionOfAvailable(String languageName) throws BadDescriptorException {
		Language myLanguage = getDescriptor().getLanguage(); 
		for (Language language : LanguageRegistry.getLanguages()) {
			if (language != myLanguage && isExtensionOf(language, languageName))
				return true;
		}
		return false;
	}
	
	/**
	 * Tests if <code>language</code> is an extension of <code>languageName</code>.
	 */
	private static boolean isExtensionOf(Language language, String languageName) {
		Descriptor descriptor = Environment.getDescriptor(language);
		if (descriptor == null)
			return false;
		for (String extended : descriptor.getExtendedLanguages()) {
			if (languageName.equals(extended))
				return true;
		}
		return false;
	}

	@Override
	public boolean validate(String buffer) {
		if (!isLanguageRegistryPatchEnabled) {
			System.err.println("Warning: LanguageRegistry patch not enabled; cannot use .meta files");
			isLanguageRegistryPatchEnabled = true;
		}
			
		return true;
	}
}