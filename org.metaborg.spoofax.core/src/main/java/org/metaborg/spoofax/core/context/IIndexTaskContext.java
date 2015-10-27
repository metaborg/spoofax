package org.metaborg.spoofax.core.context;

import org.metaborg.core.context.IContext;
import org.metaborg.runtime.task.primitives.ITaskEngineContext;
import org.spoofax.interpreter.library.index.primitives.IIndexContext;

public interface IIndexTaskContext extends IContext, IIndexContext, ITaskEngineContext {

}
