package org.metaborg.spoofax.core.stratego.primitive.legacy.parse;

import java.util.Set;

import org.metaborg.spoofax.core.stratego.primitive.generic.GenericPrimitiveLibrary;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.jsglr.JSGLRLibrary;


public class LegacySpoofaxJSGLRLibrary extends GenericPrimitiveLibrary {
    public static final String injectionName = "LegacySpoofaxJSGLRLibrary";

    
    @jakarta.inject.Inject @javax.inject.Inject public LegacySpoofaxJSGLRLibrary(@jakarta.inject.Named(injectionName) @javax.inject.Named(injectionName) Set<AbstractPrimitive> primitives) {
        super(primitives, JSGLRLibrary.REGISTRY_NAME);
    }
}
