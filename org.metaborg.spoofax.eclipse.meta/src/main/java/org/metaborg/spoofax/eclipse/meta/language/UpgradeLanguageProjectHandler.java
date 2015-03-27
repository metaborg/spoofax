package org.metaborg.spoofax.eclipse.meta.language;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.eclipse.meta.SpoofaxMetaPlugin;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.metaborg.spoofax.eclipse.util.AbstractHandlerUtils;

import com.google.inject.Injector;

public class UpgradeLanguageProjectHandler extends AbstractHandler {
    private final IEclipseResourceService resourceService;
    private final ITermFactoryService termFactoryService;


    public UpgradeLanguageProjectHandler() {
        final Injector injector = SpoofaxMetaPlugin.injector();
        this.resourceService = injector.getInstance(IEclipseResourceService.class);
        this.termFactoryService = injector.getInstance(ITermFactoryService.class);
    }


    @Override public Object execute(ExecutionEvent event) throws ExecutionException {
        final IProject project = AbstractHandlerUtils.getProjectFromSelected(event);
        if(project == null) {
            return null;
        }

        final UpgradeLanguageProjectWizard wizard =
            new UpgradeLanguageProjectWizard(resourceService, termFactoryService, project);
        final Shell shell = HandlerUtil.getActiveWorkbenchWindow(event).getShell();
        final WizardDialog dialog = new WizardDialog(shell, wizard);
        dialog.open();
        return null;
    }
}
