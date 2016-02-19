package org.metaborg.core.config;

/**
 * Generic resource export.
 */
public class ResourceExport implements IExportConfig {
    public final String directory;
    public final Iterable<String> includes;
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
