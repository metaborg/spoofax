package org.metaborg.core.action;

public interface ITransformAction extends IAction {
    public abstract ITransformGoal goal();
    
    public abstract TransformActionFlags flags();
}
