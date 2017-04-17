package org.metaborg.core.build;

import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.metaborg.core.action.ITransformGoal;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.messages.IMessagePrinter;
import org.metaborg.core.project.IProject;
import org.metaborg.core.resource.ResourceChange;

import com.google.common.collect.Multimap;

/**
 * Input for a build. Use the {@link BuildInputBuilder} fluent interface to create objects of this class.
 * 
 * @see BuildInputBuilder
 */
public class BuildInput {
    /**
     * Build state with information about previous builds.
     */
    public final BuildState state;

    /**
     * Project to build.
     */
    public final IProject project;

    /**
     * Sources that have changed.
     */
    public final Iterable<ResourceChange> sourceChanges;

    /**
     * Per-language include paths;
     */
    public final Multimap<ILanguageImpl, FileObject> includePaths;

    /**
     * Language build order.
     */
    public final BuildOrder buildOrder;


    /**
     * File selector to determine which resources should be processed, or null to process everything.
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
    public final Iterable<ITransformGoal> transformGoals;


    /**
     * Message printer to use during build, or null to skip printing messages.
     */
    public final @Nullable IMessagePrinter messagePrinter;

    /**
     * If an exception should be thrown when there are parsing, analysis, or transformation errors.
     */
    public final boolean throwOnErrors;

    /**
     * Languages for which errors are pardoned; prevents throwing an exception when {@code throwOnErrors} is true.
     */
    public final Set<ILanguageImpl> pardonedLanguages;


    public BuildInput(BuildState state, IProject project, Iterable<ResourceChange> resourceChanges,
        Multimap<ILanguageImpl, FileObject> includePaths, BuildOrder buildOrder, @Nullable FileSelector parseSelector,
        boolean analyze, @Nullable FileSelector analyzeSelector, boolean transform,
        @Nullable FileSelector transformSelector, Iterable<ITransformGoal> transformGoals,
        @Nullable IMessagePrinter messagePrinter, boolean throwOnErrors, Set<ILanguageImpl> pardonedLanguages) {
        this.state = state;
        this.project = project;
        this.sourceChanges = resourceChanges;
        this.includePaths = includePaths;
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
