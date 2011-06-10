package org.strategoxt.imp.runtime.services;

import java.util.regex.Pattern;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;


public class StrategoRefactoringWizard extends RefactoringWizard {
	
	RefactoringPageTextField inputPage;
	
	public StrategoRefactoringWizard(StrategoRefactoring refactoring, String pageTitle, Pattern idPattern) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
		setDefaultPageTitle(pageTitle);
		inputPage = new RefactoringPageTextField(idPattern);
	}

	@Override
	protected void addUserInputPages() {
		addPage(inputPage);
	}
}
