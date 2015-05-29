package org.metaborg.spoofax.eclipse.editor;

import java.util.Collection;
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
import org.metaborg.spoofax.core.editor.IEditor;
import org.metaborg.spoofax.eclipse.util.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Keeps track of all Spoofax editors, which one is currently active, and which one was active previously.
 */
public class SpoofaxEditorRegistry implements IWindowListener, IPartListener2, IEclipseEditorRegistry,
    IEclipseEditorRegistryInternal {
    private static final Logger logger = LoggerFactory.getLogger(SpoofaxEditorRegistry.class);

    public static final String contextId = IEclipseEditor.id + ".context";

    private IContextService contextService;
    private IContextActivation contextActivation;

    private volatile Set<IEclipseEditor> editors = Sets.newConcurrentHashSet();
    private volatile IEclipseEditor currentActive;
    private volatile IEclipseEditor previousActive;


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
                            final IEclipseEditor editor = get(editorRef);
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
                            final IEclipseEditor editor = get(activeEditorPart);
                            if(editor != null) {
                                activate(editor);
                            }
                        }
                    }
                }
                workbench.addWindowListener(SpoofaxEditorRegistry.this);
            }
        });
    }


    @Override public Iterable<IEditor> openEditors() {
        final Collection<IEditor> openEditors = Lists.newArrayListWithCapacity(editors.size());
        for(IEclipseEditor editor : editors) {
            openEditors.add(editor);
        }
        return openEditors;
    }

    @Override public Iterable<IEclipseEditor> openEclipseEditors() {
        return editors;
    }


    @Override public @Nullable IEclipseEditor currentEditor() {
        return currentActive;
    }

    @Override public @Nullable IEclipseEditor previousEditor() {
        return previousActive;
    }


    private IEclipseEditor get(IWorkbenchPartReference part) {
        return get(part.getPart(false));
    }

    private IEclipseEditor get(IWorkbenchPart part) {
        if(part instanceof IEclipseEditor) {
            return (IEclipseEditor) part;
        }
        return null;
    }

    private boolean isEditor(IWorkbenchPartReference part) {
        return part instanceof IEditorReference;
    }

    private void setCurrent(IEclipseEditor editor) {
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

    private void add(IEclipseEditor editor) {
        logger.trace("Adding {}", editor);
        editors.add(editor);
    }

    private void remove(IEclipseEditor editor) {
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

    private void activate(IEclipseEditor editor) {
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

    private void deactivate(IEclipseEditor editor) {
        if(currentActive == editor) {
            logger.trace("Unsetting active (by deactivate) {}", currentActive);
            unsetCurrent();
        }
    }


    @Override public void windowActivated(IWorkbenchWindow window) {
        final IEclipseEditor editor = get(window.getPartService().getActivePart());
        if(editor == null) {
            return;
        }
        activate(editor);
    }

    @Override public void windowDeactivated(IWorkbenchWindow window) {
        final IEclipseEditor editor = get(window.getPartService().getActivePart());
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
        final IEclipseEditor editor = get(partRef);
        if(editor != null) {
            activate(editor);
        } else if(isEditor(partRef)) {
            activateOther();
        }
    }

    @Override public void partBroughtToTop(IWorkbenchPartReference partRef) {

    }

    @Override public void partClosed(IWorkbenchPartReference partRef) {
        final IEclipseEditor editor = get(partRef);
        if(editor != null) {
            remove(editor);
        }
    }

    @Override public void partDeactivated(IWorkbenchPartReference partRef) {
        final IEclipseEditor editor = get(partRef);
        if(editor != null) {
            deactivate(editor);
        }
    }

    @Override public void partOpened(IWorkbenchPartReference partRef) {
        final IEclipseEditor editor = get(partRef);
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
