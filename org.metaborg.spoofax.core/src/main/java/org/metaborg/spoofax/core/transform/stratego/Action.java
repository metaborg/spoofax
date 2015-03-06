package org.metaborg.spoofax.core.transform.stratego;

import javax.annotation.Nullable;

import org.metaborg.spoofax.core.language.ILanguage;

public class Action {
    /**
     * Name of the action.
     */
    public final String name;
    /**
     * Language of input terms this builder accepts, or null if it accepts any terms.
     */
    @Nullable public final ILanguage inputLangauge;
    /**
     * Language of output terms this builder creates, or null if unknown.
     */
    @Nullable public final ILanguage outputLanguage;
    /**
     * Name of the Stratego strategy this action executes.
     */
    public final String strategy;
    /**
     * Flags for this action.
     */
    public final ActionFlags flags;


    /**
     * Creates an action from a strategy name and flags.
     * 
     * @param name
     *            Name of the action.
     * @param inputLanguage
     *            Language of input terms this builder accepts.
     * @param outputLanguage
     *            Language of input terms this builder accepts.
     * @param strategy
     *            Name of the Stratego strategy this action executes.
     * @param flags
     *            Flags for this action.
     */
    public Action(String name, @Nullable ILanguage inputLanguage, @Nullable ILanguage outputLanguage, String strategy,
        ActionFlags flags) {
        this.name = name;
        this.inputLangauge = inputLanguage;
        this.outputLanguage = outputLanguage;
        this.strategy = strategy;
        this.flags = flags;
    }


    @Override public String toString() {
        return "Action [name=" + name + ", inputLangauge=" + inputLangauge + ", outputLanguage=" + outputLanguage
            + ", strategy=" + strategy + ", flags=" + flags + "]";
    }
}
