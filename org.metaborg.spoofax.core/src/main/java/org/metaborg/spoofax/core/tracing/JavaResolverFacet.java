package org.metaborg.spoofax.core.tracing;
import org.metaborg.core.language.IFacet;

public class JavaResolverFacet implements IFacet {
    public final String javaClassName;


    public JavaResolverFacet(String javaClassName) {
        this.javaClassName = javaClassName;
    }
}
