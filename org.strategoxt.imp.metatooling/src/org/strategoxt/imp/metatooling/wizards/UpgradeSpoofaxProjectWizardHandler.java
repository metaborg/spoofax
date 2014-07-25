package org.strategoxt.imp.metatooling.wizards;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

public class UpgradeSpoofaxProjectWizardHandler extends AbstractHandler {
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        final IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
        final Object selected = selection.getFirstElement();
        final IProject project = (IProject) selected;
        final UpgradeSpoofaxProjectWizard wizard = new UpgradeSpoofaxProjectWizard(project);
        final Shell shell = HandlerUtil.getActiveWorkbenchWindow(event).getShell();
        final WizardDialog dialog = new WizardDialog(shell, wizard);
        dialog.open();
        return null;
    }
}
