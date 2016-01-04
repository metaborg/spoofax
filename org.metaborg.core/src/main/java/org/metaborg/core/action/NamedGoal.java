package org.metaborg.core.action;

import java.util.Collection;

import com.google.common.base.Joiner;

public class NamedGoal implements ITransformGoal {
    private static final long serialVersionUID = -8187990679589493213L;
    
    public final Collection<String> names;


    public NamedGoal(Collection<String> names) {
        this.names = names;
    }


    @Override public int hashCode() {
        return names.hashCode();
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final NamedGoal other = (NamedGoal) obj;
        if(!names.equals(other.names))
            return false;
        return true;
    }

    @Override public String toString() {
        return "'" + Joiner.on(" -> ").join(names) + "'";
    }
}
