package org.metaborg.spoofax.core.analysis;

import javax.inject.Inject;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.spoofax.core.dynamicclassloading.IDynamicClassLoadingService;
import org.metaborg.spoofax.core.dynamicclassloading.api.IAnalyzer;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.HybridInterpreter;

import com.google.inject.assistedinject.Assisted;

public class JavaAnalysisFacet implements IAnalysisFacet {
    public final String javaClassName;

    private final IDynamicClassLoadingService semanticProviderService;

    @Inject public JavaAnalysisFacet(IDynamicClassLoadingService semanticProviderService,
        @Assisted String javaClassName) {
        this.semanticProviderService = semanticProviderService;
        this.javaClassName = javaClassName;
    }

    @Override public IStrategoTerm analyze(HybridInterpreter runtime, IStrategoTerm inputTerm,
        ILanguageComponent contributor) throws MetaborgException {
        IAnalyzer analyzer = semanticProviderService.loadClass(contributor, javaClassName, IAnalyzer.class);
        return analyzer.analyze(runtime, inputTerm);
    }

}
