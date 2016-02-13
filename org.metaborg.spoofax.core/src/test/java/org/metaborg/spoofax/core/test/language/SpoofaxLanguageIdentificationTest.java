package org.metaborg.spoofax.core.test.language;

import org.metaborg.core.test.language.LanguageIdentitificationTest;
import org.metaborg.spoofax.core.SpoofaxModule;

public class SpoofaxLanguageIdentificationTest extends LanguageIdentitificationTest {
    public SpoofaxLanguageIdentificationTest() {
        super(new SpoofaxModule());
    }
}
