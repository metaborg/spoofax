package org.metaborg.spoofax.core.stratego.primitive.legacy.parse;

import java.util.Set;

import org.metaborg.spoofax.core.stratego.primitive.generic.GenericPrimitiveLibrary;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.jsglr.JSGLRLibrary;

import javax.inject.Inject;
import javax.inject.Named;

public class LegacySpoofaxJSGLRLibrary extends GenericPrimitiveLibrary {
    public static final String injectionName = "LegacySpoofaxJSGLRLibrary";

    
    @Inject public LegacySpoofaxJSGLRLibrary(@Named(injectionName) Set<AbstractPrimitive> primitives) {
        super(primitives, JSGLRLibrary.REGISTRY_NAME);
    }
}
