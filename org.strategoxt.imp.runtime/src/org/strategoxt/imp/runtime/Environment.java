package org.strategoxt.imp.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.imp.language.Language;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.spoofax.compiler.Compiler;
import org.spoofax.interpreter.core.Interpreter;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.adapter.aterm.WrappedATermFactory;
import org.spoofax.jsglr.InvalidParseTableException;
import org.spoofax.jsglr.ParseTable;
import org.spoofax.jsglr.ParseTableManager;
import org.spoofax.jsglr.SGLR;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.stratego.IMPJSGLRLibrary;
import org.strategoxt.imp.runtime.stratego.adapter.WrappedAstNodeFactory;

import aterm.ATermFactory;

/**
 * Environment class that maintains a maximally shared ATerm environment and
 * parse tables, shared by any editors or other plugins.
 *
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public final class Environment {
	// TODO: What about thread safety?
	
	private final static WrappedATermFactory wrappedFactory
		= new WrappedATermFactory();
		
	private final static ATermFactory factory = wrappedFactory.getFactory();
	
	private final static ParseTableManager parseTableManager
		= new ParseTableManager(factory);
	
	private final static Map<Language, ParseTable> parseTables
		= new HashMap<Language, ParseTable>();
	
	private final static Map<Language, Descriptor> descriptors
		= new HashMap<Language, Descriptor>();
	
	private final static WrappedAstNodeFactory wrappedAstNodeFactory
		= new WrappedAstNodeFactory();
	
	public static ATermFactory getATermFactory() {
		return factory;
	}
	
	public static WrappedATermFactory getWrappedTermFactory() {
		return wrappedFactory;
	}
	
	public static WrappedAstNodeFactory getWrappedAstNodeFactory() {
		return wrappedAstNodeFactory;
	}
	
	public static SGLR createSGLR(ParseTable parseTable) {
		return new SGLR(factory, parseTable);
	}

	public static Interpreter createInterpreter() throws IOException, InterpreterException {
		Interpreter result = new Interpreter(wrappedAstNodeFactory, wrappedFactory);
		result.addOperatorRegistry("JSGLR", new IMPJSGLRLibrary());
		result.load(Compiler.sharePath() + "/stratego-lib/libstratego-lib.ctree");
		result.load(Compiler.sharePath() + "/libstratego-sglr.ctree");
		return result;
	}
	
	public static ParseTable registerParseTable(Language language, InputStream parseTable)
		throws IOException, InvalidParseTableException {
		
		Debug.startTimer();
		ParseTable table = parseTableManager.loadFromStream(parseTable);
			
		parseTables.put(language, table);
		assert getParseTable(language) == table;
		
		Debug.stopTimer("Parse table loaded");
		
		return table;
	}
	
	public static ParseTable getParseTable(Language language) {
		ParseTable table = parseTables.get(language);
		
		if (table == null) throw new IllegalStateException("Parse table not available: " + language.getName());
		
		return table;
	}
	
	public static Descriptor getDescriptor(Language language) {
		return descriptors.get(language);
	}
	
	public static void registerDescriptor(Language language, Descriptor descriptor) {
		descriptors.put(language, descriptor);
	}
	
	public static void logException(String message, Throwable t) {
		System.err.println(message);
		t.printStackTrace();
		RuntimePlugin.getInstance().logException(message, t);
	}
	
	public static void logException(String message) {
		System.err.println(message);
		RuntimePlugin.getInstance().logException(message, new RuntimeException(message));
	}
}
