package org.strategoxt.imp.runtime.services;

import static org.spoofax.interpreter.terms.IStrategoTerm.*;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.*;

import java.io.IOException;

import lpg.runtime.IAst;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
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
	
	private final Interpreter interpreter;
	
	private final String feedbackFunction;
	
	private final AstMessageHandler messages = new AstMessageHandler();
	
	private boolean asyncExecuterEnqueued = false;
	
	public StrategoFeedback(Descriptor descriptor, Interpreter resolver, String feedbackFunction) {
		this.descriptor = descriptor;
		this.interpreter = resolver;
		this.feedbackFunction = feedbackFunction;
	}

	public final AnalysisRequired getAnalysisRequired() {
		return AnalysisRequired.TYPE_ANALYSIS;
	}
	
	public AstMessageHandler getMessages() {
		return messages;
	}

	/**
	 * Starts a new update() operation, asynchronously.
	 */
	public void asyncUpdate(final IParseController parseController, final IProgressMonitor monitor) {		
		synchronized (Environment.getSyncRoot()) {		
			// TODO: Properly integrate this asynchronous job into the Eclipse environment?
			
			if (!asyncExecuterEnqueued && feedbackFunction != null) {
				asyncExecuterEnqueued = true;
				
				Job job = new WorkspaceJob("Analyzing updated resource") {
					@Override
					public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
						synchronized (Environment.getSyncRoot()) {
							asyncExecuterEnqueued = false;
							update(parseController, monitor);
						}
						return Status.OK_STATUS;
					}
				};
				job.setRule(parseController.getProject().getResource());
				job.schedule();
			}			
		}
	}

	public void update(IParseController parseController, IProgressMonitor monitor) {
		synchronized (Environment.getSyncRoot()) {
			if (feedbackFunction != null) {
				ITermFactory factory = Environment.getTermFactory();
				IStrategoAstNode ast = (IStrategoAstNode) parseController.getCurrentAst();
				if (ast == null) return;

				Debug.startTimer("Invoking feedback strategy " + feedbackFunction);

				IStrategoTerm[] inputParts = {
						ast.getTerm(),
						factory.makeString(ast.getResourcePath().toOSString()),
						factory.makeString(ast.getRootPath().toOSString())
				};
				IStrategoTerm input = factory.makeTuple(inputParts);
				
				IStrategoTerm feedback = invoke(feedbackFunction, input, ast.getResourcePath().removeLastSegments(1));
				
				Debug.stopTimer("Completed feedback strategy " + feedbackFunction);
				String log = ((LoggingIOAgent) interpreter.getIOAgent()).getLog().trim();
				asyncPresentToUser(parseController, feedback, log);
			}
		}
	}
	
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

	private void presentToUser(IParseController parseController, IStrategoTerm feedback, String log) {
		messages.clearAllMarkers();

		if (feedback != null
				&& feedback.getTermType() == TUPLE
				&& termAt(feedback, 0).getTermType() == LIST
				&& termAt(feedback, 1).getTermType() == LIST
				&& termAt(feedback, 2).getTermType() == LIST) {
			
		    IStrategoList errors = termAt(feedback, 0);
		    IStrategoList warnings = termAt(feedback, 1);
		    feedbackToMarkers(parseController, errors, IMarker.SEVERITY_ERROR);
		    feedbackToMarkers(parseController, warnings, IMarker.SEVERITY_WARNING);
		} else if (feedback == null) {
			IResource resource = ((SGLRParseController) parseController).getResource();
			messages.addMarkerFirstLine(resource, "Analysis failed: " + log, IMarker.SEVERITY_ERROR);
		} else {
			IResource resource = ((SGLRParseController) parseController).getResource();
			messages.addMarkerFirstLine(resource, "Internal error - illegal output from " + feedbackFunction + ": " + feedback, IMarker.SEVERITY_ERROR);
		    Environment.logException("Illegal output from " + feedbackFunction + ": " + feedback);
		}
	}
	
	private final void feedbackToMarkers(IParseController parseController, IStrategoList feedbacks, int severity) {
	    for (IStrategoTerm feedback : feedbacks.getAllSubterms()) {
	        IStrategoTerm term = termAt(feedback, 0);
			IStrategoString message = termAt(feedback, 1);
			IResource resource = ((SGLRParseController) parseController).getResource();
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
					factory.makeString(node.getResourcePath().toOSString()),
					node.getTerm(),
					StrategoTermPath.createPath(node)
			};
			IStrategoTerm input = factory.makeTuple(inputParts);
			
			return invoke(function, input, node.getResourcePath().removeLastSegments(1));
		}
	}
	
	/**
	 * Invoke a Stratego function with a specific term its input,
	 * given a particular working directory.
	 * 
	 * @see #getAstNode(IStrategoTerm)  To retrieve the AST node associated with the resulting term.
	 */
	public IStrategoTerm invoke(String function, IStrategoTerm term, IPath workingDir) {
		synchronized (Environment.getSyncRoot()) {
		    Debug.startTimer();
		    boolean success;
			try {
				interpreter.setCurrent(term);
				initInterpreterPath(workingDir);
	
				((LoggingIOAgent) interpreter.getIOAgent()).clearLog();
				success = interpreter.invoke(function);

			} catch (InterpreterExit e) {
				success = e.getValue() == InterpreterExit.SUCCESS;
			} catch (InterpreterException e) {
				Environment.logException("Internal error evaluating function " + function, e);
				return null;
			}
			
			if (!success) return null;
			
			Debug.stopTimer("Invoked Stratego strategy " + function);
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
