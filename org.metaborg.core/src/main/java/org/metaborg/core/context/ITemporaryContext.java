package org.metaborg.core.context;

public interface ITemporaryContext extends IContext, AutoCloseable {
    public abstract void close();
}
