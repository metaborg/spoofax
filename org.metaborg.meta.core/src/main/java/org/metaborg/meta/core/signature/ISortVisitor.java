package org.metaborg.meta.core.signature;

public interface ISortVisitor {
    void visit(AnySort arg);

    void visit(PrimitiveSort arg);

    void visit(Sort arg);

    void visit(OptionalSort arg);

    void visit(ListSort arg);
}
