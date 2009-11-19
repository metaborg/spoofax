package org.strategoxt.imp.metatooling;

import java.io.File;

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
		if (!result.endsWith(".jar")) { // ensure correct jar at development time
			String result2 = result + "/../strategoxt.jar";
			if (new File(result2).exists()) return result2;
		}
		return result;
	}
}
