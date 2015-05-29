package org.metaborg.spoofax.core.processing;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.LanguageChange;

import rx.functions.Func0;

public class BlockingProcessor<P, A, T> implements IProcessor<P, A, T> {
    private final IBuilder<P, A, T> builder;


    public BlockingProcessor(IBuilder<P, A, T> builder) {
        this.builder = builder;
    }


    @Override public ITask<BuildOutput<P, A, T>> build(final BuildInput input) {
        return new BlockingTask<BuildOutput<P, A, T>>(new Func0<BuildOutput<P, A, T>>() {
            @Override public BuildOutput<P, A, T> call() {
                return builder.build(input);
            }
        });
    }

    @Override public ITask<?> clean(final FileObject location) {
        return new BlockingTask<Object>(new Func0<Object>() {
            @Override public Object call() {
                builder.clean(location);
                return new Object();
            }
        });
    }

    @Override public ITask<?> metaBuild(FileObject location) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override public ITask<?> metaClean(FileObject location) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override public ITask<?> languageChange(LanguageChange change) {
        // TODO Auto-generated method stub
        return null;
    }
}
