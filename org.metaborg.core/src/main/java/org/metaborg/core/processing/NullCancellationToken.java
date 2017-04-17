package org.metaborg.core.processing;

import org.metaborg.util.task.ICancel;

/**
 * Cancellation token implementation that never cancels.
 * 
 * @deprecated Use {@link NullCancel} instead.
 */
@Deprecated public class NullCancellationToken extends NullCancel implements ICancel {

}
