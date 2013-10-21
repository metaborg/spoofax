package org.strategoxt.imp.runtime.services;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchWindow;
import org.strategoxt.imp.runtime.RuntimeActivator;

public abstract class ToolbarButtonDelegate extends AbstractHandler {

	protected static String lastAction;
	private Menu menu;

	public ToolbarButtonDelegate() {
		super();
	}
	
	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub		
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		run(null);
		return null;
	}

	protected abstract void run(IAction action);

	protected abstract void populateMenu(Menu menu);

	public void selectionChanged(IAction action, ISelection selection) {
		// Unused
	}

	@Override
	public void dispose() {
		if (menu != null) {
			menu.dispose();
			menu = null;
		}
		super.dispose();
	}

	public Menu getMenu(Control parent) {
		if (menu != null) {
			menu.dispose();
		}
		menu = new Menu(parent);
		populateMenu(menu);
		return menu;
	}
	
	protected void openError(String message) {
		Status status = new Status(IStatus.ERROR, RuntimeActivator.PLUGIN_ID, message);
		ErrorDialog.openError(null, "Spoofax/IMP builder", null, status);
	}
}