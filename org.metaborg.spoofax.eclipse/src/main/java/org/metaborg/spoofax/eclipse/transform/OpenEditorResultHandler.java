package org.metaborg.spoofax.eclipse.transform;

import org.apache.commons.vfs2.FileObject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.transform.ITransformerGoal;
import org.metaborg.spoofax.core.transform.NamedGoal;
import org.metaborg.spoofax.core.transform.TransformResult;
import org.metaborg.spoofax.core.transform.stratego.IStrategoTransformerResultHandler;
import org.metaborg.spoofax.core.transform.stratego.StrategoTransformerCommon;
import org.metaborg.spoofax.core.transform.stratego.menu.Action;
import org.metaborg.spoofax.core.transform.stratego.menu.MenusFacet;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;

public class OpenEditorResultHandler implements IStrategoTransformerResultHandler {
    private static final Logger logger = LoggerFactory.getLogger(OpenEditorResultHandler.class);

    private final IEclipseResourceService resourceService;

    private final StrategoTransformerCommon transformer;


    @Inject public OpenEditorResultHandler(IEclipseResourceService resourceService,
        StrategoTransformerCommon transformer) {
        this.resourceService = resourceService;
        this.transformer = transformer;
    }


    @Override public void handle(TransformResult<?, IStrategoTerm> result, ITransformerGoal goal) {
        final FileObject resource = transformer.builderWriteResult(result.result, result.context.location());
        if(openEditor(resource, result.context.language(), goal)) {
            final IResource eclipseResource = resourceService.unresolve(resource);
            if(eclipseResource instanceof IFile) {
                final IFile file = (IFile) eclipseResource;
                // Run in the UI thread because we need to get the active workbench window and page.
                final Display display = Display.getDefault();
                display.asyncExec(new Runnable() {
                    @Override public void run() {
                        final IWorkbenchPage page =
                            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                        try {
                            IDE.openEditor(page, file);
                        } catch(PartInitException e) {
                            logger.error("Cannot open editor", e);
                        }
                    }
                });
            }
        }
    }

    private boolean openEditor(FileObject resource, ILanguage language, ITransformerGoal goal) {
        if(resource == null) {
            return false;
        }

        if(goal instanceof NamedGoal) {
            final MenusFacet facet = language.facet(MenusFacet.class);
            if(facet == null) {
                return false;
            }
            final NamedGoal namedGoal = (NamedGoal) goal;
            final Action action = facet.action(namedGoal.name);
            if(action == null) {
                return false;
            }
            return action.flags.openEditor;
        }
        return false;
    }
}
