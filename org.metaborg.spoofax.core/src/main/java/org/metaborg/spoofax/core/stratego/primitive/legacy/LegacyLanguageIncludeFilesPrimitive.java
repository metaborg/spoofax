package org.metaborg.spoofax.core.stratego.primitive.legacy;

import org.metaborg.spoofax.core.stratego.primitive.LanguageIncludeFilesPrimitive;
import org.metaborg.spoofax.core.stratego.primitive.generic.RedirectingPrimitive;

import com.google.inject.Inject;

public class LegacyLanguageIncludeFilesPrimitive extends RedirectingPrimitive {
    @Inject public LegacyLanguageIncludeFilesPrimitive(LanguageIncludeFilesPrimitive prim) {
        super("SSL_EXT_language_include_files", prim);
    }
}
