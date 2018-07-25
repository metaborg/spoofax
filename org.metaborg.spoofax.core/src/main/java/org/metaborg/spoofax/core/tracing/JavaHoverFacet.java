package org.metaborg.spoofax.core.tracing;
import org.metaborg.core.language.IFacet;

public class JavaHoverFacet implements IFacet {
    public final String javaClassName;


    public JavaHoverFacet(String javaClassName) {
        this.javaClassName = javaClassName;
    }
}
