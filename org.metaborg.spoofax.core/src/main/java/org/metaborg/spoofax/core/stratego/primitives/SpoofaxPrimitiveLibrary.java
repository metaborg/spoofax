package org.metaborg.spoofax.core.stratego.primitives;

import java.util.Set;

import org.spoofax.interpreter.library.AbstractPrimitive;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class SpoofaxPrimitiveLibrary extends GenericPrimitiveLibrary {
    @Inject public SpoofaxPrimitiveLibrary(@Named("SpoofaxPrimitiveLibrary") Set<AbstractPrimitive> primitives) {
        super(primitives, "SpoofaxPrimitiveLibrary");
    }
}
