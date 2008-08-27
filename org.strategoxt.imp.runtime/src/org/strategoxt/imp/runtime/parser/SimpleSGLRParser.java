package org.strategoxt.imp.runtime.parser;

import java.io.IOException;
import java.io.InputStream;
import org.spoofax.interpreter.core.Interpreter;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.ParseTable;
import org.spoofax.jsglr.SGLR;
import org.spoofax.jsglr.SGLRException;
import org.strategoxt.imp.runtime.Environment;

import aterm.ATerm;

/**
 * Simple SGLR parser and imploder wrapper that does not create
 * an IMP AST or tokens.
 * 
 * @see SGLRParser
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class SimpleSGLRParser {
	private static final Interpreter imploder;
	
	private final SGLR parser;
	
	private final String startSymbol;
	
	static {
		SGLR.setWorkAroundMultipleLookahead(true);
		
		try {
			imploder = Environment.createInterpreter();
		} catch (IOException e) {
			throw new RuntimeException(e); // no recovery
		} catch (InterpreterException e) {
			throw new RuntimeException(e); // no recovery
		}
	}
	
	public SimpleSGLRParser(ParseTable parseTable, String startSymbol) {
		// TODO: Once spoofax supports it, use a start symbol
		parser = Environment.createSGLR(parseTable);
		this.startSymbol = null;
	}
	
	public IStrategoTerm parseImplode(InputStream input) throws SGLRException {
		try {
			ATerm asfix = parser.parse(input, startSymbol);

			return implode(asfix);
		} catch (IOException x) {
			throw new RuntimeException(x); // unexpected; fatal
		}
	}

	private IStrategoTerm implode(ATerm asfix) {
		try {
			imploder.setCurrent(Environment.getWrappedTermFactory().wrapTerm(asfix));
			imploder.invoke("implode_asfix_0_0");
		
			return imploder.current();
		} catch (InterpreterException x) {
			throw new RuntimeException(x); // unexpected; fatal
		}
	}
}
