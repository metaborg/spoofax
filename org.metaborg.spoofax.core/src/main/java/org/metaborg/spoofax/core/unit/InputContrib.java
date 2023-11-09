package org.metaborg.spoofax.core.unit;

import jakarta.annotation.Nullable;

import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.unit.IUnitContrib;
import org.metaborg.spoofax.core.syntax.JSGLRParserConfiguration;

public class InputContrib implements IUnitContrib {
    public final String text;
    public final ILanguageImpl langImpl;
    public final @Nullable ILanguageImpl dialect;
    public final @Nullable JSGLRParserConfiguration config;


    public InputContrib(String text, ILanguageImpl langImpl, @Nullable ILanguageImpl dialect,
        @Nullable JSGLRParserConfiguration config) {
        this.text = text;
        this.langImpl = langImpl;
        this.dialect = dialect;
        this.config = config;
    }

    public InputContrib(String text, ILanguageImpl langImpl, @Nullable ILanguageImpl dialect) {
        this(text, langImpl, dialect, null);
    }

    public InputContrib(ILanguageImpl langImpl, @Nullable ILanguageImpl dialect) {
        this("", langImpl, dialect);
    }

    public InputContrib(ILanguageImpl langImpl) {
        this(langImpl, null);
    }


    @Override public String id() {
        return "input";
    }
}
