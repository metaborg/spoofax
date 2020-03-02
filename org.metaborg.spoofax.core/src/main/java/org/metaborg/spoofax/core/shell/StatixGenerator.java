package org.metaborg.spoofax.core.shell;

import static mb.nabl2.terms.build.TermBuild.B;
import static mb.nabl2.terms.matching.TermMatch.M;

import java.io.OutputStream;
import java.util.Arrays;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.action.CompileGoal;
import org.metaborg.core.action.TransformActionContrib;
import org.metaborg.core.build.BuildInput;
import org.metaborg.core.build.BuildInputBuilder;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageFileSelector;
import org.metaborg.core.messages.WithLocationStreamMessagePrinter;
import org.metaborg.core.processing.ITask;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.core.build.ISpoofaxBuildOutput;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;

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
        // build all Statix files in the project
        final ITask<ISpoofaxBuildOutput> task;
        try {
            final BuildInputBuilder inputBuilder = new BuildInputBuilder(context.project());
            // @formatter:off
            final BuildInput input = inputBuilder
                .withCompileDependencyLanguages(false)
                .withLanguages(Arrays.asList(statixLang))
                .withDefaultIncludePaths(true)
                .withSourcesFromDefaultSourceLocations(true)
                .withSelector(new LanguageFileSelector(S.languageIdentifierService, statixLang))
                .withMessagePrinter(new WithLocationStreamMessagePrinter(S.sourceTextService, S.projectService, MSG_OUT))
                .withThrowOnErrors(true)
                .addTransformGoal(new CompileGoal())
                .build(S.dependencyService, S.languagePathService);
            // @formatter:on
            task = S.processorRunner.build(input, null, null).schedule().block();
        } catch(MetaborgException | InterruptedException e) {
            throw new MetaborgException("Building Statix files failed unexpectedly", e);
        } catch(MetaborgRuntimeException e) {
            throw new MetaborgException("Building Statix files failed", e);
        }

        final ISpoofaxBuildOutput output = task.result();
        if(!output.success()) {
            throw new MetaborgException("Failed to build Statix files in " + context.project());
        }

        ISpoofaxAnalyzeUnit analyzeUnit;
        if((analyzeUnit = Streams.stream(output.analysisResults().iterator())
                .filter(r -> r.source().getName().equals(resource.getName())).findFirst().orElse(null)) == null) {
            throw new MetaborgException("Cannot find " + resource);
        }
        if(!analyzeUnit.success()) {
            throw new MetaborgException(resource + " has analysis errors.");
        }

        final TransformActionContrib evalAction = CLI.getNamedTransformAction("Evaluation Pair", statixLang);
        final IStrategoTerm ast = analyzeUnit.ast();
        if(ast == null || !TermUtils.isAppl(ast) || !TermUtils.isAppl(ast, "Test")) {
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
                    return r != null ? TermUtils.toJavaString(r) : t.toString();
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