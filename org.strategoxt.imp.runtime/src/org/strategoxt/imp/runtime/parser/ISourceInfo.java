package org.strategoxt.imp.runtime.parser;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.model.ISourceProject;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public interface ISourceInfo {
	IPath getPath();
	
	IResource getResource();
	
	ISourceProject getProject();
	
	Language getLanguage();
}
