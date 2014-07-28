package org.strategoxt.imp.runtime.services.views.properties;

import org.eclipse.imp.language.ILanguageService;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * @author Oskar van Rest
 */
public interface IPropertiesService extends ILanguageService {

	IStrategoTerm getProperties(int selectionOffset, int selectionLength);
}
