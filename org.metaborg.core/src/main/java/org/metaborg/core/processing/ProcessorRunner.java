package org.metaborg.core.processing;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.build.BuildInput;
import org.metaborg.core.build.CleanInput;
import org.metaborg.core.build.IBuildOutput;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.LanguageComponentChange;
import org.metaborg.core.language.LanguageImplChange;
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

        languageService.componentChanges().subscribe(new Action1<LanguageComponentChange>() {
            @Override public void call(LanguageComponentChange change) {
                languageChange(change);
            }
        });

        languageService.implChanges().subscribe(new Action1<LanguageImplChange>() {
            @Override public void call(LanguageImplChange change) {
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
