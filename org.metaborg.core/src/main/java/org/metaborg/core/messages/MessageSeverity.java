package org.metaborg.core.messages;

import java.io.Serializable;

public enum MessageSeverity implements Serializable {
    NOTE(0), WARNING(1), ERROR(2);


    public final int value;


    MessageSeverity(int value) {
        this.value = value;
    }
}
