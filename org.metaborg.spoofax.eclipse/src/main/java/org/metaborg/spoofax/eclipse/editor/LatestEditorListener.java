package org.metaborg.spoofax.eclipse.editor;

import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Keeps track of the latest active Spoofax editor.
 */
public class LatestEditorListener implements IWindowListener, IPartListener2 {
    private SpoofaxEditor latestEditor;


    public void register() {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        final IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
        for(IWorkbenchWindow window : windows) {
            window.getPartService().addPartListener(this);
        }
        workbench.addWindowListener(this);
        trySetActive(workbench.getActiveWorkbenchWindow().getPartService().getActivePart());
    }

    public SpoofaxEditor latestActive() {
        return latestEditor;
    }

    private void trySetActive(IWorkbenchPart part) {
        if(part == null)
            return;
        if(part instanceof SpoofaxEditor)
            latestEditor = (SpoofaxEditor) part;
    }


    @Override public void windowActivated(IWorkbenchWindow window) {
        trySetActive(window.getPartService().getActivePart());
    }

    @Override public void windowDeactivated(IWorkbenchWindow window) {

    }

    @Override public void windowClosed(IWorkbenchWindow window) {
        window.getPartService().removePartListener(this);
    }

    @Override public void windowOpened(IWorkbenchWindow window) {
        window.getPartService().addPartListener(this);
    }



    @Override public void partActivated(IWorkbenchPartReference partRef) {
        trySetActive(partRef.getPart(false));
    }

    @Override public void partBroughtToTop(IWorkbenchPartReference partRef) {

    }

    @Override public void partClosed(IWorkbenchPartReference partRef) {

    }

    @Override public void partDeactivated(IWorkbenchPartReference partRef) {

    }

    @Override public void partOpened(IWorkbenchPartReference partRef) {

    }

    @Override public void partHidden(IWorkbenchPartReference partRef) {

    }

    @Override public void partVisible(IWorkbenchPartReference partRef) {

    }

    @Override public void partInputChanged(IWorkbenchPartReference partRef) {

    }
}
