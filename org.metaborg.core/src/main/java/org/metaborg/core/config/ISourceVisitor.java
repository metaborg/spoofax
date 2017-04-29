package org.metaborg.core.config;

import java.util.function.Consumer;

public interface ISourceVisitor {

    void visit(LangSource langSource);

    void visit(AllLangSource allLangSource);

    static ISourceVisitor of(Consumer<LangSource> onLangSource, Consumer<AllLangSource> onAllLangSource) {
        return new ISourceVisitor() {

            @Override public void visit(LangSource langSource) {
                onLangSource.accept(langSource);
            }

            @Override public void visit(AllLangSource allLangSource) {
                onAllLangSource.accept(allLangSource);
            }

        };
    }

}