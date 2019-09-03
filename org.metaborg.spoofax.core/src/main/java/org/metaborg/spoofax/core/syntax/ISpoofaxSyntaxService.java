package org.metaborg.spoofax.core.syntax;

import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.syntax.ISyntaxService;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.util.task.ICancel;
import org.metaborg.util.task.IProgress;
import org.metaborg.util.task.NullCancel;
import org.metaborg.util.task.NullProgress;
import javax.annotation.Nullable;

/**
 * Typedef interface for {@link ISyntaxService} with Spoofax interfaces.
 */
public interface ISpoofaxSyntaxService extends ISyntaxService<ISpoofaxInputUnit, ISpoofaxParseUnit> {

    /**
     * Parses given input unit into a parse unit. Allows overriding the used imploder in the parser.
     *
     * @param input
     *            Input unit to parse.
     * @param progress
     *            Progress reporter.
     * @param cancel
     *            Cancellation token.
     * @param overrideImploder
     *            override the imploder implementation used in the parser.
     * @return Parse unit.
     * @throws ParseException
     *             When parsing fails unexpectedly.
     * @throws InterruptedException
     *             When parsing is cancelled.
     */
    ISpoofaxParseUnit parse(ISpoofaxInputUnit input, IProgress progress, ICancel cancel,
        @Nullable ImploderImplementation overrideImploder)
        throws ParseException, InterruptedException;

    /**
     * Parses given input unit into a parse unit.
     *
     * @param input
     *            Input unit to parse.
     * @return Parse unit.
     * @throws ParseException
     *             When parsing fails unexpectedly.
     */
    default ISpoofaxParseUnit parse(ISpoofaxInputUnit input,
        @Nullable ImploderImplementation overrideImploder) throws ParseException {
        try {
            return parse(input, new NullProgress(), new NullCancel(), overrideImploder);
        } catch(InterruptedException e) {
            // This cannot happen, since we pass a null cancellation token, but we need to handle the exception.
            throw new MetaborgRuntimeException("Interrupted", e);
        }
    }

}
