package org.metaborg.core.config;

/**
 * Language-specific directory export.
 */
public class LangDirExport implements IExportConfig {
    /**
     * Name of the language for which source files are exported.
     */
    public final String language;

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
     * Exclude patterns, following the Ant pattern syntax.
     * 
     * @see <a href="http://ant.apache.org/manual/dirtasks.html#patterns">Ant patterns</a>
     */
    public final Iterable<String> excludes;


    public LangDirExport(String languageName, String directory, Iterable<String> includes, Iterable<String> excludes) {
        this.language = languageName;
        this.directory = directory;
        this.includes = includes;
        this.excludes = excludes;
    }


    @Override public void accept(IExportVisitor visitor) {
        visitor.visit(this);
    }
}
