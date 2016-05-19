package org.metaborg.core.config;

import java.io.Serializable;

/**
 * Configuration for file exports. Use the visitor pattern access implementations.
 */
public interface IExportConfig extends Serializable {
    /**
     * Accepts an export visitor.
     * 
     * @param visitor
     *            Visitor to accept.
     */
    void accept(IExportVisitor visitor);
}
