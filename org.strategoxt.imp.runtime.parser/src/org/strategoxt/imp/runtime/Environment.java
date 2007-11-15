package org.strategoxt.imp.runtime;

import java.io.IOException;
import java.io.InputStream;

import org.spoofax.interpreter.Interpreter;
import org.spoofax.interpreter.adapter.aterm.WrappedATermFactory;
import org.spoofax.jsglr.InvalidParseTableException;
import org.spoofax.jsglr.ParseTable;
import org.spoofax.jsglr.ParseTableManager;
import org.spoofax.jsglr.SGLR;

import aterm.ATermFactory;

/**
 * Environment class that maintains a maximally shared ATerm environment and
 * parse tables, shared by any editors or other plugins.
 *
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public final class Environment {	
	private final static WrappedATermFactory wrappedFactory
		= new WrappedATermFactory();
	
	private final static ATermFactory factory = wrappedFactory.getFactory();
	
	private final static ParseTableManager parseTables
		= new ParseTableManager(factory);
	
	public static SGLR createSGLR(ParseTable parseTable) {
		return new SGLR(factory, parseTable);
	}

	public static Interpreter createInterpreter() {
		return new Interpreter(wrappedFactory);
	}
    
    public static ParseTable loadParseTable(InputStream parseTable)
    		throws IOException, InvalidParseTableException {
    	try {
    		Debug.startTimer();
	    	return parseTables.loadFromStream(parseTable);
		} finally {
			Debug.stopTimer("Parse table loaded");
		}
    }
}
