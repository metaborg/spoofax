package org.strategoxt.imp.runtime;

import java.io.FileNotFoundException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.imp.model.ModelFactory;
import org.eclipse.imp.model.ModelFactory.ModelException;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.dynamicloading.DynamicParseController;
import org.strategoxt.imp.runtime.editor.SpoofaxEditor;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.services.StrategoObserver;
import org.strategoxt.imp.runtime.stratego.EditorIOAgent;


/**
 * Helper class for accessing a file in some language,
 * that may not necessarily be opened in an editor.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class FileState {
	
	private final Descriptor descriptor;

	private final SGLRParseController controller;
	
	private final IResource resource;
	
	protected FileState(Descriptor descriptor, SGLRParseController controller, IResource resource) {
		this.descriptor = descriptor;
		this.controller = controller;
		this.resource = resource;
	}
	
	protected FileState(Descriptor descriptor) {
		this(descriptor, null, null); // HACK: subclass don't use these
	}
	
	/**
	 * @param path      The path for the file (see {@link Path}).
	 * @param document  The document for this file, or null.
	 */
	public static FileState getFile(IPath path, IDocument document)
			throws FileNotFoundException, BadDescriptorException, ModelException {
		Language language = LanguageRegistry.findLanguage(path, document);
		if(language == null)
			return null;
		Descriptor descriptor = Environment.getDescriptor(language);
		IResource resource = EditorIOAgent.getResource(path.toFile());
		if (descriptor == null) return null;
		return new FileState(descriptor, getParseController(descriptor, resource), resource);
	}
	
	private static SGLRParseController getParseController(Descriptor descriptor, IResource resource) 
			throws BadDescriptorException, ModelException {
		
		IParseController controller = descriptor.createParseController();
		if (controller instanceof DynamicParseController)
			controller = ((DynamicParseController) controller).getWrapped();
		if (controller instanceof SGLRParseController) {
			ISourceProject project = ModelFactory.open(resource.getProject());
			controller.initialize(resource.getProjectRelativePath(), project, null);
			return (SGLRParseController) controller;
		} else {
			throw new BadDescriptorException("SGLRParseController expected: " + controller.getClass().getName());
		}
	}
	
	public IStrategoTerm getAnalyzedAst() throws BadDescriptorException {
		StrategoObserver observer = getDescriptor().createService(StrategoObserver.class, getParseController());
		observer.getLock().lock();
		try {
			observer.update(getParseController(), new NullProgressMonitor());
			return observer.getResultingAst(getResource());
		} finally {
			observer.getLock().unlock();
		}
	}
	
	public IStrategoTerm getCurrentAnalyzedAst() throws BadDescriptorException {
		StrategoObserver observer = getDescriptor().createService(StrategoObserver.class, getParseController());
		
		observer.getLock().lock();
		try {
			return observer.getResultingAst(getResource());
		} finally {
			observer.getLock().unlock();
		}
	}
	
	public Descriptor getDescriptor() {
		return descriptor;
	}
	
	public Language getLanguage() throws BadDescriptorException {
		return getDescriptor().getLanguage();
	}
	
	public SGLRParseController getParseController() {
		return controller;
	}
	
	public IResource getResource() {
		return resource;
	}
	
	public final ISourceProject getProject() {
		return getParseController().getProject();
	}
	
	/**
	 * @see SGLRParseController#getCurrentAst
	 */
	public final IStrategoTerm getCurrentAst() {
		return getParseController().getCurrentAst();
	}

	/**
	 * Asynchronously opens or activates an editor and jump to specified offset.
	 * 
	 * Exceptions are swallowed and logged.
	 */
	public static void asyncOpenEditor(Display display, final IFile file, final int offset, final boolean activate) {
		display.asyncExec(new Runnable() {
			public void run() {
				openEditor(file, offset, activate);
			}
		});
	}

	/**
	 * Opens a new editor. Must be invoked from the UI thread.
	 * 
	 * PartInitExceptions are swallowed and logged.
	 */
	public static void openEditor(final IFile file, final boolean activate) {
		if (!isUIThread())
			throw new IllegalStateException("Must be called from UI thread");
		
		IWorkbenchPage page =
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			IDE.openEditor(page, file, SpoofaxEditor.EDITOR_ID, activate);
		} catch (PartInitException e) {
			Environment.logException("Cannot open an editor for " + file, e);
		}
	}

	/**
	 * Asynchronously opens or activates an editor.
	 * 
	 * Exceptions are swallowed and logged.
	 */
	public static void asyncOpenEditor(Display display, final IFile file, final boolean activate) {
		display.asyncExec(new Runnable() {
			public void run() {
				openEditor(file, activate);
			}
		});
	}
	
	/**
	 * Opens a new editor and jump to offset. Must be invoked from the UI thread.
	 * 
	 * PartInitExceptions are swallowed and logged.
	 */
	public static void openEditor(final IFile file, final int offset, final boolean activate) {
		if (!isUIThread())
			throw new IllegalStateException("Must be called from UI thread");
		
		IWorkbenchPage page =
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			ITextEditor ite = (ITextEditor) IDE.openEditor(page, file, SpoofaxEditor.EDITOR_ID, activate);
			ite.selectAndReveal(offset, 0);
		} catch (PartInitException e) {
			Environment.logException("Cannot open an editor for " + file, e);
		}
	}
	
	public static boolean isUIThread() {
		// return Display.getCurrent() != null; // may exist in multiple threads
		try {
			return PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null;
		} catch (IllegalStateException e) {
			// Eclipse not running
			return false;
		}
	}
}
