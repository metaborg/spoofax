package org.metaborg.core.outline;

public class Outline implements IOutline {
    public final IOutlineNode root;
    public final int expandTo;


    public Outline(IOutlineNode root, int expandTo) {
        this.root = root;
        this.expandTo = expandTo;
    }


    @Override public IOutlineNode root() {
        return root;
    }

    @Override public int expandTo() {
        return expandTo;
    }
}
