package org.metaborg.core.transform;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.action.TransformActionContrib;
import org.metaborg.core.context.IContext;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.unit.IUnit;

/**
 * Unit representing a transformed source file.
 * 
 * @param <I>
 *            Type of input unit.
 */
public interface ITransformUnit<I extends IUnit> extends IUnit {
    /**
     * @return True if this unit is valid, i.e. the transformed transformed the input unit without exceptions. Even when
     *         the unit is valid, it may still be unsuccessful, use {@link #success()} to check for that.
     */
    boolean valid();

    /**
     * @return True if transformation was successful, i.e. the transformed produced a result and no errors were
     *         encountered. False otherwise.
     */
    boolean success();

    /**
     * @return Output file that the result has been written to. Null when no file was written, or if {@link #valid()}
     *         returns false.
     */
    @Nullable FileObject output();

    /**
     * @return Messages produced by the transformer.
     */
    Iterable<IMessage> messages();


    /**
     * @return The unit this unit was made with.
     */
    I input();

    /**
     * @return The context that was used during transformation.
     */
    IContext context();

    /**
     * @return The action that was used to execute the transformation.
     */
    TransformActionContrib action();


    /**
     * @return Transformation duration in nanoseconds, or -1 if the duration is unknown.
     */
    long duration();
}
