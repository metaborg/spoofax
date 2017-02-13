package org.metaborg.core.language;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nullable;

import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class LanguageUtils {
    private static final ILogger logger = LoggerUtils.logger(LanguageUtils.class);


    public static Set<ILanguageImpl> toImpls(Iterable<? extends ILanguageComponent> components) {
        final Set<ILanguageImpl> impls = Sets.newHashSet();
        for(ILanguageComponent component : components) {
            Iterables.addAll(impls, component.contributesTo());
        }
        return impls;
    }
    
    public static Set<ILanguageComponent> toComponents(Iterable<? extends ILanguageImpl> impls) {
        final Set<ILanguageComponent> components = Sets.newHashSet();
        for(ILanguageImpl impl : impls) {
            Iterables.addAll(components, impl.components());
        }
        return components;
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
            final ILanguageImpl impl = language.activeImpl();
            if(impl == null) {
                logger.debug(
                    "Unexpected null for active implementation of language {}, skipping in active language implementations",
                    language);
                continue;
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
