package org.metaborg.spoofax.core.stratego.primitive.legacy;

import org.metaborg.spoofax.core.stratego.primitive.LanguageIncludeDirectoriesPrimitive;
import org.metaborg.spoofax.core.stratego.primitive.generic.RedirectingPrimitive;


public class LegacyLanguageIncludeLocationsPrimitive extends RedirectingPrimitive {
    @jakarta.inject.Inject public LegacyLanguageIncludeLocationsPrimitive(LanguageIncludeDirectoriesPrimitive prim) {
        super("SSL_EXT_language_includes", prim);
    }
}
