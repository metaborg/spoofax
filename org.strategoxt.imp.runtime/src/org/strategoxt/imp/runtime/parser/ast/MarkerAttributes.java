package org.strategoxt.imp.runtime.parser.ast;

import static org.eclipse.core.resources.IMarker.*;

import org.eclipse.core.resources.IMarker;

import lpg.runtime.IToken;

/**
 * Marker configuration attributes container class.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class MarkerAttributes {

	private static final String[] attributes = { LINE_NUMBER, CHAR_START, CHAR_END, MESSAGE, SEVERITY, PRIORITY, TRANSIENT };

	private Object[] values;
	
	private final int line;
	
	private final String message;
	
	public MarkerAttributes(IToken left, IToken right, String message, int severity, int priority, boolean isTransient) {
		this.line = left.getLine();
		this.message = message;
		this.values = new Object[] { line, left.getStartOffset(), right.getEndOffset() + 1, message, severity, priority, isTransient };
	}
	
	public MarkerAttributes(IMarker marker) {
		this.line = marker.getAttribute(LINE_NUMBER, 0);
		this.message = marker.getAttribute(MESSAGE, "Problem");
		this.values = null;
	}
	
	public String[] getAttributes() {
		return attributes;
	}

	public Object[] getValues() {
		return values;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + line;
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof MarkerAttributes))
			return false;
		MarkerAttributes other = (MarkerAttributes) obj;
		if (line != other.line)
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		return true;
	}
}