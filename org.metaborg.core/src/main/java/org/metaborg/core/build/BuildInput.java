package org.metaborg.core.build;

import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.project.IProject;
import org.metaborg.core.resource.IResourceChange;
import org.metaborg.core.transform.ITransformerGoal;

import com.google.common.collect.Multimap;

/**
 * Input for a build. Use {@link BuildInputBuilder} fluent interface to create objects of this class.
 * 
 * @see BuildInputBuilder
 */
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
     * File selector to determine which resources should be processed, or null to processed everything.
     */
    public final @Nullable FileSelector selector;


    /**
     * If analysis is enabled.
     */
    public final boolean analyze;

    /**
     * File selector to determine which resources should be analyzed, or null to analyze everything.
     */
    public final @Nullable FileSelector analyzeSelector;


    /**
     * If transformation is enabled.
     */
    public final boolean transform;

    /**
     * File selector to determine which resources should be transformed, or null to transform everything.
     */
    public final @Nullable FileSelector transformSelector;

    /**
     * Transformer goals to execute on analyzed or parsed results.
     */
    public final Iterable<ITransformerGoal> transformGoals;


    /**
     * Message printer to use during build, or null to skip printing messages.
     */
    public final @Nullable IBuildMessagePrinter messagePrinter;

    /**
     * If an exception should be thrown when there are parsing, analysis, or transformation errors.
     */
    public final boolean throwOnErrors;

    /**
     * Languages for which errors are pardoned; prevents throwing an exception when {@code throwOnErrors} is true.
     */
    public final Set<ILanguage> pardonedLanguages;


    public BuildInput(IProject project, Iterable<IResourceChange> resourceChanges,
        Multimap<ILanguage, FileObject> includeLocations, BuildOrder buildOrder, @Nullable FileSelector parseSelector,
        boolean analyze, FileSelector analyzeSelector, boolean transform, @Nullable FileSelector transformSelector,
        Iterable<ITransformerGoal> transformGoals, @Nullable IBuildMessagePrinter messagePrinter,
        boolean throwOnErrors, Set<ILanguage> pardonedLanguages) {
        this.project = project;
        this.resourceChanges = resourceChanges;
        this.includeLocations = includeLocations;
        this.buildOrder = buildOrder;
        this.selector = parseSelector;
        this.analyze = analyze;
        this.analyzeSelector = analyzeSelector;
        this.transform = transform;
        this.transformSelector = transformSelector;
        this.transformGoals = transformGoals;
        this.messagePrinter = messagePrinter;
        this.throwOnErrors = throwOnErrors;
        this.pardonedLanguages = pardonedLanguages;
    }
}
