package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.library.ssl.SSLLibrary;
import org.spoofax.interpreter.library.ssl.SSL_fopen;
import org.spoofax.interpreter.terms.IStrategoInt;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class IMPOpenFile extends SSL_fopen {
	
	private final SourceMappings mappings;
	
	public IMPOpenFile(SourceMappings mappings) {
		this.mappings = mappings;
	}

	@Override
	protected IStrategoInt call(IContext env, String fn, String mode) {
		IStrategoInt result = super.call(env, fn, mode);
		if (result == null) return null;
		IOAgent io = SSLLibrary.instance(env).getIOAgent();
		mappings.putInputFile(result.intValue(), io.openFile(fn));
		return result;
	}

}
