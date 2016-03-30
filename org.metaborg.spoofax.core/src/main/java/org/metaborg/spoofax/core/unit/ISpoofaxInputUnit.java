package org.metaborg.spoofax.core.unit;

import javax.annotation.Nullable;

import org.metaborg.core.syntax.IInputUnit;
import org.metaborg.spoofax.core.syntax.JSGLRParserConfiguration;

/**
 * Spoofax-specific extension of an input unit.
 */
public interface ISpoofaxInputUnit extends IInputUnit {
    /**
     * @return Parser-specific configuration, or null when no configuration has been set.
     */
    @Nullable JSGLRParserConfiguration config();
}
