package org.metaborg.spoofax.core.esv;

import static org.spoofax.interpreter.core.Tools.termAt;
import static org.spoofax.interpreter.terms.IStrategoTerm.*;
import static org.spoofax.terms.Term.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.collect.Sets;

/**
 * Term reading utility class for ESV abstract syntax.
 */
public class ESVReader {
    public static IStrategoAppl findTerm(IStrategoTerm term, String constructor) {
        if(term.getTermType() == IStrategoTerm.APPL && cons(term).equals(constructor))
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
        ArrayList<IStrategoAppl> results = new ArrayList<IStrategoAppl>();
        for(String constructor : constructors) {
            collectTerms(term, constructor, results);
        }
        return results;
    }

    private static void collectTerms(IStrategoTerm term, String constructor, ArrayList<IStrategoAppl> results) {
        if(term.getTermType() == IStrategoTerm.APPL && cons(term).equals(constructor))
            results.add((IStrategoAppl) term);

        // TODO: optimize: use TermVisitor, avoid indexed access to long lists
        for(int i = 0; i < term.getSubtermCount(); i++) {
            collectTerms(termAt(term, i), constructor, results);
        }
    }

    public static String termContents(IStrategoTerm t) {
        if(t == null)
            return null;

        String result;

        if(t.getTermType() == STRING) {
            result = asJavaString(t);
        } else if(t.getSubtermCount() == 1 && "Values".equals(tryGetName(t))) {
            return concatTermStrings(Tools.listAt(t, 0));
        } else if(t.getTermType() == APPL && t.getSubtermCount() == 1 && termAt(t, 0).getTermType() == STRING) {
            result = asJavaString(termAt(t, 0));
        } else if(t.getTermType() == APPL && t.getSubtermCount() == 1) {
            return termContents(termAt(t, 0));
        } else {
            return null;
        }

        if(result.startsWith("\"") && result.endsWith("\"") && result.length() > 1)
            result = result.substring(1, result.length() - 1).replace("\\\\", "\"");

        return result;
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
            results.append(termContents(termAt(values, 0)));

        for(int i = 1; i < values.getSubtermCount(); i++) {
            results.append(',');
            results.append(termContents(termAt(values, i)));
        }
        return results.toString();
    }

    public static int parseIntAt(IStrategoTerm t, int index) {
        return Integer.parseInt(termContents(t.getSubterm(index)));
    }

    public static String cons(IStrategoTerm t) {
        if(t == null || t.getTermType() != APPL)
            return null;
        return ((IStrategoAppl) t).getConstructor().getName();
    }


    public static String observerFunction(IStrategoAppl document) {
        final IStrategoAppl observer = findTerm(document, "SemanticObserver");
        final String observerFunction = termContents(termAt(observer, 0));
        return observerFunction;
    }


    public static String onSaveFunction(IStrategoAppl document) {
        IStrategoAppl onsave = findTerm(document, "OnSave");
        onsave = onsave == null ? findTerm(document, "OnSaveDeprecated") : onsave;
        if(onsave != null) {
            String function = ((IStrategoString) onsave.getSubterm(0).getSubterm(0)).stringValue();
            return function;
        }
        return null;
    }


    public static @Nullable String resolverStrategy(IStrategoAppl document) {
        final IStrategoAppl resolver = findTerm(document, "ReferenceRule");
        if(resolver == null)
            return null;
        return termContents(termAt(resolver, 1));
    }

    public static @Nullable String hoverStrategy(IStrategoAppl document) {
        final IStrategoAppl hover = findTerm(document, "HoverRule");
        if(hover == null)
            return null;
        return termContents(termAt(hover, 1));
    }


    public static @Nullable String completionStrategy(IStrategoAppl document) {
        final IStrategoAppl completer = findTerm(document, "CompletionProposer");
        if(completer == null)
            return null;
        return termContents(termAt(completer, 1));
    }


    public static String startSymbol(IStrategoAppl document) {
        final IStrategoAppl result = findTerm(document, "StartSymbols");
        if(result == null)
            return null;

        return termContents(termAt(termAt(result, 0), 0));
    }

    public static String parseTableName(IStrategoAppl document) {
        String file = getProperty(document, "Table", getProperty(document, "LanguageName"));
        if(!file.endsWith(".tbl"))
            file += ".tbl";
        return file;
    }


    public static Set<FileObject> attachedFiles(IStrategoAppl document, FileObject basepath) throws FileSystemException {
        final Set<FileObject> attachedFiles = Sets.newLinkedHashSet(); // Use LinkedHashSet: must maintain JAR
                                                                       // order.

        for(IStrategoAppl s : collectTerms(document, "SemanticProvider")) {
            attachedFiles.add(basepath.resolveFile(termContents(s)));
        }

        return attachedFiles;
    }


    public static String languageName(IStrategoAppl document) {
        return getProperty(document, "LanguageName");
    }

    public static String[] extensions(IStrategoAppl document) {
        return getProperty(document, "Extensions").split(",");
    }


    public static Iterable<IStrategoAppl> builders(IStrategoAppl document) {
        return collectTerms(document, "Action");
    }

    public static String builderName(IStrategoAppl action) {
        assert action.getName().equals("Action");
        assert action.getSubtermCount() == 3;

        return asJavaString(action.getSubterm(0).getSubterm(0)).replace("\\", "").replace("\"", "");
    }

    public static String builderTarget(IStrategoAppl action) {
        assert action.getConstructor().getName().equals("Action");
        assert action.getConstructor().getArity() == 3;
        return asJavaString(action.getSubterm(1).getSubterm(0));
    }

    public static boolean builderIsOpenEditor(IStrategoAppl action) {
        assert action.getConstructor().getName().equals("Action");
        assert action.getConstructor().getArity() == 3;

        return builderAnnos(action).contains("OpenEditor");
    }

    public static boolean builderIsMeta(IStrategoAppl action) {
        assert action.getConstructor().getName().equals("Action");
        assert action.getConstructor().getArity() == 3;
        return builderAnnos(action).contains("Meta");
    }

    public static boolean builderIsOnSource(IStrategoAppl action) {
        assert action.getConstructor().getName().equals("Action");
        assert action.getConstructor().getArity() == 3;

        return builderAnnos(action).contains("Source");
    }

    public static Collection<String> builderAnnos(IStrategoAppl action) {
        assert action.getConstructor().getName().equals("Action");
        assert action.getConstructor().getArity() == 3;
        Collection<String> annos = new LinkedList<>();
        IStrategoList annoterm = (IStrategoList) action.getSubterm(2);
        for(IStrategoTerm anno : annoterm) {
            annos.add(((IStrategoAppl) anno).getName());
        }
        return annos;
    }


    public static Iterable<IStrategoAppl> styleDefinitions(IStrategoAppl document) {
        return collectTerms(document, "ColorDef");
    }

    public static Iterable<IStrategoAppl> styleRules(IStrategoAppl document) {
        return collectTerms(document, "ColorRule");
    }
}
