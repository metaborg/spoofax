package org.metaborg.spoofax.core.build.processing;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.metaborg.spoofax.core.build.BuildInput;
import org.metaborg.spoofax.core.build.IBuildOutput;
import org.metaborg.spoofax.core.build.IBuilder;
import org.metaborg.spoofax.core.language.LanguageChange;

import rx.functions.Func0;

public class BlockingProcessor<P, A, T> implements IProcessor<P, A, T> {
    private final IBuilder<P, A, T> builder;


    public BlockingProcessor(IBuilder<P, A, T> builder) {
        this.builder = builder;
    }


    @Override public ITask<IBuildOutput<P, A, T>> build(final BuildInput input) {
        return new BlockingTask<IBuildOutput<P, A, T>>(new Func0<IBuildOutput<P, A, T>>() {
            @Override public IBuildOutput<P, A, T> call() {
                return builder.build(input);
            }
        });
    }

    @Override public ITask<?> clean(final FileObject location, final FileSelector excludeSelector) {
        return new BlockingTask<Object>(new Func0<Object>() {
            @Override public Object call() {
                builder.clean(location, excludeSelector);
                return null;
            }
        });
    }

    @Override public ITask<?> languageChange(LanguageChange change) {
        // TODO Auto-generated method stub
        return null;
    }
}
