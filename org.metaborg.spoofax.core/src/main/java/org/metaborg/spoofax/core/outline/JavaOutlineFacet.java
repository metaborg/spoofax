package org.metaborg.spoofax.core.outline;

public class JavaOutlineFacet implements IOutlineFacet {
    public final String javaClassName;
    public final int expandTo;


    public JavaOutlineFacet(String javaClassName, int expandTo) {
        this.javaClassName = javaClassName;
        this.expandTo = expandTo;
    }


    @Override
    public int getExpansionLevel() {
        return expandTo;
    }
}
