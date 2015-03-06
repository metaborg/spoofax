package org.metaborg.spoofax.eclipse.stratego.primitives;

import java.util.Set;

import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.AbstractStrategoOperatorRegistry;

import com.google.inject.Inject;

public class SpoofaxEclipsePrimitiveLibrary extends AbstractStrategoOperatorRegistry {
    public static final String REGISTRY_NAME = SpoofaxEclipsePrimitiveLibrary.class.getName();


    @Inject public SpoofaxEclipsePrimitiveLibrary(Set<AbstractPrimitive> primitives) {
        for(AbstractPrimitive primitive : primitives) {
            add(primitive);
        }
    }


    @Override public String getOperatorRegistryName() {
        return REGISTRY_NAME;
    }
}
