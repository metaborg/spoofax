package org.metaborg.spoofax.meta.core.pluto.build.main;

import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.inject.Inject;

import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.Pie;
import mb.pie.api.PieBuilder;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.runtime.tracer.LoggingTracer;
import mb.pie.taskdefs.guice.GuiceTaskDefs;

public class PieProvider implements IPieProvider {
    private static final ILogger logger = LoggerUtils.logger(GenerateSourcesBuilder.class);
    protected final Pie pie;

    protected boolean logInfoAndBelow = false;

    @Inject public PieProvider(GuiceTaskDefs guiceTaskDefs) {
        final PieBuilder pieBuilder = new PieBuilderImpl();
        pieBuilder.withLoggerFactory(new LoggerFactory() {
            @Override public Logger create(String name) {
                return new SpoofaxLogger();
            }

            @Override public Logger create(Class<?> clazz) {
                return new SpoofaxLogger();
            }
        });
        pieBuilder.withTaskDefs(guiceTaskDefs);
        pieBuilder.withTracerFactory(LoggingTracer::new);
        this.pie = pieBuilder.build();
    }

    @Override public Pie pie() {
        return pie;
    }

    @Override public void setLogLevelWarn() {
        logInfoAndBelow = false;

    }

    @Override public void setLogLevelTrace() {
        logInfoAndBelow = true;
    }

    class SpoofaxLogger implements Logger {
        @Override public boolean isTraceEnabled() {
            return true;
        }

        @Override public void trace(String format, Object... args) {
            logger.trace(format, args);
        }

        @Override public void trace(String format, Throwable cause, Object... args) {
            logger.trace(format, cause, args);
        }

        @Override public boolean isDebugEnabled() {
            return true;
        }

        @Override public void debug(String format, Object... args) {
            logger.debug(format, args);
        }

        @Override public void debug(String format, Throwable cause, Object... args) {
            logger.debug(format, cause, args);
        }

        @Override public boolean isInfoEnabled() {
            return logInfoAndBelow;
        }

        @Override public void info(String format, Object... args) {
            if(!isInfoEnabled()) {
                debug(format, args);
                return;
            }
            logger.info(format, args);
        }

        @Override public void info(String format, Throwable cause, Object... args) {
            if(!isInfoEnabled()) {
                debug(format, cause, args);
                return;
            }
            logger.info(format, cause, args);
        }

        @Override public boolean isWarnEnabled() {
            return true;
        }

        @Override public void warn(String format, Object... args) {
            logger.warn(format, args);
        }

        @Override public void warn(String format, Throwable cause, Object... args) {
            logger.warn(format, cause, args);
        }

        @Override public boolean isErrorEnabled() {
            return true;
        }

        @Override public void error(String format, Object... args) {
            logger.error(format, args);
        }

        @Override public void error(String format, Throwable cause, Object... args) {
            logger.error(format, cause, args);
        }
    }
}
