package org.metaborg.spoofax.core;

import org.apache.commons.vfs2.FileSystemManager;
import org.metaborg.spoofax.core.language.ILanguageService;

import com.google.inject.Inject;

public class SpoofaxSession {
    public final FileSystemManager fileSystemManager;
    public final ILanguageService language;

    @Inject
    public SpoofaxSession(FileSystemManager fileSystemManager, ILanguageService language) {
        this.fileSystemManager = fileSystemManager;
        this.language = language;
    }
}
