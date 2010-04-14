package org.strategoxt.imp.runtime.stratego;

import java.io.IOException;

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
	protected IStrategoString call(IContext env, int fd) throws IOException {
		IStrategoString result = super.call(env, fd);
		if (result == null) return null;
		
		// Wrap in an unshared object; strings may be shared
		// TODO: use identity hash code or something?
		SourceMappings.MappableTerm mappableResult = new SourceMappings.MappableTerm(result); 
		mappings.putInputFileForString(mappableResult, mappings.getInputFile(fd));
		
		return mappableResult;
	}

}
