package org.metaborg.spoofax.core.style;

public class TokenCategory implements ICategory {
    public final String token;


    public TokenCategory(String token) {
        this.token = token;
    }


    @Override public String name() {
        return token;
    }
}
