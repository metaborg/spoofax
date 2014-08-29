package org.metaborg.spoofax.eclipse.resource;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;
import org.apache.commons.vfs2.provider.ram.RamFileProvider;
import org.apache.commons.vfs2.provider.res.ResourceFileProvider;
import org.metaborg.spoofax.core.resource.DefaultFileSystemManagerProvider;

public class EclipseFileSystemManagerProvider extends DefaultFileSystemManagerProvider {
    @Override protected void addDefaultProvider(DefaultFileSystemManager manager) throws FileSystemException {
        final EclipseResourceProvider provider = new EclipseResourceProvider();
        manager.addProvider("eclipse", provider);
        manager.setDefaultProvider(provider);
    }

    @Override protected void addProviders(DefaultFileSystemManager manager) throws FileSystemException {
        manager.addProvider("ram", new RamFileProvider());
        manager.addProvider("res", new ResourceFileProvider());
        manager.addProvider("file", new DefaultLocalFileProvider());
    }
}
