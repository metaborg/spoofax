package org.metaborg.spoofax.meta.core.build;

import org.metaborg.core.MetaborgException;
import org.metaborg.spoofax.meta.core.config.LanguageSpecBuildPhase;

public interface IBuildStep {
    /**
     * Executes the build step.
     * 
     * @param phase
     *            Phase in which the build step is executed.
     * @param input
     *            Build input
     */
    void execute(LanguageSpecBuildPhase phase, LanguageSpecBuildInput input) throws MetaborgException;
}
