package org.strategoxt.imp.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.imp.language.Language;
import org.spoofax.interpreter.core.Interpreter;
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
	
	private final static Map<Language, ParseTable> parseTables
		= new HashMap<Language, ParseTable>();
	
	public static ATermFactory getATermFactory() {
		return factory;
	}
	
	public static WrappedATermFactory getWrappedTermFactory() {
		return wrappedFactory;
	}
	
	public static SGLR createSGLR(ParseTable parseTable) {
		return new SGLR(factory, parseTable);
	}

	public static Interpreter createInterpreter() {
		return new Interpreter(wrappedFactory);
	}
	
	public static void registerParseTable(Language grammar, InputStream parseTable)
		throws IOException, InvalidParseTableException {
		
		try {
			Debug.startTimer();
			ParseTable table = parseTableManager.loadFromStream(parseTable);
			
			parseTables.put(grammar, table);
		} finally {
			Debug.stopTimer("Parse table loaded");
		}
	}
	
	public static ParseTable getParseTable(Language grammar) {
		ParseTable table = parseTables.get(grammar);
		
		if (table == null) throw new IllegalStateException("Parse table not available: " + grammar);
		
		return table;
	}
}
