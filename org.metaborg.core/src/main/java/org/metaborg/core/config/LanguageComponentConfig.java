package org.metaborg.core.config;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageBuilder;

import com.google.common.collect.Lists;

/**
 * An implementation of the {@link ILanguageComponentConfig} interface that is backed by an
 * {@link ImmutableConfiguration} object.
 */
public class LanguageComponentConfig extends AConfig implements ILanguageComponentConfig, IConfig {
    private static final String PROP_IDENTIFIER = "id";
    private static final String PROP_NAME = "name";
    private static final String PROP_LANGUAGE_CONTRIBUTIONS = "contributions";
    private static final String PROP_GENERATES = "generates";
    private static final String PROP_EXPORTS = "exports";

    private static final String PROP_SDF_ENABLED = "language.sdf.enabled";
    private static final String PROP_SDF_PARSE_TABLE = "language.sdf.parse-table";
    private static final String PROP_SDF_COMPLETION_PARSE_TABLE = "language.sdf.completion-parse-table";

    private final ProjectConfig projectConfig;

    public LanguageComponentConfig(HierarchicalConfiguration<ImmutableNode> config, ProjectConfig projectConfig) {
        super(config);
        this.projectConfig = projectConfig;
    }

    protected LanguageComponentConfig(HierarchicalConfiguration<ImmutableNode> config, ProjectConfig projectConfig,
            @Nullable LanguageIdentifier identifier, @Nullable String name, @Nullable Boolean sdfEnabled,
            @Nullable String parseTable, @Nullable String completionParseTable,
            @Nullable Collection<LanguageContributionIdentifier> langContribs,
            @Nullable Collection<IGenerateConfig> generates, @Nullable Collection<IExportConfig> exports) {
        super(config);
        this.projectConfig = projectConfig;

        if(sdfEnabled != null) {
            config.setProperty(PROP_SDF_ENABLED, sdfEnabled);
        }
        if(parseTable != null) {
            config.setProperty(PROP_SDF_PARSE_TABLE, parseTable);
        }
        if(completionParseTable != null) {
            config.setProperty(PROP_SDF_COMPLETION_PARSE_TABLE, completionParseTable);
        }

        if(name != null) {
            config.setProperty(PROP_NAME, name);
        }
        if(identifier != null) {
            config.setProperty(PROP_IDENTIFIER, identifier);
        }
        if(langContribs != null) {
            config.setProperty(PROP_LANGUAGE_CONTRIBUTIONS, langContribs);
        }
        if(generates != null) {
            config.setProperty(PROP_GENERATES, generates);
        }
        if(exports != null) {
            config.setProperty(PROP_EXPORTS, exports);
        }
    }


    @Override public HierarchicalConfiguration<ImmutableNode> getConfig() {
        return projectConfig.getConfig();
    }


    @Override public String metaborgVersion() {
        return projectConfig.metaborgVersion();
    }

    @Override public Collection<ISourceConfig> sources() {
        return projectConfig.sources();
    }

    @Override public Collection<LanguageIdentifier> compileDeps() {
        return projectConfig.compileDeps();
    }

    @Override public Collection<LanguageIdentifier> sourceDeps() {
        return projectConfig.sourceDeps();
    }

    @Override public Collection<LanguageIdentifier> javaDeps() {
        return projectConfig.javaDeps();
    }


    @Override public LanguageIdentifier identifier() {
        return config.get(LanguageIdentifier.class, PROP_IDENTIFIER);
    }

    @Override public String name() {
        return config.getString(PROP_NAME);
    }

    @Override public Collection<LanguageContributionIdentifier> langContribs() {
        final List<HierarchicalConfiguration<ImmutableNode>> langContribConfigs =
                config.configurationsAt(PROP_LANGUAGE_CONTRIBUTIONS);
        final List<LanguageContributionIdentifier> langContribs =
                Lists.newArrayListWithCapacity(langContribConfigs.size());
        for(HierarchicalConfiguration<ImmutableNode> langContribConfig : langContribConfigs) {
            // HACK: for some reason get(LanguageIdentifier.class, "id") does not work here, it cannot convert to a
            // language identifier, do manually instead.
            final String idString = langContribConfig.getString("id");
            final LanguageIdentifier id = LanguageIdentifier.parse(idString);
            final String name = langContribConfig.getString("name");
            langContribs.add(new LanguageContributionIdentifier(id, name));
        }
        return langContribs;
    }

    @Override public Collection<IGenerateConfig> generates() {
        final List<HierarchicalConfiguration<ImmutableNode>> generateConfigs = config.configurationsAt(PROP_GENERATES);
        final List<IGenerateConfig> generates = Lists.newArrayListWithCapacity(generateConfigs.size());
        for(HierarchicalConfiguration<ImmutableNode> generateConfig : generateConfigs) {
            final List<String> languages = generateConfig.getList(String.class, "language", Collections.emptyList());
            final String directory = generateConfig.getString("directory");
            if(directory != null) {
                for(String language : languages) {
                    generates.add(new GenerateConfig(language, directory));
                }
            }
        }
        return generates;
    }

    @Override public Collection<IExportConfig> exports() {
        final List<HierarchicalConfiguration<ImmutableNode>> exportConfigs =
                config.configurationsAt(PROP_EXPORTS, false);
        final List<IExportConfig> exports = Lists.newArrayListWithCapacity(exportConfigs.size());
        for(HierarchicalConfiguration<ImmutableNode> exportConfig : exportConfigs) {
            final List<String> languages = exportConfig.getList(String.class, "language");
            final String directory = exportConfig.getString("directory");
            final String file = exportConfig.getString("file");
            final List<String> includes = exportConfig.getList(String.class, "includes", Lists.newArrayList());
            final List<String> excludes = exportConfig.getList(String.class, "excludes", Lists.newArrayList());
            if(languages != null) {
                if(directory != null) {
                    for(String language : languages) {
                        exports.add(new LangDirExport(language, directory, includes, excludes));
                    }
                } else if(file != null) {
                    for(String language : languages) {
                        exports.add(new LangFileExport(language, file));
                    }
                }
            } else {
                if(directory != null) {
                    exports.add(new ResourceExport(directory, includes, excludes));
                }
            }
        }
        return exports;
    }


    public Collection<IMessage> validate(MessageBuilder mb) {
        final Collection<IMessage> messages = projectConfig.validate(mb);
        final String idStr = config.getString(PROP_IDENTIFIER);
        if(idStr == null) {
            messages.add(mb.withMessage("Field 'id' must be set").build());
        } else {
            try {
                LanguageIdentifier.parseFull(idStr);
            } catch(IllegalArgumentException e) {
                messages.add(mb.withMessage("Invalid identifier in 'id' field. " + e.getMessage()).build());
            }
        }

        final String name = config.getString(PROP_NAME);
        if(name == null) {
            messages.add(mb.withMessage("Field 'name' must be set").build());
        } else {
            if(!LanguageIdentifier.validId(name)) {
                messages.add(mb
                        .withMessage("Field 'name' contains invalid characters, " + LanguageIdentifier.errorDescription)
                        .build());
            }
        }

        // TODO: validate language contributions
        // TODO: validate generates
        // TODO: validate exports

        return messages;
    }

    @Override public Boolean sdfEnabled() {
        return this.config.getBoolean(PROP_SDF_ENABLED, true);
    }

    @Override public String parseTable() {
        final String value = this.config.getString(PROP_SDF_PARSE_TABLE);
        return value != null ? value : "target/metaborg/sdf.tbl";
    }

    @Override public String completionsParseTable() {
        final String value = this.config.getString(PROP_SDF_COMPLETION_PARSE_TABLE);
        return value != null ? value : "target/metaborg/sdf-completions.tbl";
    }
}
