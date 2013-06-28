package org.strategoxt.imp.runtime.services.outline;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class SpoofaxOutlineLabelProvider extends LabelProvider {
	
	private String pluginPath;
	
	public SpoofaxOutlineLabelProvider(String pluginPath) {
		this.pluginPath = pluginPath;
	}

	@Override
	public String getText(Object element) {
		IStrategoString label = (IStrategoString) ((IStrategoTerm) element).getSubterm(0);
		return label.stringValue();
	}
	
	@Override
	public Image getImage(Object element) {
		IStrategoTerm term = (IStrategoTerm) element;
		IStrategoString label = (IStrategoString) term.getSubterm(0);
		
		if (!label.getAnnotations().isEmpty()) {
			IStrategoString iconPath = (IStrategoString) label.getAnnotations().getSubterm(0);
			return new Image(Display.getDefault() , pluginPath + iconPath.stringValue());
		}
		
		return null;
	}
}
