package org.strategoxt.imp.metatooling.stratego;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.RegisteringStrategy;

public class LibraryInitializer extends org.strategoxt.lang.LibraryInitializer {

	@Override
	protected List<RegisteringStrategy> getLibraryStrategies() {
		return Arrays.<RegisteringStrategy> asList(SDFBundleCommand.instance, IMPParseStrategoFileStrategy.instance);
	}

	@Override
	protected void initializeLibrary(Context context) {
		try {
			SDFBundleCommand.instance.init();
		} catch (IOException e) {
			Environment.logException("Could not determine the binary path for the native tool bundle ("
					+ Platform.getOS() + "/" + Platform.getOSArch() + ")", e);
		} catch (RuntimeException e) {
			Environment.logException("Failed to initialize the native tool bundle (" + Platform.getOS() + "/"
					+ Platform.getOSArch() + ")", e);
		}
	}

}
