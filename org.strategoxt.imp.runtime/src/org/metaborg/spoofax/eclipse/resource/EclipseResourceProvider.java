package org.metaborg.spoofax.eclipse.resource;

import java.util.Collection;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;

import com.google.common.collect.ImmutableList;

public class EclipseResourceProvider extends AbstractOriginatingFileProvider {
    // @formatter:off
    public static final Collection<Capability> capabilities = ImmutableList.of(
        Capability.CREATE,
        Capability.DELETE,
        Capability.RENAME,
        Capability.GET_TYPE,
        Capability.GET_LAST_MODIFIED,
        Capability.SET_LAST_MODIFIED_FILE,
        Capability.SET_LAST_MODIFIED_FOLDER,
        Capability.LIST_CHILDREN,
        Capability.READ_CONTENT,
        Capability.URI,
        Capability.WRITE_CONTENT,
        Capability.APPEND_CONTENT,
        Capability.RANDOM_ACCESS_READ,
        Capability.RANDOM_ACCESS_WRITE
    );
    // @formatter:on

    @Override public Collection<Capability> getCapabilities() {
        return capabilities;
    }

    @Override protected FileSystem doCreateFileSystem(FileName rootName, FileSystemOptions fileSystemOptions)
        throws FileSystemException {
        return new EclipseResourceFileSystem(rootName, null, fileSystemOptions);
    }
}
