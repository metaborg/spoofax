package org.metaborg.spoofax.meta.core.ant;

import javax.annotation.Nullable;

import org.metaborg.core.processing.ICancellationToken;

public interface IAntRunner {
    void execute(String target, @Nullable ICancellationToken cancellationToken) throws Exception;
}
