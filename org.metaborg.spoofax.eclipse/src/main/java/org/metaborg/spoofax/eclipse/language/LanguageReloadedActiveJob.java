package org.metaborg.spoofax.eclipse.language;

import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorRegistry;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageCache;
import org.metaborg.spoofax.core.language.ResourceExtensionFacet;
import org.metaborg.spoofax.eclipse.editor.SpoofaxEditor;
import org.metaborg.spoofax.eclipse.util.EditorMappingUtils;
import org.metaborg.spoofax.eclipse.util.StatusUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

public class LanguageReloadedActiveJob extends Job {
    private static final Logger logger = LoggerFactory.getLogger(LanguageReloadedActiveJob.class);

    private final Set<ILanguageCache> languageCaches;

    private final IEditorRegistry editorRegistry;

    private final ILanguage oldLanguage;
    private final ILanguage newLanguage;


    public LanguageReloadedActiveJob(Set<ILanguageCache> languageCaches, IEditorRegistry editorRegistry,
        ILanguage oldLanguage, ILanguage newLanguage) {
        super("Active language reloaded");

        this.languageCaches = languageCaches;

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
                logger.debug("Unassociating extension(s) {} from Spoofax editor",
                    Joiner.on(", ").join(removeExtensions));
            }
            if(addExtensions.size() > 0) {
                logger.debug("Associating extension(s) {} to Spoofax editor", Joiner.on(", ").join(addExtensions));
            }
            display.asyncExec(new Runnable() {
                @Override public void run() {
                    EditorMappingUtils.remove(editorRegistry, SpoofaxEditor.id, removeExtensions);
                    EditorMappingUtils.set(editorRegistry, SpoofaxEditor.id, addExtensions);
                }
            });
        }

        // Invalidate cached language resources
        for(ILanguageCache languageCache : languageCaches) {
            languageCache.invalidateCache(oldLanguage);
        }

        // GTODO: Mark editors as changed, trigger editor update.

        // GTODO: Mark workspace resources as changed, trigger project builds.

        return StatusUtils.success();
    }
}
