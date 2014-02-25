package org.strategoxt.imp.runtime.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.stratego.StrategoTermPath;
import org.strategoxt.lang.Context;

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

	public IStrategoTerm getSelectionAST() {
		if (EditorState.getEditorFor(spoofaxEditor) != null) {
			return EditorState.getEditorFor(spoofaxEditor).getSelectionAst(false);
		}
		return null;
	}
	
	public IStrategoTerm getAnalyzedSelectionAST() {
		EditorState editorState = EditorState.getEditorFor(spoofaxEditor);
		
		if (getSelectionAST() != null) {
			try {
				IStrategoList path = StrategoTermPath.getTermPathWithOrigin(new Context(), editorState.getCurrentAnalyzedAst(), getSelectionAST());
				if (path != null) {
					return StrategoTermPath.getTermAtPath(new Context(), editorState.getCurrentAnalyzedAst(), path);
				}
			} catch (BadDescriptorException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	@Override // IStructuredSelection
	public IStrategoTerm getFirstElement() {
		// TODO: return properties model instead of selectionAST
		return getSelectionAST();
	}

	@Override // IStructuredSelection
	public Iterator<IStrategoTerm> iterator() {
		return toList().iterator();
	}

	@Override // IStructuredSelection
	public int size() {
		return toList().size();
	}

	@Override // IStructuredSelection
	public Object[] toArray() {
		return toList().toArray();
	}

	@Override // IStructuredSelection
	public List<IStrategoTerm> toList() {
		List<IStrategoTerm> result = new ArrayList<IStrategoTerm>(1);
		if (getSelectionAST() != null) {
			result.add(getFirstElement());
		}
		return result;
	}
}
