package org.metaborg.spoofax.core.syntax;

import org.metaborg.core.syntax.IParserConfiguration;

public class JSGLRParserConfiguration implements IParserConfiguration {
    public final boolean implode;
    public final boolean recovery;
    public final boolean completion;
    public final int timeout;
    public int cursorPosition;


    public JSGLRParserConfiguration() {
        this(true, true, false, 30000, Integer.MAX_VALUE);
    }

    public JSGLRParserConfiguration(boolean implode, boolean recovery, boolean completion, int timeout, int cursorPosition) {
        this.implode = implode;
        this.recovery = recovery;
        this.completion = completion;
        this.timeout = timeout;
        this.cursorPosition = cursorPosition;
    }
}
