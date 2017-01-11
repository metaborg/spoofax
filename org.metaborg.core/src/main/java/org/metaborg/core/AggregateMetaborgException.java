package org.metaborg.core;

import com.google.common.collect.ImmutableList;

public class AggregateMetaborgException extends MetaborgException {

    private static final long serialVersionUID = -6575523728704461990L;

    private final ImmutableList<MetaborgException> causes;

    public AggregateMetaborgException(Iterable<MetaborgException> causes) {
        this(ImmutableList.copyOf(causes));
    }

    private AggregateMetaborgException(ImmutableList<MetaborgException> causes) {
        super("Multiple exception occurred.");
        this.causes = causes;
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
        ImmutableList<MetaborgException> causes = ImmutableList.copyOf(exceptions);
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