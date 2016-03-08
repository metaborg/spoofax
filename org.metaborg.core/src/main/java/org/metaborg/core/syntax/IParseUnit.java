package org.metaborg.core.syntax;

import org.metaborg.core.messages.IMessage;
import org.metaborg.core.unit.IUnit;

public interface IParseUnit extends IUnit {
    boolean valid();
    
    boolean success();

    Iterable<IMessage> messages();


    IInputUnit input();


    long duration();
}
