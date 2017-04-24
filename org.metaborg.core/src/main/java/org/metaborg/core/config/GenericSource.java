package org.metaborg.core.config;

import java.io.Serializable;

/**
 * Generic source.
 */
public class GenericSource implements ISourceConfig, Serializable {
    private static final long serialVersionUID = 5130391001472648535L;

    /**
     * Directory which is exported, relative to the location of the language component that exports sources.
     */
    public final String directory;


    public GenericSource(String directory) {
        this.directory = directory;
    }


    @Override public void accept(ISourceVisitor visitor) {
        visitor.visit(this);
    }


    @Override public String toString() {
        return "generic source " + directory;
    }

}
