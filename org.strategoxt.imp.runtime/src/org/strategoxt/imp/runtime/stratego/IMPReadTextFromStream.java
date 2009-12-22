package org.strategoxt.imp.runtime.stratego;

import java.io.InputStream;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.terms.IStrategoString;
import org.strategoxt.lang.compat.SSL_EXT_read_text_from_stream;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class IMPReadTextFromStream extends SSL_EXT_read_text_from_stream {
	
	private final SourceMappings mappings;
	
	public IMPReadTextFromStream(SourceMappings mappings) {
		this.mappings = mappings;
	}

	@Override
	protected IStrategoString call(IContext env, InputStream input) {
		final IStrategoString result = super.call(env, input);
		if (result == null) return null;
		
		mappings.putInputFile(result, mappings.getInputFile(input));
		
		return result;
	}

}
