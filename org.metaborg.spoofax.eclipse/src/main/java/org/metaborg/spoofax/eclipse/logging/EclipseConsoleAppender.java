package org.metaborg.spoofax.eclipse.logging;

import java.io.IOException;

import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.metaborg.spoofax.eclipse.util.ConsoleUtils;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

public class EclipseConsoleAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    private PatternLayoutEncoder encoder;
    private String consoleName;

    private MessageConsole console;
    private MessageConsoleStream stream;


    @Override public void start() {
        if(encoder == null) {
            addError("No encoder set for appender named" + name);
            return;
        }

        console = ConsoleUtils.get(consoleName != null ? consoleName : name);
        stream = console.newMessageStream();
        try {
            encoder.init(stream);
        } catch(IOException e) {
            addError("Could not initialize encoder", e);
            return;
        }

        super.start();
    }

    @Override protected void append(ILoggingEvent event) {
        try {
            encoder.doEncode(event);
        } catch(IOException e) {
            addError("Could not encode log message", e);
        }
    }

    public PatternLayoutEncoder getEncoder() {
        return encoder;
    }

    public void setEncoder(PatternLayoutEncoder encoder) {
        this.encoder = encoder;
    }

    public String getConsoleName() {
        return consoleName;
    }

    public void setConsoleName(String consoleName) {
        this.consoleName = consoleName;
    }
}
