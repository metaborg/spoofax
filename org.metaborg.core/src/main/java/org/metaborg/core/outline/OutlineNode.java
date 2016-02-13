package org.metaborg.core.outline;

import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.source.ISourceRegion;

import com.google.common.collect.Lists;

public class OutlineNode implements IOutlineNode {
    private final String label;
    private final @Nullable FileObject icon;
    private final @Nullable ISourceRegion origin;
    private final @Nullable IOutlineNode parent;
    private final Collection<IOutlineNode> nodes = Lists.newLinkedList();


    public OutlineNode(String label, @Nullable FileObject icon, @Nullable ISourceRegion origin,
        @Nullable IOutlineNode parent) {
        this.label = label;
        this.icon = icon;
        this.origin = origin;
        this.parent = parent;
    }


    @Override public String label() {
        return label;
    }

    @Override public @Nullable FileObject icon() {
        return icon;
    }

    @Override public @Nullable ISourceRegion origin() {
        return origin;
    }

    @Override public IOutlineNode parent() {
        return parent;
    }

    @Override public Iterable<IOutlineNode> nodes() {
        return nodes;
    }


    public void addChild(IOutlineNode child) {
        nodes.add(child);
    }


    @Override public String toString() {
        return label;
    }
}
