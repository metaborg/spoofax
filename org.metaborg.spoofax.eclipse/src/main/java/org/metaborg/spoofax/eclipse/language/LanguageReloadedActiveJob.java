package org.metaborg.spoofax.eclipse.language;

import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorRegistry;
import org.metaborg.spoofax.core.editor.IEditor;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageCache;
import org.metaborg.spoofax.core.language.ResourceExtensionFacet;
import org.metaborg.spoofax.eclipse.editor.IEclipseEditor;
import org.metaborg.spoofax.eclipse.editor.IEclipseEditorRegistry;
import org.metaborg.spoofax.eclipse.util.EditorMappingUtils;
import org.metaborg.spoofax.eclipse.util.StatusUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

public class LanguageReloadedActiveJob extends Job {
    private static final Logger logger = LoggerFactory.getLogger(LanguageReloadedActiveJob.class);

    private final Set<ILanguageCache> languageCaches;

    private final IEclipseEditorRegistry spoofaxEditorListener;

    private final IEditorRegistry editorRegistry;

    private final ILanguage oldLanguage;
    private final ILanguage newLanguage;


    public LanguageReloadedActiveJob(Set<ILanguageCache> languageCaches, IEclipseEditorRegistry spoofaxEditorListener,
        IEditorRegistry editorRegistry, ILanguage oldLanguage, ILanguage newLanguage) {
        super("Processing language reload");

        this.languageCaches = languageCaches;

        this.spoofaxEditorListener = spoofaxEditorListener;

        this.editorRegistry = editorRegistry;

        this.oldLanguage = oldLanguage;
        this.newLanguage = newLanguage;
    }


    @Override protected IStatus run(IProgressMonitor monitor) {
        logger.debug("Running language reloaded job for {}", newLanguage);

        final Display display = Display.getDefault();

        // Update editor associations
        final ResourceExtensionFacet oldResourceExtensionsFacet = oldLanguage.facet(ResourceExtensionFacet.class);
        if(oldResourceExtensionsFacet == null) {
            logger
                .error("Cannot update editor association for {}, no resource extensions facet was found", oldLanguage);
        }
        final ResourceExtensionFacet newResourceExtensionsFacet = newLanguage.facet(ResourceExtensionFacet.class);
        if(oldResourceExtensionsFacet == null) {
            logger
                .error("Cannot update editor association for {}, no resource extensions facet was found", newLanguage);
        }
        if(oldResourceExtensionsFacet != null && newResourceExtensionsFacet != null) {
            final Set<String> oldExtensions = Sets.newHashSet(oldResourceExtensionsFacet.extensions());
            final Set<String> newExtensions = Sets.newHashSet(newResourceExtensionsFacet.extensions());
            final Set<String> removeExtensions = Sets.difference(oldExtensions, newExtensions);
            final Set<String> addExtensions = Sets.difference(newExtensions, removeExtensions);
            if(removeExtensions.size() > 0) {
                logger.debug("Unassociating extension(s) {} from Spoofax editor", Joiner.on(", ")
                    .join(removeExtensions));
            }
            if(addExtensions.size() > 0) {
                logger.debug("Associating extension(s) {} to Spoofax editor", Joiner.on(", ").join(addExtensions));
            }
            display.asyncExec(new Runnable() {
                @Override public void run() {
                    EditorMappingUtils.remove(editorRegistry, IEclipseEditor.id, removeExtensions);
                    EditorMappingUtils.set(editorRegistry, IEclipseEditor.id, addExtensions);
                }
            });
        }

        // Invalidate cached language resources
        for(ILanguageCache languageCache : languageCaches) {
            languageCache.invalidateCache(oldLanguage);
        }

        // Update editors
        final Iterable<IEditor> editors = spoofaxEditorListener.openEditors();
        for(IEditor editor : editors) {
            final ILanguage editorLanguage = editor.language();
            if(editorLanguage == null || oldLanguage.equals(editorLanguage)) {
                editor.reconfigure();
                editor.forceUpdate();
            }
        }

        return StatusUtils.success();
    }
}
