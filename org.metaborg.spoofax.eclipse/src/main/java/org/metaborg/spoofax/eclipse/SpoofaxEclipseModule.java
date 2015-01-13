package org.metaborg.spoofax.eclipse;

import org.apache.commons.vfs2.FileSystemManager;
import org.metaborg.spoofax.core.SpoofaxModule;
import org.metaborg.spoofax.eclipse.resource.EclipseFileSystemManagerProvider;

import com.google.inject.Singleton;

public class SpoofaxEclipseModule extends SpoofaxModule {
    public SpoofaxEclipseModule(ClassLoader resourceClassLoader) {
        super(resourceClassLoader);
    }

    public SpoofaxEclipseModule() {
        super(SpoofaxEclipseModule.class.getClassLoader());
    }


    @Override protected void bindFileSystemManager() {
        bind(FileSystemManager.class).toProvider(EclipseFileSystemManagerProvider.class).in(Singleton.class);
    }
}
