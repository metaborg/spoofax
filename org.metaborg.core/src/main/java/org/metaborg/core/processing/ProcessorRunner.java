package org.metaborg.core.processing;

import javax.annotation.Nullable;

import org.metaborg.core.build.BuildInput;
import org.metaborg.core.build.CleanInput;
import org.metaborg.core.build.IBuildOutput;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.LanguageChange;
import org.metaborg.core.project.IProject;
import org.metaborg.core.resource.ResourceChange;

import rx.functions.Action1;

import com.google.inject.Inject;

/**
 * Default implementation for the processor runner.
 */
public class ProcessorRunner<P, A, T> implements IProcessorRunner<P, A, T> {
    private final IProcessor<P, A, T> processor;


    @Inject public ProcessorRunner(IProcessor<P, A, T> processor, ILanguageService languageService) {
        this.processor = processor;

        languageService.changes().subscribe(new Action1<LanguageChange>() {
            @Override public void call(LanguageChange change) {
                languageChange(change);
            }
        });
    }


    @Override public ITask<IBuildOutput<P, A, T>> build(BuildInput input, @Nullable IProgressReporter progressReporter,
        @Nullable ICancellationToken cancellationToken) {
        return processor.build(input, progressReporter, cancellationToken);
    }

    @Override public ITask<?> clean(CleanInput input, @Nullable IProgressReporter progressReporter,
        @Nullable ICancellationToken cancellationToken) {
        return processor.clean(input, progressReporter, cancellationToken);
    }


    @Override public ITask<?> updateDialects(IProject project, Iterable<ResourceChange> changes) {
        return processor.updateDialects(project, changes);
    }


    private void languageChange(LanguageChange change) {
        final ITask<?> task = processor.languageChange(change);
        task.schedule();
    }
}
