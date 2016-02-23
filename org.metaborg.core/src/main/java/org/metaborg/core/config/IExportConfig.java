package org.metaborg.core.config;

/**
 * Configuration for file exports. Use the visitor pattern access implementations.
 */
public interface IExportConfig {
    /**
     * Accepts an export visitor.
     * 
     * @param visitor
     *            Visitor to accept.
     */
    void accept(IExportVisitor visitor);
}
