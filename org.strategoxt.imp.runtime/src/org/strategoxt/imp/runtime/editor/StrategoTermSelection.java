package org.strategoxt.imp.runtime.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.EditorState;

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
		return EditorState.getEditorFor(spoofaxEditor).getSelectionAst(false);
	}
	
	public IStrategoTerm getAnalyzedSelectionAST() {
		return null; // TODO
	}
	
	@Override // IStructuredSelection
	public Object getFirstElement() {
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
			result.add(getSelectionAST());
		}
		return result;
	}
}
