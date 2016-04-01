package org.metaborg.spoofax.meta.core.wizard;

import org.metaborg.meta.core.wizard.CreateLanguageSpecWizard;
import org.metaborg.spoofax.meta.core.generator.language.AnalysisType;
import org.metaborg.spoofax.meta.core.generator.language.LanguageSpecGeneratorSettingsBuilder;
import org.metaborg.spoofax.meta.core.generator.language.SyntaxType;

/**
 * Spoofax specialization of the 'create language specification' wizard helper.
 */
public abstract class CreateSpoofaxLanguageSpecWizard extends CreateLanguageSpecWizard {
    public SyntaxType syntaxType() {
        return SyntaxType.mapping().get(inputSyntaxTypeString());
    }

    public AnalysisType analysisType() {
        return AnalysisType.mapping().get(inputAnalysisTypeString());
    }


    protected abstract boolean inputSyntaxTypeModified();

    protected abstract String inputSyntaxTypeString();

    protected abstract void setSyntaxType(String syntaxTypeString);


    protected abstract boolean inputAnalysisTypeModified();

    protected abstract String inputAnalysisTypeString();

    protected abstract void setAnalysisType(String analysisTypeString);


    @Override public void setDefaults() {
        super.setDefaults();

        if(!inputSyntaxTypeModified()) {
            setSyntaxType(LanguageSpecGeneratorSettingsBuilder.standardSyntaxType.name);
        }
        if(!inputAnalysisTypeModified()) {
            setAnalysisType(LanguageSpecGeneratorSettingsBuilder.standardAnalysisType.name);
        }
    }
}
