package org.metaborg.spoofax.core.editor;


public interface IEditorRegistry {
    /**
     * @return All open editors.
     */
    public abstract Iterable<IEditor> openEditors();
}
