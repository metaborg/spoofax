package org.strategoxt.imp.runtime.stratego;

import java.util.Map;
import java.util.WeakHashMap;
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
	 * Cache for language-specific StrategoObserver. Avoids creation of an
	 * observer on every subsequent invocation for a language
	 */
	private static final Map<Descriptor, StrategoObserver> asyncCache = new WeakHashMap<Descriptor, StrategoObserver>();

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
		final String oLangName = ((IStrategoString) tvars[0]).stringValue();
		final String strategyName = ((IStrategoString) tvars[1]).stringValue();
		boolean result = false;
		try {
			IStrategoTerm inputTerm = env.current();
			Language oLang = LanguageRegistry.findLanguage(oLangName);
			if (oLang == null)
				return false;
			Descriptor oLangDescr = Environment.getDescriptor(oLang);
			StrategoObserver observer = asyncCache.get(oLangDescr);

			if (observer == null) {
				observer = loadDescriptor(oLangDescr, (EditorIOAgent) SSLLibrary.instance(env).getIOAgent());
			}
			assert observer != null;
			observer.getRuntime().setCurrent(inputTerm);
			result = observer.getRuntime().invoke(strategyName);
			if (!result) {
				observer.reportRewritingFailed();
			}
			env.setCurrent(observer.getRuntime().current());
		} catch (RuntimeException cex) {
			Environment.logException(cex);
		} catch (BadDescriptorException e) {
			Environment.logException(e);
		} catch (InterpreterException e) {
			Environment.logException(e);
		}
		return result;
	}

	private StrategoObserver loadDescriptor(Descriptor oLangDescr, EditorIOAgent editorIOAgent)
			throws BadDescriptorException {
		StrategoObserver observer = oLangDescr.createService(StrategoObserver.class, null);
		observer.configureRuntime(editorIOAgent.getProject(), editorIOAgent.getProjectPath());
		oLangDescr.createParseController();
		asyncCache.put(oLangDescr, observer);
		return observer;
	}
}
