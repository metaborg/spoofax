package org.strategoxt.imp.runtime.services;

import static org.spoofax.interpreter.terms.IStrategoTerm.*;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.*;

import java.io.File;
import java.io.IOException;

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
	
	private Interpreter interpreter;
	
	private Job asyncLastBuildJob;
	
	public StrategoFeedback(Descriptor descriptor, String feedbackFunction) {
		this.descriptor = descriptor;
		this.feedbackFunction = feedbackFunction;
	}

	public final AnalysisRequired getAnalysisRequired() {
		return AnalysisRequired.TYPE_ANALYSIS;
	}
	
	public AstMessageHandler getMessages() {
		return messages;
	}
	
	private void init(IProgressMonitor monitor) {
		monitor.subTask("Instantiating analysis runtime");
		
		for (File file : descriptor.getAttachedFiles()) {
			String filename = file.toString();
			if (filename.endsWith(".ctree")) {
				if (interpreter == null) {
					try {
						interpreter = Environment.createInterpreter();
					} catch (Exception e) {
						Environment.logException("Could not create interpreter", e);
					}
					monitor.subTask("Loading analysis runtime components");
				}
				try {
					Debug.startTimer("Loading Stratego module ", filename);
					Environment.addToInterpreter(interpreter, descriptor.openAttachment(filename));
					Debug.stopTimer("Successfully loaded " +  filename);
				} catch (InterpreterException e) {
					Environment.logException(new BadDescriptorException("Error loading compiler service provider " + filename, e));
				} catch (IOException e) {
					Environment.logException(new BadDescriptorException("Could not load compiler service provider" + filename, e));
				}
			}
		}
		
		monitor.subTask(null);
	}

	/**
	 * Starts a new update() operation, asynchronously.
	 */
	public void asyncUpdate(final IParseController parseController, final IProgressMonitor monitor) {		
		synchronized (this) {
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
		if (feedbackFunction == null || monitor.isCanceled())
			return;
		
		IStrategoTerm feedback;
		String log;
		
		synchronized (Environment.getSyncRoot()) {
			if (interpreter == null)
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
			log = ((LoggingIOAgent) interpreter.getIOAgent()).getLog().trim();
		}
		
		if (!monitor.isCanceled())
			presentToUser((ISourceInfo) parseController, feedback, log);
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

		if (feedback != null
				&& feedback.getTermType() == TUPLE
				&& termAt(feedback, 0).getTermType() == LIST
				&& termAt(feedback, 1).getTermType() == LIST
				&& termAt(feedback, 2).getTermType() == LIST) {
			
		    IStrategoList errors = termAt(feedback, 0);
		    IStrategoList warnings = termAt(feedback, 1);
		    feedbackToMarkers(sourceInfo, errors, IMarker.SEVERITY_ERROR);
		    feedbackToMarkers(sourceInfo, warnings, IMarker.SEVERITY_WARNING);
		} else if (feedback == null) {
			IResource resource = ((SGLRParseController) sourceInfo).getResource();
			messages.addMarkerFirstLine(resource, "Analysis failed: " + log, IMarker.SEVERITY_ERROR);
		} else {
			IResource resource = ((SGLRParseController) sourceInfo).getResource();
			messages.addMarkerFirstLine(resource, "Internal error - illegal output from " + feedbackFunction + ": " + feedback, IMarker.SEVERITY_ERROR);
		    Environment.logException("Illegal output from " + feedbackFunction + ": " + feedback);
		}
	}
	
	private final void feedbackToMarkers(ISourceInfo sourceInfo, IStrategoList feedbacks, int severity) {
	    for (IStrategoTerm feedback : feedbacks.getAllSubterms()) {
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
					getRoot(node).getTerm(),
					factory.makeString(node.getSourceInfo().getPath().toOSString()),
					node.getTerm(),
					StrategoTermPath.createPath(node)
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
		synchronized (Environment.getSyncRoot()) {
		    Debug.startTimer();
		    boolean success;
			try {
				// TODO: Make interpreter support monitor.isCanceled()?
				//       (e.g., overriding Context.lookupSVar to throw an OperationCanceledException) 
				
				interpreter.setCurrent(term);
				initInterpreterPath(sourceInfo.getPath().removeLastSegments(1));
	
				((LoggingIOAgent) interpreter.getIOAgent()).clearLog();
				success = interpreter.invoke(function);

			} catch (InterpreterExit e) {
				success = e.getValue() == InterpreterExit.SUCCESS;
			} catch (InterpreterException e) {
				// (source marker should be added by invoking method) 
				Environment.logException("Internal error evaluating strategy " + function, e);
				return null;
			} catch (RuntimeException e) {
				// (source marker should be added by invoking method) 
				Environment.logException("Internal error evaluating strategy " + function, e);
				return null;
			}
			
			if (!success) return null;
			
			Debug.stopTimer("Evaluated strategy " + function);
			return interpreter.current();
		}
	}

	public IAst getAstNode(IStrategoTerm term) {
		if (term == null) return null;
			
		if (term instanceof WrappedAstNode) {
			return ((WrappedAstNode) term).getNode();
		} else {
			Environment.logException("Resolved reference is not associated with an AST node " + interpreter.current());
			return null;
		}
	}
	
	private void initInterpreterPath(IPath workingDir) {
		try {
			interpreter.getIOAgent().setWorkingDir(workingDir.toOSString());
			((EditorIOAgent) interpreter.getIOAgent()).setDescriptor(descriptor);
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
