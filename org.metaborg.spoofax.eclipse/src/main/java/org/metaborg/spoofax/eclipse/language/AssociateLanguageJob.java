package org.metaborg.spoofax.eclipse.language;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.internal.registry.EditorRegistry;
import org.eclipse.ui.internal.registry.FileEditorMapping;
import org.eclipse.ui.progress.UIJob;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ResourceExtensionFacet;
import org.metaborg.spoofax.eclipse.editor.SpoofaxEditor;
import org.metaborg.spoofax.eclipse.util.StatusUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

@SuppressWarnings("restriction")
public class AssociateLanguageJob extends UIJob {
    private static final Logger logger = LoggerFactory.getLogger(AssociateLanguageJob.class);

    private final ILanguage language;
    private final EditorRegistry editorRegistry;


    public AssociateLanguageJob(ILanguage language, IEditorRegistry editorRegistry) {
        super("Associating language with Spoofax editor");

        this.language = language;
        // HACK: Eclipse API expects an EditorRegistry, and it is the only implementation, so always cast.
        this.editorRegistry = (EditorRegistry) editorRegistry;
    }


    @Override public IStatus runInUIThread(IProgressMonitor monitor) {
        final ResourceExtensionFacet resourceExtensionsFacet = language.facet(ResourceExtensionFacet.class);
        if(resourceExtensionsFacet == null) {
            final String message =
                String.format("Cannot create editor association for {}, no resource extensions facet was found",
                    language);
            logger.error(message);
            return StatusUtils.error(message);
        }

        // Eclipse API expects EditorDescriptor instead of IEditorDescriptor.
        final EditorDescriptor editorDescription = (EditorDescriptor) editorRegistry.findEditor(SpoofaxEditor.id);
        final String[] extensions = Iterables.toArray(resourceExtensionsFacet.extensions(), String.class);
        final FileEditorMapping[] additionalMappings = new FileEditorMapping[extensions.length];
        for(int i = 0; i < extensions.length; ++i) {
            final String extension = extensions[i];
            final FileEditorMapping mapping = new FileEditorMapping(extension);
            mapping.addEditor(editorDescription);
            mapping.setDefaultEditor(editorDescription);
            additionalMappings[i] = mapping;
        }
        final IFileEditorMapping[] iMappings = editorRegistry.getFileEditorMappings();
        final FileEditorMapping[] mappings = Arrays.copyOf(iMappings, iMappings.length, FileEditorMapping[].class);
        final FileEditorMapping[] newMappings = ArrayUtils.addAll(mappings, additionalMappings);
        if(monitor.isCanceled())
            return StatusUtils.cancel();
        logger.debug("Associating {} with extension(s) {} to Spoofax editor", language, Joiner.on(", ")
            .join(extensions));
        editorRegistry.setFileEditorMappings(newMappings);

        return StatusUtils.success();
    }
}
