package org.metaborg.spoofax.core.source;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;

public class SourceTextService implements ISourceTextService {
    @Override public String text(FileObject resource) throws IOException {
        return IOUtils.toString(resource.getContent().getInputStream());
    }
}
