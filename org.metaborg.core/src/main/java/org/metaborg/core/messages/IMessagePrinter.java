package org.metaborg.core.messages;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.IProject;

/**
 * Interface for printing messages.
 */
public interface IMessagePrinter {
    /**
     * Prints given message.
     * 
     * @param message
     *            Message to print.
     * @param pardoned
     *            If the (error or warning) message is pardoned, i.e. the message is acceptable even if it indicates a
     *            problem.
     */
    void print(IMessage message, boolean pardoned);

    /**
     * Prints given message and exception, located at a resource. Used if the source location is not available.
     * 
     * @param resource
     *            Resource the message occurred in, or null if the message was detached.
     * @param message
     *            Message to print.
     * @param e
     *            Exception to print, or null if there is no exception.
     * @param pardoned
     *            If the (error or warning) message is pardoned, i.e. the message is acceptable even if it indicates a
     *            problem.
     */
    void print(@Nullable FileObject resource, String message, @Nullable Throwable e, boolean pardoned);

    /**
     * Prints given message and exception, located at a project. Used if the source location or resource is not
     * available.
     *
     * @param project
     *            Project the message occurred in.
     * @param message
     *            Message to print.
     * @param e
     *            Exception to print, or null if there is no exception.
     * @param pardoned
     *            If the (error or warning) message is pardoned, i.e. the message is acceptable even if it indicates a
     *            problem.
     */
    void print(IProject project, String message, @Nullable Throwable e, boolean pardoned);

    /**
     * Prints a summary based on printed messages before.
     */
    void printSummary();
}
