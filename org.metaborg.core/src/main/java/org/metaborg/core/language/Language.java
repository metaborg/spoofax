package org.metaborg.core.language;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

public class Language implements ILanguage, ILanguageInternal {
    private final String name;
    private final Set<ILanguageImplInternal> impls;


    public Language(String name) {
        this.name = name;
        this.impls = new HashSet<ILanguageImplInternal>();
    }


    @Override public String name() {
        return name;
    }

    @Override public Iterable<? extends ILanguageImpl> impls() {
        return impls;
    }

    @Override public @Nullable ILanguageImpl activeImpl() {
        return LanguageUtils.active(impls);
    }

    @Override public void add(ILanguageImplInternal implementation) {
        impls.add(implementation);
    }

    @Override public void remove(ILanguageImplInternal implementation) {
        impls.remove(implementation);
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final Language other = (Language) obj;
        if(!name.equals(other.name))
            return false;
        return true;
    }

    @Override public String toString() {
        return "language " + name;
    }
}
