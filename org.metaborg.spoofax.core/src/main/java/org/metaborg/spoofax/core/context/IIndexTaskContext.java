package org.metaborg.spoofax.core.context;

import javax.annotation.Nullable;

import org.metaborg.runtime.task.engine.ITaskEngine;
import org.metaborg.runtime.task.primitives.ITaskEngineContext;
import org.spoofax.interpreter.library.index.IIndex;
import org.spoofax.interpreter.library.index.primitives.IIndexContext;

public interface IIndexTaskContext extends IIndexContext, ITaskEngineContext {
    public abstract @Nullable IIndex index();

    public abstract @Nullable ITaskEngine taskEngine();
}
