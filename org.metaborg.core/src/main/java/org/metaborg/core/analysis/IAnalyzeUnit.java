package org.metaborg.core.analysis;

import org.metaborg.core.context.IContext;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.syntax.IParseUnit;
import org.metaborg.core.unit.IUnit;

public interface IAnalyzeUnit extends IUnit {
    boolean valid();
    
    boolean success();
    
    AnalyzeUnitType type();

    Iterable<IMessage> messages();

    
    IParseUnit input();

    IContext context();
    

    long duration();
}
