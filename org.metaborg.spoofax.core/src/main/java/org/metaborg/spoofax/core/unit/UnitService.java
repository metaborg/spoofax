package org.metaborg.spoofax.core.unit;

import jakarta.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.action.TransformActionContrib;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.unit.IUnit;
import org.metaborg.spoofax.core.syntax.JSGLRParserConfiguration;
import org.spoofax.interpreter.terms.ITermFactory;


public class UnitService implements ISpoofaxUnitService {
    private final ITermFactory termFactory;


    @jakarta.inject.Inject @javax.inject.Inject public UnitService(ITermFactory termFactory) {
        this.termFactory = termFactory;
    }


    private Unit unit() {
        return new Unit();
    }

    private Unit unit(FileObject source) {
        return new Unit(source);
    }


    @Override public ISpoofaxInputUnit inputUnit(FileObject source, String text, ILanguageImpl langImpl,
        @Nullable ILanguageImpl dialect, @Nullable JSGLRParserConfiguration config) {
        final Unit unit = unit(source);
        final InputContrib contrib = new InputContrib(text, langImpl, dialect, config);
        final InputUnit inputUnit = new InputUnit(unit, contrib);
        return inputUnit;
    }

    @Override public ISpoofaxInputUnit inputUnit(String text, ILanguageImpl langImpl, @Nullable ILanguageImpl dialect,
        @Nullable JSGLRParserConfiguration config) {
        final Unit unit = unit();
        final InputContrib contrib = new InputContrib(text, langImpl, dialect, config);
        final InputUnit inputUnit = new InputUnit(unit, contrib);
        return inputUnit;
    }

    @Override public ISpoofaxInputUnit inputUnit(FileObject source, String text, ILanguageImpl langImpl,
        @Nullable ILanguageImpl dialect) {
        return inputUnit(source, text, langImpl, dialect, null);
    }

    @Override public ISpoofaxInputUnit inputUnit(String text, ILanguageImpl langImpl, @Nullable ILanguageImpl dialect) {
        return inputUnit(text, langImpl, dialect, null);
    }

    @Override public ISpoofaxInputUnit emptyInputUnit(FileObject source, ILanguageImpl langImpl,
        @Nullable ILanguageImpl dialect) {
        final Unit unit = unit(source);
        final InputContrib contrib = new InputContrib(langImpl, dialect);
        final InputUnit inputUnit = new InputUnit(unit, contrib);
        return inputUnit;
    }

    @Override public ISpoofaxInputUnit emptyInputUnit(ILanguageImpl langImpl, @Nullable ILanguageImpl dialect) {
        final Unit unit = unit();
        final InputContrib contrib = new InputContrib(langImpl, dialect);
        final InputUnit inputUnit = new InputUnit(unit, contrib);
        return inputUnit;
    }


    @Override public ISpoofaxParseUnit parseUnit(ISpoofaxInputUnit input, ParseContrib contrib) {
        if(!(input instanceof UnitWrapper)) {
            throw new MetaborgRuntimeException("Input unit is not a SpoofaxUnitWrapper, cannot create a parse unit");
        }
        final UnitWrapper wrapper = (UnitWrapper) input;
        final ParseUnit parseUnit = new ParseUnit(wrapper.unit, contrib, input);
        return parseUnit;
    }

    @Override public ISpoofaxParseUnit emptyParseUnit(ISpoofaxInputUnit input) {
        return parseUnit(input, new ParseContrib(termFactory.makeTuple()));
    }


    @Override public ISpoofaxAnalyzeUnit analyzeUnit(ISpoofaxParseUnit input, AnalyzeContrib contrib,
        IContext context) {
        if(!(input instanceof UnitWrapper)) {
            throw new MetaborgRuntimeException("Input unit is not a SpoofaxUnitWrapper, cannot create an analyze unit");
        }
        final UnitWrapper wrapper = (UnitWrapper) input;
        final AnalyzeUnit analyzeUnit = new AnalyzeUnit(wrapper.unit, contrib, input, context);
        return analyzeUnit;
    }

    @Override public ISpoofaxAnalyzeUnit emptyAnalyzeUnit(ISpoofaxParseUnit input, IContext context) {
        return analyzeUnit(input, new AnalyzeContrib(), context);
    }


    @Override public ISpoofaxAnalyzeUnitUpdate analyzeUnitUpdate(FileObject source, AnalyzeUpdateData contrib,
        IContext context) {
        return new AnalyzeUnitUpdate(source, contrib);
    }

    @Override public ISpoofaxAnalyzeUnitUpdate emptyAnalyzeUnitUpdate(FileObject source, IContext context) {
        return analyzeUnitUpdate(source, new AnalyzeUpdateData(), context);
    }


    @Override public <I extends IUnit> ISpoofaxTransformUnit<I> transformUnit(I input, TransformContrib contrib,
        IContext context, TransformActionContrib action) {
        if(!(input instanceof UnitWrapper)) {
            throw new MetaborgRuntimeException(
                "Input unit is not a SpoofaxUnitWrapper, cannot create a transform unit");
        }
        final UnitWrapper wrapper = (UnitWrapper) input;
        final TransformUnit<I> analyzeUnit = new TransformUnit<>(wrapper.unit, contrib, input, context, action);
        return analyzeUnit;
    }

    @Override public ISpoofaxTransformUnit<ISpoofaxParseUnit> emptyTransformUnit(ISpoofaxParseUnit input,
        IContext context, TransformActionContrib action) {
        return transformUnit(input, new TransformContrib(), context, action);
    }

    @Override public ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit> emptyTransformUnit(ISpoofaxAnalyzeUnit input,
        IContext context, TransformActionContrib action) {
        return transformUnit(input, new TransformContrib(), context, action);
    }
}
