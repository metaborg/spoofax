package org.strategoxt.imp.runtime.services.outline;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.Environment;

/**
 * @author Oskar van Rest
 */
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
				Environment.logException("Expected Node(...) or [Node(...), ...] but was: " + inputElement);
				break;
		}
		
		return new Object[0];
	}

	public Object[] getChildren(Object parentElement) {
		if (((IStrategoTerm) parentElement).getSubtermCount() == 2) {
			IStrategoTerm children = ((IStrategoTerm) parentElement).getSubterm(1);
			return children.getAllSubterms();
		}
		
		Environment.logException("Expected Node(..., [...]) but was: " + parentElement);
		return new Object[0];
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		return getChildren(element).length != 0;
	}
}
