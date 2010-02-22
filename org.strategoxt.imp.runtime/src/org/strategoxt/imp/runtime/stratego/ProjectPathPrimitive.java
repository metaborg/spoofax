package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.library.ssl.SSLLibrary;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Returns the directory of the current Eclipse project.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class ProjectPathPrimitive extends AbstractPrimitive {

	public ProjectPathPrimitive() {
		super("SSL_EXT_projectpath", 0, 0);
	}

	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
			throws InterpreterException {
		
		IOAgent agent = SSLLibrary.instance(env).getIOAgent();
		if (!(agent instanceof EditorIOAgent) || ((EditorIOAgent) agent).getProjectPath() == null)
			return false;
		env.setCurrent(env.getFactory().makeString(((EditorIOAgent) agent).getProjectPath()));
		return true;
	}
	
}
