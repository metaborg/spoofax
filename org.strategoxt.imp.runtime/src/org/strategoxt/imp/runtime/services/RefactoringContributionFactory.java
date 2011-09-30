package org.strategoxt.imp.runtime.services;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.RuntimeActivator;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;

public class RefactoringContributionFactory extends ExtensionContributionFactory {
	
	public RefactoringContributionFactory() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
		final EditorState editor = EditorState.getActiveEditor();
		if (editor == null) return;
		IRefactoringMap refactorings = getRefactorings(editor);
		if (refactorings.getAll().size() == 0) return;
		MenuManager submenu = new MenuManager("Refactor");
		additions.addContributionItem(submenu, null);		
		for (final IRefactoring refactoring : refactorings.getAll()) {
			IAction action = refactoring.getAction();
			action.setEnabled(refactoring.isDefinedOnSelection(editor));
			submenu.add(action);
		}
	}
	
	private IRefactoringMap getRefactorings(EditorState editor) {
		IRefactoringMap refactorings;
		try {
			refactorings = editor.getDescriptor().createService(IRefactoringMap.class, editor.getParseController());
		} catch (BadDescriptorException e) {
			Environment.logException("Could not load refactorings", e);
			openError("Could not load refactorings");
			throw new RuntimeException(e);
		}
		return refactorings;
	}

	protected void openError(String message) {
		Status status = new Status(IStatus.ERROR, RuntimeActivator.PLUGIN_ID, message);
		ErrorDialog.openError(null, "Spoofax/IMP builder", null, status);
	}
}
