package org.metaborg.spoofax.core.esv;

import static org.spoofax.interpreter.terms.IStrategoTerm.*;
//import static org.spoofax.terms.Term.*;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.collect.Lists;
import org.spoofax.terms.util.TermUtils;

/**
 * Term reading utility class for ESV abstract syntax.
 */
public class ESVReader {
    public static IStrategoAppl findTerm(IStrategoTerm term, String constructor) {
        if(TermUtils.isAppl(term) && cons(term).equals(constructor))
            return (IStrategoAppl) term;

        IStrategoTerm[] subterms = term.getAllSubterms();
        for(int i = subterms.length - 1; i >= 0; i--) {
            IStrategoAppl result = findTerm(subterms[i], constructor);
            if(result != null)
                return result;
        }

        return null;
    }

    public static ArrayList<IStrategoAppl> collectTerms(IStrategoAppl term, String... constructors) {
        ArrayList<IStrategoAppl> results = new ArrayList<>();
        for(String constructor : constructors) {
            collectTerms(term, constructor, results);
        }
        return results;
    }

    private static void collectTerms(IStrategoTerm term, String constructor, ArrayList<IStrategoAppl> results) {
        if(TermUtils.isAppl(term) && cons(term).equals(constructor))
            results.add((IStrategoAppl) term);

        // TODO: optimize: use TermVisitor, avoid indexed access to long lists
        for(int i = 0; i < term.getSubtermCount(); i++) {
            collectTerms(term.getSubterm(i), constructor, results);
        }
    }

    @Nullable
    public static String termContents(IStrategoTerm t) {
        if(t == null)
            return null;

        String result;

        if(TermUtils.isString(t)) {
            result = TermUtils.toJavaString(t);
        } else if(TermUtils.isAppl(t, "Values", 1)) {
            return concatTermStrings(TermUtils.toListAt(t, 0));
        } else if(TermUtils.isAppl(t, null, 1) && TermUtils.isStringAt(t, 0)) {
            result = TermUtils.toJavaStringAt(t, 0);
        } else if(TermUtils.isAppl(t, null, 1)) {
            return termContents(t.getSubterm(0));
        } else {
            return null;
        }

        if(result.startsWith("\"") && result.endsWith("\"") && result.length() > 1)
            result = result.substring(1, result.length() - 1).replace("\\\\", "\"");

        return result;
    }

    @Nullable
    public static List<String> termListContents(IStrategoTerm t) {
        if(t == null)
            return null;

        List<String> results = Lists.newArrayList();

        if(TermUtils.isAppl(t, "Values", 1)) {
            IStrategoList values = TermUtils.toListAt(t, 0);
            for(int i = 0; i < values.getSubtermCount(); i++) {
                results.add(termContents(values.getSubterm(i)));
            }
        } else {
            return null;
        }

        return results;
    }

    public static String getProperty(IStrategoAppl document, String name) {
        return getProperty(document, name, null);
    }

    public static String getProperty(IStrategoAppl document, String name, String defaultValue) {
        IStrategoAppl result = findTerm(document, name);
        if(result == null)
            return defaultValue;

        return termContents(result);
    }

    public static String concatTermStrings(IStrategoList values) {
        StringBuilder results = new StringBuilder();

        if(values.getSubtermCount() > 0)
            results.append(termContents(values.getSubterm(0)));

        for(int i = 1; i < values.getSubtermCount(); i++) {
            results.append(',');
            results.append(termContents(values.getSubterm(i)));
        }
        return results.toString();
    }

    public static String cons(IStrategoTerm t) {
        if(!TermUtils.isAppl(t))
            return null;
        return ((IStrategoAppl) t).getConstructor().getName();
    }
}
