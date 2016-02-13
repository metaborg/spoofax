package org.metaborg.spoofax.core.style;

import org.metaborg.core.style.ICategory;

public class TokenCategory implements ICategory {
    private static final long serialVersionUID = 5080900364515756478L;
    
	public final String token;


    public TokenCategory(String token) {
        this.token = token;
    }


    @Override public String name() {
        return token;
    }


    @Override public String toString() {
        return name();
    }
}
