package org.strategoxt.imp.runtime.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.services.views.properties.IPropertiesService;

/**
 * @author Oskar van Rest
 * 
 * Why implement IStructuredSelection? Because the properties view only takes IStructuredSelections.
 */
public class StrategoTermSelection extends TextSelection implements IStructuredSelection {

	private final SpoofaxEditor spoofaxEditor;

	public StrategoTermSelection(SpoofaxEditor spoofaxEditor, int offset, int length) {
		super(spoofaxEditor.getDocumentProvider().getDocument(spoofaxEditor.getEditorInput()), offset, length);
		this.spoofaxEditor = spoofaxEditor;
	}

	public IStrategoTerm getSelectionAst() {
		if (EditorState.getEditorFor(spoofaxEditor) != null) {
			return EditorState.getEditorFor(spoofaxEditor).getSelectionAst(false);
		}
		return null;
	}
	
	public IStrategoTerm getAnalyzedSelectionAst() {
		if (EditorState.getEditorFor(spoofaxEditor) != null) {
			return EditorState.getEditorFor(spoofaxEditor).getAnalyzedSelectionAst(false);
		}
		return null;
	}
	
	/**
	 * Return the properties for the properties view.
	 */
	@Override // IStructuredSelection (properties view)
	public IStrategoTerm getFirstElement() {
		EditorState editorState = EditorState.getEditorFor(spoofaxEditor);
		IPropertiesService propertiesService = null;
		try {
			propertiesService = editorState.getDescriptor().createService(IPropertiesService.class, editorState.getParseController());
		} catch (BadDescriptorException e) {
			e.printStackTrace();
		}
		return propertiesService.getProperties();
	}

	@Override // IStructuredSelection (properties view)
	public Iterator<IStrategoTerm> iterator() {
		return toList().iterator();
	}

	@Override // IStructuredSelection (properties view)
	public int size() {
		return toList().size();
	}

	@Override // IStructuredSelection (properties view)
	public Object[] toArray() {
		return toList().toArray();
	}

	@Override // IStructuredSelection (properties view)
	public List<IStrategoTerm> toList() {
		List<IStrategoTerm> result = new ArrayList<IStrategoTerm>(1);
		if (getFirstElement() != null) {
			result.add(getFirstElement());
		}
		return result;
	}
}
