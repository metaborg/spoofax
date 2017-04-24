package org.metaborg.core.config;

public interface ISourceVisitor {

    void visit(LangSource langSource);

    void visit(GenericSource genericSource);

}