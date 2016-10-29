package org.metaborg.spoofax.core.stratego.primitive.legacy;

import org.metaborg.spoofax.core.stratego.primitive.LanguageSourceDirectoriesPrimitive;
import org.metaborg.spoofax.core.stratego.primitive.generic.RedirectingPrimitive;

import com.google.inject.Inject;

public class LegacyLanguageSourceLocationsPrimitive2 extends RedirectingPrimitive {
    @Inject public LegacyLanguageSourceLocationsPrimitive2(LanguageSourceDirectoriesPrimitive prim) {
        super("SSL_EXT_language_source_locations", prim);
    }
}
