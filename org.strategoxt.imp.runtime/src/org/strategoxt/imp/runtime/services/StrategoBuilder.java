package org.strategoxt.imp.runtime.services;

import static org.spoofax.interpreter.core.Tools.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.spoofax.interpreter.core.InterpreterErrorExit;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.InterpreterExit;
import org.spoofax.interpreter.core.UndefinedStrategyException;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.RuntimeActivator;
import org.strategoxt.imp.runtime.dynamicloading.TermReader;
import org.strategoxt.imp.runtime.stratego.StrategoConsole;
import org.strategoxt.imp.runtime.stratego.adapter.IStrategoAstNode;
import org.strategoxt.lang.Context;
import org.strategoxt.stratego_aterm.aterm_escape_strings_0_0;
import org.strategoxt.stratego_aterm.pp_aterm_box_0_0;
import org.strategoxt.stratego_gpp.box2text_string_0_1;
import org.strategoxt.stratego_lib.concat_strings_0_0;
import org.strategoxt.stratego_lib.try_1_0;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StrategoBuilder implements IBuilder {
	
	private final StrategoObserver observer;

	private final String caption;
	
	private final String builderRule;
	
	private final boolean realTime;
	
	private final boolean openEditor;
	
	private final boolean persistent;
	
	public StrategoBuilder(StrategoObserver observer, String caption, String builderRule, boolean openEditor, boolean realTime, boolean persistent) {
		this.observer = observer;
		this.caption = caption;
		this.builderRule = builderRule;
		this.openEditor = openEditor;
		this.realTime = realTime;
		this.persistent = persistent;
	}
	
	public String getCaption() {
		return caption;
	}
	
	public void execute(EditorState editor, IStrategoAstNode node, IFile errorReportFile, boolean isRebuild) {
		// TODO: refactor
		String filename = null;
		String result = null;
		String errorReport = null;
		
		synchronized (observer.getSyncRoot()) {
			try {
				if (node == null) {
					node = editor.getSelectionAst(true);
					if (node == null) node = editor.getParseController().getCurrentAst();
				}
				if (node == null) {
					openError(editor, "Editor is still analyzing");
					return;
				}
				
				IStrategoTerm resultTerm = observer.invoke(builderRule, node);
				if (resultTerm == null) {
					observer.reportRewritingFailed();
					Environment.logException("Builder failed:\n" + observer.getLog());
					if (!observer.isUpdateScheduled())
						observer.scheduleUpdate(editor.getParseController());
					openError(editor, "Builder failed (see error log)");
					return;
				}
		
				if (isTermAppl(resultTerm) && "None".equals(TermReader.cons(resultTerm))) {
					return;
				} else if (!isTermTuple(resultTerm) || !isTermString(termAt(resultTerm, 0))) {
					Environment.logException("Illegal builder result (must be a filename/string tuple)");
					openError(editor, "Illegal builder result (must be a filename/string tuple): " + resultTerm);
				}
	
				IStrategoTerm filenameTerm = termAt(resultTerm, 0);
				filename = asJavaString(filenameTerm);
				
				resultTerm = termAt(resultTerm, 1);
				resultTerm = try_1_0.instance.invoke(observer.getRuntime().getCompiledContext(),
						resultTerm, concat_strings_0_0.instance);
				
				if (resultTerm != null && filename != null) {
					result = isTermString(resultTerm) 
						? asJavaString(resultTerm)
						: ppATerm(resultTerm).stringValue();
				}
			} catch (InterpreterErrorExit e) {
				Environment.logException("Builder failed:\n" + observer.getLog(), e);
				if (editor.getDescriptor().isDynamicallyLoaded()) StrategoConsole.activateConsole();
				if (errorReportFile == null || !openEditor) {
					openError(editor, e.getMessage());
				} else {
					// UNDONE: Printing stack trace in editor
					// ByteArrayOutputStream trace = new ByteArrayOutputStream();
					// observer.getRuntime().getCompiledContext().printStackTrace(new PrintStream(trace), false);
					errorReport = e.getMessage();
					if (e.getTerm() != null) errorReport += "\n\t" + toEscapedString(ppATerm(e.getTerm()));
				}
			} catch (UndefinedStrategyException e) {
				reportGenericException(editor, e);
			} catch (InterpreterExit e) {
				reportGenericException(editor, e);
			} catch (InterpreterException e) {
				reportGenericException(editor, e);
			} catch (RuntimeException e) {
				reportGenericException(editor, e);
			}
		}

		try {
			if (errorReport != null) {
				setFileContents(editor, errorReportFile, errorReport);
			}
		
			if (result != null) {
				IFile file = editor.getProject().getRawProject().getFile(filename);
				setFileContents(editor, file, result);
				// TODO: if not persistent, create IEditorInput from result String
				if (openEditor && !isRebuild) {
					IEditorPart target = openEditor(file, realTime);
					if (!persistent) new File(file.getLocationURI()).delete();
					if (realTime)
						StrategoBuilderListener.addListener(editor.getEditor(), target, file, getCaption(), node);
				}
			}
		} catch (CoreException e) {
			Environment.logException("Builder failed", e);
			openError(editor, "Failed (see error log): " + e.getMessage());
		}
	}

	private IStrategoString ppATerm(IStrategoTerm term) {
		Context context = observer.getRuntime().getCompiledContext();
		term = aterm_escape_strings_0_0.instance.invoke(context, term);
		term = pp_aterm_box_0_0.instance.invoke(context, term);
		term = box2text_string_0_1.instance.invoke(context, term, Environment.getTermFactory().makeInt(120));
		return (IStrategoString) term;
	}

	private static String toEscapedString(IStrategoString term) {
		return Environment.getATermConverter().convert(term).toString();
	}

	private void reportGenericException(EditorState editor, Exception e) {
		boolean isDynamic = editor.getDescriptor().isDynamicallyLoaded();
		Environment.logException("Builder failed for " + (isDynamic ? "" : "non-") + "dynamically loaded editor", e);
		if (isDynamic) StrategoConsole.activateConsole();
		
		if (EditorState.isUIThread()) {
			// Only show if builder runs interactively (and not from the StrategoBuilderListener background builder)
			Status status = new Status(IStatus.ERROR, RuntimeActivator.PLUGIN_ID, e.getLocalizedMessage(), e);
			ErrorDialog.openError(editor.getEditor().getSite().getShell(), caption, null, status);
		}
	}
	
	private void openError(EditorState editor, String message) {
		Status status = new Status(IStatus.ERROR, RuntimeActivator.PLUGIN_ID, message);
		ErrorDialog.openError(editor.getEditor().getSite().getShell(),
				caption, null, status);
	}

	private void setFileContents(final EditorState editor, IFile file, final String contents) throws CoreException {
		assert !Thread.holdsLock(observer.getSyncRoot()) || Thread.holdsLock(Environment.getSyncRoot())
			: "Acquiring a resource lock can cause a deadlock";

		InputStream resultStream = new ByteArrayInputStream(contents.getBytes());
		if (file.exists()) {
			file.setContents(resultStream, true, true, null);
			
			/* TODO: update editor contents instead of file?
			if (editor.getEditor().getTitleImage().isDisposed()) {
				InputStream resultStream = new ByteArrayInputStream(contents.getBytes());
				file.setContents(resultStream, true, true, null);
				...save...
			} else {
				Job job = new UIJob("Update derived editor") {
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						try {
							editor.getDocument().set(contents);
							...save...
			                ...ensure listener knows updated time stamp...
						} catch (RuntimeException e) {
							Environment.logException("Could not update derived editor", e);
						}
						return Status.OK_STATUS;
					}
				};
				job.setSystem(true);
				job.schedule();
			}
			*/
		} else {
			file.create(resultStream, true, null);
			// UNDONE: file.setDerived(!persistent); // marks it as "derived" for life, even after editing...
		}
	}

	/**
	 * Opens or activates an editor.
	 * (Asynchronous) exceptions are swallowed and logged.
	 */
	private IEditorPart openEditor(IFile file, boolean realTime) throws PartInitException {
		assert !Thread.holdsLock(observer.getSyncRoot()) || Thread.holdsLock(Environment.getSyncRoot())
			: "Opening a new editor and acquiring a resource lock can cause a deadlock";
		
		// TODO: non-persistent editor: WorkBenchPage.openEditor with a custom IEditorInput?
		IWorkbenchPage page =
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		
		SidePaneEditorHelper sidePane = null;
		
		if (realTime) {
			try {
				sidePane = SidePaneEditorHelper.openSidePane();
			} catch (Throwable t) {
				// org.eclipse.ui.internal API might have changed
				Environment.logException("Unable to open side pane", t);
			}
		}
		
		IEditorPart result = null;
		try {
			result = IDE.openEditor(page, file, !realTime);
		} finally {
			if (result == null && sidePane != null) sidePane.undoOpenSidePane();
		}
		
		if (sidePane != null) sidePane.restoreFocus();
		
		return result;
	}
	
	@Override
	public String toString() {
		return "Builder: " + builderRule + " - " + caption; 
	}
}
