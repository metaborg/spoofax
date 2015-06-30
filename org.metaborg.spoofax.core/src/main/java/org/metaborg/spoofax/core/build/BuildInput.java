package org.metaborg.spoofax.core.build;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.project.IProject;
import org.metaborg.spoofax.core.resource.IResourceChange;
import org.metaborg.spoofax.core.transform.ITransformerGoal;

import com.google.common.collect.Multimap;

public class BuildInput {
    /**
     * Project to build.
     */
    public final IProject project;

    /**
     * Resources that have changed.
     */
    public final Iterable<IResourceChange> resourceChanges;

    /**
     * Per-language locations for include files.
     */
    public final Multimap<ILanguage, FileObject> includeLocations;

    /**
     * Language build order.
     */
    public final BuildOrder buildOrder;


    /**
     * File selector to determine which files should be parsed, or null to parse everything.
     */
    public final @Nullable FileSelector parseSelector;


    /**
     * If analysis is enabled.
     */
    public final boolean analyze;

    /**
     * File selector to determine which files should be analyzed, or null to analyze everything.
     */
    public final @Nullable FileSelector analyzeSelector;


    /**
     * If transformation is enabled.
     */
    public final boolean transform;

    /**
     * File selector to determine which files should be transformed, or null to transform everything.
     */
    public final @Nullable FileSelector transformSelector;

    /**
     * Transformer goals to execute on analyzed or parsed results.
     */
    public final Iterable<ITransformerGoal> transformGoals;


    /**
     * If an exception should be thrown when there are parsing, analysis, or transformation errors.
     */
    public final boolean throwOnErrors;

    /**
     * Languages for which errors are pardoned; prevents throwing an exception when {@code throwOnErrors} is true.
     */
    public final Iterable<ILanguage> pardonedLanguages;


    public BuildInput(IProject project, Iterable<IResourceChange> resourceChanges,
        Multimap<ILanguage, FileObject> includeLocations, BuildOrder buildOrder, FileSelector parseSelector,
        boolean analyze, FileSelector analyzeSelector, boolean transform, FileSelector transformSelector,
        Iterable<ITransformerGoal> transformGoals, boolean throwOnErrors, Iterable<ILanguage> pardonedLanguages) {
        this.project = project;
        this.resourceChanges = resourceChanges;
        this.includeLocations = includeLocations;
        this.buildOrder = buildOrder;
        this.parseSelector = parseSelector;
        this.analyze = analyze;
        this.analyzeSelector = analyzeSelector;
        this.transform = transform;
        this.transformSelector = transformSelector;
        this.transformGoals = transformGoals;
        this.throwOnErrors = throwOnErrors;
        this.pardonedLanguages = pardonedLanguages;
    }
}
