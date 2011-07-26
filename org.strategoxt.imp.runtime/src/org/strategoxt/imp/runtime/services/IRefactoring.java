package org.strategoxt.imp.runtime.services;

import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.strategoxt.imp.runtime.EditorState;

/**
 * @author Maartje de Jonge
 */
public interface IRefactoring {

	String getCaption();
	
	String getActionDefinitionId();
	
	String getName();

	void prepareExecute(EditorState editor);
	
	boolean isDefinedOnSelection(EditorState editor);

	ArrayList<IPath> getRelativePathsOfAffectedFiles();

	void setAction(IAction action);

	IAction getAction();
}
