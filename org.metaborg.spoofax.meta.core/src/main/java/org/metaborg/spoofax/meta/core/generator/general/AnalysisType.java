package org.metaborg.spoofax.meta.core.generator.general;

import java.util.Map;

import com.google.common.collect.Maps;

public enum AnalysisType {
    NaBL2("nabl2", "NaBL2"), NaBL_TS("nabl_ts", "NaBL & TS (deprecated)"), Stratego("stratego", "Stratego (deprecated)"), None("none", "None");


    public final String id;
    public final String name;


    AnalysisType(String id, String name) {
        this.id = id;
        this.name = name;
    }


    public static Map<String, AnalysisType> mapping() {
        final Map<String, AnalysisType> analysisTypes = Maps.newHashMap();
        for(AnalysisType analysisType : AnalysisType.values()) {
            analysisTypes.put(analysisType.name, analysisType);
        }
        return analysisTypes;
    }
}
