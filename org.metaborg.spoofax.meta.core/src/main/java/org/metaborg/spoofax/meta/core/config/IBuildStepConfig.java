package org.metaborg.spoofax.meta.core.config;

import java.io.Serializable;

/**
 * Configuration for additional build steps. Use the visitor pattern access implementations.
 */
public interface IBuildStepConfig extends Serializable {
    /**
     * @return The phase in which the build step is configured to run.
     */
    LanguageSpecBuildPhase phase();

    /**
     * Accepts a build step visitor.
     * 
     * @param visitor
     *            Visitor to accept.
     */
    void accept(IBuildStepVisitor visitor);

    /**
     * Accepts a build step visitor, only visiting configurations for given phase.
     * 
     * @param visitor
     *            Visitor to accept.
     * @param phase
     *            Phase of build step configurations to visit.
     */
    void accept(IBuildStepVisitor visitor, LanguageSpecBuildPhase phase);
}
