package org.metaborg.spoofax.core;

import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.metaborg.spoofax.core.language.ILanguageService;
import org.metaborg.spoofax.core.language.LanguageService;

import com.google.inject.AbstractModule;

public class SpoofaxModule extends AbstractModule {
    @Override protected void configure() {
        try {
            bind(FileSystemManager.class).toInstance(VFS.getManager());
            bind(ILanguageService.class).to(LanguageService.class);
            bind(SpoofaxSession.class).asEagerSingleton();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
