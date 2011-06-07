package org.strategoxt.imp.runtime.services;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;


public class StrategoRefactoringWizard extends RefactoringWizard {
	
	public StrategoRefactoringWizard(StrategoRefactoring refactoring, String pageTitle) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
		setDefaultPageTitle(pageTitle);
	}

	@Override
	protected void addUserInputPages() {
		addPage(new RefactoringPageTextField("SpoofaxRenameInputPage"));
	}


}
