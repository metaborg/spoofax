package org.strategoxt.imp.runtime.services.views;

import java.util.LinkedList;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.services.views.outline.SpoofaxOutlineUtil;

/**
 * @author Oskar van Rest
 */
public class StrategoTreeContentProvider implements ITreeContentProvider {

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
				return filterWellFormedOutlineNodes(new Object[]{term});

			case IStrategoTerm.LIST:
				return filterWellFormedOutlineNodes(term.getAllSubterms());
				
			default:
				Environment.logException("Expected Node(\"label\"{\"pathToIcon\"}, [...]) or [Node(...) ...] but was: " + inputElement);
				break;
		}
		
		return new Object[0];
	}

	public Object[] getChildren(Object parentElement) {
		IStrategoTerm children = ((IStrategoTerm) parentElement).getSubterm(1);
		return filterWellFormedOutlineNodes(children.getAllSubterms());
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		IStrategoList children = (IStrategoList) ((IStrategoTerm) element).getSubterm(1);
		return !children.isEmpty();
	}
	
	private Object[] filterWellFormedOutlineNodes(Object[] outlineNodes) {
		LinkedList<Object> result = new LinkedList<Object>();
		for (int i=0; i<outlineNodes.length; i++) {
			Object outlineNode = outlineNodes[i];
			if (SpoofaxOutlineUtil.isWellFormedOutlineNode(outlineNode)) {
				result.add(outlineNode);
			}
			else {
				Environment.logException("Not a well-formed outline node: " + outlineNode + "\nOutline nodes should be of the form Node(\"label\", [...]) or Node(\"label\"{\"pathToIcon\"}, [...])");
			}
		}
		
		return result.toArray();
	}
}
