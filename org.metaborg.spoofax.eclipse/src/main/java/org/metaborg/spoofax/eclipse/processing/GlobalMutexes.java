package org.metaborg.spoofax.eclipse.processing;

import com.google.inject.Inject;

public class GlobalMutexes {
    public final MutexRule startupMutex;
    public final MutexRule languageServiceMutex;


    @Inject public GlobalMutexes() {
        this.startupMutex = new MutexRule();
        this.languageServiceMutex = new MutexRule();
    }
}
