package org.metaborg.spoofax.core.stratego.primitive;

import java.util.List;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.build.dependency.IDependencyService;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageUtils;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxContextPrimitive;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class CallStrategyPrimitive extends ASpoofaxContextPrimitive {
    private static final ILogger logger = LoggerUtils.logger(CallStrategyPrimitive.class);

    private final IDependencyService dependencyService;
    private final IContextService contextService;
    private final IStrategoCommon common;


    @Inject public CallStrategyPrimitive(IDependencyService dependencyService, IContextService contextService,
        IStrategoCommon common) {
        super("call_strategy", 0, 2);

        this.dependencyService = dependencyService;
        this.contextService = contextService;
        this.common = common;
    }


    @Override protected IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
        ITermFactory factory, IContext currentContext) throws MetaborgException {
        final String languageName = Tools.asJavaString(tvars[0]);
        final String strategyName = Tools.asJavaString(tvars[1]);

        final Iterable<ILanguageComponent> compileDeps = dependencyService.compileDeps(currentContext.project());
        final Iterable<ILanguageImpl> impls = LanguageUtils.toImpls(compileDeps);
        final List<ILanguageImpl> selectedImpls = Lists.newArrayList();
        for(ILanguageImpl impl : impls) {
            if(impl.belongsTo().name().equals(languageName)) {
                selectedImpls.add(impl);
            }
        }
        if(selectedImpls.isEmpty()) {
            final String message = logger.format(
                "Stratego strategy call of '{}' into language '{}' failed, no language implementation found",
                strategyName, languageName);
            throw new MetaborgException(message);
        } else if(selectedImpls.size() > 1) {
            final String message = logger.format(
                "Stratego strategy call of '{}' into language '{}' failed, multiple language implementations found: {}",
                strategyName, languageName, Joiner.on(", ").join(selectedImpls));
            throw new MetaborgException(message);
        }
        final ILanguageImpl impl = selectedImpls.get(0);

        final IContext context = contextService.get(currentContext.location(), currentContext.project(), impl);
        return common.invoke(impl, context, current, strategyName);
    }
}
