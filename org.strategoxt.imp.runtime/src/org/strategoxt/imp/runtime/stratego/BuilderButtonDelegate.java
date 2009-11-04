package org.strategoxt.imp.runtime.stratego;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.services.IBuilder;
import org.strategoxt.imp.runtime.services.IBuilderMap;

/**
 * Implements a dropdown button with builder actions.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class BuilderButtonDelegate implements IWorkbenchWindowPulldownDelegate {
	
	// TODO: IWorkbenchWindowPulldownDelegate?
	
	private Menu result;

	public void init(IWorkbenchWindow window) {
		// Initialized using getMenu()
	}

	public void run(IAction action) {
		// Unused
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}
		
	private void populateMenu(Menu menu) {
		MenuItem dummy = new MenuItem(menu, SWT.PUSH);
		dummy.setText("No builders defined for this editor");
		
		EditorState editor = EditorState.getActiveEditor();
		if (editor == null) return;
		
		IBuilderMap builders;
		try {
			builders = editor.getDescriptor().createService(IBuilderMap.class);
		} catch (BadDescriptorException e) {
			Environment.logException("Could not load builder", e);
			// TODO: fix error dialog not showing?
			ErrorDialog.openError(null, "Spoofax/IMP building", "Could not load builders", Status.OK_STATUS); 
			return;
		}
		
		for (final IBuilder builder : builders.getAll()) {
			ActionContributionItem item = new ActionContributionItem(builder.toAction(editor));
			item.fill(menu, Action.AS_PUSH_BUTTON);
		}
		
		dummy.dispose();
	}
		
	public void dispose() {
		if (result != null) {
			result.dispose();
			result = null;
		}
	}

	public Menu getMenu(Control parent) {
		if (result != null) {
			result.dispose();
		}
		result = new Menu(parent);
		populateMenu(result);
		return result;
	}

}
