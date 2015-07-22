package org.metaborg.core.language;

import java.util.Set;

import com.google.common.collect.Sets;

public class Language implements ILanguage, ILanguageInternal {
    private final String name;
    private final Set<ILanguageImpl> implementations;


    public Language(String name) {
        this.name = name;
        this.implementations = Sets.newHashSet();
    }


    @Override public String name() {
        return name;
    }

    @Override public Iterable<ILanguageImpl> all() {
        return implementations;
    }


    @Override public void add(ILanguageImpl implementation) {
        implementations.add(implementation);
    }

    @Override public void remove(ILanguageImpl implementation) {
        implementations.remove(implementation);
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
