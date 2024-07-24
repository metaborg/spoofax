package org.metaborg.spoofax.core.stratego.primitive.statix;

import java.util.Set;

import org.metaborg.spoofax.core.stratego.primitive.generic.GenericPrimitiveLibrary;
import org.spoofax.interpreter.library.AbstractPrimitive;


public class StatixLibrary extends GenericPrimitiveLibrary {
    public static final String name = "StatixLibrary";
    public static final String REGISTRY_NAME = "STATIX";

    @jakarta.inject.Inject public StatixLibrary(@jakarta.inject.Named(name) Set<AbstractPrimitive> primitives) {
        super(primitives, StatixLibrary.REGISTRY_NAME);
    }

}