package org.strategoxt.imp.runtime.services;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;

public class RefactoringButtonDelegate extends ToolbarButtonDelegate implements IWorkbenchWindowPulldownDelegate {

	@Override
	public void run(IAction action) {
		EditorState editor = EditorState.getActiveEditor();
		if (editor == null) {
			openError("No refactorings defined for this editor");
			return;
		}
		IRefactoringMap refactorings = getRefactorings(editor);
		IRefactoring refactoring = refactorings.get(lastAction);
		if (refactoring == null && refactorings.getAll().size() > 0) {
			refactoring = refactorings.getAll().iterator().next();
		}
		if (refactoring == null) {
			openError("No Refactorings defined for the current selection");
		} else {
			refactoring.getAction().run();
		}
	}

	@Override
	protected void populateMenu(Menu menu) {
		System.out.println("last action: "+lastAction);
		MenuItem dummy = new MenuItem(menu, SWT.PUSH);
		dummy.setText("No refactorings defined for the current selection");
		
		final EditorState editor = EditorState.getActiveEditor();
		if (editor == null) return;
		IRefactoringMap refactorings = getRefactorings(editor);
		if (refactorings.getAll().size() == 0) return;
		
		for (final IRefactoring refactoring : refactorings.getAll()) {
			IAction action = refactoring.getAction();
			ActionContributionItem item = new ActionContributionItem(action);
			item.getAction().setEnabled(refactoring.isDefinedOnSelection(editor));
			item.fill(menu, menu.getItemCount());
		}		
		dummy.dispose();
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
}
