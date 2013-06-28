package org.strategoxt.imp.runtime.services.outline;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class SpoofaxOutlineContentProvider implements ITreeContentProvider {

	public void dispose() {
		// Do nothing.
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// Do nothing.
	}

	public Object[] getElements(Object inputElement) {
		IStrategoTerm term = (IStrategoTerm) inputElement;
		
		switch (term.getTermType()) {
			case IStrategoTerm.APPL:
				return new IStrategoTerm[]{term};

			case IStrategoTerm.LIST:
				return term.getAllSubterms();
				
			default:
				break;
		}
		
		return new Object[0];
	}

	public Object[] getChildren(Object parentElement) {
		IStrategoTerm children = ((IStrategoTerm) parentElement).getSubterm(1);
		return children.getAllSubterms();
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		return ((IStrategoTerm) element).getSubterm(1).getSubtermCount() != 0;
	}
}
