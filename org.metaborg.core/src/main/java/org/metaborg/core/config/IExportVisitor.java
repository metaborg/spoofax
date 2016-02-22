package org.metaborg.core.config;

/**
 * Visitor for {@link IExportConfig}s.
 */
public interface IExportVisitor {
    /**
     * Visit a language-specific directory export
     * 
     * @param export
     *            Language directory export.
     */
    void visit(LangDirExport export);

    /**
     * Visit a language-specific single-file export.
     * 
     * @param export
     *            Language file export.
     */
    void visit(LangFileExport export);

    /**
     * Visit a generic resource export.
     * 
     * @param export
     *            Generic resource export.
     */
    void visit(ResourceExport export);
}
