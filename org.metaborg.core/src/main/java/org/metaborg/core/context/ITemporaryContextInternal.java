package org.metaborg.core.context;

/**
 * Extension of {@link ITemporaryContext} with {@link IContextInternal} that should not be exposed to clients.
 */
public interface ITemporaryContextInternal extends IContextInternal, ITemporaryContext {

}
