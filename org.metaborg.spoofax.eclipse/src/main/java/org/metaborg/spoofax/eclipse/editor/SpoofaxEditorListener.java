package org.metaborg.spoofax.eclipse.editor;

import java.util.Set;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.metaborg.spoofax.eclipse.util.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * Keeps track of all Spoofax editors, which one is currently active, and which one was active previously.
 */
public class SpoofaxEditorListener implements IWindowListener, IPartListener2, ISpoofaxEditorListener {
    private static final Logger logger = LoggerFactory.getLogger(SpoofaxEditorListener.class);

    public static final String contextId = ISpoofaxEclipseEditor.id + ".context";

    private IContextService contextService;
    private IContextActivation contextActivation;

    private volatile Set<ISpoofaxEclipseEditor> editors = Sets.newConcurrentHashSet();
    private volatile ISpoofaxEclipseEditor currentActive;
    private volatile ISpoofaxEclipseEditor previousActive;


    @Override public void register() {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        contextService = (IContextService) workbench.getService(IContextService.class);

        Display.getDefault().asyncExec(new Runnable() {
            @Override public void run() {
                final IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
                for(IWorkbenchWindow window : windows) {
                    windowOpened(window);
                    for(IWorkbenchPage page : window.getPages()) {
                        for(IEditorReference editorRef : page.getEditorReferences()) {
                            final ISpoofaxEclipseEditor editor = get(editorRef);
                            if(editor != null) {
                                add(editor);
                            }
                        }
                    }
                }
                final IWorkbenchWindow activeWindow = workbench.getActiveWorkbenchWindow();
                if(activeWindow != null) {
                    final IWorkbenchPage activePage = activeWindow.getActivePage();
                    if(activePage != null) {
                        final IEditorPart activeEditorPart = activePage.getActiveEditor();
                        if(activeEditorPart != null) {
                            final ISpoofaxEclipseEditor editor = get(activeEditorPart);
                            if(editor != null) {
                                activate(editor);
                            }
                        }
                    }
                }
                workbench.addWindowListener(SpoofaxEditorListener.this);
            }
        });
    }


    @Override public Iterable<ISpoofaxEclipseEditor> openEditors() {
        return editors;
    }

    @Override public @Nullable ISpoofaxEclipseEditor currentEditor() {
        return currentActive;
    }

    @Override public @Nullable ISpoofaxEclipseEditor previousEditor() {
        return previousActive;
    }


    private ISpoofaxEclipseEditor get(IWorkbenchPartReference part) {
        return get(part.getPart(false));
    }

    private ISpoofaxEclipseEditor get(IWorkbenchPart part) {
        if(part instanceof ISpoofaxEclipseEditor) {
            return (ISpoofaxEclipseEditor) part;
        }
        return null;
    }

    private boolean isEditor(IWorkbenchPartReference part) {
        return part instanceof IEditorReference;
    }

    private void setCurrent(ISpoofaxEclipseEditor editor) {
        currentActive = editor;
        if(contextActivation == null) {
            contextActivation = contextService.activateContext(contextId);
        }
    }

    private void unsetCurrent() {
        currentActive = null;
        if(contextActivation != null) {
            contextService.deactivateContext(contextActivation);
            contextActivation = null;
        }
    }

    private void add(ISpoofaxEclipseEditor editor) {
        logger.trace("Adding {}", editor);
        editors.add(editor);
    }

    private void remove(ISpoofaxEclipseEditor editor) {
        logger.trace("Removing {}", editor);
        editors.remove(editor);
        if(currentActive == editor) {
            logger.trace("Unsetting active (by remove) {}", editor);
            unsetCurrent();
        }
        if(previousActive == editor) {
            logger.trace("Unsetting latest (by remove) {}", editor);
            previousActive = null;
        }
    }

    private void activate(ISpoofaxEclipseEditor editor) {
        logger.trace("Setting active {}", editor);
        setCurrent(editor);
        logger.trace("Setting latest {}", editor);
        previousActive = editor;
    }

    private void activateOther() {
        logger.trace("Unsetting active (by activate other) {}", currentActive);
        unsetCurrent();
        logger.trace("Unsetting latest (by activate other) {}", previousActive);
        previousActive = null;
    }

    private void deactivate(ISpoofaxEclipseEditor editor) {
        if(currentActive == editor) {
            logger.trace("Unsetting active (by deactivate) {}", currentActive);
            unsetCurrent();
        }
    }


    @Override public void windowActivated(IWorkbenchWindow window) {
        final ISpoofaxEclipseEditor editor = get(window.getPartService().getActivePart());
        if(editor == null) {
            return;
        }
        activate(editor);
    }

    @Override public void windowDeactivated(IWorkbenchWindow window) {
        final ISpoofaxEclipseEditor editor = get(window.getPartService().getActivePart());
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
        final ISpoofaxEclipseEditor editor = get(partRef);
        if(editor != null) {
            activate(editor);
        } else if(isEditor(partRef)) {
            activateOther();
        }
    }

    @Override public void partBroughtToTop(IWorkbenchPartReference partRef) {

    }

    @Override public void partClosed(IWorkbenchPartReference partRef) {
        final ISpoofaxEclipseEditor editor = get(partRef);
        if(editor != null) {
            remove(editor);
        }
    }

    @Override public void partDeactivated(IWorkbenchPartReference partRef) {
        final ISpoofaxEclipseEditor editor = get(partRef);
        if(editor != null) {
            deactivate(editor);
        }
    }

    @Override public void partOpened(IWorkbenchPartReference partRef) {
        final ISpoofaxEclipseEditor editor = get(partRef);
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
