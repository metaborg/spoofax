package org.metaborg.spoofax.meta.core.generator.general;

import java.util.LinkedHashMap;
import java.util.Map;

public enum AnalysisType {
    Statix("statix", "Statix (traditional)"), Statix_Concurrent("statix_concurrent", "Statix (concurrent)"),
    NaBL2("nabl2", "NaBL2 (deprecated)"), NaBL_TS("nabl_ts", "NaBL & TS (deprecated)"),
    Stratego("stratego", "Stratego (deprecated)"), None("none", "None");


    public final String id;
    public final String name;


    AnalysisType(String id, String name) {
        this.id = id;
        this.name = name;
    }


    public static Map<String, AnalysisType> mapping() {
        final Map<String, AnalysisType> analysisTypes = new LinkedHashMap<>();
        for(AnalysisType analysisType : AnalysisType.values()) {
            analysisTypes.put(analysisType.name, analysisType);
        }
        return analysisTypes;
    }
}
