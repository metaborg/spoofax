package org.metaborg.spoofax.core.shell;

import javax.annotation.Nullable;

import org.metaborg.spoofax.core.esv.ESVReader;
import org.spoofax.interpreter.terms.IStrategoAppl;

public class ShellFacetFromESV {

    public static @Nullable ShellFacet create(IStrategoAppl esv) {
        IStrategoAppl shellTerm = ESVReader.findTerm(esv, "Shell");
        if (shellTerm == null) {
            return null;
        }
        String commandPrefix = commandPrefix(shellTerm);
        String evaluationMethod = evaluationMethod(shellTerm);
        return new ShellFacet(commandPrefix, evaluationMethod);
    }

    private static String commandPrefix(IStrategoAppl term) {
        // Default is ":"
        return ESVReader.getProperty(term, "CommandPrefix", ":");
    }

    private static String evaluationMethod(IStrategoAppl term) {
        // Default is "dynsem"
        return ESVReader.getProperty(term, "EvaluationMethod", "dynsem");
    }
}