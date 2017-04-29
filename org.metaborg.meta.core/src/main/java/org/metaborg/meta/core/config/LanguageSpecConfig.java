package org.metaborg.meta.core.config;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nullable;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.metaborg.core.config.IConfig;
import org.metaborg.core.config.IGenerateConfig;
import org.metaborg.core.config.IExportConfig;
import org.metaborg.core.config.LanguageComponentConfig;
import org.metaborg.core.config.ProjectConfig;
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


    public LanguageSpecConfig(HierarchicalConfiguration<ImmutableNode> config, ProjectConfig projectConfig) {
        super(config, projectConfig);
    }

    protected LanguageSpecConfig(HierarchicalConfiguration<ImmutableNode> config, ProjectConfig projectConfig,
            @Nullable LanguageIdentifier id, @Nullable String name, @Nullable Boolean sdfEnabled,
            @Nullable String parseTable, @Nullable String completionsParseTable,
            @Nullable Collection<LanguageContributionIdentifier> langContribs,
            @Nullable Collection<IGenerateConfig> generates, @Nullable Collection<IExportConfig> exports,
            @Nullable Collection<String> pardonedLanguages, @Nullable Boolean useBuildSystemSpec) {
        super(config, projectConfig, id, name, sdfEnabled, parseTable, completionsParseTable, langContribs, generates,
                exports);

        if(pardonedLanguages != null) {
            config.setProperty(PROP_PARDONED_LANGUAGES, pardonedLanguages);
        }
        if(useBuildSystemSpec != null) {
            config.setProperty(PROP_USE_BUILD_SYSTEM_SPEC, useBuildSystemSpec);
        }
    }


    @Override public Collection<String> pardonedLanguages() {
        return config.getList(String.class, PROP_PARDONED_LANGUAGES, Collections.<String>emptyList());
    }

    @Override public boolean useBuildSystemSpec() {
        return config.getBoolean(PROP_USE_BUILD_SYSTEM_SPEC, false);
    }


    @Override public Collection<IMessage> validate(MessageBuilder mb) {
        final Collection<IMessage> messages = super.validate(mb);

        // TODO: validate pardonedLanguages

        return messages;
    }
}
