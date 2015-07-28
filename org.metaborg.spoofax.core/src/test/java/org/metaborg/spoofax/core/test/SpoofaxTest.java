package org.metaborg.spoofax.core.test;

import org.metaborg.core.test.MetaborgTest;
import org.metaborg.spoofax.core.SpoofaxModule;

public class SpoofaxTest extends MetaborgTest {
    public SpoofaxTest() {
        super(new SpoofaxModule());
    }
}
