package org.strategoxt.imp.runtime;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.model.ISourceProject;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public interface ISourceInfo {
	// TODO: Just use a IResource or IFile instead of ISourceInfo
	//       now that it no longer provides the active language
	
	IPath getPath();
	
	IResource getResource();
	
	ISourceProject getProject();
}
