package org.metaborg.spoofax.eclipse.logging;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.metaborg.spoofax.eclipse.SpoofaxPlugin;
import org.metaborg.spoofax.eclipse.util.StatusUtils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

public class EclipseLogAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    private PatternLayoutEncoder encoder;

    private ILog log;


    @Override public void start() {
        log = SpoofaxPlugin.plugin().getLog();

        super.start();
    }

    @Override protected void append(ILoggingEvent event) {
        final String message = format(event);
        final IThrowableProxy proxy = event.getThrowableProxy();

        final IStatus status;
        switch(event.getLevel().levelInt) {
            case Level.INFO_INT:
                status = StatusUtils.info(message);
                break;
            case Level.WARN_INT:
                if(proxy == null || !(proxy instanceof ThrowableProxy)) {
                    status = StatusUtils.warn(message);
                } else {
                    final ThrowableProxy proxyImpl = (ThrowableProxy) proxy;
                    status = StatusUtils.warn(message, proxyImpl.getThrowable());
                }
                break;
            case Level.ERROR_INT:
                if(proxy == null || !(proxy instanceof ThrowableProxy)) {
                    status = StatusUtils.error(message);
                } else {
                    final ThrowableProxy proxyImpl = (ThrowableProxy) proxy;
                    status = StatusUtils.error(message, proxyImpl.getThrowable());
                }
                break;
            case Level.TRACE_INT:
            case Level.DEBUG_INT:
            default:
                status = null;
                break;
        }

        if(status != null) {
            log.log(status);
        }
    }

    public PatternLayoutEncoder getEncoder() {
        return encoder;
    }

    public void setEncoder(PatternLayoutEncoder encoder) {
        this.encoder = encoder;
    }

    private String format(ILoggingEvent event) {
        if(encoder != null) {
            return encoder.getLayout().doLayout(event);
        } else {
            return event.getFormattedMessage();
        }
    }
}
