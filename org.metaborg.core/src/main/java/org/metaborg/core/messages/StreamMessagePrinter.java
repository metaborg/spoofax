package org.metaborg.core.messages;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.IProject;
import org.metaborg.core.source.AffectedSourceHelper;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.Level;
import org.metaborg.util.log.LoggerUtils;

/**
 * Message printer implementation that prints detailed messages to a stream.
 */
public class StreamMessagePrinter implements IMessagePrinter {
    private final ISourceTextService sourceTextService;
    private final PrintStream infoStream;
    private final PrintStream warnStream;
    private final PrintStream errorStream;
    private final boolean printHighlight;
    private final boolean printExceptions;

    private int notes = 0;
    private int warnings = 0;
    private int warningsPardoned = 0;
    private int errors = 0;
    private int errorsPardoned = 0;
    private int exceptions = 0;
    private int exceptionsPardoned = 0;


    public StreamMessagePrinter(ISourceTextService sourceTextService, boolean printHighlight, boolean printExceptions,
        OutputStream infoStream, OutputStream warnStream, OutputStream errorStream) {
        this.sourceTextService = sourceTextService;
        this.infoStream = new PrintStream(infoStream);
        this.warnStream = new PrintStream(warnStream);
        this.errorStream = new PrintStream(errorStream);
        this.printHighlight = printHighlight;
        this.printExceptions = printExceptions;
    }

    public StreamMessagePrinter(ISourceTextService sourceTextService, boolean printHighlight, boolean printExceptions,
        ILogger logger) {
        this(sourceTextService, printHighlight, printExceptions, LoggerUtils.stream(logger, Level.Info),
            LoggerUtils.stream(logger, Level.Warn), LoggerUtils.stream(logger, Level.Error));
    }


    @Override public void print(IMessage message, boolean pardoned) {
        final StringBuilder sb = new StringBuilder();

        final MessageSeverity severity = message.severity();
        sb.append(severity);
        if(severity != MessageSeverity.NOTE && pardoned) {
            sb.append(" (pardoned)");
        }

        final FileObject source = message.source();
        final ISourceRegion region = message.region();
        if(source != null) {
            sb.append(" in ");
            sb.append(source.getName().getURI());
            if(region != null) {
                sb.append(":").append(region.startRow());
            }
            sb.append('\n');
        }

        if(printHighlight) {
            try {
                final String sourceText = sourceTextService.text(source);
                if(region != null) {
                    final String affected =
                        AffectedSourceHelper.affectedSourceText(message.region(), sourceText, "    ");
                    if(affected != null) {
                        sb.append(affected);
                    }
                }
            } catch(IOException e) {
            }
        }

        print(sb, message.message(), message.exception(), message.severity(), pardoned);

        switch(severity) {
            case NOTE:
                ++notes;
                break;
            case WARNING:
                if(pardoned) {
                    ++warningsPardoned;
                } else {
                    ++warnings;
                }
                break;
            case ERROR:
                if(pardoned) {
                    ++errorsPardoned;
                } else {
                    ++errors;
                }
                break;
        }
    }

    @Override public void print(@Nullable FileObject source, String message, @Nullable Throwable e, boolean pardoned) {
        final StringBuilder sb = new StringBuilder();
        sb.append("EXCEPTION");
        if(pardoned) {
            sb.append(" (pardoned)");
        }
        sb.append(" in ");
        sb.append(source != null ? source.getName().getPath() : "detached source");
        sb.append('\n');

        print(sb, message, e, MessageSeverity.ERROR, pardoned);

        if(pardoned) {
            ++exceptionsPardoned;
        } else {
            ++exceptions;
        }
    }

    @Override public void print(IProject project, String message, @Nullable Throwable e, boolean pardoned) {
        final StringBuilder sb = new StringBuilder();
        sb.append("EXCEPTION");
        if(pardoned) {
            sb.append(" (pardoned)");
        }
        sb.append(" in project ");
        sb.append(project.location().getName().getPath());
        sb.append('\n');

        print(sb, message, e, MessageSeverity.ERROR, pardoned);

        if(pardoned) {
            ++exceptionsPardoned;
        } else {
            ++exceptions;
        }
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

    @Override public void printSummary() {
        final int total =
            notes + warnings + warningsPardoned + errors + errorsPardoned + exceptions + exceptionsPardoned;
        if(total == 0) {
            return;
        }

        final StringBuilder sb = new StringBuilder();

        sb.append(total);
        sb.append(" messages (");

        sb.append(exceptions);
        sb.append(" exception");
        if(exceptions == 0 || exceptions > 1) {
            sb.append('s');
        }
        if(exceptionsPardoned > 0) {
            sb.append(" [");
            sb.append(exceptionsPardoned);
            sb.append(" pardoned]");
        }
        sb.append(", ");

        sb.append(errors);
        sb.append(" error");
        if(errors == 0 || errors > 1) {
            sb.append('s');
        }
        if(errorsPardoned > 0) {
            sb.append(" [");
            sb.append(errorsPardoned);
            sb.append(" pardoned]");
        }
        sb.append(", ");

        sb.append(warnings);
        sb.append(" warning");
        if(warnings == 0 || warnings > 1) {
            sb.append('s');
        }
        if(warningsPardoned > 0) {
            sb.append(" [");
            sb.append(warningsPardoned);
            sb.append(" pardoned]");
        }
        sb.append(", ");

        sb.append(notes);
        sb.append(" note");
        if(notes == 0 || notes > 1) {
            sb.append('s');
        }
        sb.append(')');
        sb.append('\n');

        final String str = sb.toString();
        infoStream.print(str);
        infoStream.flush();
    }
}
