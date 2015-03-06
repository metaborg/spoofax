package org.metaborg.spoofax.eclipse.resource;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;
import org.metaborg.spoofax.core.resource.DefaultFileSystemManagerProvider;

public class EclipseFileSystemManagerProvider extends DefaultFileSystemManagerProvider {
    @Override protected void addDefaultProvider(DefaultFileSystemManager manager) throws FileSystemException {
        final EclipseResourceProvider provider = new EclipseResourceProvider();
        manager.addProvider("eclipse", provider);
        manager.setDefaultProvider(provider);
    }

    @Override protected void setBaseFile(DefaultFileSystemManager manager) throws FileSystemException {
        manager.setBaseFile(manager.resolveFile("eclipse:///"));
    }

    @Override protected void addProviders(DefaultFileSystemManager manager) throws FileSystemException {
        super.addProviders(manager);
        manager.addProvider("file", new DefaultLocalFileProvider());
        manager.addProvider("bundleresource", new EclipseBundleResourceProvider());
    }
}
