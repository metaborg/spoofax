package org.metaborg.spoofax.core.stratego.primitive.legacy;

import java.util.Set;

import org.metaborg.spoofax.core.stratego.primitive.generic.GenericPrimitiveLibrary;
import org.spoofax.interpreter.library.AbstractPrimitive;

import javax.inject.Inject;
import javax.inject.Named;

public class LegacySpoofaxPrimitiveLibrary extends GenericPrimitiveLibrary {
    public static final String name = "LegacySpoofaxLibrary";


    @Inject public LegacySpoofaxPrimitiveLibrary(@Named(name) Set<AbstractPrimitive> primitives) {
        super(primitives, name);
    }
}
