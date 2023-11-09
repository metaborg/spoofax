package org.metaborg.spoofax.meta.core.ant;

import jakarta.annotation.Nullable;

import org.metaborg.core.MetaborgException;
import org.metaborg.util.task.ICancel;

public interface IAntRunner {
    void execute(String target, @Nullable ICancel cancel) throws MetaborgException;
}
