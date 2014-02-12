/**
 * 
 */
package org.strategoxt.imp.runtime.services;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.metaborg.runtime.task.primitives.TaskLibrary;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.InterpreterExit;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.library.index.legacy.LegacyIndexLibrary;
import org.spoofax.interpreter.library.jsglr.JSGLRLibrary;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermFactory;
import org.strategoxt.HybridInterpreter;
import org.strategoxt.IncompatibleJarException;
import org.strategoxt.imp.debug.core.str.launching.DebuggableHybridInterpreter;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.WeakWeakMap;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.stratego.EditorIOAgent;
import org.strategoxt.imp.runtime.stratego.IMPJSGLRLibrary;
import org.strategoxt.imp.runtime.stratego.IMPLibrary;
import org.strategoxt.imp.runtime.stratego.IMPOpenFile;
import org.strategoxt.imp.runtime.stratego.IMPParseStringPTPrimitive;

/**
 * Singleton service for providing Stratego Runtimes (Hybrid/DebuggableHybrid)
 * intepreters. It mains a (wannabe) cache of prototipical interpreters
 * organized by language (identified by its {@link Descriptor}).
 * 
 * @author vladvergu
 * 
 */
public class StrategoRuntimeFactory {

	/**
	 * Global setting to enable the Stratego Debugger feature.
	 */
	public static final boolean DEBUG_INTERPRETER_ENABLED = true;

	/**
	 * The base of all term factories. This is shared between languages
	 */
	public static final ITermFactory BASE_TERM_FACTORY = new TermFactory()
			.getFactoryWithStorageType(IStrategoTerm.MUTABLE);

	private Map<Descriptor, HybridInterpreter> prototypes = Collections
			.synchronizedMap(new WeakWeakMap<Descriptor, HybridInterpreter>());

	private static StrategoRuntimeFactory INSTANCE;

	private StrategoRuntimeFactory() {
	}

	public static StrategoRuntimeFactory instance() {
		if (INSTANCE == null) {
			INSTANCE = new StrategoRuntimeFactory();
		}
		return INSTANCE;
	}

	/**
	 * Retrieve an interpreter for this descriptor. This method creates
	 * prototypes and new interpreters as needed. The returned interpreter will
	 * be either a {@link HybridInterpreter} or a
	 * {@link DebuggableHybridInterpreter} depending on the global
	 * {@link StrategoRuntimeFactory#DEBUG_INTERPRETER_ENABLED} flag.
	 * 
	 * @param descriptor
	 * @param allowPrototype
	 *            if true then the interpreter created will be based on a
	 *            prototype if it exists otherwise a prototype will be created
	 *            and stored
	 * @return a new HybridInterpreter for this descriptor
	 */
	public HybridInterpreter getInterpreter(Descriptor descriptor,
			boolean allowPrototype) {

		if (allowPrototype) {
			HybridInterpreter prototype = prototypes.get(descriptor);
			if (prototype == null) {
				prototype = createPrototype(descriptor);
				prototypes.put(descriptor, prototype);
			}
			return createInterpreterFromPrototype(prototype);
		}
		return createPrototype(descriptor);
	}

	/**
	 * Removes the stored prototype for this descriptor if one exists.
	 * 
	 * @param descriptor
	 */
	public void resetPrototype(Descriptor descriptor) {
		prototypes.remove(descriptor);
	}

	private HybridInterpreter createPrototype(Descriptor descriptor) {
		final HybridInterpreter runtime = createInterpreter(descriptor, true);
		runtime.init();

		Debug.startTimer();
		List<String> jars = new ArrayList<>();
		List<String> ctrees = new ArrayList<>();
		for (File file : descriptor.getAttachedFiles()) {
			String filename = file.toString();
			if (filename.endsWith(".ctree")) {
				ctrees.add(filename);
			} else if (filename.endsWith(".jar")) {
				jars.add(filename);
			} else if (filename.endsWith(".str")) {
				Environment
						.asynOpenErrorDialog(
								"Loading analysis components",
								"Cannot use .str files as a provider: please specify a .ctree or .jar file instead (usually built in /include/)",
								null);
			}
		}

		if (Environment.allowsDebugging(descriptor)) {
			IPath utilsPath = descriptor.getBasePath().append("utils");
			boolean javajarExists = utilsPath
					.append("stratego-debug-runtime-java.jar").toFile()
					.exists();
			boolean jarExists = utilsPath.append("stratego-debug-runtime.jar")
					.toFile().exists();
			if (!javajarExists || !jarExists) {
				// one of the required jars does not exist!
				// make sure the project builds jars instead of ctree's!
				Environment
						.asynOpenErrorDialog(
								"Loading debugging components",
								"Debug runtime jars not found! Please rebuild with jars instead of ctree's",
								null);
			} else {
				jars.add("utils/stratego-debug-runtime-java.jar");
				jars.add("utils/stratego-debug-runtime.jar");
			}
		}

		loadCTrees(runtime, descriptor, ctrees);
		loadJars(runtime, descriptor, jars);

		Debug.stopTimer("Loaded analysis components");

		return runtime;
	}

	private void loadCTrees(HybridInterpreter runtime, Descriptor descriptor,
			List<String> filenames) {
		for (String filename : filenames) {

			try {
				Debug.startTimer("Loading Stratego module ", filename);
				// assert getLock() == Environment.getStrategoLock()
				// || !Environment.getStrategoLock().isHeldByCurrentThread();
				Environment.getStrategoLock().lock();
				try {
					runtime.load(descriptor.openAttachment(filename));
				} finally {
					Environment.getStrategoLock().unlock();
				}
				Debug.stopTimer("Successfully loaded " + filename);
			} catch (InterpreterException | IOException e) {
				Environment.logException(new BadDescriptorException(
						"Could not load compiler service provider(s): "
								+ filename, e));
				if (descriptor.isDynamicallyLoaded())
					Environment.asynOpenErrorDialog(
							"Dynamic descriptor loading",
							"Error loading compiler service provider(s): "
									+ filename, e);
			}
		}
	}

	private void loadJars(HybridInterpreter runtime, Descriptor descriptor,
			List<String> jars) {
		if (jars.size() > 0) {
			try {
				Debug.startTimer("Loading Stratego modules " + jars);
				URL[] classpath = new URL[jars.size()];
				for (int i = 0; i < classpath.length; i++) {
					File file = new File(jars.get(i));
					if (!file.isAbsolute())
						file = descriptor.getBasePath().append(file.getPath())
								.toFile();
					if (descriptor.isDynamicallyLoaded())
						file = new FileCopier().copyToTempFile(file);
					classpath[i] = file.toURI().toURL();
				}
				Class<?> attachments = descriptor.getAttachmentProvider();
				Debug.log("Loading JARs from " + Arrays.toString(classpath));
				Debug.log("Parent class loader: ", attachments.getName() + "; "
						+ attachments.getClassLoader().getClass().getName());
				runtime.loadJars(getClass().getClassLoader(), classpath);
				Debug.stopTimer("Successfully loaded " + jars);
			} catch (IncompatibleJarException | IOException | Error
					| RuntimeException e) {
				Environment.logException(new BadDescriptorException(
						"Could not load compiler service provider(s): " + jars,
						e));
				if (descriptor.isDynamicallyLoaded())
					Environment.asynOpenErrorDialog(
							"Dynamic descriptor loading",
							"Error loading compiler service provider(s): "
									+ jars, e);
			}
		}
	}

	private HybridInterpreter createInterpreter(Descriptor descriptor,
			boolean globalInterpreterLock) {
		HybridInterpreter result = null;
		ITermFactory termFactory = descriptor.getTermFactory(true, false);
		assert termFactory != null;
		if (DEBUG_INTERPRETER_ENABLED) {
			result = !globalInterpreterLock ? new DebuggableHybridInterpreter(
					termFactory) : new LockableDebuggableHybridInterpreter(
					termFactory);
		} else {
			result = !globalInterpreterLock ? new HybridInterpreter(termFactory)
					: new LockableHybridInterpreter(termFactory);
		}
		result.getCompiledContext().getExceptionHandler().setEnabled(false);
		result.getCompiledContext().registerComponent("stratego_lib"); // ensure
		// op.
		// registry
		// available
		result.getCompiledContext().registerComponent("stratego_sglr"); // ensure
		// op.
		// registry
		// available
		result.getCompiledContext().addOperatorRegistry(new TaskLibrary());
		result.getCompiledContext().addOperatorRegistry(
				new LegacyIndexLibrary());
		JSGLRLibrary sglrLibrary = (JSGLRLibrary) result.getContext()
				.getOperatorRegistry(JSGLRLibrary.REGISTRY_NAME);
		IMPJSGLRLibrary impSglrLibrary = new IMPJSGLRLibrary(sglrLibrary);
		result.addOperatorRegistry(impSglrLibrary);
		result.addOperatorRegistry(new IMPLibrary());
		// (all libraries added here must also be in
		// StrategoObserver.initialize())
		impSglrLibrary.addOverrides(result.getCompiledContext());
		assert result.getContext().lookupOperator(
				IMPParseStringPTPrimitive.NAME) instanceof IMPParseStringPTPrimitive;
		assert result.getCompiledContext().lookupPrimitive(
				IMPParseStringPTPrimitive.NAME) instanceof IMPParseStringPTPrimitive;
		assert result.getCompiledContext().lookupPrimitive(IMPOpenFile.NAME) instanceof IMPOpenFile;
		assert result.getCompiledContext().getOperatorRegistry(
				IMPJSGLRLibrary.REGISTRY_NAME) instanceof IMPJSGLRLibrary;
		result.setIOAgent(new EditorIOAgent());

		return result;
	}

	private static HybridInterpreter createInterpreterFromPrototype(
			HybridInterpreter prototype) {
		if (!DEBUG_INTERPRETER_ENABLED) {
			HybridInterpreter result = new HybridInterpreter(prototype,
					IMPJSGLRLibrary.REGISTRY_NAME, // is spoofax-specific
					JSGLRLibrary.REGISTRY_NAME, // connected to the library
												// above
					IMPLibrary.REGISTRY_NAME); // also used
			result.getCompiledContext().getExceptionHandler().setEnabled(false);
			IMPJSGLRLibrary parseLibrary = ((IMPJSGLRLibrary) result
					.getContext().getOperatorRegistry(
							IMPJSGLRLibrary.REGISTRY_NAME));
			parseLibrary.addOverrides(result.getCompiledContext());
			return result;
		} else {
			DebuggableHybridInterpreter result = new DebuggableHybridInterpreter(
					prototype, IMPJSGLRLibrary.REGISTRY_NAME, // is
																// spoofax-specific
					JSGLRLibrary.REGISTRY_NAME, // connected to the library
												// above
					IMPLibrary.REGISTRY_NAME); // also used
			result.getCompiledContext().getExceptionHandler().setEnabled(false);
			IMPJSGLRLibrary parseLibrary = ((IMPJSGLRLibrary) result
					.getContext().getOperatorRegistry(
							IMPJSGLRLibrary.REGISTRY_NAME));
			parseLibrary.addOverrides(result.getCompiledContext());

			IOAgent agent = prototype.getIOAgent();
			if (agent instanceof EditorIOAgent) {
				EditorIOAgent eioAgent = (EditorIOAgent) agent;
				result.setProjectpath(eioAgent.getProjectPath());
			}
			return result;
		}
	}

	private class LockableDebuggableHybridInterpreter extends
			DebuggableHybridInterpreter {

		public LockableDebuggableHybridInterpreter(ITermFactory termFactory) {
			super(termFactory);
		}

		@Override
		public boolean invoke(String name) throws InterpreterExit,
				InterpreterException {
			Environment.assertLock();
			return super.invoke(name);
		}

		@Override
		public void load(IStrategoTerm program) throws InterpreterException {
			Environment.assertLock();
			super.load(program);
		}

		@Override
		public IStrategoTerm current() {
			Environment.assertLock();
			return super.current();
		}
	}

	private class LockableHybridInterpreter extends HybridInterpreter {

		public LockableHybridInterpreter(ITermFactory termFactory) {
			super(termFactory);
		}

		@Override
		public boolean invoke(String name) throws InterpreterExit,
				InterpreterException {
			Environment.assertLock();
			return super.invoke(name);
		}

		@Override
		public void load(IStrategoTerm program) throws InterpreterException {
			Environment.assertLock();
			super.load(program);
		}

		@Override
		public IStrategoTerm current() {
			Environment.assertLock();
			return super.current();
		}
	}

}
