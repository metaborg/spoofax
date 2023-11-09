package org.metaborg.core.action;

import java.util.List;
import java.util.stream.Collectors;

import org.metaborg.util.Strings;

/**
 * Named transform goal that points to a builder in a menu via a list of menu names and builder name.
 */
public class NamedGoal implements ITransformGoal {
    private static final long serialVersionUID = -8187990679589493213L;

    public final List<String> names;


    public NamedGoal(List<String> names) {
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
        return "'" + Strings.join(names," -> ") + "'";
    }
}
