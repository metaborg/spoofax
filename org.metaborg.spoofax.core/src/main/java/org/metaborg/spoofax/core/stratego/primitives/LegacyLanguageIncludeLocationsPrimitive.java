package org.metaborg.spoofax.core.stratego.primitives;

import com.google.inject.Inject;

public class LegacyLanguageIncludeLocationsPrimitive extends RedirectingPrimitive {
    @Inject public LegacyLanguageIncludeLocationsPrimitive(LanguageIncludeLocationsPrimitive prim) {
        super("SSL_EXT_language_includes", prim);
    }
}
