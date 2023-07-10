package org.metaborg.spoofax.meta.core.build;

import java.util.HashMap;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.meta.core.ant.IAntRunner;
import org.metaborg.spoofax.meta.core.ant.IAntRunnerService;
import org.metaborg.spoofax.meta.core.config.AntBuildStepConfig;
import org.metaborg.spoofax.meta.core.config.LanguageSpecBuildPhase;

import javax.inject.Inject;

public class AntBuildStep extends AConfigBuildStep<AntBuildStepConfig> {
    private final IResourceService resourceService;
    private final IAntRunnerService antRunnerService;


    @Inject public AntBuildStep(IResourceService resourceService, IAntRunnerService antRunnerService) {
        super(AntBuildStepConfig.class);
        this.resourceService = resourceService;
        this.antRunnerService = antRunnerService;
    }


    @Override protected void execute(AntBuildStepConfig config, LanguageSpecBuildPhase phase,
        LanguageSpecBuildInput input) throws MetaborgException {
        final FileObject root = input.languageSpec().location();
        final FileObject file = resourceService.resolve(root, config.file);
        final IAntRunner runner = antRunnerService.get(file, root, new HashMap<String, String>(), null, null);
        runner.execute(config.target, null);
    }


    @Override public String toString() {
        return "Ant";
    }
}
