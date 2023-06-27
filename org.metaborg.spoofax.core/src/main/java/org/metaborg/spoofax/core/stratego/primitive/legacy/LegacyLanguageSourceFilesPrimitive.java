package org.metaborg.spoofax.core.stratego.primitive.legacy;

import org.metaborg.spoofax.core.stratego.primitive.LanguageSourceFilesPrimitive;
import org.metaborg.spoofax.core.stratego.primitive.generic.RedirectingPrimitive;

import javax.inject.Inject;

public class LegacyLanguageSourceFilesPrimitive extends RedirectingPrimitive {
    @Inject public LegacyLanguageSourceFilesPrimitive(LanguageSourceFilesPrimitive prim) {
        super("SSL_EXT_language_source_files", prim);
    }
}
