package org.metaborg.spoofax.meta.core.generator.general;

import java.util.HashMap;
import java.util.Map;

public enum SyntaxType {
    SDF3("sdf3", "SDF3"), SDF2("sdf2", "SDF2 (deprecated)"), None("none", "None");


    public final String id;
    public final String name;


    SyntaxType(String id, String name) {
        this.id = id;
        this.name = name;
    }


    public static Map<String, SyntaxType> mapping() {
        final Map<String, SyntaxType> syntaxTypes = new HashMap<>();
        for(SyntaxType syntaxType : SyntaxType.values()) {
            syntaxTypes.put(syntaxType.name, syntaxType);
        }
        return syntaxTypes;
    }
}
