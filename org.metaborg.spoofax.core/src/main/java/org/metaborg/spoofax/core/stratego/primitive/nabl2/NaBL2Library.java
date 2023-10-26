package org.metaborg.spoofax.core.stratego.primitive.nabl2;

import java.util.Set;

import org.metaborg.spoofax.core.stratego.primitive.generic.GenericPrimitiveLibrary;
import org.spoofax.interpreter.library.AbstractPrimitive;


public class NaBL2Library extends GenericPrimitiveLibrary {
    public static final String name = "NaBL2Library";
    public static final String REGISTRY_NAME = "NaBL2";

    @jakarta.inject.Inject @javax.inject.Inject public NaBL2Library(@jakarta.inject.Named(name) @javax.inject.Named(name) Set<AbstractPrimitive> primitives) {
        super(primitives, NaBL2Library.REGISTRY_NAME);
    }

}
