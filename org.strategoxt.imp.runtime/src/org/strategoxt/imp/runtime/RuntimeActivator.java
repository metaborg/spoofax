package org.strategoxt.imp.runtime;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.strategoxt.imp.runtime.stratego.FileNotificationServer;

public class RuntimeActivator extends AbstractUIPlugin {
	
	public static final String PLUGIN_ID = new String("org.strategoxt.imp.runtime"); 

	private static RuntimeActivator instance; 
	
	public RuntimeActivator() {
		instance = this;

		FileNotificationServer.init();
		checkJVMOptions();
		
		// Trigger static initialization in this safe context
		Environment.getStrategoLock(); 
	}

	private static void checkJVMOptions() {
		boolean ssOption = false;
		boolean serverOption = false;
		boolean mxOption = false;
		
		for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
			if (arg.startsWith("-Xserver") || arg.startsWith("-server")) serverOption = true;
			if (arg.startsWith("-Xss") || arg.startsWith("-ss")) ssOption = true;
			if (arg.startsWith("-Xmx") || arg.startsWith("-mx")) mxOption = true;
		}
		
		if (!serverOption)
			Environment.logWarning("Make sure Eclipse is started with -vmargs -server (can be set in eclipse.ini) for best performance");
		if (!mxOption)
			Environment.logWarning("Make sure Eclipse is started with -vmargs -Xmx1024m (can be set in eclipse.ini) for at least 1024 MiB heap space (adjust downwards for low-memory systems)");
		if (!ssOption)
			Environment.logWarning("Make sure Eclipse is started with -vmargs -Xss8m (can be set in eclipse.ini) for an 8 MiB stack size");
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
