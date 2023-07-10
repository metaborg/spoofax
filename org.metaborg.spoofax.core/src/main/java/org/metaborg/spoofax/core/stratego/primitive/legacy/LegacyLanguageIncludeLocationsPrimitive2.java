package org.metaborg.spoofax.core.stratego.primitive.legacy;

import org.metaborg.spoofax.core.stratego.primitive.LanguageIncludeDirectoriesPrimitive;
import org.metaborg.spoofax.core.stratego.primitive.generic.RedirectingPrimitive;

import javax.inject.Inject;

public class LegacyLanguageIncludeLocationsPrimitive2 extends RedirectingPrimitive {
    @Inject public LegacyLanguageIncludeLocationsPrimitive2(LanguageIncludeDirectoriesPrimitive prim) {
        super("SSL_EXT_language_include_locations", prim);
    }
}
