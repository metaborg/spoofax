package org.metaborg.spoofax.core.stratego.primitive.legacy;

import org.metaborg.spoofax.core.stratego.primitive.CallStrategyPrimitive;
import org.metaborg.spoofax.core.stratego.primitive.generic.RedirectingPrimitive;

import com.google.inject.Inject;

public class LegacyForeignCallPrimitive extends RedirectingPrimitive {
    @Inject public LegacyForeignCallPrimitive(CallStrategyPrimitive prim) {
        super("SSL_EXT_foreigncall", prim);
    }
}
