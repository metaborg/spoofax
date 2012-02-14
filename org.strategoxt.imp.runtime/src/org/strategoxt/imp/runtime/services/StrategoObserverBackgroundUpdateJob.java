package org.strategoxt.imp.runtime.services;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.imp.model.ModelFactory;
import org.eclipse.imp.model.ModelFactory.ModelException;
import org.eclipse.imp.parser.IParseController;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.dynamicloading.DynamicParseController;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.stratego.EditorIOAgent;
import org.strategoxt.imp.runtime.stratego.SourceAttachment;

public class StrategoObserverBackgroundUpdateJob implements StrategoAnalysisJob {

	private final IPath path;
	private final IProject project;
	private StrategoProgressMonitor progress;
	private StrategoObserver observer;
	private SGLRParseController parseController;
	
	public StrategoObserverBackgroundUpdateJob(IPath path, IProject project) {
		this.path = path;
		this.project = project;
	}
	
	public IStatus analyze(IProgressMonitor monitor) {
		
		this.progress = new StrategoProgressMonitor(monitor);
		
		try {
			
			IPath absolutePath = project == null ? path : project.getLocation().append(path);
			
			// Get descriptor
			Language lang = LanguageRegistry.findLanguage(absolutePath, null);
			if (lang == null) {
				Environment.logException("Could not determine language for queued analysis of " + absolutePath);
				return Status.OK_STATUS;
			}
			Descriptor descriptor = Environment.getDescriptor(lang); 
			
			// Get parse controller
			parseController = asSGLRParseController(descriptor.createParseController());
			observer = descriptor.createService(StrategoObserver.class, parseController);
			
			observer.getLock().lock();
			try {
				((EditorIOAgent)observer.getRuntime().getIOAgent()).setJob(this);
				// Don't perform initial (foreground) update
				parseController.setPerformInitialUpdate(false);
				
				// Read file
				File file = absolutePath.toFile();
				String input = readFile(file);
			
			    // Parse file
			    ISourceProject sproject = ModelFactory.open(project);
			    
			    parseController.initialize(path, sproject, null);
			    IStrategoTerm ast = parseController.parse(input, new NullProgressMonitor());
			    if (ast == null) {
			    	// HACK: default to () as the AST
			    	parseController.internalSetAst(makeFakeAST(parseController, file));
			    }
				
			    observer.update(parseController, monitor);
			} finally {
				observer.getLock().unlock();
			}
		    
		} catch (ModelException e) {
			Environment.logException("Background job failed", e);
		} catch (BadDescriptorException e) {
			Environment.logException("Background job failed", e);
		}
		
		return Status.OK_STATUS;
		
	}

	private SGLRParseController asSGLRParseController(IParseController controller) throws BadDescriptorException {
		if (controller instanceof DynamicParseController)
			controller = ((DynamicParseController) controller).getWrapped();
		if (controller instanceof SGLRParseController) {
			return (SGLRParseController) controller;
		} else {
			throw new BadDescriptorException("SGLRParseController expected: " + controller.getClass().getName());
		}
	}

	private static IStrategoTerm makeFakeAST(SGLRParseController controller, File file) {
		// HACK: make fake resource using internal API
		//       we really need to stop using the stupid IResource interface where possible
		IStrategoTerm result = Environment.getTermFactory().makeTuple();
		Workspace workspace = (Workspace) ResourcesPlugin.getWorkspace();
		IResource resource = workspace.newResource(new Path(file.getAbsolutePath()), IResource.FILE);
		SourceAttachment.putSource(result, resource, controller);
		return result;
	}

	private String readFile(File file) {
		if (file.exists()) {
			try {
				byte[] buffer = new byte[(int) file.length()];
			    BufferedInputStream f = new BufferedInputStream(new FileInputStream(file));
			    f.read(buffer);
			    return new String(buffer);
			} catch (IOException e) {
				Environment.logWarning("Could not read file", e);
				return ""; // treat as empty file
			}
		} else {
			return ""; // treat as empty file
		}
	}

	public IPath getPath() {
		return this.path;
	}

	public StrategoProgressMonitor getProgressMonitor() {
		return progress;
	}

	public StrategoObserver getObserver() {
		return this.observer;
	}

	public IParseController getParseController() {
		return parseController;
	}

}
