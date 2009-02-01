package org.strategoxt.imp.metatooling;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class MetatoolingActivator extends AbstractUIPlugin {

	private static MetatoolingActivator instance; 
	
	public MetatoolingActivator() {
		instance = this;
	}

	public static MetatoolingActivator getDefault() { 
		return instance;
	}

	public static InputStream getResourceAsStream(String string) throws IOException {
        URL r = FileLocator.find(MetatoolingActivator.getDefault().getBundle(), new Path(string), null);
        if(r == null)
        	throw new FileNotFoundException("Resource not found '" + string + "'");
        return r.openStream();
	}
}
