package org.metaborg.core.transform;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.action.TransformActionContribution;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.unit.IUnit;

public interface ITransformUnit<I extends IUnit> extends IUnit {
    boolean success();

    @Nullable FileObject output();

    Iterable<IMessage> messages();


    I input();

    TransformActionContribution action();


    long duration();
}
