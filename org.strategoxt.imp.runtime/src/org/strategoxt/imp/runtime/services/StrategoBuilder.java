package org.strategoxt.imp.runtime.services;

import static org.spoofax.interpreter.core.Tools.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.stratego.adapter.IStrategoAstNode;
import org.strategoxt.lang.StrategoException;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StrategoBuilder implements IBuilder {
	
	private final StrategoObserver observer;

	private final String caption;
	
	private final String builderRule;
	
	private final boolean realTime, persistent;
	
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
	
	public IAction toAction(final EditorState editor) {
		return new Action(caption) {
			@Override
			public void run() {
				try {
					// TODO: support selection AST in builders
					execute(editor, editor.getParseController().getCurrentAst());
				} catch (CoreException e) {
					// Show in Eclipse error pop-up
					Environment.logException("Builder failed", e);
					ErrorDialog.openError(null, "Spoofax/IMP building", "Builder failed (see error log)", Status.OK_STATUS); 
				}
			}
		};
	}
	
	public void execute(EditorState editor, IStrategoAstNode node) throws CoreException {
		IStrategoTerm resultTerm = observer.invoke(builderRule, node);
		if (resultTerm == null) {
			if (!observer.isUpdateStarted())
				observer.asyncUpdate(editor.getParseController());
			return;
		}

		// TODO: support "None()" result
		if (!isTermTuple(resultTerm) || !isTermString(resultTerm.getSubterm(0))
				|| !isTermString(resultTerm.getSubterm(0))) {
			throw new StrategoException("Illegal builder result (must be a filename/string tuple)");
		}
		
		IStrategoTerm filenameTerm = termAt(resultTerm, 0);
		String filename = asJavaString(filenameTerm);
		String result = asJavaString(termAt(resultTerm, 1));
		IFile file = createFile(editor, filename, result);
		// TODO: if not persistent, create IEditorInput from result String
		if (openEditor) {
			IEditorPart target = openEditor(file, !realTime);
			if (realTime)
				editor.getEditor().addModelListener(new StrategoBuilderListener(editor, target, file, getCaption(), null, null));
		}
	}

	private IFile createFile(EditorState editor, String filename, String result) throws CoreException {
		IFile file = editor.getProject().getRawProject().getFile(filename);
		InputStream resultStream = new ByteArrayInputStream(result.getBytes());
		if (file.exists()) {
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
