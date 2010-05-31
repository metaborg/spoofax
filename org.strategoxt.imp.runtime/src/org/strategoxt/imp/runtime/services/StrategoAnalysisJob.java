package org.strategoxt.imp.runtime.services;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.imp.parser.IParseController;

public interface StrategoAnalysisJob {

	public IPath getPath();
	public StrategoProgressMonitor getProgressMonitor();
	public StrategoObserver getObserver();
	public IParseController getParseController();

	public IStatus analyze(IProgressMonitor monitor);
	
}
