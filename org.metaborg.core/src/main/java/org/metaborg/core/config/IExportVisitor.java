package org.metaborg.core.config;

/**
 * Visitor for {@link IExportConfig}s.
 */
public interface IExportVisitor {
    /**
     * Visit a language-specific resource directory.
     * 
     * @param resource
     *            Language resource directory.
     */
    void visit(LangDirExport resource);

    /**
     * Visit a language-specific single-file resource.
     * 
     * @param resource
     *            Language resource file.
     */
    void visit(LangFileExport resource);

    /**
     * Visit a generic resource.
     * 
     * @param resource
     *            Generic resource.
     */
    void visit(ResourceExport resource);
}
