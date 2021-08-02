package org.metaborg.spoofax.meta.core.generator.general;

import java.util.Map;

import com.google.common.collect.Maps;

public enum TransformationType {
    Stratego1("stratego", "Stratego"), Stratego2("stratego2", "Stratego 2"), None("none", "None");


    public final String id;
    public final String name;


    TransformationType(String id, String name) {
        this.id = id;
        this.name = name;
    }


    public static Map<String, TransformationType> mapping() {
        final Map<String, TransformationType> syntaxTypes = Maps.newHashMap();
        for(TransformationType syntaxType : TransformationType.values()) {
            syntaxTypes.put(syntaxType.name, syntaxType);
        }
        return syntaxTypes;
    }
}
