package org.strategoxt.imp.runtime.services.menus;

import java.lang.ref.WeakReference;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.ui.IEditorPart;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.WeakWeakMap;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.services.menus.model.IBuilder;
import org.strategoxt.imp.runtime.stratego.StrategoTermPath;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StrategoBuilderListener implements IModelListener {

	/**
	 * Maps target editors to their builder listener.
	 */
	private static final WeakWeakMap<IEditorPart, StrategoBuilderListener> asyncListeners =
		new WeakWeakMap<IEditorPart, StrategoBuilderListener>();
	
	private final Object builderData;
	
	private final List<String> path;
	
	private WeakReference<UniversalEditor> editor;
	
	private final WeakReference<IEditorPart> targetEditor;
	
	private final IFile targetFile;

	private IStrategoTerm selection;
	
	private long lastChanged;
	
	private boolean enabled = true;
	
	private StrategoBuilderListener(UniversalEditor editor, IEditorPart targetEditor, IFile targetFile,
			IBuilder builder, List<String> path, IStrategoTerm selection) {
		
		this.editor = new WeakReference<UniversalEditor>(editor);
		this.targetEditor = new WeakReference<IEditorPart>(targetEditor);
		this.builderData = builder.getData();
		this.path = path;
		this.targetFile = targetFile;
		this.lastChanged = targetFile.getLocalTimeStamp();
		this.selection = selection;
	}

	public static StrategoBuilderListener addListener(UniversalEditor editor, IEditorPart target, IFile file, IBuilder builder, List<String> path, IStrategoTerm node) {
		synchronized (asyncListeners) {
			StrategoBuilderListener listener = asyncListeners.get(editor);
			if (listener != null) listener.setEnabled(false);
			listener = new StrategoBuilderListener(editor, target, file, builder, path, node);
			asyncListeners.put(target, listener);
			editor.addModelListener(listener);
			return listener;
		}
	}
	
	public static StrategoBuilderListener getListener(IEditorPart targetEditor) {
		synchronized (asyncListeners) {
			return asyncListeners.get(targetEditor);
		}
	}
	
	/**
	 * Gets the source editor for this builder, if has not been garbage collected.
	 */
	public UniversalEditor getSourceEditor() {
		return editor.get();
	}
	
	public void setSourceEditor(UniversalEditor editor) {
		this.editor = new WeakReference<UniversalEditor>(editor);
	}
	
	/**
	 * Gets the target editor for this builder, if has not been garbage collected.
	 */
	public IEditorPart getTargetEditor() {
		return targetEditor.get();
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
				|| !EditorState.isEditorOpen(targetEditor) 
				|| targetFile.getLocalTimeStamp() > lastChanged) {
			enabled = false;
			selection = null;
			editor.removeModelListener(this);
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
			IMenuList menus = editor.getDescriptor().createService(IMenuList.class, editor.getParseController());
			IBuilder builder = menus.getBuilder(path);
			if (builder == null)
			    throw new RuntimeException("No builder exists with this path: " + path);
			builder.setData(builderData);
			
			IStrategoTerm newSelection = findNewSelection(editor);
			Job job;
			if (newSelection != null) {
				job = builder.scheduleExecute(editor, selection = newSelection, targetFile, true);
			} else {
				job = builder.scheduleExecute(editor, editor.getParseController().getCurrentAst(), targetFile, true);
			}
			if (job == null) {
				enabled = false;
			} else {
				job.join(); // wait to get new time stamp
			}

		} catch (BadDescriptorException e) {
			Environment.logException("Could not update derived editor for " + editor.getResource(), e);
		} catch (RuntimeException e) {
			Environment.logException("Could not update derived editor for " + editor.getResource(), e);
		} catch (InterruptedException e) {
			Environment.logException("Could not update derived editor for " + editor.getResource(), e);
		} finally {
			if (targetFile.exists()) lastChanged = targetFile.getLocalTimeStamp();
		}
	}
	
	private IStrategoTerm findNewSelection(EditorState editor) {
		if (selection == null) return null;
		IStrategoTerm newAst = editor.getParseController().getCurrentAst();
		if (newAst == null) return null;
		return StrategoTermPath.findCorrespondingSubtree(newAst, selection);
	}
}
