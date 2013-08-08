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

	@Override
	public String getText(Object element) {
		IStrategoTerm label = getLabel(element);
		
		if (label != null && label.getTermType() == IStrategoTerm.STRING) {
			return ((IStrategoString) label).stringValue();
		}
		else { // fallback
			return label == null ? "" : label.toString(); 
		}
	}

	@Override
	public Image getImage(Object element) {
		IStrategoTerm label = getLabel(element);
		if (label != null && !label.getAnnotations().isEmpty()) {
			IStrategoTerm iconPath = label.getAnnotations().getSubterm(0);		
			if (iconPath.getTermType() == IStrategoTerm.STRING) {
				String pluginPath = SpoofaxOutlineUtil.getPluginPath(element);
				File iconFile = new File(pluginPath, ((IStrategoString) iconPath).stringValue());
				if (iconFile.exists()) {
					return new Image(Display.getDefault(), iconFile.getAbsolutePath());
				}
				else {
					Environment.logException("Can't find icon " + iconFile.getAbsolutePath());
				}
			}
		}		
		
		return null;
	}

	private IStrategoTerm getLabel(Object element) {
		IStrategoTerm term = (IStrategoTerm) element;
		if (term.getSubtermCount() >= 1) {
			return term.getSubterm(0);
		}

		return null;
	}
}
