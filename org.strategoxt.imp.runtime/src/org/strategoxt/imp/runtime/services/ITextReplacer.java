package org.strategoxt.imp.runtime.services;

import org.eclipse.imp.language.ILanguageService;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * @author Oskar van Rest
 */
public interface ITextReplacer extends ILanguageService {
	
	void replaceText(IStrategoTerm resultTuple);
}
