package org.strategoxt.imp.runtime.services;

import org.eclipse.jface.viewers.LabelProvider;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class StrategoLabelProvider extends LabelProvider {
	
	@Override
	public String getText(Object element) {
		IStrategoString label = (IStrategoString) ((IStrategoTerm) element).getSubterm(0);
		return label.stringValue();
	}
}
