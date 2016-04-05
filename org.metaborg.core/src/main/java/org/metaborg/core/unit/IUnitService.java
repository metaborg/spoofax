package org.metaborg.core.unit;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.action.TransformActionContrib;
import org.metaborg.core.analysis.IAnalyzeUnit;
import org.metaborg.core.analysis.IAnalyzeUnitUpdate;
import org.metaborg.core.context.IContext;
import org.metaborg.core.syntax.IInputUnit;
import org.metaborg.core.syntax.IParseUnit;
import org.metaborg.core.transform.ITransformUnit;

public interface IUnitService<I extends IInputUnit, P extends IParseUnit, A extends IAnalyzeUnit, AU extends IAnalyzeUnitUpdate, TP extends ITransformUnit<P>, TA extends ITransformUnit<A>>
    extends IInputUnitService<I> {
    P emptyParseUnit(I input);


    A emptyAnalyzeUnit(P input, IContext context);

    AU emptyAnalyzeUnitUpdate(FileObject source, IContext context);


    TP emptyTransformUnit(P input, IContext context, TransformActionContrib action);

    TA emptyTransformUnit(A input, IContext context, TransformActionContrib action);
}
