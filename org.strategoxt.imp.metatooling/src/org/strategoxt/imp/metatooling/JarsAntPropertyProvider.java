package org.strategoxt.imp.metatooling;

import java.io.File;
import java.net.URISyntaxException;

import org.eclipse.ant.core.IAntPropertyValueProvider;
import org.strategoxt.permissivegrammars.make_permissive;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class JarsAntPropertyProvider implements IAntPropertyValueProvider {
	
	public String getAntPropertyValue(String antPropertyName) {
		String result;
		try {
			result = new File(make_permissive.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
		} catch (URISyntaxException e) {
			throw new RuntimeException("Could not locate Spoofax/IMP jar files", e);
		}
		return result;
	}
}
