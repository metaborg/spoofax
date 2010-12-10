package org.strategoxt.imp.runtime.services;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.jobs.Job;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.stratego.adapter.IStrategoAstNode;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public interface IBuilder {

	String getCaption();
	
	Job scheduleExecute(EditorState editor, IStrategoAstNode ast, IFile errorReportFile, boolean isRebuild);

	Object getData();
	
	void setData(Object data);
}
