package org.strategoxt.imp.metatooling;

import java.io.IOException;

import org.eclipse.ant.core.IAntPropertyValueProvider;
import org.strategoxt.imp.nativebundle.SDFBundleCommand;
import org.strategoxt.imp.runtime.Environment;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class NativePrefixAntPropertyProvider implements IAntPropertyValueProvider {

	public String getAntPropertyValue(String antPropertyName) {		
		try {
			// Ensure proper chmod first
			return SDFBundleCommand.getInstance().getBinaryPath();
		} catch (IOException e) {
			Environment.logException("Could not determine the prefix path for the native tool bundle", e);
			return ".";
		}
	}
	
}
