package org.metaborg.spoofax.core.stratego.primitives;

import java.util.Set;

import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.jsglr.JSGLRLibrary;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class SpoofaxJSGLRLibrary extends GenericPrimitiveLibrary {
    @Inject public SpoofaxJSGLRLibrary(@Named("SpoofaxJSGLRLibrary") Set<AbstractPrimitive> primitives) {
        super(primitives, JSGLRLibrary.REGISTRY_NAME);
    }
}
