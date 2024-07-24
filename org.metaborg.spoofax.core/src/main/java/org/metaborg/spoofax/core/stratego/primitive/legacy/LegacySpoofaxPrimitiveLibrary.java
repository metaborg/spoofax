package org.metaborg.spoofax.core.stratego.primitive.legacy;

import java.util.Set;

import org.metaborg.spoofax.core.stratego.primitive.generic.GenericPrimitiveLibrary;
import org.spoofax.interpreter.library.AbstractPrimitive;


public class LegacySpoofaxPrimitiveLibrary extends GenericPrimitiveLibrary {
    public static final String name = "LegacySpoofaxLibrary";


    @jakarta.inject.Inject public LegacySpoofaxPrimitiveLibrary(@jakarta.inject.Named(name) Set<AbstractPrimitive> primitives) {
        super(primitives, name);
    }
}
