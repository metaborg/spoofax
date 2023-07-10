package org.metaborg.core.build.dependency;

import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.util.iterators.Iterables2;

public class MissingDependencies {
    public final Iterable<LanguageIdentifier> compile;
    public final Iterable<LanguageIdentifier> runtime;


    public MissingDependencies(Iterable<LanguageIdentifier> compile, Iterable<LanguageIdentifier> runtime) {
        this.compile = compile;
        this.runtime = runtime;
    }

    public MissingDependencies() {
        this(Iterables2.<LanguageIdentifier>empty(), Iterables2.<LanguageIdentifier>empty());
    }


    public boolean empty() {
        return Iterables2.isEmpty(compile) && Iterables2.isEmpty(runtime);
    }


    @Override public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("The following dependencies are missing: \n");
        sb.append("  Compile-time: \n");
        printIdentifiers(compile, sb);
        sb.append("  Runtime: \n");
        printIdentifiers(runtime, sb);
        return sb.toString();
    }

    private void printIdentifiers(Iterable<LanguageIdentifier> identifiers, StringBuilder sb) {
        for(LanguageIdentifier identifier : identifiers) {
            sb.append("    ");
            sb.append(identifier);
            sb.append('\n');
        }
    }
}
