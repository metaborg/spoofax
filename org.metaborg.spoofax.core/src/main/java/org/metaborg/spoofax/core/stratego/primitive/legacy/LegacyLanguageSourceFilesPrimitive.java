package org.metaborg.spoofax.core.stratego.primitive.legacy;

import org.metaborg.spoofax.core.stratego.primitive.LanguageSourceFilesPrimitive;
import org.metaborg.spoofax.core.stratego.primitive.generic.RedirectingPrimitive;


public class LegacyLanguageSourceFilesPrimitive extends RedirectingPrimitive {
    @jakarta.inject.Inject @javax.inject.Inject public LegacyLanguageSourceFilesPrimitive(LanguageSourceFilesPrimitive prim) {
        super("SSL_EXT_language_source_files", prim);
    }
}
