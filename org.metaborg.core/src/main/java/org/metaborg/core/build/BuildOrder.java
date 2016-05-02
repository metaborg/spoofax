package org.metaborg.core.build;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.config.IGenerateConfig;
import org.metaborg.core.config.ILanguageImplConfig;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.util.collection.BiLinkedHashMultimap;
import org.metaborg.util.collection.BiSetMultimap;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Language build order calculation.
 */
public class BuildOrder {
    private final Iterable<ILanguageImpl> languages;
    private final List<ILanguageImpl> buildOrder;


    /**
     * Creates a build order for given languages, using their dependencies.
     * 
     * @param languages
     *            Languages to create a build order for.
     * @throws MetaborgRuntimeException
     *             When there is a cyclic dependency between languages.
     */
    public BuildOrder(Iterable<ILanguageImpl> languages) throws MetaborgRuntimeException {
        final int size = Iterables.size(languages);
        this.languages = languages;
        this.buildOrder = Lists.newArrayListWithCapacity(size);
        if(size == 0) {
            return;
        }

        // We're only interested in dependencies between given languages, create a lookup map for them.
        final Map<String, ILanguageImpl> lookup = Maps.newHashMap();
        for(ILanguageImpl language : languages) {
            lookup.put(language.belongsTo().name(), language);
        }

        // Create dependency graph and set of languages that do not generate anything.
        final Set<ILanguageImpl> noGenerates = Sets.newHashSet(languages);
        final BiSetMultimap<ILanguageImpl, ILanguageImpl> generates = BiLinkedHashMultimap.create();
        for(ILanguageImpl source : languages) {
            final ILanguageImplConfig config = source.config();
            boolean hasGenerates = false;
            for(IGenerateConfig generate : config.generates()) {
                final ILanguageImpl target = lookup.get(generate.languageName());
                if(target != null) {
                    generates.put(source, target);
                    hasGenerates = true;
                }
            }
            if(hasGenerates) {
                noGenerates.remove(source);
            }
        }
        if(noGenerates.isEmpty()) {
            throw new MetaborgRuntimeException("Build order is cyclic");
        }

        // Execute Kahn's topological sort algorithm to create a build order and check for cycles.
        while(!noGenerates.isEmpty()) {
            final ILanguageImpl impl = Iterables.get(noGenerates, 0);
            noGenerates.remove(impl);
            buildOrder.add(impl);

            // Copy collection to prevent ConcurrentModificationException by removal during iteration.
            final Iterable<ILanguageImpl> allGeneratesFrom = Lists.newArrayList(generates.getInverse(impl));
            for(ILanguageImpl generatesFrom : allGeneratesFrom) {
                generates.remove(generatesFrom, impl);
                if(!generates.containsKey(generatesFrom)) {
                    noGenerates.add(generatesFrom);
                }
            }
        }
        if(!generates.isEmpty()) {
            throw new MetaborgRuntimeException("Build order is cyclic");
        }
    }

    /**
     * @return Build order.
     */
    public Iterable<ILanguageImpl> buildOrder() {
        return buildOrder;
    }

    /**
     * @return Languages in this build order, in the same order that they were passed in the constructor.
     */
    public Iterable<ILanguageImpl> languages() {
        return languages;
    }
}
