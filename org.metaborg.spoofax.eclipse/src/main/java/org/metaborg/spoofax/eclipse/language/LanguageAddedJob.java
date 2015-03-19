package org.metaborg.spoofax.eclipse.language;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorRegistry;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ResourceExtensionFacet;
import org.metaborg.spoofax.eclipse.editor.ISpoofaxEditorListener;
import org.metaborg.spoofax.eclipse.editor.SpoofaxEditor;
import org.metaborg.spoofax.eclipse.util.EditorMappingUtils;
import org.metaborg.spoofax.eclipse.util.StatusUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

public class LanguageAddedJob extends Job {
    private static final Logger logger = LoggerFactory.getLogger(LanguageAddedJob.class);

    private final ISpoofaxEditorListener spoofaxEditorListener;

    private final IEditorRegistry editorRegistry;

    private final ILanguage language;


    public LanguageAddedJob(ISpoofaxEditorListener spoofaxEditorListener, IEditorRegistry editorRegistry,
        ILanguage language) {
        super("Processing added language");

        this.spoofaxEditorListener = spoofaxEditorListener;

        this.editorRegistry = editorRegistry;

        this.language = language;
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

        // Enable editors
        final Iterable<SpoofaxEditor> spoofaxEditors = spoofaxEditorListener.openEditors();
        for(SpoofaxEditor editor : spoofaxEditors) {
            editor.enable();
        }

        return StatusUtils.success();
    }
}
