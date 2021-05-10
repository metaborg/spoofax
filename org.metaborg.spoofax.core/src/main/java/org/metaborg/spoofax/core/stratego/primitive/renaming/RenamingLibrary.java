package org.metaborg.spoofax.core.stratego.primitive.renaming;

import java.util.Set;

import org.metaborg.spoofax.core.stratego.primitive.generic.GenericPrimitiveLibrary;
import org.spoofax.interpreter.library.AbstractPrimitive;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class RenamingLibrary extends GenericPrimitiveLibrary {
    public static final String name = "RenamingLibrary";
    public static final String REGISTRY_NAME = "RENAMING";

    @Inject public RenamingLibrary(@Named(name) Set<AbstractPrimitive> primitives) {
        super(primitives, RenamingLibrary.REGISTRY_NAME);
    }

}
