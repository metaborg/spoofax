package org.metaborg.spoofax.meta.core.config;

/**
 * Visitor for {@link IBuildStepConfig}s.
 */
public interface IBuildStepVisitor {
    /**
     * Visit a Stratego build step configuration.
     * 
     * @param buildStep
     *            Stratego build step configuration
     */
    void visit(StrategoBuildStepConfig buildStep);

    /**
     * Visit an Ant build step configuration.
     * 
     * @param buildStep
     *            Ant build step configuration.
     */
    void visit(AntBuildStepConfig buildStep);
}
