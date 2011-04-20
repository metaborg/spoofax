package org.strategoxt.imp.runtime.services;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.imp.parser.IParseController;
import org.strategoxt.imp.runtime.stratego.EditorIOAgent;

public class StrategoObserverUpdateJob implements StrategoAnalysisJob {

	IParseController parseController;
	StrategoObserver observer;
	StrategoProgressMonitor progress;
	
	public StrategoObserverUpdateJob(StrategoObserver observer) {
		this.observer = observer;
	}
	
	public void setup(IParseController parseController) {
		this.parseController = parseController;
	}
	
	public IStatus analyze(IProgressMonitor monitor) {
		
		observer.getLock().lock();
		try {
			this.progress = new StrategoProgressMonitor(monitor);
			((EditorIOAgent)observer.getRuntime().getIOAgent()).setJob(this);
			observer.update(parseController, monitor);
			return Status.OK_STATUS;
		} finally {
			observer.getLock().unlock();
 		}
		
	}

	public IPath getPath() {
		return parseController.getPath();
	}

	public StrategoProgressMonitor getProgressMonitor() {
		return this.progress;
	}

	public StrategoObserver getObserver() {
		return observer;
	}

	public IParseController getParseController() {
		return parseController;
	}

}
