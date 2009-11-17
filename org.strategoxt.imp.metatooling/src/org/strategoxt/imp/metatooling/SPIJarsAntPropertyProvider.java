package org.strategoxt.imp.metatooling;

import org.eclipse.ant.core.IAntPropertyValueProvider;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class SPIJarsAntPropertyProvider implements IAntPropertyValueProvider {
	
	public String getAntPropertyValue(String antPropertyName) {
		/* TODO
		String result = new File(make_permissive.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			// FIXME: proper paths on Windows
			result = result.substring(1);
		}
		return result;
		*/
		return "<notimplemented>";
	}
}
