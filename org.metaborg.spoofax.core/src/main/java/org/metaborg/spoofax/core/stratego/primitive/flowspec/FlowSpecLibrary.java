package org.metaborg.spoofax.core.stratego.primitive.flowspec;

import java.util.Set;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.metaborg.spoofax.core.stratego.primitive.generic.GenericPrimitiveLibrary;
import org.spoofax.interpreter.library.AbstractPrimitive;


public class FlowSpecLibrary extends GenericPrimitiveLibrary {
    public static final String name = "FlowSpecLibrary";
    public static final String REGISTRY_NAME = "FLOWSPEC";

    @Inject public FlowSpecLibrary(@Named(name) Set<AbstractPrimitive> primitives) {
        super(primitives, FlowSpecLibrary.REGISTRY_NAME);
    }

}
