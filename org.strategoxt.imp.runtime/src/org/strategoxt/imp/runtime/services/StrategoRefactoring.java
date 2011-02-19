package org.strategoxt.imp.runtime.services;

import static org.spoofax.interpreter.core.Tools.asJavaString;
import static org.spoofax.interpreter.core.Tools.isTermAppl;
import static org.spoofax.interpreter.core.Tools.isTermTuple;
import static org.spoofax.interpreter.core.Tools.termAt;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.hasImploderOrigin;
import static org.strategoxt.imp.runtime.stratego.SourceAttachment.getResource;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CancellationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.BadLocationException;
import org.spoofax.interpreter.core.InterpreterErrorExit;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.InterpreterExit;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.core.UndefinedStrategyException;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.generator.construct_textual_change_1_1;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.MonitorStateWatchDog;
import org.strategoxt.imp.runtime.RuntimeActivator;
import org.strategoxt.imp.runtime.dynamicloading.TermReader;
import org.strategoxt.imp.runtime.stratego.StrategoConsole;
import org.strategoxt.imp.runtime.stratego.StrategoTermPath;
import org.strategoxt.imp.runtime.stratego.TextChangePrimitive;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

/**
 * @author Maartje
 */
public class StrategoRefactoring implements IBuilder { //TODO extract "AbstractStrategoBuilder"
	
	private final String ppTable;
	
	private final String ppStrategy;
	
	private final StrategoObserver observer;

	private final String caption;
	
	private String builderRule;
			
	private final IResource resource;
	
	// Since StrategoRefactorings are not persistent (per constructor of BuilderFactory)
	// we maintain a map with running jobs in a static field
	private static Map<String, Job> activeJobs = new WeakHashMap<String, Job>();
	
	private final boolean cursor;
	
	private final boolean source;
	
	/**
	 * Creates a new Stratego refactoring.
	 */
	public StrategoRefactoring(StrategoObserver observer, String caption, String builderRule,
			boolean cursor, boolean source,
			String ppTable, String ppStrategy,
			IResource resource) { //TODO Check if the refactoring is defined for the given Sort-Constructor
		this.cursor=cursor;
		this.source=source;
		this.ppTable=ppTable;
		this.ppStrategy=ppStrategy;
		this.observer = observer;
		this.caption = caption;
		this.builderRule = builderRule;
		this.resource=resource;
	}
	
	public String getCaption() {
		return caption;
	}
	
	public Object getData() {
		// Data not used for normal builders
		return null;
	}
	
	public void setData(Object data) {
		// Data not used for normal builders
	}
	
	public String getBuilderRule() {
		return builderRule;
	}
	
	protected StrategoObserver getObserver() {
		return observer;
	}
		
	protected void setBuilderRule(String builderRule) {
		this.builderRule = builderRule;
	}
	
	public Job scheduleExecute(final EditorState editor, IStrategoTerm node,
			final IFile errorReportFile, final boolean isRebuild) {
		
		String displayCaption = caption.endsWith("...")
			? caption.substring(caption.length() - 3)
			: caption;
		
		Job lastJob = activeJobs.get(caption);
		if (lastJob != null && lastJob.getState() != Job.NONE) {
			if (!isRebuild)
				openError(editor, "Already running: " + displayCaption);
			return null;
		}
		
		if (node == null) {
			node = editor.getSelectionAst(!cursor);
			if (node == null) node = editor.getParseController().getCurrentAst();
		}
		
		final IStrategoTerm node2 = node;
			
		Job job = new Job("Executing " + displayCaption) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				MonitorStateWatchDog protector = new MonitorStateWatchDog(this, monitor, observer);
				try {
					execute(editor, node2, errorReportFile, isRebuild);
					return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
				} finally {
					protector.endProtect();
				}
			}
		};
		job.setUser(true);
		job.schedule();
		activeJobs.put(caption, job);
		return job;
	}
	
	private void execute(EditorState editor, IStrategoTerm node, IFile errorReportFile, boolean isRebuild) {
		IStrategoTerm builderResult=null;
		IStrategoTerm textReplaceTerm=null;
		//IFile file = null;

		String resultText = null;
		int startLocation = -1;
		int endLocation = -1;

		observer.getLock().lock();
		try {
			try {
				builderResult = getBuilderResult(editor, node);
				if(builderResult!=null){	
					if (isInvalidResultTerm(builderResult)) { //TODO: multifile support [(fname, oldnode, newnode), ...]
						Environment.logException("Illegal refactoring result (must be a tuple '(original-node, newnode)')");
						openError(editor, "Illegal refactoring result (must be a tuple '(original-node, newnode)': )" + builderResult);
						return;
					}
					textReplaceTerm=getTextReplacement(builderResult);
					if(textReplaceTerm==null){
						observer.reportRewritingFailed();
						Environment.logException("Text-reconstruction unexpectedly fails, did you specify a valid pp-table?: \n"+ observer.getLog());
						if (!observer.isUpdateScheduled())
							observer.scheduleUpdate(editor.getParseController());
						return;
					}
					//file = ...;
					startLocation=Tools.asJavaInt(termAt(textReplaceTerm, 0));
					endLocation=Tools.asJavaInt(termAt(textReplaceTerm, 1));
					resultText = asJavaString(termAt(textReplaceTerm, 2));
					try {		
						TextChangePrimitive.applyTextChange(editor, startLocation, endLocation, resultText); //TODO refactor text-change handling (in files)
					} catch (BadLocationException e) {
						reportGenericException(editor, e);
					}
				}					
			} catch (InterpreterErrorExit e) {
				reportGenericException(editor, e);
			} catch (UndefinedStrategyException e) {
				reportGenericException(editor, e);
			} catch (InterpreterExit e) {
				reportGenericException(editor, e);
			} catch (InterpreterException e) {
				reportGenericException(editor, e);
			} catch (CancellationException e) {
				return;
			} catch (RuntimeException e) {
				reportGenericException(editor, e);
			} catch (Error e) {
				reportGenericException(editor, e);
			}			
			
		} finally {
			observer.getLock().unlock();
		}		
	}

	private IStrategoTerm getBuilderResult(EditorState editor, IStrategoTerm node)
			throws UndefinedStrategyException, InterpreterErrorExit, InterpreterExit,
			InterpreterException {
		IStrategoTerm resultTerm;
		if (node == null) {
			openError(editor, "Editor is still analyzing");
			return null;
		}
		resultTerm = invokeObserver(node);
		if (resultTerm == null) {
			observer.reportRewritingFailed();
			Environment.logException("Builder failed:\n" + observer.getLog());
			if (!observer.isUpdateScheduled())
				observer.scheduleUpdate(editor.getParseController());
			openError(editor, "Builder failed (see error log)");
			return null;
		}
		if (isTermAppl(resultTerm) && "None".equals(TermReader.cons(resultTerm))) {
			return null;
		} 
		return resultTerm;
	}

	private boolean isInvalidResultTerm(IStrategoTerm resultTerm) {
		return 
			!isTermTuple(resultTerm) || 
			!hasImploderOrigin(termAt(resultTerm, 0)) ||
			resultTerm.getSubtermCount()!=2;
	}
	
	private IStrategoTerm getTextReplacement(IStrategoTerm resultTuple) {
		IStrategoTerm ppTableTerm;
		if (ppTable == null)
			ppTableTerm=observer.getRuntime().getCompiledContext().getFactory().makeString("");
		else {
			ppTableTerm = observer.invokeSilent(ppTable, null, resource);
		}
		IStrategoTerm textreplace=construct_textual_change_1_1.instance.invoke(
				observer.getRuntime().getCompiledContext(), 
				resultTuple, 
				new Strategy() {
					@Override
					public IStrategoTerm invoke(Context context, IStrategoTerm current) {
						if (ppStrategy!=null)
							return observer.invokeSilent(ppStrategy, current, resource);
						return null;
					}
				},
				ppTableTerm
			);
		return textreplace;
	}

	protected IStrategoTerm invokeObserver(IStrategoTerm node) throws UndefinedStrategyException,
			InterpreterErrorExit, InterpreterExit, InterpreterException {
		node = StrategoTermPath.getMatchingAncestor(node, false);
		IStrategoTerm inputTerm = observer.getInputBuilder().makeInputTerm(node, true, source);
		IStrategoTerm result = observer.invoke(builderRule, inputTerm, getResource(node));
		return result;
	}

	private void reportGenericException(EditorState editor, Throwable e) {
		boolean isDynamic = editor.getDescriptor().isDynamicallyLoaded();
		Environment.logException("Builder failed for " + (isDynamic ? "" : "non-") + "dynamically loaded editor", e);
		if (isDynamic) {
			Writer writer = observer.getRuntime().getIOAgent().getWriter(IOAgent.CONST_STDERR);
			PrintWriter printWriter = new PrintWriter(writer);
			e.printStackTrace(printWriter);
			printWriter.flush();
			StrategoConsole.activateConsole();
		}
		
		if (EditorState.isUIThread()) {
			// Only show if builder runs interactively (and not from the StrategoBuilderListener background builder)
			String message = e.getLocalizedMessage() == null ? e.getMessage() : e.getLocalizedMessage();
			Status status = new Status(IStatus.ERROR, RuntimeActivator.PLUGIN_ID, message, e);
			ErrorDialog.openError(editor.getEditor().getSite().getShell(), caption, null, status);
		}
	}
	
	private void openError(EditorState editor, String message) {
		try {
			Status status = new Status(IStatus.ERROR, RuntimeActivator.PLUGIN_ID, message);
			ErrorDialog.openError(editor.getEditor().getSite().getShell(),
					caption, null, status);
		} catch (RuntimeException e) {
			Environment.logException("Problem reporting error: " + message);
		}
	}
	
	@Override
	public String toString() {
		return "Refactoring: " + builderRule + " - " + caption; 
	}
}
