package org.metaborg.spoofax.core.analysis;

import javax.inject.Inject;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.spoofax.core.dynamicclassloading.IDynamicClassLoadingService;
import org.metaborg.spoofax.core.dynamicclassloading.api.IAnalyzer;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.HybridInterpreter;

public class JavaGeneratedAnalysisFacet implements IAnalysisFacet {
    private final IDynamicClassLoadingService semanticProviderService;

    @Inject public JavaGeneratedAnalysisFacet(IDynamicClassLoadingService semanticProviderService) {
        this.semanticProviderService = semanticProviderService;
    }

    @Override public IStrategoTerm analyze(HybridInterpreter runtime, IStrategoTerm inputTerm,
        ILanguageComponent contributor) throws MetaborgException {
        for(IAnalyzer analyzer : semanticProviderService.loadClasses(contributor, IAnalyzer.Generated.class)) {
            IStrategoTerm analysisResult = analyzer.analyze(runtime, inputTerm);
            if(analysisResult != null) {
                return analysisResult;
            }
        }
        return null;
    }
}
