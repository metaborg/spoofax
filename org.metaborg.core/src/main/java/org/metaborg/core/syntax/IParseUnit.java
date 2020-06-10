package org.metaborg.core.syntax;

import org.metaborg.core.analysis.IAnalysisService;
import org.metaborg.core.analysis.IAnalyzeUnit;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.transform.ITransformService;
import org.metaborg.core.unit.IUnit;

/**
 * Unit representing a parsed source file. A parse unit can be passed to the {@link IAnalysisService} to analyze the
 * parse unit into a {@link IAnalyzeUnit}, or passed to the {@link ITransformService} to transform the parse unit into a
 * {@link ITransformUnit<IParseUnit>}.
 */
public interface IParseUnit extends IUnit {
    /**
     * @return True if this unit is valid, i.e. the parser parsed the source file without exceptions. Even when this
     *         unit is valid, it may still be unsuccessful, use {@link #success()} to check for that.
     */
    boolean valid();

    /**
     * @return True if parsing was successful, i.e. the parser produced a result and no errors were encountered. False
     *         otherwise.
     */
    boolean success();

    /**
     * Gets whether the parse result is ambiguous.
     *
     * @return {@code true} when the parse result is ambiguous;
     * otherwise, {@code false} when the parse result is unambiguous, invalid, or unsuccessful.
     */
    boolean isAmbiguous();

    /**
     * @return Messages produced by the parser.
     */
    Iterable<IMessage> messages();


    /**
     * @return The input unit this unit was made with.
     */
    IInputUnit input();


    /**
     * @return Parse duration in nanoseconds, or -1 if the duration is unknown.
     */
    long duration();
}
