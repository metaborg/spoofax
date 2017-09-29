package org.metaborg.spoofax.meta.core.pluto;

import java.io.File;
import java.util.Set;

import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import build.pluto.BuildUnit;
import build.pluto.builder.BuildCycle;
import build.pluto.builder.BuildCycleException;
import build.pluto.builder.BuildRequest;
import build.pluto.builder.Builder;
import build.pluto.builder.CycleHandler;
import build.pluto.builder.RequiredBuilderFailed;
import build.pluto.dependency.FileRequirement;
import build.pluto.dependency.Requirement;
import build.pluto.output.Output;
import build.pluto.util.IReporting;

public class SpoofaxReporting implements IReporting {
    private static final ILogger log = LoggerUtils.logger("Build log");


    @Override public <O extends Output> void buildRequirement(BuildRequest<?, O, ?, ?> req) {

    }

    @Override public <O extends Output> void finishedBuildRequirement(BuildRequest<?, O, ?, ?> req) {

    }

    @Override public <O extends Output> void startedBuilder(BuildRequest<?, O, ?, ?> req, Builder<?, ?> b,
        BuildUnit<O> oldUnit, Set<BuildReason> reasons) {
        String desc =  b.description();
        if (desc != null)
            log.info("> {}", desc);
    }

    @Override public <O extends Output> void finishedBuilder(BuildRequest<?, O, ?, ?> req, BuildUnit<O> unit) {

    }

    @Override public <O extends Output> void skippedBuilder(BuildRequest<?, O, ?, ?> req, BuildUnit<O> unit) {

    }

    @Override public <O extends Output> void canceledBuilderFailure(BuildRequest<?, O, ?, ?> req, BuildUnit<O> unit) {

    }

    @Override public <O extends Output> void canceledBuilderException(BuildRequest<?, O, ?, ?> req, BuildUnit<O> unit,
        Throwable t) {
        log.error("Builder failed unexpectedly", t);
    }

    @Override public <O extends Output> void canceledBuilderCycle(BuildRequest<?, O, ?, ?> req, BuildUnit<O> unit,
        BuildCycleException t) {
        log.error("Cyclic builder failed", t);
    }

    @Override public <O extends Output> void canceledBuilderInterrupt(BuildRequest<?, O, ?, ?> req, BuildUnit<O> unit) {
        log.warn("Builder interrupted");
    }

    @Override public <O extends Output> void canceledBuilderRequiredBuilderFailed(BuildRequest<?, O, ?, ?> req,
        BuildUnit<O> unit, RequiredBuilderFailed e) {
        if(e != null && !e.getCause().getMessage().equals("Builder failed")) {
            log.error("Required builder failed", e.getCause());
        }
    }

    @Override public void startBuildCycle(BuildCycle cycle, CycleHandler cycleSupport) {

    }

    @Override public void finishedBuildCycle(BuildCycle cycle, CycleHandler cycleSupport, Set<BuildUnit<?>> units) {

    }

    @Override public void cancelledBuildCycleException(BuildCycle cycle, CycleHandler cycleSupport, Throwable t) {

    }

    @Override public void inconsistentRequirement(Requirement req) {
        if(req instanceof FileRequirement) {
            final FileRequirement fileReq = (FileRequirement) req;
            final File file = fileReq.file;
            if(file.isDirectory()) {
                log.debug("Directory structure changed: {}", file);
            } else {
                log.debug("File changed: {}", file);
            }
        } else {
            log.debug("Requirement inconsistent: {}", req);
        }
    }

    @Override public void messageFromBuilder(String message, boolean isError, Builder<?, ?> from) {
        if(isError) {
            log.error(message);
        } else {
            log.info(message);
        }
    }

    @Override public void messageFromSystem(String message, boolean isError, int verbosity) {
        if(message.contains("Incrementally rebuild inconsistent units")) {
            return;
        }

        if(verbosity <= 3) {
            log.info(message);
        } else {
            log.debug(message);
        }
    }
}
