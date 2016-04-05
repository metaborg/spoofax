package org.metaborg.core.language;

import java.io.Serializable;

public class LanguageContributionIdentifier implements Serializable {
    private static final long serialVersionUID = -3074869698162405693L;
    
    public final LanguageIdentifier id;
    public final String name;


    public LanguageContributionIdentifier(LanguageIdentifier id, String name) {
        this.id = id;
        this.name = name;
    }
}
