package org.metaborg.core;

import org.metaborg.util.collection.ImList;

public class AggregateMetaborgException extends MetaborgException {

    private static final long serialVersionUID = -6575523728704461990L;

    private final ImList.Immutable<MetaborgException> causes;

    public AggregateMetaborgException(Iterable<MetaborgException> causes) {
        this(ImList.Immutable.copyOf(causes));
    }

    private AggregateMetaborgException(ImList.Immutable<MetaborgException> causes) {
        super("Multiple exception occurred.");
        this.causes = causes;
    }

    @Override
    public synchronized Throwable getCause() {
        if(causes.size() == 1) {
            return causes.get(0);
        }
        return super.getCause();
    }

    public Iterable<MetaborgException> getCauses() {
        return causes;
    }

    @Override public String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getMessage());
        for(MetaborgException cause : causes) {
            sb.append(" * ");
            sb.append(cause.getMessage().replace("\n", "   \n"));
            sb.append("\n");
        }
        return sb.toString();
    }

    public static void throwIfAny(Iterable<MetaborgException> exceptions) throws MetaborgException {
        ImList.Immutable<MetaborgException> causes = ImList.Immutable.copyOf(exceptions);
        switch(causes.size()) {
        case 0:
            return;
        case 1:
            throw causes.get(0);
        default:
            throw new AggregateMetaborgException(causes);
        }
    }

}