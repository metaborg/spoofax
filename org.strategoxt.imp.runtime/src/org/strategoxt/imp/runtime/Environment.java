package org.strategoxt.imp.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.imp.language.Language;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.spoofax.interpreter.adapter.aterm.WrappedATermFactory;
import org.spoofax.interpreter.core.Interpreter;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.LoggingIOAgent;
import org.spoofax.interpreter.stratego.SDefT;
import org.spoofax.jsglr.InvalidParseTableException;
import org.spoofax.jsglr.ParseTable;
import org.spoofax.jsglr.ParseTableManager;
import org.spoofax.jsglr.SGLR;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.stratego.EditorIOAgent;
import org.strategoxt.imp.runtime.stratego.IMPJSGLRLibrary;
import org.strategoxt.imp.runtime.stratego.IMPLibrary;
import org.strategoxt.imp.runtime.stratego.adapter.WrappedAstNodeFactory;

import aterm.ATermFactory;

/**
 * Environment class that maintains a maximally shared ATerm environment and
 * parse tables, shared by any editors or other plugins.
 *
 * Methods in this class are either synchronized on the {@link #getSyncRoot()}
 * property, have to be synchronized, or may only be ran from the main thread,
 * as neatly "documented" in the source code at the moment.
 *
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public final class Environment {
	
	private final static WrappedATermFactory wrappedFactory;
		
	private final static ATermFactory factory;
	
	private final static ParseTableManager parseTableManager;
	
	private final static Map<String, ParseTable> parseTables;
	
	private final static Map<String, Descriptor> descriptors;
	
	private final static WrappedAstNodeFactory wrappedAstNodeFactory;
	
	private final static Object syncRoot = new Object();
	
	private static Thread mainThread;
	
	static {
		wrappedFactory = new WrappedATermFactory();
		factory = wrappedFactory.getFactory();
		parseTableManager = new ParseTableManager(factory);
		parseTables = new HashMap<String, ParseTable>();
		descriptors = new HashMap<String, Descriptor>();
		wrappedAstNodeFactory = new WrappedAstNodeFactory();
	}
	
	// TODO: Split up shared and non-shared environment entities?
	
	// LOCKING
	
	/**
	 * Gets the object to lock on for environment entities shared
	 * between the main thread and the workspace thread.
	 */
	public static Object getSyncRoot() {
		return syncRoot;
	}
	
	private static void assertLock() {
		assert Thread.holdsLock(getSyncRoot()) : "Please use the course-grained Environment.getSyncRoot() lock";
	}
	
	private static void assertMainThread() {
		if (mainThread == null)
			mainThread = Thread.currentThread();
		assert "main".equals(mainThread.getName()) : "Please only perform this operation from the main thread";
		assert mainThread == Thread.currentThread() : "Please only perform this operation from the main thread";
	}
	
	// BASIC ACCESSORS
	
	public static WrappedAstNodeFactory getTermFactory() {
		// (no state; no assertion)
		return wrappedAstNodeFactory;
	}

	public static WrappedATermFactory getWrappedATermFactory() {
		assertLock();
		return wrappedFactory;
	}
	
	public static SGLR createSGLR(ParseTable parseTable) {
		// (no state; no assertion)
		return new SGLR(factory, parseTable);
	}
	
	// ENVIRONMENT ACCESS AND MANIPULATION

	public static Interpreter createInterpreter() throws IOException, InterpreterException {
		synchronized (getSyncRoot()) {
			// We use the wrappedAstNode factory for both the programs and the terms,
			// to ensure they are compatible.
			Interpreter result = new Interpreter(getTermFactory());
	
			result.addOperatorRegistry(new IMPJSGLRLibrary());
			result.addOperatorRegistry(new IMPLibrary());
			result.setIOAgent(new EditorIOAgent());
			
			result.load(Environment.class.getResourceAsStream("/include/libstratego-lib.ctree"));
			result.load(Environment.class.getResourceAsStream("/include/libstratego-sglr.ctree"));
			result.load(Environment.class.getResourceAsStream("/include/libstratego-gpp.ctree"));
			result.load(Environment.class.getResourceAsStream("/include/libstratego-xtc.ctree"));
			result.load(Environment.class.getResourceAsStream("/include/stratego-editor-support.ctree"));
			
			SDefT call = result.getContext().lookupSVar("REPLACE_call_0_0");
			result.getContext().getVarScope().addSVar("call_0_0", call);
			
			return result;
		}
	}
	
	public static void addToInterpreter(Interpreter interpreter, InputStream stream) throws IOException, InterpreterException {
		synchronized (getSyncRoot()) {
			interpreter.load(stream);			
		}
	}
	
	public static ParseTable registerParseTable(Language language, InputStream parseTable)
			throws IOException, InvalidParseTableException {
		
		synchronized (getSyncRoot()) {		
			Debug.startTimer();
			ParseTable table = parseTableManager.loadFromStream(parseTable);
				
			parseTables.put(language.getName(), table);
			
			Debug.stopTimer("Parse table loaded");
			
			return table;
		}
	}
	
	public static ParseTable getParseTable(Language language) {
		assertMainThread();
		
		synchronized (getSyncRoot()) { // synchronized on registration
			ParseTable table = parseTables.get(language.getName());
			
			if (table == null)
				throw new IllegalStateException("Parse table not available: " + language.getName());
			
			return table;
		}
	}
	
	public static void registerDescriptor(Language language, Descriptor descriptor) {
		synchronized (getSyncRoot()) {
			Descriptor oldDescriptor = getDescriptor(language);
			
			if (oldDescriptor != null) {
				oldDescriptor.uninitialize();
			}
			
			descriptors.put(language.getName(), descriptor);
		}
	}
	
	public static Descriptor getDescriptor(Language language) {
		synchronized (getSyncRoot()) {
			return descriptors.get(language.getName());			
		}
	}
	
	// ERROR HANDLING
	
	// TODO: Move out error handling to a separate class
	
	public static void logException(String message, Throwable t) {
		System.err.println(message);
		t.printStackTrace();
		RuntimePlugin.getInstance().logException(message, t);
	}
	
	public static void logException(String message) {
		System.err.println(message);
		RuntimePlugin.getInstance().logException(message, new RuntimeException(message));
	}
	
	public static void logException(Throwable t) {
		RuntimePlugin.getInstance().logException(null, t);
	}
	
	public static void logStrategyFailure(String message, Interpreter interpreter) {
		if (interpreter.getIOAgent() instanceof LoggingIOAgent) {
			System.err.println(message);
			String log = ((LoggingIOAgent) interpreter.getIOAgent()).getLog().trim();
			logException(message,
					new InterpreterException(message + " \nLog follows. \n\n" + log));
		} else {
			logException(message);
		}
	}
}
