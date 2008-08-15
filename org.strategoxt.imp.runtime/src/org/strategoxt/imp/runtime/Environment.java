package org.strategoxt.imp.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.imp.runtime.RuntimePlugin;
import org.spoofax.compiler.Compiler;
import org.spoofax.interpreter.core.Interpreter;
import org.spoofax.interpreter.core.InterpreterException;
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
	
	private final static ParseTableManager parseTableManager
		= new ParseTableManager(factory);
	
	private final static Map<String, ParseTable> parseTables
		= new HashMap<String, ParseTable>();
	
	public static ATermFactory getATermFactory() {
		return factory;
	}
	
	public static WrappedATermFactory getWrappedTermFactory() {
		return wrappedFactory;
	}
	
	public static SGLR createSGLR(ParseTable parseTable) {
		return new SGLR(factory, parseTable);
	}

	public static Interpreter createInterpreter() throws IOException, InterpreterException {
		Interpreter result = new Interpreter(wrappedFactory);
		result.load(Compiler.sharePath() + "/stratego-lib/libstratego-lib.ctree");
		result.load(Compiler.sharePath() + "/libstratego-sglr.ctree");
		return result;
	}
	
	public static void registerParseTable(String language, InputStream parseTable)
		throws IOException, InvalidParseTableException {
		
		try {
			Debug.startTimer();
			ParseTable table = parseTableManager.loadFromStream(parseTable);
			
			parseTables.put(language, table);
		} finally {
			Debug.stopTimer("Parse table loaded");
		}
	}
	
	public static ParseTable getParseTable(String grammar) {
		ParseTable table = parseTables.get(grammar);
		
		if (table == null) throw new IllegalStateException("Parse table not available: " + grammar);
		
		return table;
	}
	
	public static void logException(String message, Throwable t) {
		System.err.println(message);
		t.printStackTrace();
		RuntimePlugin.getInstance().logException(message, t);
	}
}
