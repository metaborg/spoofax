package org.metaborg.core.config;

/**
 * Language single-file export.
 */
public class LangFileExport implements IExportConfig {
    public final String language;
    public final String file;


    public LangFileExport(String languageName, String file) {
        this.language = languageName;
        this.file = file;
    }


    @Override public void accept(IExportVisitor visitor) {
        visitor.visit(this);
    }
}
