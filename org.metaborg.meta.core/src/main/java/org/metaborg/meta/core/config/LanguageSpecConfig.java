package org.metaborg.meta.core.config;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.metaborg.core.config.IConfig;
import org.metaborg.core.config.IExportConfig;
import org.metaborg.core.config.IGenerateConfig;
import org.metaborg.core.config.LanguageComponentConfig;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageBuilder;

/**
 * An implementation of the {@link ILanguageSpecConfig} interface that is backed by an {@link ImmutableConfiguration}
 * object.
 */
public class LanguageSpecConfig extends LanguageComponentConfig implements ILanguageSpecConfig, IConfig {
    private static final String PROP_PARDONED_LANGUAGES = "pardonedLanguages";
    private static final String PROP_USE_BUILD_SYSTEM_SPEC = "build.useBuildSystemSpec";


    public LanguageSpecConfig(HierarchicalConfiguration<ImmutableNode> config) {
        super(config);
    }

    protected LanguageSpecConfig(HierarchicalConfiguration<ImmutableNode> config, String metaborgVersion,
        LanguageIdentifier id, String name, Collection<LanguageIdentifier> compileDeps,
        Collection<LanguageIdentifier> sourceDeps, Collection<LanguageIdentifier> javaDeps, boolean typesmart,
        Collection<LanguageContributionIdentifier> langContribs, Collection<IGenerateConfig> generates,
        Collection<IExportConfig> exports, Collection<String> pardonedLanguages, boolean useBuildSystemSpec) {
        super(config, metaborgVersion, id, name, compileDeps, sourceDeps, javaDeps, typesmart, langContribs, generates,
            exports);

        config.setProperty(PROP_PARDONED_LANGUAGES, pardonedLanguages);
        config.setProperty(PROP_USE_BUILD_SYSTEM_SPEC, useBuildSystemSpec);
    }



    @Override public Collection<String> pardonedLanguages() {
        return config.getList(String.class, PROP_PARDONED_LANGUAGES, Collections.<String>emptyList());
    }

    @Override public boolean useBuildSystemSpec() {
        return config.getBoolean(PROP_USE_BUILD_SYSTEM_SPEC, false);
    }

    public Collection<IMessage> validate(MessageBuilder mb) {
        final Collection<IMessage> messages = super.validate(mb);

        // TODO: validate pardonedLanguages

        return messages;
    }
}
