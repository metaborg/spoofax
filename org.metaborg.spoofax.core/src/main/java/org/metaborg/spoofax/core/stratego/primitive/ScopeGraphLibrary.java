package org.metaborg.spoofax.core.stratego.primitive;

import java.util.Set;

import org.metaborg.spoofax.core.stratego.primitive.generic.GenericPrimitiveLibrary;
import org.spoofax.interpreter.library.AbstractPrimitive;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class ScopeGraphLibrary extends GenericPrimitiveLibrary {
    public static final String REGISTRY_NAME = "SCOPEGRAPH";

    @Inject public ScopeGraphLibrary(@Named("ScopeGraphLibrary") Set<AbstractPrimitive> primitives) {
        super(primitives, ScopeGraphLibrary.REGISTRY_NAME);
    }

}
