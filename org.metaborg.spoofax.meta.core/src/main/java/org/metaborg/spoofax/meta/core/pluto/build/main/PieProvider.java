package org.metaborg.spoofax.meta.core.pluto.build.main;

import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.inject.Inject;

import mb.pie.api.Logger;
import mb.pie.api.Pie;
import mb.pie.api.PieBuilder;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.taskdefs.guice.GuiceTaskDefs;

public class PieProvider implements IPieProvider {
    private static final ILogger logger = LoggerUtils.logger(GenerateSourcesBuilder.class);
    protected final Pie pie;

    protected boolean logInfoAndBelow = false;
    protected final Logger pieLogger = new Logger() {
        @Override public void error(String s, Throwable throwable) {
            logger.error(s, throwable);
        }

        @Override public void warn(String s, Throwable throwable) {
            logger.warn(s, throwable);
        }

        @Override public void info(String s) {
            if(logInfoAndBelow) {
                logger.info(s);
            } else {
                logger.debug("(INFO) " + s);
            }
        }

        @Override public void debug(String s) {
            logger.debug(s);
        }

        @Override public void trace(String s) {
            logger.trace(s);
        }
    };

    @Inject public PieProvider(GuiceTaskDefs guiceTaskDefs) {
        final PieBuilder pieBuilder = new PieBuilderImpl();
        pieBuilder.withLogger(pieLogger);
        pieBuilder.withTaskDefs(guiceTaskDefs);
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
}
