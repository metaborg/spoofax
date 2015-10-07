package org.metaborg.core.transform;

import java.util.List;

import com.google.common.base.Joiner;

public class NestedNamedGoal implements ITransformerGoal {
    public final List<String> names;


    public NestedNamedGoal(List<String> names) {
        this.names = names;
    }


    @Override public String toString() {
        return "'" + Joiner.on(" -> ").join(names) + "'";
    }
}
