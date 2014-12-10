package org.strategoxt.imp.runtime;

import static org.strategoxt.imp.runtime.dynamicloading.TermReader.*;

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
import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
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
		checkJVMOptions();
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

	private final static String DEFAULT_SS_OPT = "-Xss8m";
	
	private final static String DEFAULT_MX_OPT = "-Xmx1024m";
	
	private final static boolean DEFAULT_SERVER_OPT = true;
	
	private final static String DEFAULT_LANGUAGE_NAME = "Spoofax";

	/**
	 * Checks Eclipse's JVM command-line options. Can only be called after
	 * RuntimeActivator has been initialized. Should be called asynchronously
	 * from the UI thread otherwise will block.
	 */
	private void checkJVMOptions() {
		String currentSsOpt = null;
		String currentMxOpt = null;
		String highestSsOpt = null; // highest ss among all languages
		String highestMxOpt = null; // highest mx among all languages
		String languageName = null;

		boolean serverOpt = false;
		boolean correctJavaVersion = false;
		final RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();

		for (String arg : runtime.getInputArguments()) {
			if (arg.startsWith("-Xss") || arg.startsWith("-ss"))
				currentSsOpt = arg;
			if (arg.startsWith("-Xmx") || arg.startsWith("-mx"))
				currentMxOpt = arg;
		}
		
		final String javaVersion = runtime.getSpecVersion();
		if (runtime.getSpecVersion().contains("1.7")
				|| runtime.getSpecVersion().contains("1.8"))
			correctJavaVersion = true;

		int languageCount = 0;

		for (Language l : LanguageRegistry.getLanguages()) {
			String name = l.getName();

			if (name.equals("DynamicRoot"))
				continue;
			
			languageCount++;

			Descriptor d = Environment.getDescriptor(l);
			if (d == null)
				continue;

			IStrategoAppl esv = d.getDocument();
			if (esv == null)
				continue;

			String ssOpt = null;
			String mxOpt = null;

			if (findTerm(esv, "JvmOpts") == null) {
			  if (DEFAULT_SERVER_OPT) {
			    serverOpt = true;
			  }
				ssOpt = DEFAULT_SS_OPT;
				mxOpt = DEFAULT_MX_OPT;
			} else {       
        IStrategoTerm serverOptT = findTerm(esv, "ServerOpt");
        if (serverOptT != null) {
          serverOpt = true;
        }
        
				IStrategoTerm ssOptT = findTerm(esv, "XssOpt");
				if (ssOptT != null) {
					ssOpt = termContents(ssOptT);
				}

				IStrategoTerm mxOptT = findTerm(esv, "XmxOpt");
				if (mxOptT != null) {
					mxOpt = termContents(mxOptT);
				}
			}

			highestMxOpt = maxOpt(highestMxOpt, mxOpt);
			highestSsOpt = maxOpt(highestSsOpt, ssOpt);
			languageName = name;
		}

		if (languageCount > 1)
			languageName = DEFAULT_LANGUAGE_NAME;

		boolean showServerOpt = serverOpt && !runtime.getVmName().contains("Server");
		boolean showMxOpt = !maxOpt(currentMxOpt, highestMxOpt).equals(
				currentMxOpt);
		boolean showSsOpt = !maxOpt(currentSsOpt, highestSsOpt).equals(
				currentSsOpt);

		if (showServerOpt || showMxOpt || showSsOpt || !correctJavaVersion) {
			final String JVM_OPTS_DIAG_TITLE = languageName
					+ " configuration warning";

			final String JVM_OPT_DIAG_MSG_JAVA_VER = languageName
					+ " requires Eclipse to be started with a Java 7 (or higher) VM. Ensure that Java 7 is installed on your system. If you have multiple VMs installed on your system, you can force Eclipse to start with a specific VM by adding the following option to the eclipse.ini file:\n-vm\n<path-to-java-bin>\n\nThis option needs to be on the first line in the eclipse.ini file and the newline after -vm is required.";

			final String JVM_OPTS_DIAG_TOG_MSG = "Don't warn me anymore until next "
					+ languageName + " version";

			final String version = context.getBundle().getVersion().toString();
			final Preferences prefs = Platform.getPreferencesService()
					.getRootNode().node(Plugin.PLUGIN_PREFERENCE_SCOPE)
					.node(PLUGIN_ID);
			final StringBuilder msgBuilder = new StringBuilder();

			if (!prefs.get(LAST_JVM_OPT_CHECK, "").equalsIgnoreCase(version)) {

				if (showServerOpt || showMxOpt || showSsOpt) {

					if (showServerOpt)
						Environment
								.logWarning("Make sure Eclipse is started with -vmargs -server (can be set in eclipse.ini) for best performance");
					if (showMxOpt)
						Environment
								.logWarning("Make sure Eclipse is started with -vmargs "
										+ highestMxOpt
										+ " (can be set in eclipse.ini) to increase the maximum heap space (adjust downwards for low-memory systems)");
					if (showSsOpt)
						Environment
								.logWarning("Make sure Eclipse is started with -vmargs "
										+ highestSsOpt
										+ " (can be set in eclipse.ini) to increase the stack size");

					msgBuilder
							.append(languageName
									+ " needs Eclipse to be started with (can be set in eclipse.ini):\n-vmargs");

					if (showServerOpt)
						msgBuilder.append(" -server");
					if (highestMxOpt != null)
						msgBuilder.append(" " + highestMxOpt);
					if (highestSsOpt != null)
						msgBuilder.append(" " + highestSsOpt);

					msgBuilder
							.append("\n\nThe following options are currently missing:");

					if (showServerOpt)
						msgBuilder.append("\n-server");
					if (showMxOpt)
						msgBuilder.append("\n" + highestMxOpt);
					if (showSsOpt)
						msgBuilder.append("\n" + highestSsOpt);

					if (!correctJavaVersion)
						msgBuilder.append("\n\n");
				}

				if (!correctJavaVersion) {
					msgBuilder.append(JVM_OPT_DIAG_MSG_JAVA_VER);
				}

				getWorkbench().getDisplay().asyncExec(new Runnable() {
					public void run() {
						final MessageDialogWithToggle diag = MessageDialogWithToggle
								.openWarning(PlatformUI.getWorkbench()
										.getDisplay().getActiveShell(),
										JVM_OPTS_DIAG_TITLE,
										msgBuilder.toString(),
										JVM_OPTS_DIAG_TOG_MSG, false, null,
										null);

						if (diag.getToggleState()) {
							prefs.put(LAST_JVM_OPT_CHECK, javaVersion);
							try {
								prefs.flush();
							} catch (BackingStoreException e) {
								Environment.logException(
										"Could not save preference store", e);
							}
						}
					}
				});
			}
		}
	}

	private static String maxOpt(String opt1, String opt2) {
		if (opt1 == null)
			return opt2;
		if (opt2 == null)
			return opt1;
		return sizeInKiloByte(opt1) >= sizeInKiloByte(opt2) ? opt1 : opt2;
	}

	private static int sizeInKiloByte(String opt) {
		int i = 0;
		while (!Character.isDigit(opt.charAt(i)))
			i++;
		int value = Integer.parseInt(opt.substring(i, opt.length() - 1));
		char unit = opt.toUpperCase().toCharArray()[opt.length() - 1];
		switch (unit) {
		case 'K':
			return value;
		case 'M':
			return value * 1024;
		case 'G':
			return value * 1024 * 1024;
		default:
			return -1;
		}
	}

	public static RuntimeActivator getInstance() {
		return plugin;
	}

	public static InputStream getResourceAsStream(String string)
			throws IOException {
		URL url = FileLocator.find(RuntimeActivator.getInstance().getBundle(),
				new Path(string), null);

		if (url != null)
			return url.openStream();

		// In Java 5, the above approach doesn't seem to work
		InputStream result = RuntimeActivator.class.getResourceAsStream(string);

		if (result == null)
			throw new FileNotFoundException("Resource not found '" + string
					+ "'");

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
