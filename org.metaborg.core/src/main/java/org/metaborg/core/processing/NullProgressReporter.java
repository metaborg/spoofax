package org.metaborg.core.processing;

import org.metaborg.util.task.IProgress;
import org.metaborg.util.task.NullProgress;

/**
 * Progress reporter implementation that ignores all progress reporting.
 * 
 * @deprecated Use {@link NullProgress} instead.
 */
@Deprecated
public class NullProgressReporter extends NullProgress implements IProgress {

}
