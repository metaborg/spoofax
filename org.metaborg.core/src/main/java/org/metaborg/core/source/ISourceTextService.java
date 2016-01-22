package org.metaborg.core.source;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;

/**
 * Interface for retrieving text of resources.
 */
public interface ISourceTextService {
    /**
     * Retrieves the text for given resource.
     * 
     * @param resource
     *            Resource to retrieve text for.
     * @return Text for given resource.
     */
    String text(FileObject resource) throws IOException;
}
