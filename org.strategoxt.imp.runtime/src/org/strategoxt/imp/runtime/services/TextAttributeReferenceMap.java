package org.strategoxt.imp.runtime.services;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.TextAttribute;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;

/**
 * A lazy palette of colors.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class TextAttributeReferenceMap {
	private final HashMap<String, TextAttributeReference> map = new HashMap<String, TextAttributeReference>();
	
	public TextAttribute getAttribute(String name) {
		TextAttributeReference result = map.get(name);
		return result.get();
	}
	
	public void checkAllColors() throws BadDescriptorException {
		for (Map.Entry<String, TextAttributeReference> entry : map.entrySet()) {
			if (entry.getValue().get() == null) {
				throw new BadDescriptorException("Bad definition for token colorer identifier " + entry.getKey());
			}
		}
	}
	
	public void register(String name, TextAttributeReference color) {
		map.put(name, color);
	}
}
