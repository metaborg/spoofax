package org.strategoxt.imp.runtime.services.views.outline;

import org.eclipse.imp.language.ILanguageService;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * @author Oskar van Rest
 */
public interface IOutlineService extends ILanguageService {

	void setOutlineRule(String rule);
	
	void setExpandToLevel(int level);
	
	IStrategoTerm getOutline();
	
	int getExpandToLevel();
	
}
