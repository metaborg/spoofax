package org.metaborg.spoofax.meta.core.pluto.build.main;

import com.google.inject.Inject;
import mb.log.slf4j.SLF4JLoggerFactory;
import mb.pie.api.Pie;
import mb.pie.api.PieBuilder;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.taskdefs.guice.GuiceTaskDefs;
import mb.stratego.build.strincr.StrIncr;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

public class PieProvider implements IPieProvider {
    private static final ILogger logger = LoggerUtils.logger(StrIncr.class);
    protected final Pie pie;

    protected boolean logInfoAndBelow = false;

    @Inject public PieProvider(GuiceTaskDefs guiceTaskDefs) {
        final PieBuilder pieBuilder = new PieBuilderImpl();
        pieBuilder.withLoggerFactory(new SLF4JLoggerFactory());
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
