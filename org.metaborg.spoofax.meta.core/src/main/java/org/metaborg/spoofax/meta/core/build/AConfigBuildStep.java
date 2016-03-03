package org.metaborg.spoofax.meta.core.build;

import org.metaborg.core.MetaborgException;
import org.metaborg.spoofax.meta.core.config.IBuildStepConfig;
import org.metaborg.spoofax.meta.core.config.LanguageSpecBuildPhase;

public abstract class AConfigBuildStep<T> implements IBuildStep {
    private final Class<T> clazz;


    public AConfigBuildStep(Class<T> clazz) {
        this.clazz = clazz;
    }


    @SuppressWarnings("unchecked") @Override public void execute(LanguageSpecBuildPhase phase,
        LanguageSpecBuildInput input) throws MetaborgException {
        final Iterable<IBuildStepConfig> configs = input.languageSpec().config().buildSteps();
        for(IBuildStepConfig config : configs) {
            if(!config.phase().equals(phase) || !config.getClass().equals(clazz)) {
                continue;
            }
            execute((T) config, phase, input);
        }
    }


    protected abstract void execute(T config, LanguageSpecBuildPhase phase, LanguageSpecBuildInput input)
        throws MetaborgException;
}
