package org.metaborg.core.analysis;

import org.metaborg.core.context.IContext;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.syntax.IParseUnit;
import org.metaborg.core.transform.ITransformService;
import org.metaborg.core.unit.IUnit;

/**
 * Unit representing an analyzed source file. An analyze unit can be passed to the {@link ITransformService} to
 * transform the parse unit into a {@link ITransformUnit<IAnalyzeUnit>}
 */
public interface IAnalyzeUnit extends IUnit {
    /**
     * @return True if this unit is valid, i.e. the analyzer analyzed the parse unit without exceptions. Even when the
     *         unit is valid, it may still be unsuccessful, use {@link #success()} to check for that.
     */
    boolean valid();

    /**
     * @return True if analysis was successful, i.e. the analyzed produced a result and no errors were encountered.
     *         False otherwise.
     */
    boolean success();

    /**
     * @return Type of this unit.
     */
    AnalyzeUnitType type();

    /**
     * @return Messages produced by the analyzer.
     */
    Iterable<IMessage> messages();


    /**
     * @return The parse unit this unit was made with.
     */
    IParseUnit input();

    /**
     * @return The context that was used during analysis.
     */
    IContext context();


    /**
     * @return Analysis duration in nanoseconds, or -1 if the duration is unknown.
     */
    long duration();
}
