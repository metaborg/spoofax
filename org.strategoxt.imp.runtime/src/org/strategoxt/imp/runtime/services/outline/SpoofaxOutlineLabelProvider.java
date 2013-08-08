package org.strategoxt.imp.runtime.services.outline;

import java.io.File;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.Environment;

/**
 * @author Oskar van Rest
 */
public class SpoofaxOutlineLabelProvider extends LabelProvider {

	private String pluginPath;
	
	public SpoofaxOutlineLabelProvider(String pluginPath) {
		this.pluginPath = pluginPath;
	}
	
	@Override
	public String getText(Object element) {
		return ((IStrategoString) ((IStrategoTerm) element).getSubterm(0)).stringValue();
	}

	@Override
	public Image getImage(Object element) {
		IStrategoTerm label = ((IStrategoTerm) element).getSubterm(0);
		
		if (!label.getAnnotations().isEmpty()) {
			String iconPath = ((IStrategoString) label.getAnnotations().head()).stringValue();
			
			File iconFile = new File(pluginPath, iconPath);
			
			if (iconFile.exists()) {
				return new Image(Display.getDefault(), iconFile.getAbsolutePath());
			}
			else {
				Environment.logException("Can't find icon " + iconFile.getAbsolutePath());
			}
		}
		
		return null;
	}
}
