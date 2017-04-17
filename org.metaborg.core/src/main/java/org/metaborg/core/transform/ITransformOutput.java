package org.metaborg.core.transform;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;

/**
 * Class representing output of a transformation.
 */
public interface ITransformOutput {
    /**
     * Name of the output result, usually a string representation of the output file.
     */
    String name();

    /**
     * Output file, or null if output was not written to a file.
     */
    @Nullable FileObject output();
}
