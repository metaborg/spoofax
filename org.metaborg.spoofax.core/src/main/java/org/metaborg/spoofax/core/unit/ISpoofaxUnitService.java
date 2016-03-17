package org.metaborg.spoofax.core.unit;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.action.TransformActionContrib;
import org.metaborg.core.context.IContext;
import org.metaborg.core.unit.IUnit;
import org.metaborg.core.unit.IUnitService;

/**
 * Typedef interface for {@link IUnitService} with Spoofax interfaces, extended with methods to create new parse,
 * analyze, and transform units.
 */
public interface ISpoofaxUnitService extends
    IUnitService<ISpoofaxInputUnit, ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxAnalyzeUnitUpdate, ISpoofaxTransformUnit<ISpoofaxParseUnit>, ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit>>,
    ISpoofaxInputUnitService {
    ISpoofaxParseUnit parseUnit(ISpoofaxInputUnit input, ParseContrib contrib);


    ISpoofaxAnalyzeUnit analyzeUnit(ISpoofaxParseUnit input, AnalyzeContrib contrib, IContext context);

    ISpoofaxAnalyzeUnitUpdate analyzeUnitUpdate(FileObject source, AnalyzeUpdateData contrib, IContext context);


    <I extends IUnit> ISpoofaxTransformUnit<I> transformUnit(I input, TransformContrib contrib, IContext context,
        TransformActionContrib action);
}
