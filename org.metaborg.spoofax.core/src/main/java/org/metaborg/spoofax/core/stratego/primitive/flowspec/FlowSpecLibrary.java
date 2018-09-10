package org.metaborg.spoofax.core.stratego.primitive.flowspec;

import java.util.Set;

import org.metaborg.spoofax.core.stratego.primitive.generic.GenericPrimitiveLibrary;
import org.spoofax.interpreter.library.AbstractPrimitive;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class FlowSpecLibrary extends GenericPrimitiveLibrary {
    public static final String name = "FlowSpecLibrary";
    public static final String REGISTRY_NAME = "FLOWSPEC";

    @Inject public FlowSpecLibrary(@Named(name) Set<AbstractPrimitive> primitives) {
        super(primitives, FlowSpecLibrary.REGISTRY_NAME);
    }

}
