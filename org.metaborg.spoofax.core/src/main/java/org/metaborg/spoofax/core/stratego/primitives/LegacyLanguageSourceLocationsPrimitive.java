package org.metaborg.spoofax.core.stratego.primitives;

import com.google.inject.Inject;

public class LegacyLanguageSourceLocationsPrimitive extends RedirectingPrimitive {
    @Inject public LegacyLanguageSourceLocationsPrimitive(LanguageSourceLocationsPrimitive prim) {
        super("SSL_EXT_language_sources", prim);
    }
}
