package org.metaborg.spoofax.core.stratego.primitive;

import java.util.Set;

import org.metaborg.spoofax.core.stratego.primitive.generic.GenericPrimitiveLibrary;
import org.spoofax.interpreter.library.AbstractPrimitive;


public class SpoofaxPrimitiveLibrary extends GenericPrimitiveLibrary {
    public static final String name = "SpoofaxLibrary";


    @jakarta.inject.Inject @javax.inject.Inject public SpoofaxPrimitiveLibrary(@jakarta.inject.Named(name) @javax.inject.Named(name) Set<AbstractPrimitive> primitives) {
        super(primitives, name);
    }
}
