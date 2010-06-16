package org.strategoxt.imp.runtime.parser;

import static org.eclipse.core.resources.IMarker.SEVERITY;
import static org.eclipse.core.resources.IMarker.SEVERITY_ERROR;
import static org.eclipse.core.resources.IMarker.SEVERITY_WARNING;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class AmbSuggestionGenerator implements IMarkerResolutionGenerator2 {

	public boolean hasResolutions(IMarker marker) {
		if (marker.getAttribute(SEVERITY, SEVERITY_ERROR) != SEVERITY_WARNING)
			return false;
		return false;
	}

	public IMarkerResolution[] getResolutions(IMarker marker) {
		// TODO Auto-generated method stub
		return null;
	}

}
