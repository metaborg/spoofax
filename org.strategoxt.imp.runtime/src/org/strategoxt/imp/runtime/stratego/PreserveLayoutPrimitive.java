package org.strategoxt.imp.runtime.stratego;

import static org.spoofax.interpreter.core.Tools.asJavaString;

import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.dynamicloading.RefactoringFactory;
import org.strategoxt.imp.runtime.services.StrategoObserver;
import org.strategoxt.imp.runtime.services.StrategoTextChangeCalculator;

/**
 * @author Oskar van Rest
 */
public class PreserveLayoutPrimitive extends AbstractPrimitive {

	public PreserveLayoutPrimitive() {
		super("SSL_EXT_preserve_layout", 0, 1);
	}

	@Override
	public boolean call(final IContext env, Strategy[] svars, IStrategoTerm[] tvars)
			throws InterpreterException {

		assert tvars.length == 1;
		assert tvars[0] instanceof IStrategoString;
		assert env.current() instanceof IStrategoTuple;
		assert env.current().getSubtermCount() == 2;

		Language language = LanguageRegistry.findLanguage(asJavaString(tvars[0]));
		if (language == null) {
			return false;
		}
		Descriptor d = Environment.getDescriptor(language);

		try {
			StrategoObserver observer = d.createService(StrategoObserver.class, null);
			StrategoTextChangeCalculator stcc = RefactoringFactory.createTextChangeCalculator(d);
			IStrategoTerm textReplacement = stcc.getTextReplacement(env.current(), observer);
			env.setCurrent(textReplacement.getSubterm(2));
			return true;
		} catch (BadDescriptorException e) {
			e.printStackTrace();
		}
		
		return false;
	}
}
