package org.metaborg.core.processing;

import org.metaborg.util.task.ICancel;

/**
 * Interface for figuring out if something has been cancelled.
 * 
 * @deprecated Use {@link ICancel} instead.
 */
@Deprecated
public interface ICancellationToken extends ICancel {
}
