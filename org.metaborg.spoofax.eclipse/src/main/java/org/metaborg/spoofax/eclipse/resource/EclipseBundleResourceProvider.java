package org.metaborg.spoofax.eclipse.resource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileProvider;
import org.eclipse.core.runtime.FileLocator;

import com.google.common.collect.ImmutableList;

public class EclipseBundleResourceProvider extends AbstractFileProvider {
    // @formatter:off
    public static final Collection<Capability> capabilities = ImmutableList.of(
        Capability.DISPATCHER
    );
    // @formatter:on

    @Override public FileObject findFile(FileObject baseFile, String uri, FileSystemOptions fileSystemOptions)
        throws FileSystemException {
        try {
            final URL eclipseURL = new URL(uri);
            final URL nativeURL = FileLocator.resolve(eclipseURL);
            final FileObject file = getContext().getFileSystemManager().resolveFile(nativeURL.toExternalForm());
            return file;
        } catch(MalformedURLException e) {
            throw new FileSystemException("Invalid URI", e);
        } catch(IOException e) {
            throw new FileSystemException("Could not resolve Eclipse URI to Java native URI", e);
        }
    }

    @Override public Collection<Capability> getCapabilities() {
        return capabilities;
    }
}
