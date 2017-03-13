package org.metaborg.core.processing;

import java.util.Set;

import javax.annotation.Nullable;

import org.metaborg.core.context.IContextProcessor;
import org.metaborg.core.editor.IEditor;
import org.metaborg.core.editor.IEditorRegistry;
import org.metaborg.core.language.ILanguageCache;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageComponentChange;
import org.metaborg.core.language.LanguageImplChange;
import org.metaborg.core.language.dialect.IDialectProcessor;

import com.google.inject.Inject;

/**
 * Default implementation for the language change processor.
 */
public class LanguageChangeProcessor implements ILanguageChangeProcessor {
    private final IDialectProcessor dialectProcessor;
    private final IContextProcessor contextProcessor;
    private final IEditorRegistry editorRegistry;
    private final Set<ILanguageCache> languageCaches;


    @Inject public LanguageChangeProcessor(IDialectProcessor dialectProcessor, IContextProcessor contextProcessor,
        IEditorRegistry editorRegistry, Set<ILanguageCache> languageCaches) {
        this.dialectProcessor = dialectProcessor;
        this.contextProcessor = contextProcessor;
        this.editorRegistry = editorRegistry;
        this.languageCaches = languageCaches;
    }


    @Override public void processComponentChange(LanguageComponentChange change) {
        switch(change.kind) {
            case Add:
                addedComponent(change.newComponent);
                break;
            case Reload:
                reloadedComponent(change.oldComponent, change.newComponent);
                break;
            case Remove:
                removedComponent(change.oldComponent);
                break;
        }
    }

    /**
     * Component was added
     * 
     * @param component
     *            Added component
     */
    protected void addedComponent(ILanguageComponent component) {

    }

    /**
     * Component was reloaded
     * 
     * @param oldComponent
     *            Component before the reload
     * @param newComponent
     *            Component after the reload
     */
    protected void reloadedComponent(ILanguageComponent oldComponent, ILanguageComponent newComponent) {
        for(ILanguageCache languageCache : languageCaches) {
            languageCache.invalidateCache(oldComponent);
        }
    }

    /**
     * Component was removed
     * 
     * @param component
     *            Removed component
     */
    protected void removedComponent(ILanguageComponent component) {
        for(ILanguageCache languageCache : languageCaches) {
            languageCache.invalidateCache(component);
        }
    }


    @Override public void processImplChange(LanguageImplChange change) {
        switch(change.kind) {
            case Add:
                addedImpl(change.impl);
                break;
            case Reload:
                reloadedImpl(change.impl);
                break;
            case Remove:
                removedImpl(change.impl);
                break;
        }

        dialectProcessor.update(change);
        contextProcessor.update(change);
    }

    /**
     * Implementation was added
     * 
     * @param impl
     *            Added implementation
     */
    protected void addedImpl(ILanguageImpl impl) {
        final Iterable<IEditor> editors = editorRegistry.openEditors();
        for(IEditor editor : editors) {
            if(editor.language() == null) {
                editor.reconfigure();
            }
            if(!editor.enabled() && impl.equals(editor.language())) {
                editor.enable();
            }
        }
    }

    /**
     * Implementation was reloaded
     * 
     * @param impl
     *            Reloaded implementation
     */
    protected void reloadedImpl(ILanguageImpl impl) {
        for(ILanguageCache languageCache : languageCaches) {
            languageCache.invalidateCache(impl);
        }

        final Iterable<IEditor> editors = editorRegistry.openEditors();
        for(IEditor editor : editors) {
            final ILanguageImpl editorLanguage = editor.language();
            if(editorLanguage == null || impl.equals(editorLanguage)) {
                editor.reconfigure();
                editor.forceUpdate();
            }
        }
    }

    /**
     * Implementation was removed
     * 
     * @param impl
     *            Removed implementation
     */
    protected void removedImpl(ILanguageImpl impl) {
        for(ILanguageCache languageCache : languageCaches) {
            languageCache.invalidateCache(impl);
        }

        final Iterable<IEditor> editors = editorRegistry.openEditors();
        for(IEditor editor : editors) {
            final ILanguageImpl language = editor.language();
            if(language != null && language.equals(impl)) {
                editor.reconfigure();
                editor.disable();
            }
        }
    }
}
