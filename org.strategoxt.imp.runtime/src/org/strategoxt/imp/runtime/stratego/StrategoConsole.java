package org.strategoxt.imp.runtime.stratego;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.progress.UIJob;
import org.spoofax.interpreter.ui.SpoofaxConsole;
import org.spoofax.interpreter.ui.SpoofaxConsoleFactory;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;

/**
 * The Stratego console.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StrategoConsole {
	
	private static IOConsole lastConsole;
	
	private static AutoFlushOutputStreamWriter lastConsoleOutputWriter;
	
	private static AutoFlushOutputStreamWriter lastConsoleErrorWriter;

	public static Writer getErrorWriter() {
		IOConsole console = getConsole();
		if (console == lastConsole && lastConsoleErrorWriter != null) {
			return lastConsoleErrorWriter;
		} else {
			IOConsoleOutputStream stream = console.newOutputStream();
			// A red color doesn't seem to make sense for Stratego
			// stream.setColor(DebugUIPlugin.getPreferenceColor(IDebugPreferenceConstants.CONSOLE_SYS_ERR_COLOR));
			lastConsoleErrorWriter = new AutoFlushOutputStreamWriter(stream);
			lastConsole = console;
			return lastConsoleErrorWriter;
		}
	}

	public static Writer getOutputWriter() {
		IOConsole console = getConsole();
		if (console == lastConsole && lastConsoleOutputWriter != null) {
			return lastConsoleOutputWriter;
		} else {
			lastConsoleOutputWriter = new AutoFlushOutputStreamWriter(console.newOutputStream());
			lastConsole = console;
			return lastConsoleOutputWriter;
		}
	}
	
	public static OutputStream getErrorStream() {
		return ((AutoFlushOutputStreamWriter) getErrorWriter()).stream;
	}
	
	public static OutputStream getOutputStream() {
		return ((AutoFlushOutputStreamWriter) getOutputWriter()).stream;
	}

	/**
	 * Gets or opens the Eclipse console for this plugin.
	 */
	private static synchronized IOConsole getConsole() {
		SpoofaxConsoleFactory factory = new SpoofaxConsoleFactory();
		SpoofaxConsole result = factory.getExistingConsole(); 
		if (result == null) {
			factory.openConsole();
			result = factory.getExistingConsole();
		}
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
				IOConsole console = StrategoConsole.getConsole();
				if (consoleViewOnly) {
					console.activate();
					return Status.OK_STATUS;
				}
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (window == null) return Status.OK_STATUS; // Eclipse exiting
				IWorkbenchPage page = window.getActivePage();
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

	/**
	 * An OutputStreamWriter that automatically flushes its buffer.
	 * 
	 * @author Lennart Kats <lennart add lclnet.nl>
	 */
	private static class AutoFlushOutputStreamWriter extends OutputStreamWriter {
		
		final OutputStream stream;

		public AutoFlushOutputStreamWriter(OutputStream stream) {
			super(stream);
			this.stream = stream;
		}
		
		@Override
		public void write(String str, int off, int len) throws IOException {
			super.write(str, off, len);
			super.flush();
		}
		
		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
			super.write(cbuf, off, len);
			super.flush();
		}
		
		@Override
		public void write(int c) throws IOException {
			super.write(c);
			super.flush();
		}
		
	}
}
