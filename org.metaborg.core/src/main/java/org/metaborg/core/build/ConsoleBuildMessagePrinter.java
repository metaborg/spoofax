package org.metaborg.core.build;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageSeverity;
import org.metaborg.core.project.IProject;
import org.metaborg.core.source.AffectedSourceHelper;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.Level;
import org.metaborg.util.log.LoggerUtils;

/**
 * Build message printer implementation that prints detailed messages to a stream.
 */
public class ConsoleBuildMessagePrinter implements IBuildMessagePrinter {
    private final ISourceTextService sourceTextService;
    private final PrintStream infoStream;
    private final PrintStream warnStream;
    private final PrintStream errorStream;
    private final boolean printHighlight;
    private final boolean printExceptions;


    public ConsoleBuildMessagePrinter(ISourceTextService sourceTextService, boolean printHighlight,
        boolean printExceptions, OutputStream infoStream, OutputStream warnStream, OutputStream errorStream) {
        this.sourceTextService = sourceTextService;
        this.infoStream = new PrintStream(infoStream);
        this.warnStream = new PrintStream(warnStream);
        this.errorStream = new PrintStream(errorStream);
        this.printHighlight = printHighlight;
        this.printExceptions = printExceptions;
    }

    public ConsoleBuildMessagePrinter(ISourceTextService sourceTextService, boolean printHighlight,
        boolean printExceptions, ILogger logger) {
        this(sourceTextService, printHighlight, printExceptions, LoggerUtils.stream(logger, Level.Info), LoggerUtils
            .stream(logger, Level.Warn), LoggerUtils.stream(logger, Level.Error));
    }


    @Override public void print(IMessage message, boolean pardoned) {
        final StringBuilder sb = new StringBuilder();

        sb.append(message.severity());

        final FileObject source = message.source();
        if(source != null) {
            sb.append(" in ");
            sb.append(source.getName().getPath());
            sb.append(":" + message.region().startRow());
            sb.append('\n');
        }

        if(printHighlight) {
            try {
                final String sourceText = sourceTextService.text(source);
                sb.append(AffectedSourceHelper.affectedSourceText(message.region(), sourceText, "    "));
            } catch(IOException e) {
            }
        }

        print(sb, message.message(), message.exception(), message.severity(), pardoned);
    }

    @Override public void print(FileObject source, String message, @Nullable Throwable e, boolean pardoned) {
        final StringBuilder sb = new StringBuilder();
        sb.append("EXCEPTION in ");
        sb.append(source.getName().getPath());
        sb.append('\n');

        print(sb, message, e, MessageSeverity.ERROR, pardoned);
    }

    @Override public void print(IProject project, String message, @Nullable Throwable e, boolean pardoned) {
        final StringBuilder sb = new StringBuilder();
        sb.append("EXCEPTION in project ");
        sb.append(project.location().getName().getPath());
        sb.append('\n');

        print(sb, message, e, MessageSeverity.ERROR, pardoned);
    }


    private void print(StringBuilder sb, String message, @Nullable Throwable e, MessageSeverity severity,
        boolean pardoned) {
        sb.append(message);
        sb.append('\n');

        if(printExceptions) {
            if(e != null) {
                sb.append("\tCaused by:\n");
                final StringWriter sw = new StringWriter();
                final PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                sb.append(sw.toString());
                sb.append('\n');
            }
        }

        sb.append('\n');

        final String str = sb.toString();
        if(pardoned) {
            infoStream.print(str);
            infoStream.flush();
        } else {
            switch(severity) {
                case NOTE:
                    infoStream.print(str);
                    infoStream.flush();
                    break;
                case WARNING:
                    warnStream.print(str);
                    warnStream.flush();
                    break;
                case ERROR:
                    errorStream.print(str);
                    errorStream.flush();
                    break;
            }
        }
    }
}
