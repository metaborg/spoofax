package org.strategoxt.imp.runtime.services;

import org.strategoxt.imp.runtime.EditorState;

/**
 * @author Maartje de Jonge
 */
public interface IRefactoring {

	String getCaption();
	
	String getActionDefinitionId();
	
	String getName();

	void prepareExecute(EditorState editor);

}
