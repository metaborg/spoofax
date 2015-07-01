package org.metaborg.spoofax.core.transform;

public class NamedGoal implements ITransformerGoal {
    public final String name;


    public NamedGoal(String name) {
        this.name = name;
    }


    @Override public String toString() {
        return "'" + name + "'";
    }
}
