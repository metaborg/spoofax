/**
 * 
 */
package org.strategoxt.imp.runtime.services;

import org.eclipse.jface.text.TextAttribute;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class TextAttributeReference {
	private final TextAttributeReferenceMap map;
	private TextAttribute attribute;
	private String name;
	private boolean isFollowing;
	
	public TextAttributeReference(TextAttributeReferenceMap map) {
		this.map = map;
	}
	
	public TextAttributeReference(TextAttributeReferenceMap map, TextAttribute attribute) {
		this.map = map;
		this.attribute = attribute;
	}
	
	public TextAttributeReference(TextAttributeReferenceMap map, String name) {
		this.map = map;
		this.name = name;
	}
	
	public TextAttribute get() {
		if (attribute != null) {
			return attribute;
		} else {
			if (isFollowing == true) {
				Environment.logException("Unable to apply attribute to token", new BadDescriptorException("Circular definition of color: " + name));
				return null;
			}
			isFollowing = true;
			TextAttribute result = this.map.getAttribute(name);
			isFollowing = false;
			return result;
		}
	}
}