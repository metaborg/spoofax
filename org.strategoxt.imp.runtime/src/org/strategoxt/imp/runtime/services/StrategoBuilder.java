package org.strategoxt.imp.runtime.services;

import static org.spoofax.interpreter.core.Tools.asJavaString;
import static org.spoofax.interpreter.core.Tools.isTermAppl;
import static org.spoofax.interpreter.core.Tools.isTermString;
import static org.spoofax.interpreter.core.Tools.isTermTuple;
import static org.spoofax.interpreter.core.Tools.termAt;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CancellationException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.progress.UIJob;
import org.spoofax.interpreter.core.InterpreterErrorExit;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.InterpreterExit;
import org.spoofax.interpreter.core.UndefinedStrategyException;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.MonitorStateWatchDog;
import org.strategoxt.imp.runtime.RuntimeActivator;
import org.strategoxt.imp.runtime.dynamicloading.TermReader;
import org.strategoxt.imp.runtime.stratego.EditorIOAgent;
import org.strategoxt.imp.runtime.stratego.SourceAttachment;
import org.strategoxt.imp.runtime.stratego.StrategoConsole;
import org.strategoxt.lang.Context;
import org.strategoxt.stratego_aterm.aterm_escape_strings_0_0;
import org.strategoxt.stratego_aterm.pp_aterm_box_0_0;
import org.strategoxt.stratego_gpp.box2text_string_0_1;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StrategoBuilder implements IBuilder {
	
	private final StrategoObserver observer;

	private final String caption;
	
	private String builderRule;
	
	private final boolean realTime;
	
	private final boolean openEditor;
	
	private final boolean cursor;
	
	private final boolean source;
	
	@SuppressWarnings("unused")
	private final boolean persistent;
	
	private final EditorState derivedFromEditor;
	
	// Since StrategoBuilders are not persistent (per constructor of BuilderFactory)
	// we maintain a map with running jobs in a static field
	private static Map<String, Job> activeJobs = new WeakHashMap<String, Job>();
	
	/**
	 * Creates a new Stratego builder.
	 * 
	 * @param derivedFromEditor  The editor the present editor is derived from, if the present editor is an IStrategoTerm editor.
	 */
	public StrategoBuilder(StrategoObserver observer, String caption, String builderRule,
			boolean openEditor, boolean realTime, boolean cursor, boolean source, boolean persistent,
			EditorState derivedFromEditor) {
		
		this.observer = observer;
		this.caption = caption;
		this.builderRule = builderRule;
		this.openEditor = openEditor;
		this.realTime = realTime;
		this.cursor = cursor;
		this.source = source;
		this.persistent = persistent;
		this.derivedFromEditor = derivedFromEditor;
	}
	
	public String getCaption() {
		return caption;
	}
	
	public Object getData() {
		// Data not used for normal builders
		return null;
	}
	
	public void setData(Object data) {
		// Data not used for normal builders
	}
	
	public String getBuilderRule() {
		return builderRule;
	}
	
	protected StrategoObserver getObserver() {
		return observer;
	}
	
	protected EditorState getDerivedFromEditor() {
		return derivedFromEditor;
	}
	
	protected void setBuilderRule(String builderRule) {
		this.builderRule = builderRule;
	}
	
	public Job scheduleExecute(final EditorState editor, IStrategoTerm node,
			final IFile errorReportFile, final boolean isRebuild) {

		String displayCaption = caption.endsWith("...")
			? caption.substring(caption.length() - 3)
			: caption;
		
		Job lastJob = activeJobs.get(caption);
		if (lastJob != null && lastJob.getState() != Job.NONE) {
			if (!isRebuild)
				openError(editor, "Already running: " + displayCaption);
			return null;
		}
		
		if (node == null) {
			node = editor.getSelectionAst(!cursor);
			if (node == null) node = editor.getParseController().getCurrentAst();
		}
		
		final IStrategoTerm node2 = node;
			
		Job job = new Job("Executing " + displayCaption) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				MonitorStateWatchDog protector = new MonitorStateWatchDog(this, monitor, observer);
				try {
					execute(editor, node2, errorReportFile, isRebuild);
					return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
				} finally {
					protector.endProtect();
				}
			}
		};
		job.setUser(true);
		job.schedule();
		activeJobs.put(caption, job);
		return job;
	}
	
	private void execute(EditorState editor, IStrategoTerm node, IFile errorReportFile, boolean isRebuild) {
		// TODO: refactor
		assert derivedFromEditor == null || editor.getDescriptor().isATermEditor();
		IFile file = null;
		String result = null;
		String errorReport = null;
		
		observer.getLock().lock();
		try {
			try {
				if (node == null) {
					openError(editor, "Editor is still parsing or analyzing");
					return;
				}
				
				IStrategoTerm resultTerm = invokeObserver(node);
				if (resultTerm == null) {
					observer.reportRewritingFailed();
					Environment.logException("Builder failed:\n" + observer.getLog());
					if (!observer.isUpdateScheduled())
						observer.scheduleUpdate(editor.getParseController());
					openError(editor, "Builder failed (see error log)");
					return;
				}
		
				if (isTermAppl(resultTerm) && "None".equals(TermReader.cons(resultTerm))) {
					return;
				} else if (!isTermTuple(resultTerm) || !isTermString(termAt(resultTerm, 0))) {
					Environment.logException("Illegal builder result (must be a filename/string tuple or None())");
					openError(editor, "Illegal builder result (must be a filename/string tuple or None()): " + resultTerm);
					return;
				}
	
				file = getFile(resultTerm);
				result = getResultString(resultTerm);
				
			} catch (InterpreterErrorExit e) {
				errorReport = reportErrorExit(e, editor, errorReportFile);
			} catch (UndefinedStrategyException e) {
				reportGenericException(editor, e);
			} catch (InterpreterExit e) {
				reportGenericException(editor, e);
			} catch (InterpreterException e) {
				reportGenericException(editor, e);
			} catch (CancellationException e) {
				return;
			} catch (FileNotFoundException e) {
				reportGenericException(editor, e);
			} catch (RuntimeException e) {
				reportGenericException(editor, e);
			} catch (Error e) {
				reportGenericException(editor, e);
			}
		} finally {
			observer.getLock().unlock();
		}

		try {
			if (errorReport != null) {
				setFileContents(editor, errorReportFile, errorReport);
			}
		
			if (result != null) {
				setFileContents(editor, file, result);
				// TODO: if not persistent, create IEditorInput from result String
				if (openEditor && !isRebuild) {
					scheduleOpenEditorAndListener(editor, node, file);
				}
			}
		} catch (CoreException e) {
			Environment.logException("Builder failed", e);
			openError(editor, "Builder failed (" + e.getClass().getName() + "; see error log): " + e.getMessage());
		}
	}

	private String reportErrorExit(InterpreterErrorExit e, EditorState editor, IFile errorReportFile) {
		Environment.logException("Builder failed:\n" + observer.getLog(), e);
		if (editor.getDescriptor().isDynamicallyLoaded()) StrategoConsole.activateConsole();
		if (errorReportFile == null || !openEditor) {
			openError(editor, e.getMessage());
			return null;
		} else {
			return e.getLocalizedMessage();
		}
	}

	private IFile getFile(IStrategoTerm resultTerm) throws FileNotFoundException {
		String filename = asJavaString(termAt(resultTerm, 0));
		IFile result = EditorIOAgent.getFile(
				observer.getRuntime().getContext(), filename);
		return result;
	}

	private String getResultString(IStrategoTerm resultTerm) {
		resultTerm = termAt(resultTerm, 1);
		
		return isTermString(resultTerm) ? asJavaString(resultTerm) : ppATerm(resultTerm).stringValue();
	}

	private void scheduleOpenEditorAndListener(final EditorState editor, final IStrategoTerm node, final IFile file)
			throws PartInitException {
		
		Job job = new UIJob("Opening editor") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				try {
					IEditorPart target = openEditor(file, realTime);
				
					// UNDONE: don't delete non-persistent files for now since it causes problem with workspace auto-refresh
					// if (!persistent) new File(file.getLocationURI()).delete();
					// Create a listener *and* editor-derived editor relation
					StrategoBuilderListener listener = 
						StrategoBuilderListener.addListener(editor.getEditor(), target, file, StrategoBuilder.this, node);
					if (!realTime || editor == target || derivedFromEditor != null)
						listener.setEnabled(false);
					if (derivedFromEditor != null) // ensure we get builders from the source
						listener.setSourceEditor(derivedFromEditor.getEditor());
				} catch (PartInitException e) {
					Environment.logException("Builder failed", e);
					openError(editor, "Builder failed (" + e.getClass().getName() + "; see error log): " + e.getMessage());
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	protected IStrategoTerm invokeObserver(IStrategoTerm node) throws UndefinedStrategyException,
			InterpreterErrorExit, InterpreterExit, InterpreterException {
		
		node = InputTermBuilder.getMatchingAncestor(node, false);
		IStrategoTerm inputTerm = derivedFromEditor != null
				? observer.getInputBuilder().makeATermInputTerm(node, true, derivedFromEditor.getResource()) 
				: observer.getInputBuilder().makeInputTerm(node, true, source);
		IStrategoTerm result = observer.invoke(builderRule, inputTerm, SourceAttachment.getResource(node));
		return result;
	}

	private IStrategoString ppATerm(IStrategoTerm term) {
		Context context = observer.getRuntime().getCompiledContext();
		term = aterm_escape_strings_0_0.instance.invoke(context, term);
		term = pp_aterm_box_0_0.instance.invoke(context, term);
		term = box2text_string_0_1.instance.invoke(context, term, Environment.getTermFactory().makeInt(80));
		return (IStrategoString) term;
	}

	private void reportGenericException(EditorState editor, Throwable e) {
		boolean isDynamic = editor.getDescriptor().isDynamicallyLoaded();
		Environment.logException("Builder failed for " + (isDynamic ? "" : "non-") + "dynamically loaded editor", e);
		if (isDynamic) {
			Writer writer = observer.getRuntime().getIOAgent().getWriter(IOAgent.CONST_STDERR);
			PrintWriter printWriter = new PrintWriter(writer);
			e.printStackTrace(printWriter);
			printWriter.flush();
			StrategoConsole.activateConsole();
		}
		
		if (EditorState.isUIThread()) {
			// Only show if builder runs interactively (and not from the StrategoBuilderListener background builder)
			String message = e.getLocalizedMessage() == null ? e.getMessage() : e.getLocalizedMessage();
			Status status = new Status(IStatus.ERROR, RuntimeActivator.PLUGIN_ID, message, e);
			ErrorDialog.openError(editor.getEditor().getSite().getShell(), caption, null, status);
		}
	}
	
	private void openError(final EditorState editor, final String message) {
		Job job = new UIJob("Reporting error") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				Status status = new Status(IStatus.ERROR, RuntimeActivator.PLUGIN_ID, message);
				ErrorDialog.openError(editor.getEditor().getSite().getShell(),
						caption, null, status);
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	private void setFileContents(final EditorState editor, IFile file, final String contents) throws CoreException {
		assert !observer.getLock().isHeldByCurrentThread() && !Environment.getStrategoLock().isHeldByCurrentThread()
			: "Acquiring a resource lock can cause a deadlock";

		/* TODO: update editor contents instead of file?
		if (file.exists()):
		if (editor.getEditor().getTitleImage().isDisposed()) {
			InputStream resultStream = new ByteArrayInputStream(contents.getBytes());
			file.setContents(resultStream, true, true, null);
			...save...
		} else {
			Job job = new UIJob("Update derived editor") {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					try {
						editor.getDocument().set(contents);
						...save...
		                ...ensure listener knows updated time stamp...
					} catch (RuntimeException e) {
						Environment.logException("Could not update derived editor", e);
					}
					return Status.OK_STATUS;
				}
			};
			job.setSystem(true);
			job.schedule();
		}
		*/
		setFileContentsDirect(file, contents);
	}

	public static void setFileContentsDirect(IFile file, final String contents) throws CoreException {
		assert !Environment.getStrategoLock().isHeldByCurrentThread();
		InputStream resultStream;
		try {
			resultStream = new ByteArrayInputStream(contents.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		if (file.exists()) {
			// UNDONE: file.setCharset("UTF-8", null); // not allowed for existing file?
			file.setContents(resultStream, true, true, null);
		} else {
			createDirs(file.getParent());
			file.create(resultStream, true, null);
			// UNDONE: file.setDerived(!persistent); // marks it as "derived" for life, even after editing...
		}
	}
	
	private static void createDirs(IContainer dir) throws CoreException {
		assert !Environment.getStrategoLock().isHeldByCurrentThread();
		if (dir == null) {
			return;
		} else if (!dir.exists()) {
			createDirs(dir.getParent());
			dir.refreshLocal(0, new NullProgressMonitor());
			if (!dir.exists()) {
				dir.getLocation().toFile().mkdir();
				dir.refreshLocal(0, new NullProgressMonitor());
			}
		}
	}

	/**
	 * Opens or activates an editor.
	 * (Asynchronous) exceptions are swallowed and logged.
	 */
	private IEditorPart openEditor(IFile file, boolean realTime) throws PartInitException {
		assert !observer.getLock().isHeldByCurrentThread() || Environment.getStrategoLock().isHeldByCurrentThread()
			: "Opening a new editor and acquiring a resource lock can cause a deadlock";
		
		// TODO: non-persistent editor: WorkBenchPage.openEditor with a custom IEditorInput?
		IWorkbenchPage page =
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		
		SidePaneEditorHelper sidePane = null;
		
		if (realTime) {
			try {
				sidePane = SidePaneEditorHelper.openSidePane();
			} catch (Throwable t) {
				// org.eclipse.ui.internal API might have changed
				Environment.logException("Unable to open side pane", t);
			}
		}
		
		IEditorPart result = null;
		try {
			result = IDE.openEditor(page, file, !realTime);
			if (sidePane != null) sidePane.setOpenedEditor(result);
		} finally {
			if (result == null && sidePane != null) sidePane.undoOpenSidePane();
		}
		
		if (sidePane != null) sidePane.restoreFocus();
		
		return result;
	}
	
	@Override
	public String toString() {
		return "Builder: " + builderRule + " - " + caption; 
	}
}
