package org.metaborg.core.processing;

import org.metaborg.util.task.ICancel;
import org.metaborg.util.task.NullCancel;

/**
 * Cancellation token implementation that never cancels.
 * 
 * @deprecated Use {@link NullCancel} instead.
 */
@Deprecated public class NullCancellationToken extends NullCancel implements ICancel {

}
