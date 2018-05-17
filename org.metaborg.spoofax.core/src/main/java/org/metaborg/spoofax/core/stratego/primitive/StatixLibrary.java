package org.metaborg.spoofax.core.stratego.primitive;

import java.util.Set;

import org.metaborg.spoofax.core.stratego.primitive.generic.GenericPrimitiveLibrary;
import org.spoofax.interpreter.library.AbstractPrimitive;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class StatixLibrary extends GenericPrimitiveLibrary {
    public static final String name = "StatixLibrary";
    public static final String REGISTRY_NAME = "STATIX";

    @Inject public StatixLibrary(@Named(name) Set<AbstractPrimitive> primitives) {
        super(primitives, StatixLibrary.REGISTRY_NAME);
    }

}
