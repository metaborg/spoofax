package org.strategoxt.imp.runtime.stratego;

import org.eclipse.core.resources.IProject;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.ssl.SSLLibrary;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.services.StrategoObserver;

/**
 * Call a strategy by name in another language by name
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class ForeignLangCallPrimitive extends AbstractPrimitive {

	public ForeignLangCallPrimitive() {
		super("SSL_EXT_foreigncall", 0, 2);
	}

	/**
	 * Example usage:
	 * 
	 * <code>
	 * foreign-call(|lang,strategy) = PRIM("SSL_EXT_buildercall");
	 * 
	 * foobar:
	 *  foreign-call(|"OtherLang", "strategy-name")
	 * 
	 * </code>
	 */
	@Override
	public boolean call(final IContext env, Strategy[] svars, IStrategoTerm[] tvars)
			throws InterpreterException {

		assert tvars.length == 2;
		assert tvars[0] instanceof IStrategoString;
		assert tvars[1] instanceof IStrategoString;
		try {
			final String oLangName = ((IStrategoString) tvars[0]).stringValue();
			final String strategyName = ((IStrategoString) tvars[1]).stringValue();
			final IStrategoTerm inputTerm = env.current();
			final EditorIOAgent agent = (EditorIOAgent) SSLLibrary.instance(env).getIOAgent();
			final IProject project = agent.getProject();

			final Language oLang = LanguageRegistry.findLanguage(oLangName);
			if (oLang == null)
				return false;
			final Descriptor oLangDescr = Environment.getDescriptor(oLang);
			assert oLangDescr != null;
			final StrategoObserver observer = oLangDescr.createService(StrategoObserver.class, null);
			final IStrategoTerm outputTerm = observer.invoke(strategyName, inputTerm, project
					.getLocation().toFile());
			env.setCurrent(outputTerm);
		} catch (ClassCastException cex) {
			Environment.logException(cex);
			return false;
		} catch (BadDescriptorException e) {
			Environment.logException(e);
			return false;
		}
		return true;
	}
}
