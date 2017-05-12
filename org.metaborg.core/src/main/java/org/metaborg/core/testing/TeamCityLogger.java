package org.metaborg.core.testing;

import com.google.inject.Inject;
import org.metaborg.util.log.AbstractLogger;
import org.slf4j.helpers.MessageFormatter;

import javax.annotation.Nullable;

/**
 * Logger producing TeamCity messages.
 */
public class TeamCityLogger extends AbstractLogger {

    private final TeamCityWriter writer;

    @Inject
    public TeamCityLogger(TeamCityWriter writer) {
        this.writer = writer;
    }

    @Override
    public void trace(String msg, @Nullable Throwable cause) {
        // Not supported.
    }

    @Override public boolean traceEnabled() { return false; }
    @Override public boolean debugEnabled() { return false; }
    @Override public boolean infoEnabled() { return true; }
    @Override public boolean warnEnabled() { return true; }
    @Override public boolean errorEnabled() { return true; }

    @Override
    public void debug(String msg, @Nullable Throwable cause) {
        // Not supported.
    }

    @Override
    public void info(String msg, @Nullable Throwable cause) {
        this.writer.send("message",
            new TeamCityWriter.Attribute("text", msg),
            new TeamCityWriter.Attribute("errorDetails", cause),
            new TeamCityWriter.Attribute("status", "NORMAL")
        );
    }

    @Override
    public void warn(String msg, @Nullable Throwable cause) {
        this.writer.send("message",
                new TeamCityWriter.Attribute("text", msg),
                new TeamCityWriter.Attribute("errorDetails", cause),
                new TeamCityWriter.Attribute("status", "WARNING")
        );
    }

    @Override
    public void error(String msg, @Nullable Throwable cause) {
        this.writer.send("message",
                new TeamCityWriter.Attribute("text", msg),
                new TeamCityWriter.Attribute("errorDetails", cause),
                new TeamCityWriter.Attribute("status", "ERROR")
        );
    }

    @Override
    public String format(String msg, Object... args) {
        return MessageFormatter.arrayFormat(msg, args).getMessage();
    }
}
