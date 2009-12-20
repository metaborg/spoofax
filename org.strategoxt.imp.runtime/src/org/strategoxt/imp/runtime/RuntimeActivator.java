package org.strategoxt.imp.runtime;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class RuntimeActivator extends AbstractUIPlugin {
	
	public static final String PLUGIN_ID = new String("org.strategoxt.imp.runtime"); 

	private static RuntimeActivator instance; 
	
	public RuntimeActivator() {
		instance = this;
	}

	public static RuntimeActivator getInstance() { 
		return instance;
	}

	public static InputStream getResourceAsStream(String string) throws IOException {
        URL url = FileLocator.find(RuntimeActivator.getInstance().getBundle(), new Path(string), null);
        
        if (url != null)
        	return url.openStream();
        
        // In Java 5, the above approach doesn't seem to work         
        InputStream result = RuntimeActivator.class.getResourceAsStream(string);
        
        if (result == null)
        	throw new FileNotFoundException("Resource not found '" + string + "'");
        
        return result;
	}
}
