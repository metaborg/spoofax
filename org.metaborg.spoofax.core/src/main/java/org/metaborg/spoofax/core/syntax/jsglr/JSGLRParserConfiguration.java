package org.metaborg.spoofax.core.syntax.jsglr;

import org.metaborg.core.syntax.IParserConfiguration;

public class JSGLRParserConfiguration implements IParserConfiguration {
    public final boolean implode;
    public final boolean recovery;
    public final boolean completion;
    public final int timeout;


    public JSGLRParserConfiguration() {
        this(true, true, false, 5000);
    }

    public JSGLRParserConfiguration(boolean implode, boolean recovery, boolean completion, int timeout) {
        this.implode = implode;
        this.recovery = recovery;
        this.completion = completion;
        this.timeout = timeout;
    }
}
