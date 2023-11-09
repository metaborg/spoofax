package org.metaborg.spoofax.core.stratego.primitive.legacy;

import org.metaborg.spoofax.core.stratego.primitive.LanguageIncludeFilesPrimitive;
import org.metaborg.spoofax.core.stratego.primitive.generic.RedirectingPrimitive;


public class LegacyLanguageIncludeFilesPrimitive extends RedirectingPrimitive {
    @jakarta.inject.Inject @javax.inject.Inject public LegacyLanguageIncludeFilesPrimitive(LanguageIncludeFilesPrimitive prim) {
        super("SSL_EXT_language_include_files", prim);
    }
}
