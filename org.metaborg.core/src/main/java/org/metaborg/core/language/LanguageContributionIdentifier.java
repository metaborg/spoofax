package org.metaborg.core.language;

import java.io.Serializable;

public class LanguageContributionIdentifier implements Serializable {
    private static final long serialVersionUID = -3074869698162405693L;
    
    public final LanguageIdentifier identifier;
    public final String name;


    public LanguageContributionIdentifier(LanguageIdentifier identifier, String name) {
        this.identifier = identifier;
        this.name = name;
    }
}
