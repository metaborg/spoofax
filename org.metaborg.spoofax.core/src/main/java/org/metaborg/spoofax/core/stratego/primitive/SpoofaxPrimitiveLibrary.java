package org.metaborg.spoofax.core.stratego.primitive;

import java.util.Set;

import org.metaborg.spoofax.core.stratego.primitive.generic.GenericPrimitiveLibrary;
import org.spoofax.interpreter.library.AbstractPrimitive;

import javax.inject.Inject;
import javax.inject.Named;

public class SpoofaxPrimitiveLibrary extends GenericPrimitiveLibrary {
    public static final String name = "SpoofaxLibrary";


    @Inject public SpoofaxPrimitiveLibrary(@Named(name) Set<AbstractPrimitive> primitives) {
        super(primitives, name);
    }
}
