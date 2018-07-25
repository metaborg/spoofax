package org.metaborg.spoofax.core.action;

import org.metaborg.core.action.ITransformAction;
import org.metaborg.core.action.ITransformGoal;
import org.metaborg.core.action.TransformActionFlags;

public class JavaTransformAction implements ITransformAction {
    public final String name;
    public final ITransformGoal goal;
    public final TransformActionFlags flags;
    public final String className;


    public JavaTransformAction(String name, ITransformGoal goal, TransformActionFlags flags, String className) {
        this.name = name;
        this.goal = goal;
        this.flags = flags;
        this.className = className;
    }


    @Override public String name() {
        return name;
    }
    
    @Override public ITransformGoal goal() {
        return goal;
    }

    @Override public TransformActionFlags flags() {
        return flags;
    }

    @Override public String toString() {
        return name;
    }
}
