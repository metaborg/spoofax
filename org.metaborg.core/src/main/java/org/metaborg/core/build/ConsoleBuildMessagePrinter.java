package org.metaborg.core.build;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.project.IProject;
import org.metaborg.core.source.AffectedSourceHelper;
import org.metaborg.core.source.ISourceTextService;

public class ConsoleBuildMessagePrinter implements IBuildMessagePrinter {
    private final ISourceTextService sourceTextService;
    private final PrintStream stream;
    private final boolean printHighlight;
    private final boolean printExceptions;


    public ConsoleBuildMessagePrinter(ISourceTextService sourceTextService, OutputStream stream,
        boolean printHighlight, boolean printExceptions) {
        this.sourceTextService = sourceTextService;
        this.stream = new PrintStream(stream);
        this.printHighlight = printHighlight;
        this.printExceptions = printExceptions;
    }


    @Override public void print(IMessage message) {
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

        print(sb, message.message(), message.exception());
    }

    @Override public void print(FileObject source, String message, @Nullable Throwable e) {
        final StringBuilder sb = new StringBuilder();
        sb.append("EXCEPTION in ");
        sb.append(source.getName().getPath());
        sb.append('\n');

        print(sb, message, e);
    }

    @Override public void print(IProject project, String message, @Nullable Throwable e) {
        final StringBuilder sb = new StringBuilder();
        sb.append("EXCEPTION in project ");
        sb.append(project.location().getName().getPath());
        sb.append('\n');

        print(sb, message, e);
    }


    private void print(StringBuilder sb, String message, @Nullable Throwable e) {
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
        print(sb);
    }

    private void print(StringBuilder sb) {
        final String str = sb.toString();
        stream.print(str);
        stream.flush();
    }
}
