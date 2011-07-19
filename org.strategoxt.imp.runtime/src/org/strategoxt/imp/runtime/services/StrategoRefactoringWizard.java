package org.strategoxt.imp.runtime.services;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;


public class StrategoRefactoringWizard extends RefactoringWizard {
	
	StrategoRefactoringPage inputPage;
	
	public StrategoRefactoringWizard(StrategoRefactoring refactoring, String pageTitle) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
		setDefaultPageTitle(pageTitle);
		inputPage = new StrategoRefactoringPage();
	}

	@Override
	protected void addUserInputPages() {
		if(hasUserInputValues())
			addPage(inputPage);
	}

	private boolean hasUserInputValues() {
		return ((StrategoRefactoring)getRefactoring()).getInputFields().size() > 0;
	}
}
