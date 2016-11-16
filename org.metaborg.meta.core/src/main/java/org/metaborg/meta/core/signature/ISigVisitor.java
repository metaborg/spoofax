package org.metaborg.meta.core.signature;

public interface ISigVisitor {
    void visitInjection(InjectionSig sig);

    void visitApplication(ConstructorSig sig);
}
