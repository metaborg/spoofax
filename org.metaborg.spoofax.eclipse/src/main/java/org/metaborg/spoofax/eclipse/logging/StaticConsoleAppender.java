package org.metaborg.spoofax.eclipse.logging;

import java.io.IOException;
import java.io.OutputStream;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

public class StaticConsoleAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    private PatternLayoutEncoder encoder;
    private String targetName;
    private OutputStream targetStream;


    @Override public void start() {
        if(encoder == null) {
            addError("No encoder set for appender named" + name);
            return;
        }

        try {
            encoder.init(targetStream);
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

    public void setTarget(String value) {
        switch(value) {
            case "System.out":
                targetStream = System.out;
                targetName = value;
                break;
            case "System.err":
                targetStream = System.err;
                targetName = value;
                break;
            default:
                targetStream = System.out;
                targetName = "System.out";
                break;
        }
    }

    public String getTarget() {
        return targetName;
    }
}
