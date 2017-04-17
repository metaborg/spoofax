package org.metaborg.core.messages;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;
import org.metaborg.core.source.AffectedSourceHelper;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.core.source.ISourceTextService;

/**
 * Prints note, warning, and error messages similar to large compilers such as GCC or Clang. That is, the message comes
 * with filename, line, column, and severity. Also the affected code is underlined with carets (^).
 */
public class WithLocationStreamMessagePrinter implements IMessagePrinter {

    /**
     * To pretty-print the affected source code of a message.
     */
    private final ISourceTextService sourceTextService;
    /**
     * To get the relative filename of source file.
     */
    private final IProjectService projectService;

    private final PrintStream outputStream;

    public WithLocationStreamMessagePrinter(ISourceTextService sourceTextService, IProjectService projectService,
        OutputStream outputStream) {
        this.sourceTextService = sourceTextService;
        this.projectService = projectService;
        this.outputStream = new PrintStream(outputStream);
    }

    @Override public void print(IMessage message, boolean pardoned) {
        print(message.message(), pardoned, message.source(), message.severity(), message.region());
    }

    @Override public void print(@Nullable FileObject resource, String message, Throwable e, boolean pardoned) {
        print(message, pardoned, resource, null, null);
    }

    @Override public void print(IProject project, String message, Throwable e, boolean pardoned) {
        print(message, pardoned, null, null, null);
    }

    @Override public void printSummary() {
    }

    private void print(String message, boolean pardoned, @Nullable FileObject sourceFile,
        @Nullable MessageSeverity severity, @Nullable ISourceRegion sourceRegion) {
        if(pardoned) {
            return;
        }

        final StringBuilder sb = new StringBuilder();
        // if available, print filename, line number, and column
        if(sourceFile != null) {
            try {
                // source file name relative to project location (so that we don't output long absolute paths)
                final IProject project = projectService.get(sourceFile);
                String relativeSourceFilename = sourceFile.getName().getBaseName();
                if(project != null // projectService.get() is @Nullable
                    && project.location().getName() != sourceFile.getName() // don't "relativize" if project == source
                ) {
                    relativeSourceFilename = project.location().getName().getRelativeName(sourceFile.getName());
                }

                sb.append(relativeSourceFilename);
                sb.append(':');

                if(sourceRegion != null && sourceRegion.startRow() != -1 && sourceRegion.startColumn() != -1) {
                    sb.append(sourceRegion.startRow() + 1); // startRow() is in [0, #lines)
                    sb.append(':');
                    sb.append(sourceRegion.startColumn() + 1);
                    sb.append(':');
                }

                sb.append(' ');
            } catch(FileSystemException ignored) {
            }
        }

        // print severity and message
        if(severity != null) {
            sb.append(severity.name().toLowerCase());
            sb.append(": ");
        }
        sb.append(message);
        sb.append('\n');

        // if available, pretty-print source code
        if(sourceFile != null && sourceRegion != null) {
            try {
                final String affectedSource =
                    AffectedSourceHelper.affectedSourceText(sourceRegion, sourceTextService.text(sourceFile), " ");
                if(affectedSource != null) { // affectedSourceText() is @Nullable
                    sb.append(affectedSource);
                }
            } catch(IOException ignored) {
            }
        }

        outputStream.print(sb.toString());
        outputStream.flush();
    }

}
