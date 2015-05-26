package org.metaborg.spoofax.eclipse.language;

import java.util.Collection;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorRegistry;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.language.LanguageFileSelector;
import org.metaborg.spoofax.core.language.ResourceExtensionFacet;
import org.metaborg.spoofax.eclipse.editor.ISpoofaxEclipseEditor;
import org.metaborg.spoofax.eclipse.editor.ISpoofaxEditorListener;
import org.metaborg.spoofax.eclipse.editor.SpoofaxEditor;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.metaborg.spoofax.eclipse.util.EditorMappingUtils;
import org.metaborg.spoofax.eclipse.util.MarkerUtils;
import org.metaborg.spoofax.eclipse.util.ResourceUtils;
import org.metaborg.spoofax.eclipse.util.StatusUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

public class LanguageRemovedJob extends Job {
    private static final Logger logger = LoggerFactory.getLogger(LanguageRemovedJob.class);

    private final IEclipseResourceService resourceService;
    private final ILanguageIdentifierService languageIdentifier;

    private final ISpoofaxEditorListener spoofaxEditorListener;

    private final IEditorRegistry editorRegistry;
    private final IWorkspace workspace;

    private final ILanguage language;


    public LanguageRemovedJob(IEclipseResourceService resourceService, ILanguageIdentifierService languageIdentifier,
        ISpoofaxEditorListener spoofaxEditorListener, IEditorRegistry editorRegistry, IWorkspace workspace,
        ILanguage language) {
        super("Processing language removal");

        this.resourceService = resourceService;
        this.languageIdentifier = languageIdentifier;

        this.spoofaxEditorListener = spoofaxEditorListener;

        this.editorRegistry = editorRegistry;
        this.workspace = workspace;

        this.language = language;
    }


    @Override protected IStatus run(IProgressMonitor monitor) {
        logger.debug("Running language removed job for {}", language);

        final Display display = Display.getDefault();

        // Remove editor associations
        final ResourceExtensionFacet resourceExtensionsFacet = language.facet(ResourceExtensionFacet.class);
        if(resourceExtensionsFacet == null) {
            final String message =
                String.format("Cannot remove editor association for %s, no resource extensions facet was found",
                    language);
            logger.error(message);
        } else {
            final Iterable<String> extensions = resourceExtensionsFacet.extensions();
            logger.debug("Unassociating extension(s) {} from Spoofax editor", Joiner.on(", ").join(extensions));
            display.asyncExec(new Runnable() {
                @Override public void run() {
                    EditorMappingUtils.remove(editorRegistry, SpoofaxEditor.id, extensions);
                }
            });
        }

        // Disable editors
        final Iterable<ISpoofaxEclipseEditor> spoofaxEditors = spoofaxEditorListener.openEditors();
        for(ISpoofaxEclipseEditor editor : spoofaxEditors) {
            if(editor.language().equals(language)) {
                editor.reconfigure();
                editor.disable();
            }
        }

        // Remove markers
        try {
            final Collection<FileObject> resources =
                ResourceUtils.workspaceResources(resourceService,
                    new LanguageFileSelector(languageIdentifier, language), workspace.getRoot());
            final Collection<IResource> eclipseResources = ResourceUtils.toEclipseResources(resourceService, resources);
            logger.debug("Removing markers from {} workspace resources", resources.size());
            for(IResource resource : eclipseResources) {
                try {
                    MarkerUtils.clearAll(resource);
                } catch(CoreException e) {
                    final String message = String.format("Cannot remove markers for resource %s", resource);
                    logger.error(message, e);
                }
            }
        } catch(FileSystemException e) {
            final String message = String.format("Cannot retrieve all workspace resources for %s", language);
            logger.error(message, e);
        }

        return StatusUtils.success();
    }
}
