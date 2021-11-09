package org.metaborg.spoofax.meta.core.wizard;

import org.metaborg.meta.core.wizard.CreateLanguageSpecWizard;
import org.metaborg.spoofax.meta.core.generator.general.AnalysisType;
import org.metaborg.spoofax.meta.core.generator.general.LangSpecGeneratorSettingsBuilder;
import org.metaborg.spoofax.meta.core.generator.general.SyntaxType;
import org.metaborg.spoofax.meta.core.generator.general.TransformationType;

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

    public TransformationType transformationType() {
        return TransformationType.mapping().get(inputTransformationTypeString());
    }

    public abstract boolean generateExampleProject();

    public abstract boolean generateTestProject();

    public abstract boolean generateEclipsePluginProject();

    public abstract boolean generateEclipseFeatureProject();

    public abstract boolean generateEclipseUpdatesiteProject();


    protected abstract boolean inputSyntaxTypeModified();

    protected abstract String inputSyntaxTypeString();

    protected abstract void setSyntaxType(String syntaxTypeString);


    protected abstract boolean inputAnalysisTypeModified();

    protected abstract String inputAnalysisTypeString();

    protected abstract void setAnalysisType(String analysisTypeString);


    protected abstract boolean inputTransformationTypeModified();

    protected abstract String inputTransformationTypeString();

    protected abstract void setTransformationType(String transformationTypeString);


    protected abstract void setGenerateExampleProject(boolean generate);

    protected abstract void setGenerateTestProject(boolean generate);

    protected abstract void setGenerateEclipsePluginProject(boolean generate);

    protected abstract void setGenerateEclipseFeatureProject(boolean generate);

    protected abstract void setGenerateEclipseUpdatesiteProject(boolean generate);


    @Override public void setDefaults() {
        super.setDefaults();

        if(!inputSyntaxTypeModified()) {
            setSyntaxType(LangSpecGeneratorSettingsBuilder.standardSyntaxType.name);
        }
        if(!inputAnalysisTypeModified()) {
            setAnalysisType(LangSpecGeneratorSettingsBuilder.standardAnalysisType.name);
        }
        if(!inputTransformationTypeModified()) {
            setTransformationType(LangSpecGeneratorSettingsBuilder.standardTransformationType.name);
        }

        setGenerateExampleProject(false);
        setGenerateTestProject(false);
        setGenerateEclipsePluginProject(false);
        setGenerateEclipseFeatureProject(false);
        setGenerateEclipseUpdatesiteProject(false);
    }
}
