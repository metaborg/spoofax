package org.metaborg.spoofax.core.transform.stratego.compile;

import javax.annotation.Nullable;

import org.metaborg.spoofax.core.language.ILanguageFacet;

public class CompilerFacet implements ILanguageFacet {
	private static final long serialVersionUID = 3377061041002083621L;
	
	public final @Nullable String strategyName;


    public CompilerFacet() {
        this(null);
    }

    public CompilerFacet(String strategyName) {
        this.strategyName = strategyName;
    }
}
