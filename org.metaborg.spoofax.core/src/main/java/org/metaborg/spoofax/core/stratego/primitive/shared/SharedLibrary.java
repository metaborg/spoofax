package org.metaborg.spoofax.core.stratego.primitive.shared;

import java.util.Set;

import org.metaborg.spoofax.core.stratego.primitive.generic.GenericPrimitiveLibrary;
import org.spoofax.interpreter.library.AbstractPrimitive;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class SharedLibrary extends GenericPrimitiveLibrary {
    public static final String name = "SharedLibrary";
    public static final String REGISTRY_NAME = "SHARED";

    @Inject public SharedLibrary(@Named(name) Set<AbstractPrimitive> primitives) {
        super(primitives, SharedLibrary.REGISTRY_NAME);
    }

}
