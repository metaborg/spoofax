package org.metaborg.spoofax.meta.core.generator.language;

public enum AnalysisType {
    NaBL_TS("nabl_ts"), 
    NaBL2("nabl2"), 
    Stratego("stratego"), 
    None("none");


    public final String name;


    AnalysisType(String name) {
        this.name = name;
    }
}
