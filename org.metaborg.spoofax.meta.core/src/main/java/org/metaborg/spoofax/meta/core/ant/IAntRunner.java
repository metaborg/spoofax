package org.metaborg.spoofax.meta.core.ant;

import javax.annotation.Nullable;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.processing.ICancellationToken;

public interface IAntRunner {
    void execute(String target, @Nullable ICancellationToken cancellationToken) throws MetaborgException;
}
