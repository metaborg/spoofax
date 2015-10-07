package org.metaborg.spoofax.core.syntax;

import org.metaborg.core.syntax.IParserConfiguration;

public class JSGLRParserConfiguration implements IParserConfiguration {
    public static final boolean defaultImplode = true;
    public static final boolean defaultRecovery = true;
    public static final boolean defaultCompletion = false;
    public static final int defaultTimeout = 30000;

    public final boolean implode;
    public final boolean recovery;
    public final boolean completion;
    public final int timeout;


    public JSGLRParserConfiguration() {
        this(defaultImplode, defaultRecovery, defaultCompletion, defaultTimeout);
    }

    public JSGLRParserConfiguration(boolean implode, boolean recovery) {
        this(implode, recovery, defaultCompletion, defaultTimeout);
    }

    public JSGLRParserConfiguration(boolean implode, boolean recovery, boolean completion, int timeout) {
        this.implode = implode;
        this.recovery = recovery;
        this.completion = completion;
        this.timeout = timeout;
    }
}
