package org.metaborg.spoofax.core.transform.compile;

import javax.annotation.Nullable;

import org.metaborg.core.language.IFacet;

public class CompilerFacet implements IFacet {
	private static final long serialVersionUID = 3377061041002083621L;
	
	public final @Nullable String strategyName;


    public CompilerFacet() {
        this(null);
    }

    public CompilerFacet(String strategyName) {
        this.strategyName = strategyName;
    }
}
