package org.metaborg.spoofax.meta.core;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.slf4j.Logger;

public class AntSLF4JLogger implements BuildListener {

    private final Logger log;

    public AntSLF4JLogger(Logger log) {
        this.log = log;
    }

    @Override
    public void buildStarted(BuildEvent event) {
        log.info("Started build {}", event.getProject().getName());
    }

    @Override
    public void buildFinished(BuildEvent event) {
        log.info("Finished build {}", event.getProject().getName());
    }

    @Override
    public void targetStarted(BuildEvent event) {
        log.info("Started target {}", event.getTarget().getName());
    }

    @Override
    public void targetFinished(BuildEvent event) {
        log.info("Finished target {}", event.getTarget().getName());
    }

    @Override
    public void taskStarted(BuildEvent event) {
        log.debug("Started task {}", event.getTask().getTaskName());
    }

    @Override
    public void taskFinished(BuildEvent event) {
        log.debug("Finished task {}", event.getTask().getTaskName());
    }

    @Override
    public void messageLogged(BuildEvent event) {
        switch ( event.getPriority() ) {
            case Project.MSG_ERR:
                log.error(event.getMessage());
                break;
            case Project.MSG_WARN:
                log.warn(event.getMessage());
                break;
            case Project.MSG_INFO:
                log.info(event.getMessage());
                break;
            default:
                log.debug(event.getMessage());
                break;
        }
    }
    
}
