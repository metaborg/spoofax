package org.strategoxt.imp.runtime.services;

import java.lang.ref.WeakReference;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.IEditorPart;
import org.spoofax.NotImplementedException;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.parser.ast.AstNode;
import org.strategoxt.imp.runtime.stratego.StrategoTermPath;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StrategoBuilderListener implements IModelListener {
	
	private final String builder;
	
	private final StrategoTermPath selectionPath;

	private final String selectionConstructor;
	
	private final WeakReference<EditorState> editor;
	
	private final WeakReference<IEditorPart> targetEditor;
	
	private final IFile targetFile;
	
	private long lastChanged;
	
	private boolean enabled = true;
	
	public StrategoBuilderListener(EditorState editor, IEditorPart targetEditor, IFile targetFile,
			String builder, StrategoTermPath selectionPath, String selectionConstructor) {
		
		this.editor = new WeakReference<EditorState>(editor);
		this.targetEditor = new WeakReference<IEditorPart>(targetEditor);
		this.builder = builder;
		this.targetFile = targetFile;
		this.selectionPath = selectionPath;
		this.selectionConstructor = selectionConstructor;
		this.lastChanged = targetFile.getLocalTimeStamp();
	}
	
	public AnalysisRequired getAnalysisRequired() {
		return AnalysisRequired.SYNTACTIC_ANALYSIS;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void update(IParseController parseController, IProgressMonitor monitor) {
		EditorState editor = this.editor.get();
		IEditorPart targetEditor = this.targetEditor.get();
		
		// FIXME: Don't update if the editor is closed
		if (!enabled || editor == null || targetEditor == null || targetEditor.isDirty()
				|| targetEditor.getTitleImage().isDisposed() // editor closed
				|| targetFile.getLocalTimeStamp() > lastChanged) {
			enabled = false;
			return;
		}
		
		try {
			IBuilderMap builders = editor.getDescriptor().createService(IBuilderMap.class);
			IBuilder builder = builders.get(this.builder);
			AstNode ast = editor.getParseController().getCurrentAst();
			if (selectionPath == null) {
				builder.setOpenEditorEnabled(false);
				builder.execute(editor, ast);
			} else {	
				// TODO: support selection AST node in builders
				throw new NotImplementedException("Could not update derived editor for: " + selectionConstructor + "/" + selectionPath);
			}
			lastChanged = targetFile.getLocalTimeStamp();
		} catch (BadDescriptorException e) {
			Environment.logException("Could not update derived editor for " + editor.getResource(), e);
			ErrorDialog.openError(editor.getEditor().getSite().getShell(),
					"Spoofax/IMP builder", "Could not update derived editor for " + editor.getResource(), Status.OK_STATUS); 
		}
	}
}
