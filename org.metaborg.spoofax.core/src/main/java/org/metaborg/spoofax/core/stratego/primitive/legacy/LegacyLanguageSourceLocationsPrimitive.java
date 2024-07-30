package org.metaborg.spoofax.core.stratego.primitive.legacy;

import org.metaborg.spoofax.core.stratego.primitive.LanguageSourceDirectoriesPrimitive;
import org.metaborg.spoofax.core.stratego.primitive.generic.RedirectingPrimitive;


public class LegacyLanguageSourceLocationsPrimitive extends RedirectingPrimitive {
    @jakarta.inject.Inject public LegacyLanguageSourceLocationsPrimitive(LanguageSourceDirectoriesPrimitive prim) {
        super("SSL_EXT_language_sources", prim);
    }
}
