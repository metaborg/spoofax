package org.strategoxt.imp.runtime;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.progress.UIJob;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.dynamicloading.DynamicParseController;
import org.strategoxt.imp.runtime.parser.SGLRParseController;

/**
 * Helper class for accessing the active editor.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class EditorState {
	
	private final UniversalEditor editor;
	
	public EditorState(UniversalEditor editor) {
		this.editor = editor;
	}
	
	// FACTORY METHODS
	
	/**
	 * Gets the editor state for the active editor.
	 * 
	 * @throws IllegalStateException  if not called from the UI thread.
	 */
	public static EditorState getActiveEditor() {
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow == null)
			throw new IllegalStateException("Must be called from UI thread");
		IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
		if (activePage == null)
			return null;
		IEditorPart editor = activePage.getActiveEditor();
		return editor instanceof UniversalEditor && ((UniversalEditor) editor).getParseController() instanceof DynamicParseController
			? new EditorState((UniversalEditor) editor)
			: null;
	}
	
	/**
	 * Returns the editor state associated with a given parse controller,
	 * if any active editor can be found.
	 * 
	 * @throws IllegalStateException  if not called from the UI thread
	 */
	public static EditorState getEditorFor(DynamicParseController parseController) {
		EditorState activeEditor = getActiveEditor();
		if (activeEditor != null && activeEditor.getEditor().getParseController() == parseController)
			return activeEditor;
		
		// Search for another editor with this parser
		for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
			for (IWorkbenchPage page : window.getPages()) {
				for (IEditorReference reference : page.getEditorReferences()) {
					IWorkbenchPart editor = reference.getPart(false);
					if (editor instanceof UniversalEditor
							&& ((UniversalEditor) editor).getParseController() == parseController) {
						// HACK: showChangeInformation(false)
						((UniversalEditor) editor).showChangeInformation(false);
						return new EditorState((UniversalEditor) editor);
					}
				}
			}
		}
		
		return null;
	}
	
	// UTILITY METHODS
	
	public static boolean isUIThread() {
		return Display.getCurrent() != null;
	}

	/**
	 * Asynchronously opens or activates an editor.
	 * 
	 * Exceptions are swallowed and logged.
	 */
	public static void asyncOpenEditor(Display display, IProject project, String filename, final boolean activate) {
		final IResource file = project.findMember(filename);
		if (!file.exists() || !(file instanceof IFile)) {
			Environment.logException("Cannot open an editor for " + filename);
			return;
		}
		asyncOpenEditor(display, (IFile) file, activate);
	}

	/**
	 * Asynchronously opens or activates an editor.
	 * 
	 * Exceptions are swallowed and logged.
	 */
	public static void asyncOpenEditor(Display display, final IFile file, final boolean activate) {
		Job job = new UIJob("Open editor") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				IWorkbenchPage page =
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, file, UniversalEditor.EDITOR_ID, activate);
				} catch (PartInitException e) {
					Environment.logException("Cannot open an editor for " + file, e);
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}
	
	// ACCESSORS
	
	public UniversalEditor getEditor() {
		return editor;
	}
	
	public SGLRParseController getParseController() {
		DynamicParseController wrapper = (DynamicParseController) getEditor().getParseController();
		return (SGLRParseController) wrapper.getWrapped();
	}
	
	public Language getLanguage() {
		return getEditor().fLanguage;
	}
	
	public final Descriptor getDescriptor() {
		return Environment.getDescriptor(getLanguage());
	}

	public final IResource getResource() {
    	return getParseController().getResource();
	}
	
	public final ISourceProject getProject() {
		return getParseController().getProject();
	}
}
