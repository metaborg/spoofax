package org.metaborg.spoofax.core.stratego.primitive.nabl2;

import org.metaborg.spoofax.core.config.ISpoofaxProjectConfigService;
import org.spoofax.interpreter.terms.IStrategoTerm;


import mb.nabl2.config.NaBL2Config;

public class SG_is_debug_resolution_enabled extends ScopeGraphConfigPrimitive {

    @jakarta.inject.Inject @javax.inject.Inject public SG_is_debug_resolution_enabled(ISpoofaxProjectConfigService configService) {
        super(SG_is_debug_resolution_enabled.class.getSimpleName(), configService);
    }

    @Override protected IStrategoTerm call(NaBL2Config config, IStrategoTerm term) {
        return config.debug().resolution() ? term : null;
    }

}