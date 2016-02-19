package org.metaborg.core.config;

/**
 * Language directory export.
 */
public class LangDirExport implements IExportConfig {
    public final String language;
    public final String directory;


    public LangDirExport(String languageName, String directory) {
        this.language = languageName;
        this.directory = directory;
    }


    @Override public void accept(IExportVisitor visitor) {
        visitor.visit(this);
    }
}
