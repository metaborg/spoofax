package org.strategoxt.imp.runtime.stratego;

import java.io.PrintStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.progress.UIJob;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;

/**
 * The Stratego console.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StrategoConsole {

	private static final String CONSOLE_NAME = "Spoofax/IMP console";
	
	private static MessageConsole lastConsole;
	
	private static PrintStream lastConsoleOutputStream;
	
	private static PrintStream lastConsoleErrorStream;

	public static PrintStream getErrorStream() {
		MessageConsole console = getConsole();
		if (console == lastConsole && lastConsoleErrorStream != null) {
			return lastConsoleErrorStream;
		} else {
			IOConsoleOutputStream stream = console.newOutputStream();
			// A red color doesn't seem to make sense for Stratego
			// stream.setColor(DebugUIPlugin.getPreferenceColor(IDebugPreferenceConstants.CONSOLE_SYS_ERR_COLOR));
			lastConsoleErrorStream = new PrintStream(stream);
			lastConsole = console;
			return lastConsoleErrorStream;
		}
	}

	public static PrintStream getOutputStream() {
		MessageConsole console = getConsole();
		if (console == lastConsole && lastConsoleOutputStream != null) {
			return lastConsoleOutputStream;
		} else {
			lastConsoleOutputStream = new PrintStream(console.newOutputStream());
			lastConsole = console;
			return lastConsoleOutputStream;
		}
	}

	/**
	 * Gets or opens the Eclipse console for this plugin.
	 */
	private static MessageConsole getConsole() {
		IConsoleManager consoles = ConsolePlugin.getDefault().getConsoleManager();
		for (IConsole console: consoles.getConsoles()) {
			if (StrategoConsole.CONSOLE_NAME.equals(console.getName()))
				return (MessageConsole) console;
		}
		// No console found, so create a new one
		MessageConsole result = new MessageConsole(StrategoConsole.CONSOLE_NAME, null);
		consoles.addConsoles(new IConsole[] { result });
		return result;
	}
	
	/**
	 * Activates the console for this plugin.
	 * 
	 * Swallows and logs any PartInitException.
	 * 
	 * @see Descriptor#isDynamicallyLoaded()  Should typically be checked before opening a console.
	 */
	public static void activateConsole() {
		activateConsole(false);
	}

	/**
	 * Activates the console for this plugin.
	 * 
	 * Swallows and logs any PartInitException.
	 * 
	 * @param consoleViewOnly
	 *            Only open the console within the console view; don't activate
	 *            the console view itself.
	 * 
	 * @see Descriptor#isDynamicallyLoaded() Should typically be checked before
	 *      opening a console.
	 */
	public static void activateConsole(final boolean consoleViewOnly) {
		Job job = new UIJob("Open console") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				final String ID = IConsoleConstants.ID_CONSOLE_VIEW;
				MessageConsole console = StrategoConsole.getConsole();
				if (consoleViewOnly) {
					console.activate();
					return Status.OK_STATUS;
				}
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IConsoleView view = (IConsoleView) page.showView(ID, null, IWorkbenchPage.VIEW_VISIBLE);
					view.display(console);
				} catch (PartInitException e) {
					Environment.logException("Could not activate the console", e);
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

}
