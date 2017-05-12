package org.metaborg.core.config;

import java.io.Serializable;

/**
 * Language-specific source.
 */
public class LangSource implements ISourceConfig, Serializable {
    private static final long serialVersionUID = 4230206217435589272L;

    /**
     * Name of the language for which source files are exported.
     */
    public final String language;

    /**
     * File which is exported, relative to the location of the language component that exports sources.
     */
    public final String directory;


    public LangSource(String languageName, String directory) {
        this.language = languageName;
        this.directory = directory;
    }


    @Override public void accept(ISourceVisitor visitor) {
        visitor.visit(this);
    }


    @Override public String toString() {
        return language + " source " + directory;
    }

}
