package org.strategoxt.imp.runtime.services;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.imp.model.ModelFactory;
import org.eclipse.imp.parser.IParseController;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.stratego.EditorIOAgent;

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
			Descriptor descriptor = Environment.getDescriptor(lang); 
			
			// Get parse controller
			parseController = descriptor.createService(SGLRParseController.class, null);
			observer = descriptor.createService(StrategoObserver.class, parseController);
			
			observer.getLock().lock();
			try {
				((EditorIOAgent)observer.getRuntime().getIOAgent()).setJob(this);
				// Don't perform initial (foreground) update
				parseController.setPerformInitialUpdate(false);
				
				// Read file
				File file = absolutePath.toFile();
				byte[] buffer = new byte[(int) file.length()];
			    BufferedInputStream f = new BufferedInputStream(new FileInputStream(file));
			    f.read(buffer);
			    String input = new String(buffer);
			
			    // Parse file
			    ISourceProject sproject = ModelFactory.open(project);
			    
			    parseController.initialize(path, sproject, null);
			    parseController.parse(input, new NullProgressMonitor());
				
			    observer.update(parseController, monitor);
			} finally {
				observer.getLock().unlock();
			}
		    
		} catch (Exception e) {
			// hmm.
			e.printStackTrace();
		}
		
		return Status.OK_STATUS;
		
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
