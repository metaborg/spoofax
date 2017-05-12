package org.metaborg.core.config;

import java.io.Serializable;

/**
 * Source for all languages.
 * 
 * This is for internal use only, it should not be created from a project configuration.
 * 
 */
public class AllLangSource implements ISourceConfig, Serializable {
    private static final long serialVersionUID = 4230206217435589272L;

    /**
     * Source directory, relative to the project root.
     */
    public final String directory;

    public AllLangSource(String directory) {
        this.directory = directory;
    }

    @Override public void accept(ISourceVisitor visitor) {
        visitor.visit(this);
    }


    @Override public String toString() {
        return "source " + directory + " for all languages";
    }

}