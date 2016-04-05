package org.metaborg.core.analysis;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.messages.IMessage;

/**
 * An update to an analyze unit, made from analyzing a different unit.
 */
public interface IAnalyzeUnitUpdate {
    /**
     * @return Source file the update is for.
     */
    FileObject source();

    /**
     * @return Updated messages.
     */
    Iterable<IMessage> messages();
}
