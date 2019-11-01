package org.metaborg.spoofax.core.shell;

import static mb.nabl2.terms.build.TermBuild.B;
import static mb.nabl2.terms.matching.TermMatch.M;

import java.io.OutputStream;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.action.TransformActionContrib;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.Iterables;

import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.matching.Transform.T;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.nabl2.util.ImmutableTuple2;
import mb.nabl2.util.TermFormatter;
import mb.nabl2.util.Tuple2;
import mb.statix.solver.IConstraint;
import mb.statix.spec.Spec;
import mb.statix.spoofax.StatixTerms;

public class StatixGenerator {

    private static final String LANG_STX_NAME = "StatixLang";
    private static final OutputStream MSG_OUT = System.out;

    private final Spoofax S;
    private final CLIUtils CLI;
    private final ILanguageImpl statixLang;
    private final IContext context;
    private final Spec spec;
    private final IConstraint constraint;

    public StatixGenerator(Spoofax spoofax, IContext context, FileObject spec) throws MetaborgException {
        this.S = spoofax;
        this.CLI = new CLIUtils(S);
        this.statixLang = CLI.getLanguage(LANG_STX_NAME);
        this.context = context;
        final Tuple2<Spec, IConstraint> specAndConstraint = loadSpec(spec);
        this.spec = specAndConstraint._1();
        this.constraint = specAndConstraint._2();
    }

    private Tuple2<Spec, IConstraint> loadSpec(FileObject resource) throws MetaborgException {
        final ISpoofaxInputUnit inputUnit = CLI.read(resource, statixLang);

        final ISpoofaxParseUnit parseUnit = CLI.parse(inputUnit, statixLang);
        if(!parseUnit.success()) {
            CLI.printMessages(MSG_OUT, parseUnit.messages());
            throw new MetaborgException(resource + " has parse errors.");
        }

        final IContext context = S.contextService.get(resource, this.context.project(), statixLang);

        final ISpoofaxAnalyzeUnit analyzeUnit = CLI.analyze(parseUnit, context);
        if(!analyzeUnit.success()) {
            CLI.printMessages(MSG_OUT, analyzeUnit.messages());
            throw new MetaborgException(resource + " has analysis errors.");
        }

        final TransformActionContrib evalAction = CLI.getNamedTransformAction("Evaluation Pair", statixLang);
        final IStrategoTerm ast = analyzeUnit.ast();
        if(ast == null || !Tools.isTermAppl(ast) || !Tools.hasConstructor((IStrategoAppl) ast, "Test")) {
            throw new MetaborgException("Not a correct spec.");
        }
        final IStrategoTerm evalPair = CLI.transform(analyzeUnit, evalAction, context);
        if(!Tools.isTermTuple(evalPair) || evalPair.getSubtermCount() != 2) {
            throw new MetaborgException("Expected tuple of constraint and spec, but got " + evalPair);
        }

        final StrategoTerms strategoTerms =
                new StrategoTerms(S.termFactoryService.get(statixLang, context.project(), false));
        final IConstraint constraint =
                StatixTerms.constraint().match(strategoTerms.fromStratego(evalPair.getSubterm(0)))
                        .orElseThrow(() -> new MetaborgException("Expected constraint"));
        final Spec spec = StatixTerms.spec().match(strategoTerms.fromStratego(evalPair.getSubterm(1)))
                .orElseThrow(() -> new MetaborgException("Expected spec"));
        return ImmutableTuple2.of(spec, constraint);
    }

    public Spec spec() {
        return spec;
    }

    public IConstraint constraint() {
        return constraint;
    }

    public static TermFormatter pretty(Spoofax S, IContext context, String strategy) {
        final ILanguageImpl lang = context.language();
        final TermFormatter pp;
        if(!Iterables.isEmpty(lang.components())) {
            final ILanguageComponent lc = Iterables.getOnlyElement(lang.components());
            final ITermFactory tf;
            final HybridInterpreter runtime;
            try {
                tf = S.termFactoryService.get(lc, context.project(), false);
                runtime = S.strategoRuntimeService.runtime(lc, context, false);
            } catch(MetaborgException e) {
                throw new MetaborgRuntimeException(e);
            }
            final StrategoTerms strategoTerms = new StrategoTerms(tf);
            pp = (t) -> {
                final IStrategoTerm st = strategoTerms.toStratego(explicate(t));
                try {
                    final IStrategoTerm r = S.strategoCommon.invoke(runtime, st, strategy);
                    return r != null ? Tools.asJavaString(r) : t.toString();
                } catch(MetaborgException e) {
                    return t.toString();
                }
            };
        } else {
            pp = ITerm::toString;
        }
        return pp;
    }

    private static ITerm explicate(ITerm t) {
        // @formatter:off
        return T.sometd(
            M.<ITerm>cases(
                M.cons(M.term(), M.<ITerm>var(StatixGenerator::explicate), (cons, hd, tl) -> B.newCons(explicate(hd), B.newList(explicate(tl)), cons.getAttachments())),
                M.var(v -> B.newAppl("Var", B.newString(v.getResource()), B.newString(v.getName())))
            )::match
        ).apply(t);
        // @formatter:on
    }

}