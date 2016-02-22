package org.metaborg.core.config;

/**
 * Generic resource export.
 */
public class ResourceExport implements IExportConfig {
    /**
     * Directory which is exported, relative to the location of the language component that exports sources.
     */
    public final String directory;

    /**
     * Include patterns, following the Ant pattern syntax.
     * 
     * @see <a href="http://ant.apache.org/manual/dirtasks.html#patterns">Ant patterns</a>
     */
    public final Iterable<String> includes;

    /**
     * Exclude patterns, following the Ant pattenr syntax.
     * 
     * @see <a href="http://ant.apache.org/manual/dirtasks.html#patterns">Ant patterns</a>
     */
    public final Iterable<String> excludes;


    public ResourceExport(String directory, Iterable<String> includes, Iterable<String> excludes) {
        this.directory = directory;
        this.includes = includes;
        this.excludes = excludes;
    }


    @Override public void accept(IExportVisitor visitor) {
        visitor.visit(this);
    }
}
