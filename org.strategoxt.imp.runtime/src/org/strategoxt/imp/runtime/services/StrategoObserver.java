package org.strategoxt.imp.runtime.services;

import static org.spoofax.interpreter.core.Tools.*;
import static org.spoofax.interpreter.terms.IStrategoTerm.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import lpg.runtime.IAst;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.spoofax.interpreter.core.InterpreterErrorExit;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.InterpreterExit;
import org.spoofax.interpreter.core.StackTracer;
import org.spoofax.interpreter.core.UndefinedStrategyException;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.library.LoggingIOAgent;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;
import org.strategoxt.IncompatibleJarException;
import org.strategoxt.imp.generator.postprocess_feedback_results_0_0;
import org.strategoxt.imp.generator.sdf2imp;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.parser.ast.AstMessageHandler;
import org.strategoxt.imp.runtime.stratego.EditorIOAgent;
import org.strategoxt.imp.runtime.stratego.StrategoConsole;
import org.strategoxt.imp.runtime.stratego.StrategoTermPath;
import org.strategoxt.imp.runtime.stratego.adapter.IStrategoAstNode;
import org.strategoxt.imp.runtime.stratego.adapter.WrappedAstNode;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.StrategoException;
import org.strategoxt.stratego_lib.set_config_0_0;

/**
 * Basic Stratego feedback (i.e., errors and warnings) provider.
 * This service may also be used as a basis for other semantic services
 * such as reference resolving.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StrategoObserver implements IModelListener {
	
	private final Descriptor descriptor;
	
	private final String feedbackFunction;
	
	private final AstMessageHandler messages = new AstMessageHandler(AstMessageHandler.ANALYSIS_MARKER_TYPE);
	
	private final Object asyncUpdateSyncRoot = new Object();
	
	private HybridInterpreter runtime;
	
	private Job asyncLastBuildJob;
	
	private volatile boolean isUpdateStarted;
	
	public StrategoObserver(Descriptor descriptor, String feedbackFunction) {
		this.descriptor = descriptor;
		this.feedbackFunction = feedbackFunction;
	}

	public final AnalysisRequired getAnalysisRequired() {
		return AnalysisRequired.TYPE_ANALYSIS;
	}
	
	/**
	 * Returns a value indicating whether or not an analysis has
	 * been scheduled or completed at this point.
	 * 
	 * @return true if update() or asyncUpdate() have been called.
	 */
	public boolean isUpdateStarted() {
		return isUpdateStarted;
	}
	
	public AstMessageHandler getMessages() {
		return messages;
	}

	public String getLog() {
		return ((EditorIOAgent) runtime.getIOAgent()).getLog().trim();
	}
	
	private void init(IProgressMonitor monitor) {
		monitor.subTask("Instantiating analysis runtime");
		
		Debug.startTimer();
		List<String> jars = new ArrayList<String>();
		
		for (File file : descriptor.getAttachedFiles()) {
			String filename = file.toString();
			if (filename.endsWith(".ctree")) {
				initRuntime(monitor);
				loadCTree(filename);
			} else if (filename.endsWith(".jar")) {
				initRuntime(monitor);
				jars.add(filename);
			}
		}
		
		loadJars(jars);
		Debug.stopTimer("Loaded analysis components");
		
		monitor.subTask(null);
	}

	private void initRuntime(IProgressMonitor monitor) {
		if (runtime == null) {
			Debug.startTimer();
			runtime = Environment.createInterpreter();
			runtime.init();
			Debug.stopTimer("Created new Stratego runtime instance");
			try {
				ITermFactory factory = runtime.getFactory();
				IStrategoTuple programName = factory.makeTuple(
						factory.makeString("program"),
						factory.makeString(descriptor.getLanguage().getName()));
				set_config_0_0.instance.invoke(runtime.getCompiledContext(), programName);
			} catch (BadDescriptorException e) {
				// Ignore
			}
			monitor.subTask("Loading analysis runtime components");
		}
	}

	private void loadCTree(String filename) {
		try {
			Debug.startTimer("Loading Stratego module ", filename);
			synchronized (Environment.getSyncRoot()) {
				runtime.load(descriptor.openAttachment(filename));
			}
			Debug.stopTimer("Successfully loaded " +  filename);
		} catch (InterpreterException e) {
			Environment.logException(new BadDescriptorException("Error loading compiler service provider " + filename, e));
		} catch (IOException e) {
			Environment.logException(new BadDescriptorException("Could not load compiler service provider " + filename, e));
		}
	}
	
	private void loadJars(List<String> jars) {
		try {
			URL[] classpath = new URL[jars.size()];
			for (int i = 0; i < classpath.length; i++) {
				classpath[i] = descriptor.getBasePath().append(jars.get(i)).toFile().toURL();
			}
			runtime.loadJars(classpath);
		} catch (SecurityException e) {
			Environment.logException("Error loading compiler service providers " + jars, e);
		} catch (IncompatibleJarException e) {
			Environment.logException("Error loading compiler service providers " + jars, e);
		} catch (IOException e) {
			Environment.logException("Error loading compiler service providers " + jars, e);
		}
		/*
		try {
			URL[] classpath = new URL[jars.size()];
			for (int i = 0; i < classpath.length; i++) {
				classpath[i] = descriptor.getBasePath().append(jars.get(i)).toFile().toURL();
			}
			
			ClassLoader loader = new URLClassLoader(classpath, libstratego_lib.class.getClassLoader());
			Class<?> mainClass = loader.loadClass("trans.Main");
			Method registerer = mainClass.getMethod("registerInterop", IContext.class, Context.class);
			registerer.invoke(null, runtime.getContext(), runtime.getCompiledContext());
		
		} catch (Exception e) {
			Environment.logException("Error loading compiler service providers " + jars, e);
		}
		*/
	}

	/**
	 * Starts a new update() operation, asynchronously.
	 */
	public void asyncUpdate(final IParseController parseController) {
		isUpdateStarted = true;
		
		synchronized (asyncUpdateSyncRoot) {
			if (asyncLastBuildJob != null)
				asyncLastBuildJob.cancel();
			
			asyncLastBuildJob = new WorkspaceJob("Analyzing updated resource") {
				@Override
				public IStatus runInWorkspace(IProgressMonitor monitor) {
					monitor.beginTask("", IProgressMonitor.UNKNOWN);
					update(parseController, monitor);
					return Status.OK_STATUS;
				}
			};
			asyncLastBuildJob.setRule(parseController.getProject().getResource());
			asyncLastBuildJob.schedule();
		}
	}

	public void update(IParseController parseController, IProgressMonitor monitor) {
		isUpdateStarted = true;
		
		if (feedbackFunction == null || monitor.isCanceled())
			return;
		
		IStrategoAstNode ast = (IStrategoAstNode) parseController.getCurrentAst();
		IStrategoTerm feedback = null;
		
		synchronized (Environment.getSyncRoot()) {
			if (runtime == null)
				init(monitor);

			if (ast == null || ast.getConstructor() == null)
				return;
			
			feedback = invokeSilent(feedbackFunction, ast.getResource(), makeInputTerm(ast, false));
		}

		if (feedback == null) {
			reportRewritingFailed();
			String log = getLog();
			Environment.logException(log.length() == 0 ? "Analysis failed" : "Analysis failed:\n" + log);
			messages.clearMarkers(ast.getResource());
			messages.addMarkerFirstLine(ast.getResource(), "Analysis failed (see error log)", IMarker.SEVERITY_ERROR);
			messages.commitChanges();
		} else if (!monitor.isCanceled()) {
			// TODO: figure out how this was supposed to be synchronized
			// synchronized (feedback) {
			presentToUser(ast.getResource(), feedback);
			// }
		}
	}

	public void reportRewritingFailed() {
		StackTracer trace = runtime.getContext().getStackTracer();
		runtime.getIOAgent().getOutputStream(IOAgent.CONST_STDERR).println(
				trace.getTraceDepth() != 0 ? "rewriting failed, trace:" : "rewriting failed");
		trace.printStackTrace();
		if (descriptor.isDynamicallyLoaded())
			StrategoConsole.activateConsole();
	}
	
	/* UNDONE: asynchronous feedback presentation
	private void asyncPresentToUser(final IParseController parseController, final IStrategoTerm feedback, final String log) {
		Job job = new WorkspaceJob("Showing feedback") {
			{ setSystem(true); } // don't show to user
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				presentToUser(parseController, feedback, log);
				return Status.OK_STATUS;
			}
		};
		
		job.setRule(parseController.getProject().getResource());
		job.schedule();
	}
	*/

	private void presentToUser(IResource resource, IStrategoTerm feedback) {
		assert feedback != null;
		// UNDONE: messages.clearAllMarkers();
		// TODO: use tracking io agent to find out what to clear
		messages.clearMarkers(resource);

		try {
			if (feedback.getTermType() == TUPLE
					&& termAt(feedback, 0).getTermType() == LIST
					&& termAt(feedback, 1).getTermType() == LIST
					&& termAt(feedback, 2).getTermType() == LIST) {
				
			    IStrategoList errors = termAt(feedback, 0);
			    IStrategoList warnings = termAt(feedback, 1);
			    IStrategoList notes = termAt(feedback, 2);
			    feedbackToMarkers(resource, errors, IMarker.SEVERITY_ERROR);
			    feedbackToMarkers(resource, warnings, IMarker.SEVERITY_WARNING);
			    feedbackToMarkers(resource, notes, IMarker.SEVERITY_INFO);
			} else {
				// Throw an exception to trigger an Eclipse pop-up  
				throw new StrategoException("Illegal output from " + feedbackFunction + " (should be (errors,warnings,notes) tuple: " + feedback);
			}
		} finally {
			messages.commitChanges();
		}
	}
	
	private final void feedbackToMarkers(IResource resource, IStrategoList feedbacks, int severity) {
		Context context = runtime.getCompiledContext();
		sdf2imp.init(context);
		feedbacks = (IStrategoList) postprocess_feedback_results_0_0.instance.invoke(context, feedbacks);
		
	    for (IStrategoTerm feedback : feedbacks.getAllSubterms()) {
	        IStrategoTerm term = termAt(feedback, 0);
			IStrategoString messageTerm = termAt(feedback, 1);
			String message = AnnotationHover.convertToHTMLContent(messageTerm.stringValue());
			
			messages.addMarker(resource, term, message, severity);
	    }
	}	
	
	/**
	 * Invoke a Stratego function with a specific AST node as its input.
	 * 
	 * @see #getAstNode(IStrategoTerm)  To retrieve the AST node associated with the resulting term.
	 */
	public IStrategoTerm invoke(String function, IStrategoAstNode node)
			throws UndefinedStrategyException, InterpreterErrorExit, InterpreterExit, InterpreterException {

		IStrategoTerm input = makeInputTerm(node, true);
		return invoke(function, input, node.getResource());
	}

	private static IStrategoTerm makeInputTerm(IStrategoAstNode node, boolean includeSubNode) {
		ITermFactory factory = Environment.getTermFactory();
		String path = node.getResource().getProjectRelativePath().toPortableString();
		String absolutePath = node.getResource().getProject().getLocation().toOSString();
		
		if (includeSubNode) {
			IStrategoTerm[] inputParts = {
					node.getTerm(),
					StrategoTermPath.createPath(node),
					getRoot(node).getTerm(),
					factory.makeString(path),
					factory.makeString(absolutePath)
				};
			return factory.makeTuple(inputParts);
		} else {
			IStrategoTerm[] inputParts = {
					node.getTerm(),
					factory.makeString(path),
					factory.makeString(absolutePath)
				};
			return factory.makeTuple(inputParts);
		}
	}
	
	/**
	 * Invoke a Stratego function with a specific term its input,
	 * given a particular working directory.
	 */
	public IStrategoTerm invoke(String function, IStrategoTerm term, IResource resource)
			throws UndefinedStrategyException, InterpreterErrorExit, InterpreterExit, InterpreterException {
		
		synchronized (Environment.getSyncRoot()) {
			if (runtime == null) init(new NullProgressMonitor());
			if (runtime == null) return null;
			
		    Debug.startTimer();
			// TODO: Make Context support monitor.isCanceled()?
			//       (e.g., overriding Context.lookupPrimitive to throw an OperationCanceledException) 
			
			runtime.setCurrent(term);
			IPath path = resource.getLocation();
			initInterpreterPath(path.removeLastSegments(1));

			((LoggingIOAgent) runtime.getIOAgent()).clearLog();
			boolean success = runtime.invoke(function);
			
			Debug.stopTimer("Evaluated strategy " + function + (success ? "" : " (failed)"));
			return success ? runtime.current() : null;
		}
	}

	/**
	 * Invoke a Stratego function with a specific AST node as its input,
	 * logging and swallowing all exceptions.
	 * 
	 * @see #getAstNode(IStrategoTerm)  To retrieve the AST node associated with the resulting term.
	 */
	public IStrategoTerm invokeSilent(String function, IStrategoAstNode node) {
		return invokeSilent(function, node.getResource(), makeInputTerm(node, true));
	}
	
	/**
	 * Invoke a Stratego function with a specific term its input,
	 * given a particular working directory.
	 * Logs and swallows all exceptions.
	 */
	public IStrategoTerm invokeSilent(String function, IResource resource, IStrategoTerm input) {
		IStrategoTerm result = null;
		
		try {
			result = invoke(function, input, resource);
		} catch (InterpreterExit e) {
			if (descriptor.isDynamicallyLoaded()) StrategoConsole.activateConsole();
			messages.clearMarkers(resource);
			messages.addMarkerFirstLine(resource, "Analysis failed (see error log)", IMarker.SEVERITY_ERROR);
			messages.commitChanges();
			Environment.logException("Runtime exited when evaluating strategy " + function, e);
		} catch (UndefinedStrategyException e) {
			// Note that this condition may also be reached when the semantic service hasn't been loaded yet
			Environment.logException("Strategy does not exist: " + function, e);
		} catch (InterpreterException e) {
			Environment.logException("Internal error evaluating strategy " + function, e);
			if (descriptor.isDynamicallyLoaded()) StrategoConsole.activateConsole();
		} catch (RuntimeException e) {
			Environment.logException("Internal error evaluating strategy " + function, e);
			if (descriptor.isDynamicallyLoaded()) StrategoConsole.activateConsole();
		}
		
		return result;
	}

	public IAst getAstNode(IStrategoTerm term) {
		if (term == null) return null;
			
		if (term instanceof WrappedAstNode) {
			return ((WrappedAstNode) term).getNode();
		} else {
			Environment.logException("Resolved reference is not associated with an AST node " + runtime.current());
			return null;
		}
	}
	
	private void initInterpreterPath(IPath workingDir) {
		try {
			runtime.getIOAgent().setWorkingDir(workingDir.toOSString());
			((EditorIOAgent) runtime.getIOAgent()).setDescriptor(descriptor);
		} catch (IOException e) {
			Environment.logException("Could not set Stratego working directory", e);
			throw new RuntimeException(e);
		}
	}
	
	private static IStrategoAstNode getRoot(IStrategoAstNode node) {
		while (node.getParent() != null)
			node = node.getParent();
		return node;
	}
	
	public HybridInterpreter getRuntime() {
		return runtime;
	}

}
