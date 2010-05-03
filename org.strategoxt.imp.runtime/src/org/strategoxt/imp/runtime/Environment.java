package org.strategoxt.imp.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.imp.language.Language;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.spoofax.interpreter.adapter.aterm.ATermConverter;
import org.spoofax.interpreter.adapter.aterm.UnsharedWrappedATermFactory;
import org.spoofax.interpreter.adapter.aterm.WrappedATermFactory;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.InterpreterExit;
import org.spoofax.interpreter.library.jsglr.JSGLRLibrary;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.InvalidParseTableException;
import org.spoofax.jsglr.ParseTable;
import org.spoofax.jsglr.ParseTableManager;
import org.spoofax.jsglr.SGLR;
import org.strategoxt.HybridInterpreter;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.dynamicloading.ParseTableProvider;
import org.strategoxt.imp.runtime.stratego.EditorIOAgent;
import org.strategoxt.imp.runtime.stratego.IMPJSGLRLibrary;
import org.strategoxt.imp.runtime.stratego.IMPLibrary;
import org.strategoxt.imp.runtime.stratego.IMPOpenFile;
import org.strategoxt.imp.runtime.stratego.IMPParseStringPTPrimitive;
import org.strategoxt.imp.runtime.stratego.adapter.WrappedAstNodeFactory;
import org.strategoxt.lang.compat.sglr.SGLRCompatLibrary;

import aterm.ATermFactory;
import aterm.pure.PureFactory;

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
	
	private final static ATermConverter atermConverter;
	
	private final static ParseTableManager parseTableManager;
	
	private final static Map<String, ParseTable> parseTables;
	
	private final static Map<String, ParseTableProvider> unmanagedTables;
	
	private final static Map<String, Descriptor> descriptors;
	
	private final static WrappedAstNodeFactory wrappedAstNodeFactory;
	
	private final static PrintStream STDERR = System.err; // avoid deadlocky ant override
	
	private static Thread mainThread;
	
	private static boolean isInitialized;
	
	static {
		wrappedFactory = new UnsharedWrappedATermFactory();
		factory = new PureFactory();
		parseTableManager = new ParseTableManager(factory);
		parseTables = Collections.synchronizedMap(new HashMap<String, ParseTable>());
		descriptors = Collections.synchronizedMap(new HashMap<String, Descriptor>());
		unmanagedTables = Collections.synchronizedMap(new HashMap<String, ParseTableProvider>());
		wrappedAstNodeFactory = new WrappedAstNodeFactory();
		atermConverter = new ATermConverter(factory, wrappedAstNodeFactory, true);
		checkJVMOptions();
	}

	private static void checkJVMOptions() {
		boolean ssOption = false;
		boolean serverOption = false;
		boolean mxOption = false;
		
		for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
			if (arg.startsWith("-server")) serverOption = true;
			if (arg.startsWith("-Xss") || arg.startsWith("-ss")) ssOption = true;
			if (arg.startsWith("-Xmx") || arg.startsWith("-mx")) mxOption = true;
		}
		
		if (!serverOption)
			Environment.logWarning("Make sure Eclipse is started with -vmargs -server (can be set in eclipse.ini) for best performance");
		if (!mxOption)
			Environment.logWarning("Make sure Eclipse is started with -vmargs -Xmx1024m (can be set in eclipse.ini) for at least 1024 MiB heap space (adjust downwards for low-memory systems)");
		if (!ssOption)
			Environment.logWarning("Make sure Eclipse is started with -vmargs -Xss8m (can be set in eclipse.ini) for an 8 MiB stack size");
	}
	
	// TODO: Split up shared and non-shared environment entities?
	
	// LOCKING
	
	/**
	 * Gets the object to lock on for environment entities shared
	 * between the main thread and the workspace thread.
	 */
	public static Object getSyncRoot() {
		// TODO: disallow main thread locking everywhere except in the startup loader?
		if (!isInitialized && EditorState.isUIThread())
			isInitialized = true;
		else if (!Thread.holdsLock(Environment.class) && EditorState.isUIThread() && Debug.ENABLED)
			Environment.logWarning("Acquired environment lock from main thread");
		return Environment.class;
	}
	
	public static void assertLock() {
		assert Thread.holdsLock(getSyncRoot()) : "Please use the course-grained Environment.getSyncRoot() lock";
	}
	
	private static void initMainThread() {
		Thread thread = Thread.currentThread();
		// TODO: is main thread == EditorState.isUIThread() thread?
		//       because the OSX main thread seems to be "Thread-0"
		if ("main".equals(thread.getName()) || EditorState.isUIThread())
			mainThread = thread;
	}
	
	public static boolean isMainThread() {
		if (mainThread == null) initMainThread();
		if (mainThread == null) return false;
		return Thread.currentThread() == mainThread;
	}
	
	public static void assertNotMainThread() {
		assert !isMainThread() : "Potential deadlock when performing this synchronized operation from the main thread";
	}
	
	// BASIC ACCESSORS
	
	public static WrappedAstNodeFactory getTermFactory() {
		// (no state; no assertion)
		return wrappedAstNodeFactory;
	}

	public static WrappedATermFactory getWrappedATermFactory() {
		// (stateful factory)
		assertLock();
		return wrappedFactory;
	}
	
	public static ATermConverter getATermConverter() {
	    return atermConverter;
	}
	
	public static ATermFactory getATermFactory() {
	    return factory;
	}
	
	public static SGLR createSGLR(ParseTable parseTable) {
		// (no state; no assertion)
		return new SGLR(factory, parseTable);
	}
	
	// ENVIRONMENT ACCESS AND MANIPULATION
	
	public static HybridInterpreter createInterpreter() {
		return createInterpreter(false);
	}

	public static HybridInterpreter createInterpreter(boolean noGlobalLock) {
		HybridInterpreter result =	noGlobalLock
			? new HybridInterpreter(getTermFactory())
			: new HybridInterpreter(getTermFactory()) {
				@Override
				public boolean invoke(String name) throws InterpreterExit, InterpreterException {
					assertLock();
					return super.invoke(name);
				}
				
				@Override
				public void load(IStrategoTerm program) throws InterpreterException {
					assertLock();
					super.load(program);
				}
				
				@Override
				public IStrategoTerm current() {
					assertLock();
					return super.current();
				}
			};
		
		result.getCompiledContext().getExceptionHandler().setEnabled(false);
		result.getCompiledContext().registerComponent("stratego_lib"); // ensure op. registry available
		result.getCompiledContext().registerComponent("stratego_sglr"); // ensure op. registry available
		SGLRCompatLibrary sglrLibrary = (SGLRCompatLibrary) result.getContext().getOperatorRegistry(SGLRCompatLibrary.REGISTRY_NAME);
		IMPJSGLRLibrary impSglrLibrary = new IMPJSGLRLibrary(sglrLibrary);
		result.addOperatorRegistry(impSglrLibrary);
		result.addOperatorRegistry(new IMPLibrary());
		// (all libraries added here must also be in StrategoObserver.initialize())
		impSglrLibrary.addOverrides(result.getCompiledContext());
		assert result.getContext().lookupOperator(IMPParseStringPTPrimitive.NAME) instanceof IMPParseStringPTPrimitive;
		assert result.getCompiledContext().lookupPrimitive(IMPParseStringPTPrimitive.NAME) instanceof IMPParseStringPTPrimitive;
		assert result.getCompiledContext().lookupPrimitive(IMPOpenFile.NAME) instanceof IMPOpenFile;
		assert result.getCompiledContext().getOperatorRegistry(IMPJSGLRLibrary.REGISTRY_NAME) instanceof IMPJSGLRLibrary;
		result.setIOAgent(new EditorIOAgent());
		
		return result;
	}

	public static HybridInterpreter createInterpreter(HybridInterpreter prototype) {
		HybridInterpreter result = new HybridInterpreter(prototype,
				IMPJSGLRLibrary.REGISTRY_NAME, // is spoofax-specific
				JSGLRLibrary.REGISTRY_NAME,    // connected to the library above
				IMPLibrary.REGISTRY_NAME);     // also used
		result.getCompiledContext().getExceptionHandler().setEnabled(false);
		IMPJSGLRLibrary parseLibrary = ((IMPJSGLRLibrary) result.getContext().getOperatorRegistry(IMPJSGLRLibrary.REGISTRY_NAME));
		parseLibrary.addOverrides(result.getCompiledContext());
		return result;
	}
	
	public static ParseTable registerParseTable(Language language, InputStream parseTable)
			throws IOException, InvalidParseTableException {
		
		Debug.startTimer();
		ParseTable table = parseTableManager.loadFromStream(parseTable);	
		if (language != null) {
			parseTables.put(language.getName(), table);
			Debug.stopTimer("Parse table loaded: " + language.getName());
		}
		return table;
	}
	
	public static ParseTable getParseTable(Language language) {
		ParseTable table = parseTables.get(language.getName());
		
		if (table == null)
			throw new IllegalStateException("Parse table not available: " + language.getName());
			
		return table;
	}
	
	public static void registerDescriptor(Language language, Descriptor descriptor)
			throws BadDescriptorException {
		
		Descriptor oldDescriptor = getDescriptor(language);
		
		descriptors.put(language.getName(), descriptor);
		
		if (oldDescriptor != null) {
			descriptor.addActiveServices(oldDescriptor);
			oldDescriptor.reinitialize(descriptor);
		}
	}
	
	public static void registerUnmanagedParseTable(String name, IFile file) {
		unmanagedTables.put(name, new ParseTableProvider(file));
		synchronized (descriptors) { // object is its own syncroot, per JavaDoc
			for (Descriptor descriptor : descriptors.values()) {
				if (descriptor.isUsedForUnmanagedParseTable(name)) {
					try {
						descriptor.reinitialize(descriptor);
					} catch (BadDescriptorException e) {
						Environment.logException("Could not reinitialize descriptor", e);
					}
				}
			}
		}
	}
	
	public static ParseTable getUnmanagedParseTable(String name) {
		ParseTableProvider result = unmanagedTables.get(name);
		try {
			return result == null ? null : result.get();
		} catch (Exception e) {
			Environment.logException("Could not read unmanaged parse table " + name, e);
			return null;
		}
	}
	
	public static Descriptor getDescriptor(Language language) {
		return descriptors.get(language.getName());
	}
	
	// ERROR HANDLING
	
	public static void logException(String message, Throwable t) {
		if (Debug.ENABLED) {
			if (message != null) STDERR.println(message);
			t.printStackTrace();
		}
		if (message == null) message = t.getLocalizedMessage() == null ? t.getMessage() : t.getLocalizedMessage();
		Status status = new Status(IStatus.ERROR, RuntimeActivator.PLUGIN_ID, 0, message, null);
		RuntimeActivator activator = RuntimeActivator.getInstance();
		if (activator != null) activator.getLog().log(status);
	}
	
	public static void logException(String message) {
		logException(message, new RuntimeException(message));
	}
	
	public static void logException(Throwable t) {
		logException(null, t);
	}
	
	public static void logWarning(String message) {
		if (Debug.ENABLED) STDERR.println("Warning: " + message);
		Status status = new Status(IStatus.WARNING, RuntimeActivator.PLUGIN_ID, 0, message, new RuntimeException(message));
		RuntimeActivator activator = RuntimeActivator.getInstance();
		if (activator != null) activator.getLog().log(status);
	}

	public static void asynOpenErrorDialog(final String caption, final String message, final Throwable exception) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				Status status = new Status(IStatus.ERROR, RuntimeActivator.PLUGIN_ID, 0, message, exception);
				ErrorDialog.openError(null, caption, null, status);
			}
		});
	}
}
