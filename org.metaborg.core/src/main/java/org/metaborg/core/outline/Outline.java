package org.metaborg.core.outline;

public class Outline implements IOutline {
    public final Iterable<IOutlineNode> roots;
    public final int expandTo;


    public Outline(Iterable<IOutlineNode> roots, int expandTo) {
        this.roots = roots;
        this.expandTo = expandTo;
    }


    @Override public Iterable<IOutlineNode> roots() {
        return roots;
    }

    @Override public int expandTo() {
        return expandTo;
    }
}
