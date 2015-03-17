package org.metaborg.spoofax.core.language;

import java.util.Comparator;

import com.google.common.collect.ComparisonChain;

public class LanguageCreationDateComparator implements Comparator<ILanguage> {
    @Override public int compare(ILanguage l1, ILanguage l2) {
        // @formatter:off
        return ComparisonChain.start()
            .compare(l1.name(), l2.name())
            .compare(l1.version(), l2.version())
            .compare(l1.createdDate(), l2.createdDate())
            .compare(l1.location().getName(), l2.location().getName())
            .result();
        // @formatter:on
    }
}