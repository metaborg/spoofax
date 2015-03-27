package org.metaborg.spoofax.eclipse.meta.issue;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class ReportIssueHandler extends AbstractHandler {
    @Override public Object execute(ExecutionEvent event) throws ExecutionException {
        Display.getDefault().asyncExec(new OpenDialogRunnable());
        return null;
    }


    private final class OpenDialogRunnable implements Runnable {
        public void run() {
            final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
            final ReportIssueDialog dialog = new ReportIssueDialog(shell);
            dialog.open();
        }
    }
}
