package org.metaborg.core.action;

/**
 * Named transform goal that points to a builder in a menu, without qualified menu names.
 */
public class EndNamedGoal implements ITransformGoal {
    private static final long serialVersionUID = 1961529029651639105L;

    public final String name;


    public EndNamedGoal(String name) {
        this.name = name;
    }


    @Override public int hashCode() {
        return name.hashCode();
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final EndNamedGoal other = (EndNamedGoal) obj;
        if(!name.equals(other.name))
            return false;
        return true;
    }

    @Override public String toString() {
        return "'" + name + "'";
    }
}
