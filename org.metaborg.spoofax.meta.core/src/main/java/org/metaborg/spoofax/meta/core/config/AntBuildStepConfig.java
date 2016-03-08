package org.metaborg.spoofax.meta.core.config;

public class AntBuildStepConfig implements IBuildStepConfig {
    public final LanguageSpecBuildPhase phase;
    public final String file;
    public final String target;


    public AntBuildStepConfig(LanguageSpecBuildPhase phase, String file, String target) {
        this.phase = phase;
        this.file = file;
        this.target = target;
    }


    @Override public LanguageSpecBuildPhase phase() {
        return phase;
    }

    @Override public void accept(IBuildStepVisitor visitor) {
        visitor.visit(this);
    }

    @Override public void accept(IBuildStepVisitor visitor, LanguageSpecBuildPhase phase) {
        if(this.phase.equals(phase)) {
            visitor.visit(this);
        }
    }
}
