package org.metaborg.core.processing;

import java.util.Set;

import javax.annotation.Nullable;

import org.metaborg.core.editor.IEditor;
import org.metaborg.core.editor.IEditorRegistry;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.language.ILanguageCache;
import org.metaborg.core.language.LanguageChange;

import com.google.inject.Inject;

/**
 * Default implementation for the language change processor.
 */
public class LanguageChangeProcessor implements ILanguageChangeProcessor {
    private final IEditorRegistry editorRegistry;
    private final Set<ILanguageCache> languageCaches;


    @Inject public LanguageChangeProcessor(IEditorRegistry editorRegistry, Set<ILanguageCache> languageCaches) {
        this.editorRegistry = editorRegistry;
        this.languageCaches = languageCaches;
    }


    @Override public void process(LanguageChange change, @Nullable IProgressReporter progressReporter) {
        // GTODO: do something with progress reporter.
        switch(change.kind) {
            case ADD_FIRST:
                added(change.newLanguage);
                break;
            case REPLACE_ACTIVE:
            case RELOAD_ACTIVE:
                reloadedActive(change.oldLanguage, change.newLanguage);
                break;
            case RELOAD:
            case REMOVE:
                invalidated(change.oldLanguage);
                break;
            case REMOVE_LAST:
                removed(change.oldLanguage);
                break;
            case ADD:
                // Nothing to do when adding new implementation of existing language.
                break;
        }
    }


    protected void added(ILanguage language) {
        // Enable editors
        final Iterable<IEditor> editors = editorRegistry.openEditors();
        for(IEditor editor : editors) {
            if(editor.language() == null) {
                editor.reconfigure();
            }
            if(!editor.enabled() && language.equals(editor.language())) {
                editor.enable();
            }
        }
    }

    protected void invalidated(ILanguage language) {
        for(ILanguageCache languageCache : languageCaches) {
            languageCache.invalidateCache(language);
        }
    }

    protected void reloadedActive(ILanguage oldLanguage, @SuppressWarnings("unused") ILanguage newLanguage) {
        // Invalidate cached language resources
        for(ILanguageCache languageCache : languageCaches) {
            languageCache.invalidateCache(oldLanguage);
        }

        // Update editors
        final Iterable<IEditor> editors = editorRegistry.openEditors();
        for(IEditor editor : editors) {
            final ILanguage editorLanguage = editor.language();
            if(editorLanguage == null || oldLanguage.equals(editorLanguage)) {
                editor.reconfigure();
                editor.forceUpdate();
            }
        }
    }

    protected void removed(ILanguage language) {
        // Disable editors
        final Iterable<IEditor> editors = editorRegistry.openEditors();
        for(IEditor editor : editors) {
            if(editor.language().equals(language)) {
                editor.reconfigure();
                editor.disable();
            }
        }
    }
}
