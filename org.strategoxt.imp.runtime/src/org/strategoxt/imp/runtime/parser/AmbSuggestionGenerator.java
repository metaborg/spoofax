package org.strategoxt.imp.runtime.parser;

import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.IMarkerResolutionGenerator2;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class AmbSuggestionGenerator implements IMarkerResolutionGenerator2 {

	public boolean hasResolutions(IMarker marker) {
		return false;
	}

	public IMarkerResolution[] getResolutions(IMarker marker) {
		return new IMarkerResolution[] {
			new DemoResolution("label", "description")
		};
	}

	private static class DemoResolution implements IMarkerResolution2 {
		String label;
		String description;
		
		public DemoResolution(String label, String description) {
			this.label = label;
			this.description = description;
		}
		
		public String getDescription() {
			return description;
		}
		
		public String getLabel() {
			return label;
		}
		
		public Image getImage() {
			// TODO: Quick fix image
			return null;
		}

		public void run(IMarker marker) {
			// TODO: run quick fix?
		}
		
	}
	
}
