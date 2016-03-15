package org.metaborg.spoofax.core.unit;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.action.TransformActionContrib;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.unit.IUnit;
import org.metaborg.core.unit.IUnitService;
import org.metaborg.spoofax.core.syntax.JSGLRParserConfiguration;

/**
 * Typedef interface for {@link IUnitService} with Spoofax interfaces, extended with methods to create new parse,
 * analyze, and transform units.
 */
public interface ISpoofaxUnitService extends
    IUnitService<ISpoofaxInputUnit, ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxAnalyzeUnitUpdate, ISpoofaxTransformUnit<ISpoofaxParseUnit>, ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit>> {
    ISpoofaxInputUnit inputUnit(FileObject source, String text, ILanguageImpl langImpl, @Nullable ILanguageImpl dialect,
        @Nullable JSGLRParserConfiguration config);

    ISpoofaxInputUnit inputUnit(String text, ILanguageImpl langImpl, @Nullable ILanguageImpl dialect,
        @Nullable JSGLRParserConfiguration config);


    ISpoofaxParseUnit parseUnit(ISpoofaxInputUnit input, ParseContrib contrib);


    ISpoofaxAnalyzeUnit analyzeUnit(ISpoofaxParseUnit input, AnalyzeContrib contrib, IContext context);
    
    ISpoofaxAnalyzeUnitUpdate analyzeUnitUpdate(FileObject source, AnalyzeUpdateData contrib, IContext context);


    <I extends IUnit> ISpoofaxTransformUnit<I> transformUnit(I input, TransformContrib contrib, IContext context,
        TransformActionContrib action);
}
