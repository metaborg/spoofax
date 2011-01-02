package org.strategoxt.imp.runtime.services;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.jobs.Job;
import org.spoofax.interpreter.terms.ISimpleTerm;
import org.strategoxt.imp.runtime.EditorState;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public interface IBuilder {

	String getCaption();
	
	Job scheduleExecute(EditorState editor, ISimpleTerm ast, IFile errorReportFile, boolean isRebuild);

	Object getData();
	
	void setData(Object data);
}
