package org.metaborg.spoofax.core.test.language;

import org.metaborg.core.test.language.LanguageIdentificationTest;
import org.metaborg.spoofax.core.SpoofaxModule;

public class SpoofaxLanguageIdentificationTest extends LanguageIdentificationTest {
    public SpoofaxLanguageIdentificationTest() {
        super(new SpoofaxModule());
    }
}
