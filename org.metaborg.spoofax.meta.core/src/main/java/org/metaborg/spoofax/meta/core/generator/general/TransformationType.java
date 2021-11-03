package org.metaborg.spoofax.meta.core.generator.general;

import java.util.Map;

import com.google.common.collect.Maps;

public enum TransformationType {
    Stratego1("stratego", "Stratego", "str"), Stratego2("stratego2", "Stratego 2", "str2"), None("none", "None", "");


    public final String id;
    public final String name;
    public final String fileExtension;


    TransformationType(String id, String name, String fileExtension) {
        this.id = id;
        this.name = name;
        this.fileExtension = fileExtension;
    }


    public static Map<String, TransformationType> mapping() {
        final Map<String, TransformationType> syntaxTypes = Maps.newHashMap();
        for(TransformationType syntaxType : TransformationType.values()) {
            syntaxTypes.put(syntaxType.name, syntaxType);
        }
        return syntaxTypes;
    }
}
