package org.strategoxt.imp.runtime.services.outline;

import java.io.File;
import java.util.HashMap;

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
		IStrategoTerm label = ((IStrategoTerm) element).getSubterm(0);
		
		if (label.getTermType() == IStrategoString.STRING) {
			return ((IStrategoString) label).stringValue();
		}
		else {
			return label.toString(); // fallback
		}
	}

	@Override
	public Image getImage(Object element) {
		IStrategoTerm label = ((IStrategoTerm) element).getSubterm(0);
		
		if (!label.getAnnotations().isEmpty()) {
			String iconPath = ((IStrategoString) label.getAnnotations().head()).stringValue();
			
			if (iconCache.containsKey(iconPath)) {
				return iconCache.get(iconPath);
			}
			else {
				File iconFile = new File(pluginPath, iconPath);
				Image icon = null;
				if (iconFile.exists()) {
					icon = new Image(Display.getDefault(), iconFile.getAbsolutePath());
				}
				else {
					Environment.logException("Can't find icon " + iconFile.getAbsolutePath());
				}
				iconCache.put(iconPath, icon);
				return icon;
			}
		}
		
		return null;
	}
	
	private final HashMap<String, Image> iconCache = new HashMap<String, Image>();
}
