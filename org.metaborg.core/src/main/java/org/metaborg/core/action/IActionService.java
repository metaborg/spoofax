package org.metaborg.core.action;

import javax.annotation.Nullable;

import org.metaborg.core.language.ILanguageImpl;

public interface IActionService {
    /**
     * Gets transform actions for given goal.
     * 
     * @param language
     *            Language implementation to get transform actions from.
     * @param goal
     *            Goal to get transform actions for.
     * @return Transform actions.
     */
    Iterable<ITransformAction> actions(ILanguageImpl language, ITransformGoal goal);

    /**
     * Gets transform action contributions for given transform goal.
     * 
     * @param language
     *            Language implementation to get transform actions from.
     * @param goal
     *            Goal to get transform actions for.
     * @return Transform action contributions.
     */
    @Nullable Iterable<TransformActionContribution> actionContributions(ILanguageImpl language,
                                                                        ITransformGoal goal);

    /**
     * Checks if transform actions are available for given transform goal.
     * 
     * @param language
     *            Language implementation to check transform actions from.
     * @param goal
     *            Goal to check transform actions for.
     * @return True if transform actions are available, false if not.
     */
    boolean available(ILanguageImpl language, ITransformGoal goal);

    /**
     * Checks if analysis is required for given transform goal.
     * 
     * @param language
     *            Language implementation to check from.
     * @param goal
     *            Goal to check for.
     * @return True if analysis is required, false if not.
     */
    boolean requiresAnalysis(ILanguageImpl language, ITransformGoal goal);
}
