package org.metaborg.core.action;

public interface ITransformAction extends IAction {
    ITransformGoal goal();

    TransformActionFlags flags();
}
