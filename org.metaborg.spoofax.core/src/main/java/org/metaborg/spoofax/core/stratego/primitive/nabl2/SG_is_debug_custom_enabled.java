package org.metaborg.spoofax.core.stratego.primitive.nabl2;

import org.metaborg.spoofax.core.config.ISpoofaxProjectConfigService;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;

import mb.nabl2.config.NaBL2Config;

public class SG_is_debug_custom_enabled extends ScopeGraphConfigPrimitive {

    @Inject public SG_is_debug_custom_enabled(ISpoofaxProjectConfigService configService) {
        super(SG_is_debug_custom_enabled.class.getSimpleName(), configService);
    }

    @Override public IStrategoTerm call(NaBL2Config config, IStrategoTerm term) {
        return config.debug().custom() ? term : null;
    }

}