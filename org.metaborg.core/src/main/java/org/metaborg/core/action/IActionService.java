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
    public abstract Iterable<ITransformAction> action(ILanguageImpl language, ITransformGoal goal);

    /**
     * Gets transform action contributions for given transform goal.
     * 
     * @param language
     *            Language implementation to get transform actions from.
     * @param goal
     *            Goal to get transform actions for.
     * @return Transform action contributions.
     */
    public abstract @Nullable Iterable<TransformActionContribution> actionContribution(ILanguageImpl language,
        ITransformGoal goal);

    /**
     * Checks if transform actions are available for given goal.
     * 
     * @param language
     *            Language implementation to check transform actions from.
     * @param goal
     *            Goal to check transform actions for.
     * @return True if transform actions are available, false if not.
     */
    public abstract boolean available(ILanguageImpl language, ITransformGoal goal);
}
