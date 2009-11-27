package org.strategoxt.imp.runtime.services;

import static org.spoofax.interpreter.core.Tools.*;

import java.io.ByteArrayInputStream;
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
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.RuntimeActivator;
import org.strategoxt.imp.runtime.dynamicloading.TermReader;
import org.strategoxt.imp.runtime.stratego.StrategoConsole;
import org.strategoxt.imp.runtime.stratego.adapter.IStrategoAstNode;
import org.strategoxt.lang.Context;
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
	
	@SuppressWarnings("unused")
	private final boolean persistent;
	
	private boolean openEditor;
	
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
	
	public boolean isOpenEditorEnabled() {
		return openEditor;
	}
	
	public void setOpenEditorEnabled(boolean openEditor) {
		this.openEditor = openEditor;
	}
	
	public void execute(EditorState editor, IStrategoAstNode node) {
		// TODO: refactor
		String filename = null;
		String result = null;
		
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
					if (!observer.isUpdateStarted())
						observer.asyncUpdate(editor.getParseController());
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
						: ppATerm(resultTerm);
				}
			} catch (InterpreterErrorExit e) {
				observer.reportRewritingFailed();
				Environment.logException("Builder failed:\n" + observer.getLog(), e);
				if (editor.getDescriptor().isDynamicallyLoaded()) StrategoConsole.activateConsole();
				openError(editor, e.getMessage()); // TODO: show message in editor if st
			} catch (UndefinedStrategyException e) {
				reportException(editor, e);
			} catch (InterpreterExit e) {
				reportException(editor, e);
			} catch (InterpreterException e) {
				reportException(editor, e);
			} catch (RuntimeException e) {
				reportException(editor, e);
			}
		}

		if (result != null) {
			try {
				IFile file = setFileContents(editor, filename, result);
				// TODO: if not persistent, create IEditorInput from result String
				if (openEditor) {
					IEditorPart target = openEditor(file, realTime);
					if (realTime)
						StrategoBuilderListener.addListener(editor.getEditor(), target, file, getCaption(), node);
				}
			} catch (CoreException e) {
				Environment.logException("Builder failed", e);
				openError(editor, "Failed (see error log): " + e.getMessage());
			}
		}
	}

	private String ppATerm(IStrategoTerm term) {
		Context context = observer.getRuntime().getCompiledContext();
		term = pp_aterm_box_0_0.instance.invoke(context, term);
		term = box2text_string_0_1.instance.invoke(context, term, Environment.getTermFactory().makeInt(120));
		return asJavaString(term);
	}

	private void reportException(EditorState editor, Exception e) {
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

	private IFile setFileContents(final EditorState editor, String filename, final String contents) throws CoreException {
		assert !Thread.holdsLock(observer.getSyncRoot()) || Thread.holdsLock(Environment.getSyncRoot())
			: "Acquiring a resource lock can cause a deadlock";

		final IFile file = editor.getProject().getRawProject().getFile(filename);
		InputStream resultStream = new ByteArrayInputStream(contents.getBytes());
		if (file.exists()) {
			file.setContents(resultStream, true, true, null);
			
			/* TODO: update editor contents instead of file?
			if (editor.getEditor().getTitleImage().isDisposed()) {
				InputStream resultStream = new ByteArrayInputStream(contents.getBytes());
				file.setContents(resultStream, true, true, null);
				...save...
			} else {
				new UIJob("Update derived editor") {
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
				}.schedule();
			}
			*/
		} else {
			file.create(resultStream, true, null);
		}
		return file;
	}

	/**
	 * Opens or activates an editor.
	 * (Asynchronous) exceptions are swallowed and logged.
	 */
	private IEditorPart openEditor(IFile file, boolean realTime) throws PartInitException {
		assert !Thread.holdsLock(observer.getSyncRoot()) || Thread.holdsLock(Environment.getSyncRoot())
			: "Opening a new editor and acquiring a resource lock can cause a deadlock";
		
		// TODO: WorkBenchPage.openEdiotr with a custom IEditorInput?
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
