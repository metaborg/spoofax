package org.metaborg.meta.core.config;

import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.metaborg.core.MetaborgConstants;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.project.config.Export;
import org.metaborg.core.project.config.Generate;
import org.metaborg.core.project.config.IConfig;
import org.metaborg.core.project.config.LanguageComponentConfig;

/**
 * An implementation of the {@link ILanguageSpecConfig} interface that is backed by an {@link ImmutableConfiguration}
 * object.
 */
public class LanguageSpecConfig extends LanguageComponentConfig
    implements ILanguageSpecConfig, IConfig {
    private static final long serialVersionUID = -7053551901853301773L;

    private static final String PROP_METABORG_VERSION = "metaborgVersion";


    public LanguageSpecConfig(HierarchicalConfiguration<ImmutableNode> config) {
        super(config);
    }

    protected LanguageSpecConfig(HierarchicalConfiguration<ImmutableNode> config, LanguageIdentifier id, String name,
        Collection<LanguageIdentifier> compileDeps, Collection<LanguageIdentifier> sourceDeps,
        Collection<LanguageIdentifier> javaDeps, Collection<LanguageContributionIdentifier> langContribs,
        Collection<Generate> generates, Collection<Export> exports, String metaborgVersion) {
        super(config, id, name, compileDeps, sourceDeps, javaDeps, langContribs, generates, exports);

        config.setProperty(PROP_METABORG_VERSION, metaborgVersion);
    }


    @Override public String metaborgVersion() {
        @Nullable final String value = this.config.getString(PROP_METABORG_VERSION);
        return value != null ? value : MetaborgConstants.METABORG_VERSION;
    }
}
