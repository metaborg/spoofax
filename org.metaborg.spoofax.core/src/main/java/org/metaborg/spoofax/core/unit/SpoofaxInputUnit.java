package org.metaborg.spoofax.core.unit;

import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.spoofax.core.syntax.JSGLRParserConfiguration;

/**
 * Wraps a {@link SpoofaxUnit} and {@link InputContrib} as {@link ISpoofaxInputUnit}.
 */
public class SpoofaxInputUnit extends SpoofaxUnitWrapper implements ISpoofaxInputUnit {
    private final InputContrib contrib;


    public SpoofaxInputUnit(SpoofaxUnit unit, InputContrib contrib) {
        super(unit);
        this.contrib = contrib;
        addUnitContrib(contrib);
    }


    @Override public String text() {
        return contrib.text;
    }

    @Override public ILanguageImpl langImpl() {
        return contrib.langImpl;
    }

    @Override public ILanguageImpl dialect() {
        return contrib.dialect;
    }

    @Override public JSGLRParserConfiguration config() {
        return contrib.config;
    }
}
