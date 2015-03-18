package org.metaborg.spoofax.eclipse.language;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorRegistry;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.language.ResourceExtensionFacet;
import org.metaborg.spoofax.eclipse.editor.SpoofaxEditor;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.metaborg.spoofax.eclipse.util.EditorMappingUtils;
import org.metaborg.spoofax.eclipse.util.StatusUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

public class LanguageAddedJob extends Job {
    private static final Logger logger = LoggerFactory.getLogger(LanguageAddedJob.class);

    private final IEclipseResourceService resourceService;
    private final ILanguageIdentifierService languageIdentifier;

    private final IEditorRegistry editorRegistry;
    private final IWorkspace workspace;

    private final ILanguage language;


    public LanguageAddedJob(IEclipseResourceService resourceService, ILanguageIdentifierService languageIdentifier,
        IEditorRegistry editorRegistry, IWorkspace workspace, ILanguage language) {
        super("Language added");

        this.resourceService = resourceService;
        this.languageIdentifier = languageIdentifier;

        this.language = language;

        this.editorRegistry = editorRegistry;
        this.workspace = workspace;
    }


    @Override protected IStatus run(IProgressMonitor monitor) {
        logger.debug("Running language added job for {}", language);

        final Display display = Display.getDefault();

        // Add editor associations
        final ResourceExtensionFacet resourceExtensionsFacet = language.facet(ResourceExtensionFacet.class);
        if(resourceExtensionsFacet == null) {
            final String message =
                String.format("Cannot create editor association for %s, no resource extensions facet was found",
                    language);
            logger.error(message);
        } else {
            final Iterable<String> extensions = resourceExtensionsFacet.extensions();
            logger.debug("Associating extension(s) {} to Spoofax editor", Joiner.on(", ").join(extensions));
            display.asyncExec(new Runnable() {
                @Override public void run() {
                    EditorMappingUtils.set(editorRegistry, SpoofaxEditor.id, extensions);
                }
            });
        }

        // Disable marking resources as changed for now, since it will cause many resources to be re-built
        // unnecessarily.

        // try {
        // final Collection<IResource> resources =
        // WorkspaceUtils.languageResources(resourceService, languageIdentifier, language, workspace.getRoot());
        // logger.debug("Marking {} workspace resources as changed", resources.size());
        // for(IResource resource : resources) {
        // try {
        // resource.touch(monitor);
        // } catch(CoreException e) {
        // final String message = String.format("Cannot mark resource %s as changed", resource);
        // logger.error(message, e);
        // }
        // }
        // } catch(FileSystemException e) {
        // final String message = String.format("Cannot retrieve all workspace resources for %s", language);
        // logger.error(message, e);
        // }

        return StatusUtils.success();
    }
}
