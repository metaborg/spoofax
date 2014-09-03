package org.metaborg.spoofax.eclipse.resource;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;
import org.metaborg.spoofax.core.resource.DefaultFileSystemManagerProvider;

public class EclipseFileSystemManagerProvider extends DefaultFileSystemManagerProvider {
    @Override protected void addDefaultProvider(DefaultFileSystemManager manager) throws FileSystemException {
        final EclipseResourceProvider provider = new EclipseResourceProvider();
        manager.addProvider("eclipse", provider);
        manager.setDefaultProvider(provider);
        final FileObject root = provider.findFile(null, "eclipse:///", new FileSystemOptions());
        manager.setBaseFile(root);
    }

    @Override protected void addProviders(DefaultFileSystemManager manager) throws FileSystemException {
        super.addProviders(manager);
        manager.addProvider("file", new DefaultLocalFileProvider());
        manager.addProvider("bundleresource", new EclipseBundleResourceProvider());
    }
}
