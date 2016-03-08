package org.metaborg.core.config;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nullable;

import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.IMessagePrinter;

import com.google.common.collect.Lists;

public class ConfigRequest<T> {
    private final @Nullable T config;
    private final Collection<IMessage> errors;


    public ConfigRequest() {
        this(null, Collections.<IMessage>emptyList());
    }

    public ConfigRequest(T config) {
        this(config, Collections.<IMessage>emptyList());
    }

    public ConfigRequest(Collection<IMessage> errors) {
        this(null, errors);
    }

    public ConfigRequest(IMessage error) {
        this(null, Lists.newArrayList(error));
    }

    public ConfigRequest(@Nullable T config, Collection<IMessage> errors) {
        this.config = config;
        this.errors = errors;
    }


    /**
     * @return True if configuration is valid, false if not.
     */
    public boolean valid() {
        return errors.isEmpty();
    }

    /**
     * @return Configuration if it exists, null if it does not exist or is invalid. May be a partial configuration if it
     *         is invalid.
     */
    public @Nullable T config() {
        return config;
    }

    /**
     * @return List of errors if the configuration is invalid, empty list otherwise.
     */
    public Iterable<IMessage> errors() {
        return errors;
    }

    /**
     * Reports errors to given message printer.
     * 
     * @param printer
     *            Message printer to print errors with.
     */
    public void reportErrors(IMessagePrinter printer) {
        if(errors.isEmpty()) {
            return;
        }

        for(IMessage error : errors) {
            printer.print(error, false);
        }
        printer.printSummary();
    }
}
