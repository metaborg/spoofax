package org.strategoxt.imp.metatooling;

import org.eclipse.ant.core.IAntPropertyValueProvider;
import org.eclipse.core.runtime.Platform;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StrategoJarAntPropertyProvider implements IAntPropertyValueProvider {
	
	public String getAntPropertyValue(String antPropertyName) {
		String result = org.strategoxt.stratego_lib.Main.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			// FIXME: proper paths on Windows
			result = result.substring(1);
		}
		return result;
	}
}
