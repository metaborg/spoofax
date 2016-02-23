package org.metaborg.core.config;

import org.metaborg.core.MetaborgException;

public class ConfigException extends MetaborgException {
    private static final long serialVersionUID = -2113303204852961254L;


    public ConfigException() {
        super();
    }

    public ConfigException(String message) {
        super(message);
    }

    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigException(Throwable cause) {
        super(cause);
    }
}
