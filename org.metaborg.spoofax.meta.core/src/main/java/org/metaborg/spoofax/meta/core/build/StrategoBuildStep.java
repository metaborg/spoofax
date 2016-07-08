package org.metaborg.spoofax.meta.core.build;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.meta.core.config.LanguageSpecBuildPhase;
import org.metaborg.spoofax.meta.core.config.StrategoBuildStepConfig;
import org.metaborg.spoofax.meta.core.pluto.StrategoExecutor;
import org.metaborg.spoofax.meta.core.pluto.StrategoExecutor.ExecutionResult;
import org.metaborg.spoofax.meta.core.pluto.util.ResourceAgentTracker;

import com.google.inject.Inject;

public class StrategoBuildStep extends AConfigBuildStep<StrategoBuildStepConfig> {
    private final IResourceService resourceService;


    @Inject public StrategoBuildStep(IResourceService resourceService) {
        super(StrategoBuildStepConfig.class);

        this.resourceService = resourceService;
    }


    @Override protected void execute(StrategoBuildStepConfig config, LanguageSpecBuildPhase phase,
        LanguageSpecBuildInput input) throws MetaborgException {
        // @formatter:off
        final StrategoExecutor executor = new StrategoExecutor()
            .withToolsContext()
            .withTracker(new ResourceAgentTracker(resourceService, input.languageSpec().location()))
            .withStrategyName(config.strategy)
            .withName(config.strategy)
            ;
        // @formatter:on
        final ExecutionResult result = executor.executeCLI(config.arguments());

        if(!result.success) {
            throw new MetaborgException("Stratego execution of " + config.strategy + " failed");
        }
    }
}
