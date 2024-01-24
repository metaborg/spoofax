package org.metaborg.core.processing;

import jakarta.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.analysis.IAnalyzeUnit;
import org.metaborg.core.analysis.IAnalyzeUnitUpdate;
import org.metaborg.core.build.BuildInput;
import org.metaborg.core.build.CleanInput;
import org.metaborg.core.build.IBuildOutput;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.LanguageComponentChange;
import org.metaborg.core.language.LanguageImplChange;
import org.metaborg.core.resource.ResourceChange;
import org.metaborg.core.syntax.IParseUnit;
import org.metaborg.core.transform.ITransformUnit;
import org.metaborg.util.task.ICancel;
import org.metaborg.util.task.IProgress;


/**
 * Default implementation for the processor runner.
 */
public class ProcessorRunner<P extends IParseUnit, A extends IAnalyzeUnit, AU extends IAnalyzeUnitUpdate, T extends ITransformUnit<?>>
    implements IProcessorRunner<P, A, AU, T> {
    private final IProcessor<P, A, AU, T> processor;


    @jakarta.inject.Inject @javax.inject.Inject public ProcessorRunner(IProcessor<P, A, AU, T> processor, ILanguageService languageService) {
        this.processor = processor;

        languageService.componentChanges().subscribe(this::languageChange);

        languageService.implChanges().subscribe(this::languageChange);
    }


    @Override public ITask<? extends IBuildOutput<P, A, AU, T>> build(BuildInput input, @Nullable IProgress progress,
        @Nullable ICancel cancel) {
        return processor.build(input, progress, cancel);
    }

    @Override public ITask<?> clean(CleanInput input, @Nullable IProgress progress, @Nullable ICancel cancel) {
        return processor.clean(input, progress, cancel);
    }


    @Override public ITask<?> updateDialects(FileObject location, Iterable<ResourceChange> changes) {
        return processor.updateDialects(location, changes);
    }


    private void languageChange(LanguageComponentChange change) {
        final ITask<?> task = processor.languageChange(change);
        task.schedule();
    }

    private void languageChange(LanguageImplChange change) {
        final ITask<?> task = processor.languageChange(change);
        task.schedule();
    }
}
