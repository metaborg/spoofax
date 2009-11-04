package org.strategoxt.imp.runtime.stratego;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
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
import org.spoofax.interpreter.library.LoggingIOAgent;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;

/**
 * This class overrides the default IOAgent to support attached files in editor plugins,
 * and may redirect any disk reads to the Eclipse API. 
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class EditorIOAgent extends LoggingIOAgent {
	
	private static final String CONSOLE_NAME = "Spoofax/IMP console";
	
	private static MessageConsole lastConsole;
	
	private static PrintStream lastConsoleOutputStream;
	
	private static PrintStream lastConsoleErrorStream;
	
	private Descriptor descriptor;
	
	public void setDescriptor(Descriptor descriptor) {
		this.descriptor = descriptor;
	}
	
	public Descriptor getDescriptor() {
		return descriptor;
	}
	
	@Override
	public InputStream openInputStream(String path, boolean isDefinitionFile)
			throws FileNotFoundException {
		
		if (isDefinitionFile && descriptor != null) {
			return openAttachedFile(path);
		} else {
			return super.openInputStream(path, isDefinitionFile);
		}
	}
	
	private InputStream openAttachedFile(String path) throws FileNotFoundException {
		try {
			return descriptor.openAttachment(path);
		} catch (FileNotFoundException e) {
			File localFile = new File(path);
			if (localFile.exists()) {
				Debug.log("Reading file form the current directory: ", path);  
				return new BufferedInputStream(new FileInputStream(localFile));
			} else {
				throw e;
			}
		}
	}
	
	@Override
	public PrintStream getOutputStream(int fd) {
		// TODO: close console streams after use?
		if (fd == CONST_STDOUT && descriptor != null && descriptor.isDynamicallyLoaded()) {
			MessageConsole console = getConsole();
			if (console == lastConsole) {
				return lastConsoleOutputStream;
			} else {
				lastConsole = console;
				lastConsoleOutputStream = new PrintStream(console.newOutputStream());
				return lastConsoleOutputStream;
			}
		} else if (fd == CONST_STDERR && descriptor != null && descriptor.isDynamicallyLoaded()) {
			MessageConsole console = getConsole();
			if (console == lastConsole) {
				return lastConsoleErrorStream;
			} else {
				lastConsole = console;
				IOConsoleOutputStream stream = console.newOutputStream();
				// A red color doesn't seem to make sense for Stratego
				// stream.setColor(DebugUIPlugin.getPreferenceColor(IDebugPreferenceConstants.CONSOLE_SYS_ERR_COLOR));
				lastConsoleErrorStream = new PrintStream(stream);
				return lastConsoleErrorStream;
			}
		} else {
			return super.getOutputStream(fd);
		}
	}
	
	/**
	 * Gets or opens the Eclipse console for this plugin.
	 */
	public static MessageConsole getConsole() {
		IConsoleManager consoles = ConsolePlugin.getDefault().getConsoleManager();
		for (IConsole console: consoles.getConsoles()) {
			if (CONSOLE_NAME.equals(console.getName()))
				return (MessageConsole) console;
		}
		// No console found, so create a new one
		MessageConsole result = new MessageConsole(CONSOLE_NAME, null);
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
		Job job = new UIJob("Open console") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				final String ID = IConsoleConstants.ID_CONSOLE_VIEW;
				IConsole console = getConsole();
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
		job.schedule();
	}
}
