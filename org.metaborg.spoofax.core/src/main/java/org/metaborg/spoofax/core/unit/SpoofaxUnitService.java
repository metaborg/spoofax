package org.metaborg.spoofax.core.unit;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.action.TransformActionContrib;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.unit.IUnit;
import org.metaborg.spoofax.core.syntax.JSGLRParserConfiguration;

public class SpoofaxUnitService implements ISpoofaxUnitService {
    private SpoofaxUnit unit() {
        return new SpoofaxUnit();
    }

    private SpoofaxUnit unit(FileObject source) {
        return new SpoofaxUnit(source);
    }


    @Override public ISpoofaxInputUnit inputUnit(FileObject source, String text, ILanguageImpl langImpl,
        @Nullable ILanguageImpl dialect, @Nullable JSGLRParserConfiguration config) {
        final SpoofaxUnit unit = unit(source);
        final InputContrib contrib = new InputContrib(text, langImpl, dialect, config);
        final SpoofaxInputUnit inputUnit = new SpoofaxInputUnit(unit, contrib);
        return inputUnit;
    }

    @Override public ISpoofaxInputUnit inputUnit(String text, ILanguageImpl langImpl, @Nullable ILanguageImpl dialect,
        @Nullable JSGLRParserConfiguration config) {
        final SpoofaxUnit unit = unit();
        final InputContrib contrib = new InputContrib(text, langImpl, dialect, config);
        final SpoofaxInputUnit inputUnit = new SpoofaxInputUnit(unit, contrib);
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
        final SpoofaxUnit unit = unit(source);
        final InputContrib contrib = new InputContrib(langImpl, dialect);
        final SpoofaxInputUnit inputUnit = new SpoofaxInputUnit(unit, contrib);
        return inputUnit;
    }

    @Override public ISpoofaxInputUnit emptyInputUnit(ILanguageImpl langImpl, @Nullable ILanguageImpl dialect) {
        final SpoofaxUnit unit = unit();
        final InputContrib contrib = new InputContrib(langImpl, dialect);
        final SpoofaxInputUnit inputUnit = new SpoofaxInputUnit(unit, contrib);
        return inputUnit;
    }


    @Override public ISpoofaxParseUnit parseUnit(ISpoofaxInputUnit input, ParseContrib contrib) {
        final SpoofaxUnit unit;
        if(!(input instanceof SpoofaxUnitWrapper)) {
            throw new MetaborgRuntimeException("Input unit is not a SpoofaxUnitWrapper, cannot create a parse unit");
        }
        final SpoofaxUnitWrapper wrapper = (SpoofaxUnitWrapper) input;
        unit = wrapper.unit;
        final SpoofaxParseUnit parseUnit = new SpoofaxParseUnit(unit, contrib, input);
        return parseUnit;
    }

    @Override public ISpoofaxParseUnit emptyParseUnit(ISpoofaxInputUnit input) {
        return parseUnit(input, new ParseContrib());
    }


    @Override public ISpoofaxAnalyzeUnit analyzeUnit(ISpoofaxParseUnit input, AnalyzeContrib contrib,
        IContext context) {
        final SpoofaxUnit unit;
        if(!(input instanceof SpoofaxUnitWrapper)) {
            throw new MetaborgRuntimeException("Input unit is not a SpoofaxUnitWrapper, cannot create an analyze unit");
        }
        final SpoofaxUnitWrapper wrapper = (SpoofaxUnitWrapper) input;
        unit = wrapper.unit;
        final SpoofaxAnalyzeUnit analyzeUnit = new SpoofaxAnalyzeUnit(unit, contrib, input, context);
        return analyzeUnit;
    }

    @Override public ISpoofaxAnalyzeUnit emptyAnalyzeUnit(ISpoofaxParseUnit input, IContext context) {
        return analyzeUnit(input, new AnalyzeContrib(), context);
    }


    @Override public <I extends IUnit> ISpoofaxTransformUnit<I> transformUnit(I input, TransformContrib contrib,
        IContext context, TransformActionContrib action) {
        final SpoofaxUnit unit;
        if(!(input instanceof SpoofaxUnitWrapper)) {
            throw new MetaborgRuntimeException(
                "Input unit is not a SpoofaxUnitWrapper, cannot create a transform unit");
        }
        final SpoofaxUnitWrapper wrapper = (SpoofaxUnitWrapper) input;
        unit = wrapper.unit;
        final SpoofaxTransformUnit<I> analyzeUnit = new SpoofaxTransformUnit<>(unit, contrib, input, context, action);
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
