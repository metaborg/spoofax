package org.metaborg.spoofax.core.stratego.primitive.legacy;

import org.metaborg.spoofax.core.stratego.primitive.ProjectPathPrimitive;
import org.metaborg.spoofax.core.stratego.primitive.generic.RedirectingPrimitive;

import javax.inject.Inject;

public class LegacyProjectPathPrimitive extends RedirectingPrimitive {
    @Inject public LegacyProjectPathPrimitive(ProjectPathPrimitive prim) {
        super("SSL_EXT_projectpath", prim);
    }
}
