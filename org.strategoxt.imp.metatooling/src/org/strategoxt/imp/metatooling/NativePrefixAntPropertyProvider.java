package org.strategoxt.imp.metatooling;

import java.io.IOException;

import org.eclipse.ant.core.IAntPropertyValueProvider;
import org.strategoxt.imp.metatooling.stratego.SDFBundleCommand;
import org.strategoxt.imp.runtime.Environment;
import org.syntax_definition.sdf.Activator;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class NativePrefixAntPropertyProvider implements IAntPropertyValueProvider {

	public String getAntPropertyValue(String antPropertyName) {
		try {
			// Ensure proper chmod first
			SDFBundleCommand.getInstance().init();
			return Activator.getInstance().getBinaryPrefix().getParentFile().getAbsolutePath();
		} catch (IOException e) {
			Environment.logException("Could not determine the prefix path for the native tool bundle", e);
			return ".";
		}
	}
	
}
