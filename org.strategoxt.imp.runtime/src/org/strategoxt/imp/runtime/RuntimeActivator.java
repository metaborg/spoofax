package org.strategoxt.imp.runtime;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.strategoxt.imp.runtime.stratego.FileNotificationServer;

public class RuntimeActivator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.strategoxt.imp.runtime";

	public static final String LAST_JVM_OPT_CHECK = "lastJVMOptionCheckVersion";

	private static RuntimeActivator plugin;

	private static BundleContext context;

	@Override
	public void start(BundleContext ctx) throws Exception {
		super.start(ctx);
		plugin = this;
		context = ctx;
		FileNotificationServer.init();

		// precacheStratego();
		getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				checkJVMOptions();
			}
		});
	}

	// /**
	// * Make sure strj and sdf2imp run at least once
	// * to speed up first project build or project wizard.
	// * @deprecated
	// * Does not seem to be necessary anymore. Loading is really fast on most
	// machines.
	// */
	// private void precacheStratego() {
	// Job job = new Job("Spoofax/Stratego initialization") {
	// @Override
	// protected IStatus run(IProgressMonitor monitor) {
	// try {
	// Debug.startTimer();
	// Environment.getStrategoLock().lock();
	// try {
	// strj.mainNoExit("--version");
	// } catch (StrategoExit e) {
	// // Success!
	// }
	// try {
	// sdf2imp.mainNoExit("--version");
	// } catch (StrategoExit e) {
	// // Success!
	// }
	// Debug.stopTimer("Pre-initialized Stratego compiler");
	// } finally {
	// Environment.getStrategoLock().unlock();
	// }
	// return Status.OK_STATUS;
	// }
	// };
	// job.setSystem(true);
	// job.schedule();
	// }

	private final static String JVM_OPTS_DIAG_TITLE = "Spoofax configuration warning";

	private final static String JVM_OPT_DIAG_MSG_PREFIX =
		"Spoofax needs Eclipse to be started with (can be set in eclipse.ini):\n-vmargs -server -Xmx1024m -Xss8m\n\nThe following options are currently missing:";

	private final static String JVM_OPT_DIAG_MSG_JAVA_VER =
		"Spoofax requires Eclipse to be started with a Java 7 (or higher) VM. Ensure that Java 7 is installed on your system. If you have multiple VMs installed on your system, you can force Eclipse to start with a specific VM by adding the following option to the eclipse.ini file:\n-vm\n<path-to-java-bin>\n\nThis option needs to be on the first line in the eclipse.ini file and the newline after -vm is required.";

	private final static String JVM_OPTS_DIAG_TOG_MSG = "Don't warn me anymore until next Spoofax version";

	/**
	 * Checks Eclipse's JVM command-line options. Can only be called after RuntimeActivator has been initialized. Should
	 * be called asynchronously from the UI thread otherwise will block.
	 */
	private void checkJVMOptions() {
		boolean ssOption = false;
		boolean serverOption = false;
		boolean mxOption = false;
		boolean correctJavaVersion = false;
		final RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();

		for(String arg : runtime.getInputArguments()) {
			if(arg.startsWith("-Xss") || arg.startsWith("-ss"))
				ssOption = true;
			if(arg.startsWith("-Xmx") || arg.startsWith("-mx"))
				mxOption = true;
		}

		if(runtime.getVmName().contains("Server"))
			serverOption = true;

		final String javaVersion = runtime.getSpecVersion();
		if(runtime.getSpecVersion().contains("1.7") || runtime.getSpecVersion().contains("1.8"))
			correctJavaVersion = true;

		if(!serverOption || !mxOption || !ssOption || !correctJavaVersion) {
			final String version = context.getBundle().getVersion().toString();
			final Preferences prefs =
				Platform.getPreferencesService().getRootNode().node(Plugin.PLUGIN_PREFERENCE_SCOPE).node(PLUGIN_ID);
			final StringBuilder msgBuilder = new StringBuilder();

			if(!prefs.get(LAST_JVM_OPT_CHECK, "").equalsIgnoreCase(version)) {

				if(!serverOption || !mxOption || !ssOption) {
					msgBuilder.append(JVM_OPT_DIAG_MSG_PREFIX);

					if(!serverOption)
						Environment
							.logWarning("Make sure Eclipse is started with -vmargs -server (can be set in eclipse.ini) for best performance");
					if(!mxOption)
						Environment
							.logWarning("Make sure Eclipse is started with -vmargs -Xmx1024m (can be set in eclipse.ini) for at least 1024 MiB heap space (adjust downwards for low-memory systems)");
					if(!ssOption)
						Environment
							.logWarning("Make sure Eclipse is started with -vmargs -Xss8m (can be set in eclipse.ini) for an 8 MiB stack size");

					if(!serverOption)
						msgBuilder.append("\n-server");
					if(!mxOption)
						msgBuilder.append("\n-Xmx1024m");
					if(!ssOption)
						msgBuilder.append("\n-Xss8m");
					
					if(!correctJavaVersion)
						msgBuilder.append("\n\n");
				}

				if(!correctJavaVersion) {
					msgBuilder.append(JVM_OPT_DIAG_MSG_JAVA_VER);
				}

				final MessageDialogWithToggle diag =
					MessageDialogWithToggle.openWarning(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
						JVM_OPTS_DIAG_TITLE, msgBuilder.toString(), JVM_OPTS_DIAG_TOG_MSG, false, null, null);

				if(diag.getToggleState()) {
					prefs.put(LAST_JVM_OPT_CHECK, javaVersion);
					try {
						prefs.flush();
					} catch(BackingStoreException e) {
						Environment.logException("Could not save preference store", e);
					}
				}
			}
		}
	}

	public static RuntimeActivator getInstance() {
		return plugin;
	}

	public static InputStream getResourceAsStream(String string) throws IOException {
		URL url = FileLocator.find(RuntimeActivator.getInstance().getBundle(), new Path(string), null);

		if(url != null)
			return url.openStream();

		// In Java 5, the above approach doesn't seem to work
		InputStream result = RuntimeActivator.class.getResourceAsStream(string);

		if(result == null)
			throw new FileNotFoundException("Resource not found '" + string + "'");

		return result;
	}

	public static void tryLog(IStatus status) {
		RuntimeActivator instance = getInstance();
		if(instance != null && instance.getBundle() != null) {
			try {
				instance.getLog().log(status);
			} catch(RuntimeException e) {
				System.err.println("Logged exception:");
				e.printStackTrace();
			}
		} else {
			System.err.println("Logged exception:");
			status.getException().printStackTrace();
		}
	}
}
