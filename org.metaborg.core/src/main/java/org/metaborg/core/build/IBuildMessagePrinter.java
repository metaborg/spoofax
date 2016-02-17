package org.metaborg.core.build;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.project.IProject;

/**
 * Interface for printing messages created during a build.
 */
public interface IBuildMessagePrinter {
    /**
     * Prints given message, located inside source text.
     */
    void print(IMessage message, boolean pardoned);

    /**
     * Prints given message and exception, located at a resource. Used if the source location is not available.
     * 
     * @param resource
     *            Resource the message occurred in.
     * @param message
     *            Message to print.
     * @param e
     *            Exception to print, or null if there is no exception.
     */
    void print(FileObject resource, String message, @Nullable Throwable e, boolean pardoned);

    /**
     * Prints given message and exception, located at a language specification. Used if the source location or resource
     * is not available.
     *
     * @param project
     *            Project the message occurred in.
     * @param message
     *            Message to print.
     * @param e
     *            Exception to print, or null if there is no exception.
     */
    void print(IProject project, String message, @Nullable Throwable e, boolean pardoned);

    /**
     * Prints a summary based on printed messages before.
     */
    void printSummary();
}
