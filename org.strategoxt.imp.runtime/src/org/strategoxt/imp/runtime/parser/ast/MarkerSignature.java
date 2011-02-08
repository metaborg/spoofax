package org.strategoxt.imp.runtime.parser.ast;

import static org.eclipse.core.resources.IMarker.CHAR_END;
import static org.eclipse.core.resources.IMarker.CHAR_START;
import static org.eclipse.core.resources.IMarker.LINE_NUMBER;
import static org.eclipse.core.resources.IMarker.MESSAGE;
import static org.eclipse.core.resources.IMarker.PRIORITY;
import static org.eclipse.core.resources.IMarker.SEVERITY;
import static org.eclipse.core.resources.IMarker.SEVERITY_ERROR;
import static org.eclipse.core.resources.IMarker.TRANSIENT;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokenizer;
import org.strategoxt.imp.runtime.Environment;

/**
 * Marker configuration attributes containment and comparison.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class MarkerSignature {

	private static final String[] ATTRIBUTES = { LINE_NUMBER, CHAR_START, CHAR_END, MESSAGE, SEVERITY, PRIORITY, TRANSIENT };
	
	private IResource resource;

	private Object[] values;
	
	private final int line;
	
	private final int severity;
	
	private final String message;
	
	private final String comparisonMessage;
	
	public MarkerSignature(IResource resource, IToken left, IToken right, String message,
			int severity, int priority, boolean isTransient) {
		if (message == null) message = "Problem";
		this.resource = resource;
		this.line = left.getLine();
		this.message = message;
		this.severity = severity;
		this.comparisonMessage = removeSyntaxErrorDetails(message);
		this.values = new Object[] { line, left.getStartOffset(), right.getEndOffset() + 1, message, severity, priority, isTransient };
	}
	
	public MarkerSignature(IMarker marker) {
		this.line = marker.getAttribute(LINE_NUMBER, 0);
		this.message = marker.getAttribute(MESSAGE, "Problem");
		this.comparisonMessage = removeSyntaxErrorDetails(message);
		this.severity = marker.getAttribute(SEVERITY, SEVERITY_ERROR);
		this.values = null;
	}
	
	public final String[] getAttributes() {
		return ATTRIBUTES;
	}

	public final Object[] getValues() {
		return values;
	}
	
	public final IResource getResource() {
		return resource;
	}
	
	public final int getLine() {
		return line;
	}
	
	public final String getMessage() {
		return message;
	}
	
	public boolean attibutesEqual(IMarker marker) {
		if (!marker.exists()) throw new IllegalStateException();
		
		for (int i = 0; i < ATTRIBUTES.length; i++) {
			try {
				if (!values[i].equals(marker.getAttribute(ATTRIBUTES[i])))
					return false;
			} catch (CoreException e) {
				Environment.logException("Could not retrieve attribute value", e);
				return false;
			}
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		
		// Note: Hashcode and equals aren't very precise here, since their
		// main purpose is in identifying markers that can be reused
		
		final int prime = 31;
		int result = 1;
		// UNDONE: result = prime * result + line;
		result = prime * result + severity;
		result = prime * result + ((comparisonMessage == null) ? 0 : comparisonMessage.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof MarkerSignature))
			return false;
		MarkerSignature other = (MarkerSignature) obj;
		if (severity != other.severity)
			return false;
		if (line != other.line) {
			// Maybe the error just shifted to the next line
			return (line == other.line + 1 || line + 1 == other.line) && message.equals(other.message);
		}
		if (!comparisonMessage.equals(other.comparisonMessage))
			return false;
		return true;
	}
	
	private static String removeSyntaxErrorDetails(String s) {
		if (s.startsWith(ITokenizer.ERROR_GENERIC_PREFIX)
				|| s == ITokenizer.ERROR_SKIPPED_REGION
				|| s.startsWith(ITokenizer.ERROR_WATER_PREFIX)) {
			return "<unexpected>";
		} else {
			return s;
		}
	}
}