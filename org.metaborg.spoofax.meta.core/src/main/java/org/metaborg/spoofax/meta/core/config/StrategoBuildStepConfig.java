package org.metaborg.spoofax.meta.core.config;

import org.metaborg.util.cmd.Arguments;

public class StrategoBuildStepConfig implements IBuildStepConfig {
    public final LanguageSpecBuildPhase phase;
    public final String strategy;
    public final Arguments arguments;


    public StrategoBuildStepConfig(LanguageSpecBuildPhase phase, String strategy, Arguments arguments) {
        this.phase = phase;
        this.strategy = strategy;
        this.arguments = arguments;
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
