package org.strategoxt.imp.runtime.stratego;

import static org.spoofax.interpreter.core.Tools.asJavaString;
import static org.spoofax.interpreter.core.Tools.isTermString;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;

/**
 * Returns the language description for the current file
 * 
 * @author Maartje de Jonge
 */
public class LanguageDescriptionPrimitive extends AbstractPrimitive {

	public LanguageDescriptionPrimitive() {
		super("SSL_EXT_languagedescription", 0, 1);
	}

	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
			throws InterpreterException {
		if (!isTermString(tvars[0])) return false;
		try {
			IFile file = EditorIOAgent.getFile(env, asJavaString(tvars[0]));
			Language language = LanguageRegistry.findLanguage(file.getFullPath(), null);
			ArrayList<IStrategoTerm> extensions = new ArrayList<IStrategoTerm>();
			for (String ext : language.getFilenameExtensions()) {
				extensions.add(env.getFactory().makeString(ext));
			}
			IStrategoList langExt = env.getFactory().makeList(extensions);
			IStrategoString langName = env.getFactory().makeString(language.getName());
			env.setCurrent(
				env.getFactory().makeTuple(
					langName,
					langExt
				)
			);
		} catch (FileNotFoundException e) {
			return false;
		}
		return true;
	}
	
}
