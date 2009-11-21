package org.strategoxt.imp.metatooling;

import java.io.File;

import org.eclipse.ant.core.IAntPropertyValueProvider;
import org.strategoxt.imp.generator.sdf2imp;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class JarsAntPropertyProvider implements IAntPropertyValueProvider {
	
	public String getAntPropertyValue(String antPropertyName) {
		String result;
		result = new File(sdf2imp.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getParent();
		return result;
	}
}
