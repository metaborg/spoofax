package org.metaborg.spoofax.eclipse.editor;

import java.util.Set;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.metaborg.spoofax.eclipse.util.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * Keeps track of all Spoofax editors, which one is currently active, and which one was active previously.
 */
public class SpoofaxEditorListener implements IWindowListener, IPartListener2, ISpoofaxEditorListener {
    private static final Logger logger = LoggerFactory.getLogger(SpoofaxEditorListener.class);

    private Set<SpoofaxEditor> editors = Sets.newConcurrentHashSet();
    private volatile SpoofaxEditor currentActive;
    private volatile SpoofaxEditor previousActive;


    public void register() {
        Display.getDefault().asyncExec(new Runnable() {
            @Override public void run() {
                final IWorkbench workbench = PlatformUI.getWorkbench();
                final IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
                for(IWorkbenchWindow window : windows) {
                    windowOpened(window);
                    for(IWorkbenchPage page : window.getPages()) {
                        for(IEditorReference editorRef : page.getEditorReferences()) {
                            final SpoofaxEditor editor = get(editorRef);
                            if(editor != null) {
                                add(editor);
                            }
                        }
                    }
                }
                workbench.addWindowListener(SpoofaxEditorListener.this);
            }
        });
    }

    
    @Override public Iterable<SpoofaxEditor> openEditors() {
        return editors;
    }

    @Override public @Nullable SpoofaxEditor currentEditor() {
        return currentActive;
    }

    @Override public @Nullable SpoofaxEditor previousEditor() {
        return previousActive;
    }


    private SpoofaxEditor get(IWorkbenchPartReference part) {
        return get(part.getPart(false));
    }

    private SpoofaxEditor get(IWorkbenchPart part) {
        if(part instanceof SpoofaxEditor) {
            return (SpoofaxEditor) part;
        }
        return null;
    }

    private boolean isEditor(IWorkbenchPartReference part) {
        return part instanceof IEditorReference;
    }

    private void add(SpoofaxEditor editor) {
        logger.trace("Adding {}", editor);
        editors.add(editor);
    }

    private void remove(SpoofaxEditor editor) {
        logger.trace("Removing {}", editor);
        editors.remove(editor);
        if(currentActive == editor) {
            logger.trace("Unsetting active (by remove) {}", editor);
            currentActive = null;
        }
        if(previousActive == editor) {
            logger.trace("Unsetting latest (by remove) {}", editor);
            previousActive = null;
        }
    }

    private void activate(SpoofaxEditor editor) {
        logger.trace("Setting active {}", editor);
        currentActive = editor;
        logger.trace("Setting latest {}", editor);
        previousActive = editor;
    }

    private void activateOther() {
        logger.trace("Unsetting active (by activate other) {}", currentActive);
        currentActive = null;
        logger.trace("Unsetting latest (by activate other) {}", previousActive);
        previousActive = null;
    }

    private void deactivate(SpoofaxEditor editor) {
        if(currentActive == editor) {
            logger.trace("Unsetting active (by deactivate) {}", currentActive);
            currentActive = null;
        }
    }


    @Override public void windowActivated(IWorkbenchWindow window) {
        final SpoofaxEditor editor = get(window.getPartService().getActivePart());
        if(editor == null) {
            return;
        }
        activate(editor);
    }

    @Override public void windowDeactivated(IWorkbenchWindow window) {
        final SpoofaxEditor editor = get(window.getPartService().getActivePart());
        if(editor == null) {
            return;
        }
        deactivate(editor);
    }

    @Override public void windowOpened(IWorkbenchWindow window) {
        window.getPartService().addPartListener(this);
    }

    @Override public void windowClosed(IWorkbenchWindow window) {
        window.getPartService().removePartListener(this);
    }


    @Override public void partActivated(IWorkbenchPartReference partRef) {
        final SpoofaxEditor editor = get(partRef);
        if(editor != null) {
            activate(editor);
        } else if(isEditor(partRef)) {
            activateOther();
        }
    }

    @Override public void partBroughtToTop(IWorkbenchPartReference partRef) {

    }

    @Override public void partClosed(IWorkbenchPartReference partRef) {
        final SpoofaxEditor editor = get(partRef);
        if(editor != null) {
            remove(editor);
        }
    }

    @Override public void partDeactivated(IWorkbenchPartReference partRef) {
        final SpoofaxEditor editor = get(partRef);
        if(editor != null) {
            deactivate(editor);
        }
    }

    @Override public void partOpened(IWorkbenchPartReference partRef) {
        final SpoofaxEditor editor = get(partRef);
        if(editor != null) {
            add(editor);
        }
    }

    @Override public void partHidden(IWorkbenchPartReference partRef) {

    }

    @Override public void partVisible(IWorkbenchPartReference partRef) {

    }

    @Override public void partInputChanged(IWorkbenchPartReference partRef) {

    }
}
