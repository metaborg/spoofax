package org.metaborg.spoofax.core.stratego.primitive.flowspec;

import java.util.Set;

import org.metaborg.spoofax.core.stratego.primitive.generic.GenericPrimitiveLibrary;
import org.spoofax.interpreter.library.AbstractPrimitive;


public class FlowSpecLibrary extends GenericPrimitiveLibrary {
    public static final String name = "FlowSpecLibrary";
    public static final String REGISTRY_NAME = "FLOWSPEC";

    @jakarta.inject.Inject @javax.inject.Inject public FlowSpecLibrary(@jakarta.inject.Named(name) @javax.inject.Named(name) Set<AbstractPrimitive> primitives) {
        super(primitives, FlowSpecLibrary.REGISTRY_NAME);
    }

}
