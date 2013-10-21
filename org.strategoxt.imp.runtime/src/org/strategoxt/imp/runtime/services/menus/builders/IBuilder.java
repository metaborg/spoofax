package org.strategoxt.imp.runtime.services.menus.builders;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.jobs.Job;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.EditorState;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public interface IBuilder extends IMenuContribution {
	
	Job scheduleExecute(EditorState editor, IStrategoTerm ast, IFile errorReportFile, boolean isRebuild);

	Object getData();
	
	void setData(Object data);
	
	List<String> getPath();
}
