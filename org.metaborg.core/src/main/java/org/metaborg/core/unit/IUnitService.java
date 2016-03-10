package org.metaborg.core.unit;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.action.TransformActionContrib;
import org.metaborg.core.analysis.IAnalyzeUnit;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.syntax.IInputUnit;
import org.metaborg.core.syntax.IParseUnit;
import org.metaborg.core.transform.ITransformUnit;

public interface IUnitService<I extends IInputUnit, P extends IParseUnit, A extends IAnalyzeUnit, TP extends ITransformUnit<P>, TA extends ITransformUnit<A>> {
    I inputUnit(FileObject source, String text, ILanguageImpl langImpl, @Nullable ILanguageImpl dialect);

    I inputUnit(String text, ILanguageImpl langImpl, @Nullable ILanguageImpl dialect);

    I emptyInputUnit(FileObject source, ILanguageImpl langImpl, @Nullable ILanguageImpl dialect);

    I emptyInputUnit(ILanguageImpl langImpl, @Nullable ILanguageImpl dialect);


    P emptyParseUnit(I input);


    A emptyAnalyzeUnit(P input, IContext context);


    TP emptyTransformUnit(P input, IContext context, TransformActionContrib action);

    TA emptyTransformUnit(A input, IContext context, TransformActionContrib action);
}
