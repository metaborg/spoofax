package org.metaborg.core.language;

import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;

public class Language implements ILanguage, ILanguageInternal {
    private final String name;
    private final Set<ILanguageImpl> impls;


    public Language(String name) {
        this.name = name;
        this.impls = Sets.newHashSet();
    }


    @Override public String name() {
        return name;
    }

    @Override public Iterable<ILanguageImpl> impls() {
        return impls;
    }

    @Override public @Nullable ILanguageImpl active() {
        ILanguageImpl active = null;
        for(ILanguageImpl impl : impls) {
            if(active == null || isGreater(impl, active)) {
                active = impl;
            }

        }
        return active;
    }

    private boolean isGreater(ILanguageImpl impl, ILanguageImpl other) {
        int compareVersion = impl.id().version.compareTo(other.id().version);
        if(compareVersion > 0 || (compareVersion == 0 && impl.sequenceId() > other.sequenceId())) {
            return true;
        }
        return false;
    }


    @Override public void add(ILanguageImpl implementation) {
        impls.add(implementation);
    }

    @Override public void remove(ILanguageImpl implementation) {
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
