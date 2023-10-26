package org.metaborg.spoofax.core.stratego.primitive.renaming;

import java.util.Set;

import org.metaborg.spoofax.core.stratego.primitive.generic.GenericPrimitiveLibrary;
import org.spoofax.interpreter.library.AbstractPrimitive;


public class RenamingLibrary extends GenericPrimitiveLibrary {
    public static final String name = "RenamingLibrary";
    public static final String REGISTRY_NAME = "RENAMING";

    @jakarta.inject.Inject @javax.inject.Inject public RenamingLibrary(@jakarta.inject.Named(name) @javax.inject.Named(name) Set<AbstractPrimitive> primitives) {
        super(primitives, RenamingLibrary.REGISTRY_NAME);
    }

}
