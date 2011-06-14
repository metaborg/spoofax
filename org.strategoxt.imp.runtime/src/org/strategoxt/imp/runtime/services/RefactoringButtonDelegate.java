package org.strategoxt.imp.runtime.services;

import java.util.regex.Pattern;

import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.ServiceFactory;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;
import org.eclipse.ui.internal.KeyBindingService;
import org.spoofax.jsglr.client.KeywordRecognizer;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;

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
			executeRefactoring(editor, refactoring);
		}
	}

	private void executeRefactoring(EditorState editor, IRefactoring refactoring) {
		refactoring.prepareExecute(editor);
		StrategoRefactoringWizard wizard = new StrategoRefactoringWizard(
			(StrategoRefactoring) refactoring, 
			refactoring.getCaption(),
			getIdPattern(editor),
			getKeywordRecognizer(editor)
		);
		RefactoringWizardOpenOperation operation= new RefactoringWizardOpenOperation(wizard);
		Shell shell = editor.getEditor().getSite().getShell();
		try {
			operation.run(shell, refactoring.getCaption());
		} catch (InterruptedException exception) {
			// Do nothing
		}
	}
	
	private static Pattern getIdPattern(EditorState editor) {
		Descriptor descriptor = editor.getDescriptor();
		SyntaxProperties syntax = null;
		if (descriptor != null) {
			try {
				syntax = (SyntaxProperties) descriptor.createService(ILanguageSyntaxProperties.class, null);
			} catch (BadDescriptorException e) {
				Environment.logException("Could not read syntax properties", e);
				e.printStackTrace();
			}
		} 
		return syntax != null ? syntax.getIdentifierLexical() : null;
	}

	private KeywordRecognizer getKeywordRecognizer(EditorState editor) {
		try {
			return editor.getParseController().getParser().getParseTable().getKeywordRecognizer();
		}
		catch (Exception e){
			Environment.logException("Could not fetch keyword recognizer", e);
			e.printStackTrace();
			return null;
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
			IAction action = new Action(refactoring.getCaption()) {
				@Override
				public void run() {
					lastAction = refactoring.getCaption();
					executeRefactoring(editor, refactoring);
				}
			};
			//FIXME: keybindings now only work after the user has pressed the refactoring button once.
			action.setActionDefinitionId(refactoring.getActionDefinitionId());
			editor.getEditor().getSite().getKeyBindingService().registerAction(action);
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
