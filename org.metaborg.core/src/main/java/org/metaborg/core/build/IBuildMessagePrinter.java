package org.metaborg.core.build;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.project.IProject;

public interface IBuildMessagePrinter {
    public abstract void print(IMessage message);

    public abstract void print(FileObject resource, String message, @Nullable Throwable e);

    public abstract void print(IProject project, String message, @Nullable Throwable e);
}
