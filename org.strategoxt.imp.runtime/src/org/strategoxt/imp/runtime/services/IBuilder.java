package org.strategoxt.imp.runtime.services;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.stratego.adapter.IStrategoAstNode;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public interface IBuilder {

	String getCaption();
	
	void setOpenEditorEnabled(boolean openEditor);
	
	boolean isOpenEditorEnabled();
	
	void execute(EditorState editor, IStrategoAstNode node) throws CoreException;
	
	IAction toAction(EditorState editor);
}
