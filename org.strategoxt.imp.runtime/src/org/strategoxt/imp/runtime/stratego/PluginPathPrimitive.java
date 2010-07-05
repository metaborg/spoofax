package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.library.ssl.SSLLibrary;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;

/**
 * Returns the directory of the current Eclipse plugin.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class PluginPathPrimitive extends AbstractPrimitive {

	public PluginPathPrimitive() {
		super("SSL_EXT_pluginpath", 0, 0);
	}

	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
			throws InterpreterException {
		
		IOAgent agent = SSLLibrary.instance(env).getIOAgent();
		if (!(agent instanceof EditorIOAgent))
			return false;
		Descriptor descriptor = ((EditorIOAgent) agent).getDescriptor();
		env.setCurrent(env.getFactory().makeString(descriptor.getBasePath().toPortableString()));
		return true;
	}
	
}
