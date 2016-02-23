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


    public LangDirExport(String languageName, String directory) {
        this.language = languageName;
        this.directory = directory;
    }


    @Override public void accept(IExportVisitor visitor) {
        visitor.visit(this);
    }
}
