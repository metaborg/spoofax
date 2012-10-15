package org.strategoxt.imp.runtime.services;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * @author Maartje de Jonge
 */
public interface IStrategoRefactoringInput {

	public abstract void setInputArea(Composite result, ModifyListener modListener);

	public abstract RefactoringStatus validateUserInput();

	public abstract IStrategoTerm getInputValue();

}