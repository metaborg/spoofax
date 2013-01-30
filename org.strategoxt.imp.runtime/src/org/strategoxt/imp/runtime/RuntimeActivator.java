package org.strategoxt.imp.runtime;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.strategoxt.imp.generator.sdf2imp;
import org.strategoxt.imp.runtime.stratego.FileNotificationServer;
import org.strategoxt.lang.StrategoExit;
import org.strategoxt.strj.strj;

public class RuntimeActivator extends AbstractUIPlugin {
	
	public static final String PLUGIN_ID = "org.strategoxt.imp.runtime"; 

	private static RuntimeActivator instance; 
	
	public RuntimeActivator() {
		instance = this;

		FileNotificationServer.init();
	}
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
//		precacheStratego();
		checkJVMOptions();
	}

//	/**
//	 * Make sure strj and sdf2imp run at least once
//	 * to speed up first project build or project wizard.
//	 * @deprecated
//	 * 	Does not seem to be necessary anymore. Loading is really fast on most machines.
//	 */
//	private void precacheStratego() {
//		Job job = new Job("Spoofax/Stratego initialization") {
//			@Override
//			protected IStatus run(IProgressMonitor monitor) {
//				try {
//					Debug.startTimer();
//					Environment.getStrategoLock().lock();
//					try {
//						strj.mainNoExit("--version");
//					} catch (StrategoExit e) {
//						// Success!
//					}
//					try {
//						sdf2imp.mainNoExit("--version");
//					} catch (StrategoExit e) {
//						// Success!
//					}
//					Debug.stopTimer("Pre-initialized Stratego compiler");
//				} finally {
//					Environment.getStrategoLock().unlock();
//				}
//				return Status.OK_STATUS;
//			}
//		};
//		job.setSystem(true);
//		job.schedule();
//	}

	/**
	 * Checks Eclipse's JVM command-line options.
	 * Can only be called after RuntimeActivator has been initialized.
	 */
	private void checkJVMOptions() {
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
	
	public static void tryLog(IStatus status) {
		RuntimeActivator instance = getInstance();
		if (instance != null && instance.getBundle() != null) {
			try {
				instance.getLog().log(status);
			} catch (RuntimeException e) {
				System.err.println("Logged exception:");
				e.printStackTrace();
			}
		} else {
			System.err.println("Logged exception:");
			status.getException().printStackTrace();
		}
	}
}

