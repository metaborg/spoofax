package org.metaborg.spoofax.core.processing;

import java.util.Collection;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.resource.IResourceChange;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class BuildInput {
    /**
     * Base location of the build.
     */
    public final FileObject location;

    /**
     * Resources that have changed.
     */
    public final Iterable<IResourceChange> resourceChanges;

    /**
     * Language build order. The inner iterable is a language group; languages that can be built together. The outer
     * iterable determines the build order.
     */
    public final Iterable<Iterable<ILanguage>> buildOrder;

    /**
     * Languages that can be used during the build. Derived from {@code buildOrder}.
     */
    public final Iterable<ILanguage> languages;


    public BuildInput(FileObject location, Iterable<IResourceChange> resourceChanges,
        Iterable<Iterable<ILanguage>> buildOrder) {
        this.location = location;
        this.resourceChanges = resourceChanges;
        this.buildOrder = buildOrder;

        final Collection<ILanguage> allLanguages = Lists.newLinkedList();
        for(Iterable<ILanguage> languageGroup : buildOrder) {
            Iterables.addAll(allLanguages, languageGroup);
        }
        this.languages = allLanguages;
    }

}
