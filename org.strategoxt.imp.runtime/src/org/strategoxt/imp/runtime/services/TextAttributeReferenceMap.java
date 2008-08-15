package org.strategoxt.imp.runtime.services;

import java.util.HashMap;

import org.eclipse.swt.graphics.Color;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;

/**
 * A lazy palette of colors.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class TextAttributeReferenceMap {
	private final HashMap<String, TextAttributeReference> colors = new HashMap<String, TextAttributeReference>();
	
	public Color getColor(String name) throws BadDescriptorException {
		TextAttributeReference result = colors.get(name);
		if (result == null) throw new BadDescriptorException("Color not defined: " + name);
		return result.get();
	}
	
	public void register(String name, TextAttributeReference color) {
		colors.put(name, color);
	}
}
