package org.strategoxt.imp.runtime.services;

import static org.spoofax.interpreter.core.Tools.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.WeakHashMap;

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
import org.strategoxt.imp.runtime.stratego.StrategoConsole;
import org.strategoxt.imp.runtime.stratego.adapter.IStrategoAstNode;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StrategoBuilder implements IBuilder {
	
	private final Map<EditorState, StrategoBuilderListener> listeners =
		new WeakHashMap<EditorState, StrategoBuilderListener>();
	
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
	
			// TODO: support "None()" result
			if (!isTermTuple(resultTerm) || !isTermString(resultTerm.getSubterm(0))
					|| !isTermString(resultTerm.getSubterm(0))) {
				Environment.logException("Illegal builder result (must be a filename/string tuple)");
				openError(editor, "Illegal builder result (must be a filename/string tuple): " + resultTerm);
			}
			
			IStrategoTerm filenameTerm = termAt(resultTerm, 0);
			String filename = asJavaString(filenameTerm);
			String result = asJavaString(termAt(resultTerm, 1));
			IFile file = createFile(editor, filename, result);
			// TODO: if not persistent, create IEditorInput from result String
			if (openEditor) {
				IEditorPart target = openEditor(file, !realTime);
				if (realTime) {
					StrategoBuilderListener listener = listeners.get(editor);
					if (listener != null) listener.setEnabled(false);
					listener = new StrategoBuilderListener(editor, target, file, getCaption(), node);
					listeners.put(editor, listener);
					editor.getEditor().addModelListener(listener);
				}
			}
		} catch (CoreException e) {
			Environment.logException("Builder failed", e);
			openError(editor, "Builder failed (see error log)");
		} catch (InterpreterErrorExit e) {
			observer.reportRewritingFailed();
			Environment.logException("Builder failed:\n" + observer.getLog(), e);
			if (editor.getDescriptor().isDynamicallyLoaded()) StrategoConsole.activateConsole();
			openError(editor, e.getMessage());
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

	private void reportException(EditorState editor, Exception e) {
		Environment.logException("Builder failed", e);
		if (editor.getDescriptor().isDynamicallyLoaded()) StrategoConsole.activateConsole();
		Status status = new Status(IStatus.ERROR, RuntimeActivator.PLUGIN_ID, e.getLocalizedMessage(), e);
		ErrorDialog.openError(editor.getEditor().getSite().getShell(),
				"Spoofax/IMP builder", null, status);
	}
	
	private static void openError(EditorState editor, String message) {
		Status status = new Status(IStatus.ERROR, RuntimeActivator.PLUGIN_ID, message);
		ErrorDialog.openError(editor.getEditor().getSite().getShell(),
				"Spoofax/IMP builder", null, status);
	}

	private IFile createFile(EditorState editor, String filename, String contents) throws CoreException {
		IFile file = editor.getProject().getRawProject().getFile(filename);
		InputStream resultStream = new ByteArrayInputStream(contents.getBytes());
		if (file.exists()) {
			// TODO: editor.getDocument().set(contents);?
			file.setContents(resultStream, true, true, null);
		} else {
			file.create(resultStream, true, null);
		}
		return file;
	}

	/**
	 * Opens or activates an editor.
	 * (Asynchronous) exceptions are swallowed and logged.
	 */
	private static IEditorPart openEditor(IFile file, boolean activate) throws PartInitException {
		IWorkbenchPage page =
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		return IDE.openEditor(page, file, activate);
	}
	
	@Override
	public String toString() {
		return "Builder: " + builderRule + " - " + caption; 
	}
}
