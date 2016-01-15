package org.metaborg.core.action;

public class CompileGoal implements ITransformGoal {
    private static final long serialVersionUID = 6307112951667075620L;


    @Override public int hashCode() {
        return 0;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        return true;
    }

    @Override public String toString() {
        return "Compile";
    }
}
