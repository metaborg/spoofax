package org.strategoxt.imp.runtime.services;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.IEditorPart;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.DynamicParseController;
import org.strategoxt.imp.runtime.stratego.StrategoTermPath;
import org.strategoxt.imp.runtime.stratego.adapter.IStrategoAstNode;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StrategoBuilderListener implements IModelListener {

	/**
	 * Maps target editors to their builder listener.
	 */
	private static final Map<UniversalEditor, StrategoBuilderListener> asyncListeners =
		new WeakHashMap<UniversalEditor, StrategoBuilderListener>();
	
	private final String builder;
	
	private final WeakReference<UniversalEditor> editor;
	
	private final WeakReference<IEditorPart> targetEditor;
	
	private final IFile targetFile;

	private IStrategoAstNode selection;
	
	private long lastChanged;
	
	private boolean enabled = true;
	
	private  StrategoBuilderListener(UniversalEditor editor, IEditorPart targetEditor, IFile targetFile,
			String builder, IStrategoAstNode selection) {
		
		this.editor = new WeakReference<UniversalEditor>(editor);
		this.targetEditor = new WeakReference<IEditorPart>(targetEditor);
		this.builder = builder;
		this.targetFile = targetFile;
		this.lastChanged = targetFile.getLocalTimeStamp();
		this.selection = selection;
	}

	public static void addListener(UniversalEditor editor, IEditorPart target, IFile file, String builder, IStrategoAstNode node) {
		synchronized (asyncListeners) {
			StrategoBuilderListener listener = asyncListeners.get(editor);
			if (listener != null) listener.setEnabled(false);
			listener = new StrategoBuilderListener(editor, target, file, builder, node);
			asyncListeners.put(editor, listener);
			editor.addModelListener(listener);
		}
	}
	
	public AnalysisRequired getAnalysisRequired() {
		return AnalysisRequired.SYNTACTIC_ANALYSIS;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean isEnabled() {
		UniversalEditor editor = this.editor.get();
		IEditorPart targetEditor = this.targetEditor.get();
		
		if (!enabled || editor == null || targetEditor == null || targetEditor.isDirty()
				|| targetEditor.getTitleImage().isDisposed() // editor closed
				|| targetFile.getLocalTimeStamp() > lastChanged) {
			enabled = false;
			selection = null;
			return false;
		} else {
			return true;
		}
	}

	public void update(IParseController parseController, IProgressMonitor monitor) {
		update(monitor);
	}

	public void update(IProgressMonitor monitor) {
		EditorState editor = new EditorState(this.editor.get()); // (must appear first; garbage might be collected)
		if (!isEnabled())
			return;
		
		try {
			IBuilderMap builders = editor.getDescriptor().createService(IBuilderMap.class);
			IBuilder builder = builders.get(this.builder);
			builder.setOpenEditorEnabled(false);
			
			IStrategoAstNode newSelection = findNewSelection(editor);
			if (newSelection != null) {
				builder.execute(editor, selection = newSelection);
			} else {
				builder.execute(editor, editor.getParseController().getCurrentAst());
			}
			lastChanged = targetFile.getLocalTimeStamp();

		} catch (BadDescriptorException e) {
			Environment.logException("Could not update derived editor for " + editor.getResource(), e);
			ErrorDialog.openError(editor.getEditor().getSite().getShell(),
					"Spoofax/IMP builder", "Could not update derived editor for " + editor.getResource(), Status.OK_STATUS); 
		}
	}
	
	private IStrategoAstNode findNewSelection(EditorState editor) {
		if (selection == null) return null;
		IStrategoAstNode newAst = editor.getParseController().getCurrentAst();
		if (newAst == null) return null;
		return StrategoTermPath.findCorrespondingSubtree(newAst, selection);
	}

	public static void rescheduleAllListeners() {
		boolean required = false;
		synchronized (asyncListeners) {
			for (StrategoBuilderListener listener : asyncListeners.values()) {
				if (listener.isEnabled()) {
					required = true;
					break;
				}
			}
		}
		if (required) {
			new Job("Rebuild derived files") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					synchronized (asyncListeners) {
						for (StrategoBuilderListener listener : asyncListeners.values()) {
							try {
								listener.update(monitor);
							} catch (Exception e) {
								Environment.logException("Could not update builder", e);
							}
						}
					}
					return Status.OK_STATUS;
				}
			}.schedule(DynamicParseController.REINIT_PARSE_DELAY * 3);
		}
	}
}
