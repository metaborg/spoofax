package org.metaborg.spoofax.core.syntax;

import org.metaborg.core.syntax.IParser;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.util.task.ICancel;
import org.metaborg.util.task.IProgress;
import javax.annotation.Nullable;

/**
 * Typedef interface for {@link IParser} with Spoofax interfaces.
 */
public interface ISpoofaxParser extends IParser<ISpoofaxInputUnit, ISpoofaxParseUnit> {

    /**
     * Parses given input unit into a parse unit. Allows overriding the used imploder in the parser.
     *
     * @param input
     *            Input unit to parse.
     * @param overrideImploder
     *            override the imploder implementation used in the parser.
     * @return Parse unit.
     * @throws ParseException
     *             When parsing fails unexpectedly.
     */
    ISpoofaxParseUnit parse(ISpoofaxInputUnit input, IProgress progress, ICancel cancel,
        @Nullable ImploderImplementation overrideImploder)
        throws ParseException;

}
