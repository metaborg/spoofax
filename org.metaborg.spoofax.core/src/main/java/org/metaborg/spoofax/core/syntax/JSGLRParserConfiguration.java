package org.metaborg.spoofax.core.syntax;

public class JSGLRParserConfiguration {
    public static final boolean defaultImplode = true;
    public static final boolean defaultRecovery = true;
    public static final boolean defaultCompletion = false;
    public static final int defaultTimeout = 30000;
    public static final int defaultCursorPosition = Integer.MAX_VALUE;

    public final boolean implode;
    public final boolean recovery;
    public final boolean completion;
    public final int timeout;
    public final int cursorPosition;


    public JSGLRParserConfiguration() {
        this(defaultImplode, defaultRecovery, defaultCompletion, defaultTimeout, defaultCursorPosition);
    }

    public JSGLRParserConfiguration(boolean implode, boolean recovery) {
        this(implode, recovery, defaultCompletion, defaultTimeout, defaultCursorPosition);
    }

    public JSGLRParserConfiguration(boolean implode, boolean recovery, boolean completion, int timeout, int cursorPostion) {
        this.implode = implode;
        this.recovery = recovery;
        this.completion = completion;
        this.timeout = timeout;
        this.cursorPosition = cursorPostion;
    }
}
