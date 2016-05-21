package org.metaborg.spoofax.meta.core.config;

import org.metaborg.util.cmd.Arguments;

public class StrategoBuildStepConfig implements IBuildStepConfig {
    private static final long serialVersionUID = -6818519311323411847L;

    public final LanguageSpecBuildPhase phase;
    public final String strategy;
    public final Iterable<String> args;


    public StrategoBuildStepConfig(LanguageSpecBuildPhase phase, String strategy, Iterable<String> args) {
        this.phase = phase;
        this.strategy = strategy;
        this.args = args;
    }


    public Arguments arguments() {
        final Arguments arguments = new Arguments();
        for(String arg : args) {
            arguments.add(arg);
        }
        return arguments;
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
