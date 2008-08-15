/**
 * 
 */
package org.strategoxt.imp.runtime.services;

import org.eclipse.swt.graphics.Color;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class TextAttributeReference {
	private final TextAttributeReferenceMap colorList;
	private Color color;
	private String name;
	private boolean isFollowing;
	
	public TextAttributeReference(TextAttributeReferenceMap colorList) {
		this.colorList = colorList;
	}
	
	public TextAttributeReference(TextAttributeReferenceMap colorList, Color color) {
		this.colorList = colorList;
		this.color = color;
	}
	
	public TextAttributeReference(TextAttributeReferenceMap colorList, String name) {
		this.colorList = colorList;
		this.name = name;
	}
	
	public Color get() throws BadDescriptorException {
		if (color != null) {
			return color;
		} else {
			if (isFollowing == true)
				throw new BadDescriptorException("Circular definition of color: " + name);
			isFollowing = true;
			return this.colorList.getColor(name);
		}
	}
}