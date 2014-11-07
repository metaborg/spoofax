package org.metaborg.spoofax.core.service.actions;

import org.metaborg.spoofax.core.language.ILanguage;

/**
 * Description of a Stratego strategy that can be executed on a language.
 */
public class Action {
    /**
     * Name of the action.
     */
    public final String name;
    /**
     * Language of input terms this builder accepts.
     */
    public final ILanguage inputLangauge;
    /**
     * Name of the Stratego strategy this action executes.
     */
    public final String strategoStrategy;
    /**
     * Flag indicating if the strategy should be invoked on the source AST instead of the analyzed AST.
     */
    public final boolean source;
    /**
     * Flag indicating if this is a meta-action, which is hidden from regular users.
     */
    public final boolean meta;
    /**
     * Flag indicating if the result of this action should be shown in a new editor.
     */
    public final boolean openEditor;


    /**
     * Creates an action from a strategy name and flags.
     * 
     * @param name
     *            Name of the action.
     * @param inputLanguage
     *            Language of input terms this builder accepts.
     * @param strategoStrategy
     *            Name of the Stratego strategy this action executes.
     * @param source
     *            Flag indicating if the strategy should be invoked on the source AST instead of the analyzed AST.
     * @param meta
     *            Flag indicating if this is a meta-action, which is hidden from regular users.
     * @param openEditor
     *            Flag indicating if the result of this action should be shown in a new editor.
     */
    public Action(String name, ILanguage inputLanguage, String strategoStrategy, boolean source, boolean meta,
        boolean openEditor) {
        this.name = name;
        this.inputLangauge = inputLanguage;
        this.strategoStrategy = strategoStrategy;
        this.source = source;
        this.meta = meta;
        this.openEditor = openEditor;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + inputLangauge.hashCode();
        result = prime * result + strategoStrategy.hashCode();
        result = prime * result + (source ? 1231 : 1237);
        result = prime * result + (meta ? 1231 : 1237);
        result = prime * result + (openEditor ? 1231 : 1237);
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final Action other = (Action) obj;
        if(!name.equals(other.name))
            return false;
        if(!inputLangauge.equals(other.inputLangauge))
            return false;
        if(!strategoStrategy.equals(other.strategoStrategy))
            return false;
        if(source != other.source)
            return false;
        if(meta != other.meta)
            return false;
        if(openEditor != other.openEditor)
            return false;
        return true;
    }

    @Override public String toString() {
        return "Action [strategy=" + strategoStrategy + ", source=" + source + ", meta=" + meta + ", openEditor="
            + openEditor + "]";
    }
}
