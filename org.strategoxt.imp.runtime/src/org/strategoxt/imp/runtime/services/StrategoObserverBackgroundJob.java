package org.strategoxt.imp.runtime.services;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.imp.parser.IParseController;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.stratego.EditorIOAgent;

public class StrategoObserverBackgroundJob implements StrategoAnalysisJob {

	IProject project;
	StrategoProgressMonitor progress;
	StrategoObserver observer;
	String strategyName;
	IStrategoTerm term;
	Descriptor descriptor;
	
	public StrategoObserverBackgroundJob(String strategyName, IStrategoTerm term, Descriptor descriptor) {
		this.strategyName = strategyName;
		this.term = term;
		this.descriptor = descriptor;
	}
	
	/**
	 * Setup
	 */
	public void setup(IProject project) {
		this.project = project;
	}
	
	public IStatus analyze(IProgressMonitor monitor) {
		
		this.progress = new StrategoProgressMonitor(monitor);
		
		try {
			
			// Get parse controller
			observer = descriptor.createService(StrategoObserver.class, null);
			observer.getLock().lock();
			
			((EditorIOAgent)observer.getRuntime().getIOAgent()).setJob(this);
			observer.invoke(strategyName, term, project);
		    
		} catch (Exception e) {
			Environment.logException("Background job failed", e);
		} finally {
			if (observer != null) observer.getLock().unlock();
		}
		
		return Status.OK_STATUS;
		
	}

	public IPath getPath() {
		return project.getFullPath();
	}

	public StrategoProgressMonitor getProgressMonitor() {
		return progress;
	}

	public StrategoObserver getObserver() {
		return this.observer;
	}

	public IParseController getParseController() {
		return null;
	}

}
