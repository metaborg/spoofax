package org.strategoxt.imp.runtime;

import org.eclipse.core.resources.IResource;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.dynamicloading.DynamicParseController;
import org.strategoxt.imp.runtime.editor.SelectionUtil;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.services.StrategoObserver;

/**
 * Helper class for accessing an active editor.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class EditorState extends FileState {
	
	private final UniversalEditor editor;
	
	public EditorState(UniversalEditor editor) {
		super(getDescriptor(editor));
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
	public static EditorState getEditorFor(IParseController parseController) {
		assert !(parseController instanceof SGLRParseController)
			|| !((SGLRParseController) parseController).isReplaced();
		
		if (parseController instanceof SGLRParseController
				&& ((SGLRParseController) parseController).getEditor() != null) {
			return ((SGLRParseController) parseController).getEditor();
		}
		
		if (parseController instanceof DynamicParseController) {
			EditorState result = ((DynamicParseController) parseController).getLastEditor();
			if (result != null) return result;
		}
		
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
						return new EditorState((UniversalEditor) editor);
					}
				}
			}
		}
		
		return null;
	}
	
	public static EditorState getEditorFor(IWorkbenchPart part) {
		if (part instanceof UniversalEditor) {
			UniversalEditor editor = (UniversalEditor) part;
			IParseController controller = editor.getParseController();
			if (controller instanceof DynamicParseController) {
				return new EditorState(editor);
			}
		}
		return null;
	}
	
	// ACCESSORS

	public UniversalEditor getEditor() {
		return editor;
	}
	
	@Override
	public SGLRParseController getParseController() {
		DynamicParseController wrapper = (DynamicParseController) editor.getParseController();
		return (SGLRParseController) wrapper.getWrapped();
	}
	
	@Override
	public IResource getResource() {
		return getParseController().getResource();
	}
	
	@Override
	public Language getLanguage() {
		return getEditor().fLanguage;
	}
	
	private static Descriptor getDescriptor(UniversalEditor editor) {
		return Environment.getDescriptor(editor.fLanguage);
	}
	
	public void scheduleParserUpdate(long delay) {
		if (isEditorOpen(editor))
			getEditor().fParserScheduler.schedule(delay);
	}
	
	public void scheduleAnalysis() {
		try {
			SGLRParseController controller = getParseController();
			StrategoObserver observer = getDescriptor().createService(StrategoObserver.class, controller);
			observer.scheduleUpdate(controller);
		} catch (BadDescriptorException e) {
			Environment.logException("Could not schedule analysis", e);
		}
	}
	
	/**
	 * Gets the document model for this editor, which can be used to manipulate
	 * the contents of the editor.
	 * 
	 * See http://wiki.eclipse.org/FAQ_How_do_I_insert_text_in_the_active_text_editor%3F
	 */
	public IDocument getDocument() {
		IDocumentProvider provider = getEditor().getDocumentProvider();
		return provider == null ? null : provider.getDocument(getEditor().getEditorInput());
	}
	
	/**
	 * Gets the abstract syntax subtree for the selection in the editor.
	 * 
	 * @param ignoreEmptySelection
	 *            If true, null is returned if the selection is 0 characters wide.
	 */
	public final synchronized IStrategoTerm getSelectionAst(boolean ignoreEmptySelection) {
		ITextSelection selection = (ITextSelection) getEditor().getSelectionProvider().getSelection();
		return SelectionUtil.getSelectionAst(selection.getOffset(), selection.getLength(), ignoreEmptySelection, getParseController());
	}
	
	/**
	 * Gets the analyzed abstract syntax subtree for the selection in the editor.
	 * 
	 * @param ignoreEmptySelection
	 *            If true, null is returned if the selection is 0 characters wide.
	 */
	public IStrategoTerm getSelectionAstAnalyzed(boolean ignoreEmptySelection) {
		ITextSelection selection = (ITextSelection) getEditor().getSelectionProvider().getSelection();
		return SelectionUtil.getSelectionAstAnalyzed(selection.getOffset(), selection.getLength(), ignoreEmptySelection, getParseController());
	}
	
	public static boolean isEditorOpen(IEditorPart editor) {
		return !((editor.getTitleImage() != null && editor.getTitleImage().isDisposed())
			|| editor.getEditorInput() == null
			|| editor.getSite() == null
			|| (editor instanceof AbstractTextEditor && ((AbstractTextEditor) editor).getDocumentProvider() == null));
	}
}
