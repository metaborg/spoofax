package org.metaborg.spoofax.core.build;

import org.metaborg.core.build.BuildInput;
import org.metaborg.core.build.Builder;
import org.metaborg.core.build.IBuildOutputInternal;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalysisService;
import org.metaborg.spoofax.core.processing.analyze.ISpoofaxAnalysisResultUpdater;
import org.metaborg.spoofax.core.processing.parse.ISpoofaxParseResultUpdater;
import org.metaborg.spoofax.core.syntax.ISpoofaxSyntaxService;
import org.metaborg.spoofax.core.transform.ISpoofaxTransformService;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.metaborg.util.task.ICancel;
import org.metaborg.util.task.IProgress;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Typedef class for {@link Builder} with Spoofax interfaces.
 */
public class SpoofaxBuilder extends
    Builder<ISpoofaxInputUnit, ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxAnalyzeUnitUpdate, ISpoofaxTransformUnit<?>, ISpoofaxTransformUnit<ISpoofaxParseUnit>, ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit>>
    implements ISpoofaxBuilder {
    @Inject public SpoofaxBuilder(IResourceService resourceService, ILanguageIdentifierService languageIdentifier,
        ILanguagePathService languagePathService, ISpoofaxUnitService unitService, ISourceTextService sourceTextService,
        ISpoofaxSyntaxService syntaxService, IContextService contextService, ISpoofaxAnalysisService analysisService,
        ISpoofaxTransformService transformService, ISpoofaxParseResultUpdater parseResultUpdater,
        ISpoofaxAnalysisResultUpdater analysisResultUpdater,
        Provider<IBuildOutputInternal<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxAnalyzeUnitUpdate, ISpoofaxTransformUnit<?>>> buildOutputProvider) {
        super(resourceService, languageIdentifier, languagePathService, unitService, sourceTextService, syntaxService,
            contextService, analysisService, transformService, parseResultUpdater, analysisResultUpdater,
            buildOutputProvider);
    }


    @Override public ISpoofaxBuildOutput build(BuildInput input, IProgress progress,
        ICancel cancel) throws InterruptedException {
        return (ISpoofaxBuildOutput) super.build(input, progress, cancel);
    }

    @Override public ISpoofaxBuildOutput build(BuildInput input) throws InterruptedException {
        return (ISpoofaxBuildOutput) super.build(input);
    }
}
