package org.strategoxt.imp.runtime.services;

import static org.spoofax.interpreter.terms.IStrategoTerm.*;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.*;

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
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.spoofax.interpreter.core.Interpreter;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.InterpreterExit;
import org.spoofax.interpreter.library.LoggingIOAgent;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;
import org.strategoxt.IncompatibleJarException;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.ISourceInfo;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.parser.ast.AstMessageHandler;
import org.strategoxt.imp.runtime.stratego.EditorIOAgent;
import org.strategoxt.imp.runtime.stratego.StrategoTermPath;
import org.strategoxt.imp.runtime.stratego.adapter.IStrategoAstNode;
import org.strategoxt.imp.runtime.stratego.adapter.WrappedAstNode;
import org.strategoxt.lang.StrategoException;

/**
 * Basic Stratego feedback (i.e., errors and warnings) provider.
 * This service may also be used as a basis for other semantic services
 * such as reference resolving.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StrategoFeedback implements IModelListener {
	
	private final Descriptor descriptor;
	
	private final String feedbackFunction;
	
	private final AstMessageHandler messages = new AstMessageHandler(AstMessageHandler.ANALYSIS_MARKER_TYPE);
	
	private final Object asyncUpdateSyncRoot = new Object();
	
	private HybridInterpreter runtime;
	
	private Job asyncLastBuildJob;
	
	private volatile boolean isUpdateStarted;
	
	public StrategoFeedback(Descriptor descriptor, String feedbackFunction) {
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
		Debug.stopTimer("Loaded feedback components");
		
		monitor.subTask(null);
	}

	private void initRuntime(IProgressMonitor monitor) {
		if (runtime == null) {
			runtime = Environment.createInterpreter();
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
		
		IStrategoTerm feedback;
		String log;
		
		synchronized (Environment.getSyncRoot()) {
			if (runtime == null)
				init(monitor);

			ITermFactory factory = Environment.getTermFactory();
			IStrategoAstNode ast = (IStrategoAstNode) parseController.getCurrentAst();
			if (ast == null || ast.getConstructor() == null) return;

			String path = ast.getSourceInfo().getPath().toOSString();
			String rootPath = ast.getSourceInfo().getProject().getRawProject().getLocation().toOSString();

			IStrategoTerm[] inputParts = {
					ast.getTerm(),
					factory.makeString(path),
					factory.makeString(rootPath)
			};
			IStrategoTerm input = factory.makeTuple(inputParts);
			
			feedback = invoke(feedbackFunction, input, ast.getSourceInfo());
			log = ((LoggingIOAgent) runtime.getIOAgent()).getLog().trim();
		}
		
		// TODO: figure out how this was supposed to be synchronized
		// synchronized (feedback) {
			if (!monitor.isCanceled())
				presentToUser((ISourceInfo) parseController, feedback, log);
		// }
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

	private void presentToUser(ISourceInfo sourceInfo, IStrategoTerm feedback, String log) {
		messages.clearAllMarkers();
		messages.clearMarkers(sourceInfo.getResource());

		if (feedback != null
				&& feedback.getTermType() == TUPLE
				&& termAt(feedback, 0).getTermType() == LIST
				&& termAt(feedback, 1).getTermType() == LIST
				&& termAt(feedback, 2).getTermType() == LIST) {
			
		    IStrategoList errors = termAt(feedback, 0);
		    IStrategoList warnings = termAt(feedback, 1);
		    IStrategoList notes = termAt(feedback, 2);
		    feedbackToMarkers(sourceInfo, errors, IMarker.SEVERITY_ERROR);
		    feedbackToMarkers(sourceInfo, warnings, IMarker.SEVERITY_WARNING);
		    feedbackToMarkers(sourceInfo, notes, IMarker.SEVERITY_INFO);
		} else if (feedback == null) {
			// Note that this condition may also be reached when
			// the semantic service hasn't been loaded yet
			IResource resource = ((SGLRParseController) sourceInfo).getResource();
			if (log.length() == 0) log = "(see error log)"; // TODO: report or throw last exception if strategy not found etc.
			messages.addMarkerFirstLine(resource, "Analysis failed: " + log, IMarker.SEVERITY_ERROR);
		} else {
			// Throw an exception to trigger an Eclipse pop-up  
			throw new StrategoException("Illegal output from " + feedbackFunction + " (should be (errors,warnings,notes) tuple: " + feedback);
		}
	}
	
	private final void feedbackToMarkers(ISourceInfo sourceInfo, IStrategoList feedbacks, int severity) {
	    for (IStrategoTerm feedback : feedbacks.getAllSubterms()) {
	    	if (feedback.getSubtermCount() != 2 || feedback.getSubterm(1).getTermType() != STRING) {
				// Throw an exception to trigger an Eclipse pop-up  
	    		throw new StrategoException("Illegal feedback result (must be a term/message tuple): " + feedback);
	    	}
	        IStrategoTerm term = termAt(feedback, 0);
			IStrategoString message = termAt(feedback, 1);
			IResource resource = sourceInfo.getResource();
			messages.addMarker(resource, term, message.stringValue(), severity);
	    }
	}	
	
	/**
	 * Invoke a Stratego function with a specific AST node as its input.
	 * 
	 * @see #getAstNode(IStrategoTerm)  To retrieve the AST node associated with the resulting term.
	 */
	public IStrategoTerm invoke(String function, IStrategoAstNode node) {
		synchronized (Environment.getSyncRoot()) {
			ITermFactory factory = Environment.getTermFactory();
			IStrategoTerm[] inputParts = {
					node.getTerm(),
					StrategoTermPath.createPath(node),
					getRoot(node).getTerm(),
					factory.makeString(node.getSourceInfo().getPath().toOSString()),
					factory.makeString(node.getSourceInfo().getProject().getRawProject().getLocation().toOSString())
			};
			IStrategoTerm input = factory.makeTuple(inputParts);
			
			return invoke(function, input, node.getSourceInfo());
		}
	}
	
	/**
	 * Invoke a Stratego function with a specific term its input,
	 * given a particular working directory.
	 * 
	 * @see #getAstNode(IStrategoTerm)  To retrieve the AST node associated with the resulting term.
	 */
	public IStrategoTerm invoke(String function, IStrategoTerm term, ISourceInfo sourceInfo) {
		if (runtime == null)
			return null;
		
		synchronized (Environment.getSyncRoot()) {
		    Debug.startTimer();
		    boolean success;
			try {
				// TODO: Make Context support monitor.isCanceled()?
				//       (e.g., overriding Context.lookupPrimitive to throw an OperationCanceledException) 
				
				runtime.setCurrent(term);
				initInterpreterPath(sourceInfo.getPath().removeLastSegments(1));
	
				((LoggingIOAgent) runtime.getIOAgent()).clearLog();
				success = runtime.invoke(function);

			} catch (InterpreterExit e) {
				// (source marker should be added by invoking method) 
				Environment.logException("Runtime exited when evaluating strategy " + function, e);
				// Successful exit code or not, we needed to return a result term
				return null;
			} catch (InterpreterException e) {
				// TODO: for asyncUpdate(), programmer errors like these should just be thrown, triggering a pop-up
				// (source marker should be added by invoking method) 
				if (runtime.getContext().getVarScope().lookupSVar(Interpreter.cify(function)) == null) {
					Environment.logException("Strategy does not exist: " + function, e);
				} else {
					Environment.logException("Internal error evaluating strategy " + function, e);
				}
				return null;
			} catch (RuntimeException e) {
				// (source marker should be added by invoking method) 
				Environment.logException("Internal error evaluating strategy " + function, e);
				return null;
			}
			
			Debug.stopTimer("Evaluated strategy " + function + (success ? "" : " (failed)"));
			if (!success) return null;
			return runtime.current();
		}
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

}
