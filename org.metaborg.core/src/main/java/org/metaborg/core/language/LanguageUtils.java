package org.metaborg.core.language;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class LanguageUtils {
    public static Set<ILanguageImpl> toImpls(Iterable<? extends ILanguageComponent> components) {
        final Set<ILanguageImpl> impls = Sets.newHashSet();
        for(ILanguageComponent component : components) {
            Iterables.addAll(impls, component.contributesTo());
        }
        return impls;
    }

    public static @Nullable ILanguageImpl active(Iterable<? extends ILanguageImpl> impls) {
        ILanguageImpl active = null;
        for(ILanguageImpl impl : impls) {
            if(active == null || isGreater(impl, active)) {
                active = impl;
            }

        }
        return active;
    }

    public static Iterable<ILanguageImpl> allActiveImpls(ILanguageService languageService) {
        final Iterable<? extends ILanguage> languages = languageService.getAllLanguages();
        final Collection<ILanguageImpl> activeImpls = Lists.newLinkedList();
        for(ILanguage language : languages) {
            @Nullable final ILanguageImpl impl = language.activeImpl();
            if (impl == null) {
                throw new IllegalStateException("Unexpected null for active implementation of language " + language);
            }
            activeImpls.add(impl);
        }
        return activeImpls;
    }

    public static Iterable<ILanguageComponent> allActiveComponents(ILanguageService languageService) {
        final Iterable<ILanguageImpl> activeImpls = allActiveImpls(languageService);
        final Collection<ILanguageComponent> activeComponents = Lists.newLinkedList();
        for(ILanguageImpl impl : activeImpls) {
            Iterables.addAll(activeComponents, impl.components());
        }
        return activeComponents;
    }


    private static boolean isGreater(ILanguageImpl impl, ILanguageImpl other) {
        int compareVersion = impl.id().version.compareTo(other.id().version);
        if(compareVersion > 0 || (compareVersion == 0 && impl.sequenceId() > other.sequenceId())) {
            return true;
        }
        return false;
    }
}
