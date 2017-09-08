package org.metaborg.spoofax.meta.core.generator.general;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgConstants;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.language.LanguageVersion;
import org.metaborg.core.project.NameUtil;
import org.metaborg.core.project.ProjectException;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfig;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfigBuilder;
import org.metaborg.spoofax.meta.core.generator.GeneratorSettings;
import org.metaborg.util.prompt.Prompter;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class LangSpecGeneratorSettingsBuilder {
    public static final String standardGroupId = "org.example";
    public static final String standardVersionString = "0.1.0-SNAPSHOT";
    public static final AnalysisType standardAnalysisType = AnalysisType.NaBL2;
    public static final SyntaxType standardSyntaxType = SyntaxType.SDF3;
    public static final String standardMetaborgVersion = MetaborgConstants.METABORG_VERSION;

    private @Nullable String groupId;
    private @Nullable String id;
    private @Nullable LanguageVersion version;
    private @Nullable String name;
    private @Nullable SyntaxType syntaxType;
    private @Nullable AnalysisType analysisType;
    private @Nullable Collection<String> extensions;
    private @Nullable String metaborgVersion;

    private @Nullable String defaultGroupId = standardGroupId;
    private @Nullable LanguageVersion defaultVersion = LanguageVersion.parse(standardVersionString);
    private @Nullable AnalysisType defaultAnalysisType = standardAnalysisType;
    private @Nullable SyntaxType defaultSyntaxType = standardSyntaxType;
    private @Nullable String defaultMetaborgVersion = standardMetaborgVersion;


    public LangSpecGeneratorSettingsBuilder withGroupId(@Nullable String groupId) {
        this.groupId = groupId;
        return this;
    }

    public LangSpecGeneratorSettingsBuilder withId(@Nullable String id) {
        this.id = id;
        return this;
    }

    public LangSpecGeneratorSettingsBuilder withVersion(@Nullable LanguageVersion version) {
        this.version = version;
        return this;
    }

    public LangSpecGeneratorSettingsBuilder withName(@Nullable String name) {
        this.name = name;
        return this;
    }

    public LangSpecGeneratorSettingsBuilder withExtensions(@Nullable Collection<String> extensions) {
        this.extensions = extensions;
        return this;
    }

    public LangSpecGeneratorSettingsBuilder withoutExtensions() {
        withExtensions(Lists.<String>newArrayList());
        return this;
    }

    public LangSpecGeneratorSettingsBuilder withSyntaxType(@Nullable SyntaxType syntaxType) {
        this.syntaxType = syntaxType;
        return this;
    }

    public LangSpecGeneratorSettingsBuilder withAnalysisType(@Nullable AnalysisType analysisType) {
        this.analysisType = analysisType;
        return this;
    }

    public LangSpecGeneratorSettingsBuilder withMetaborgVersion(@Nullable String metaborgVersion) {
        this.metaborgVersion = metaborgVersion;
        return this;
    }

    public LangSpecGeneratorSettingsBuilder withConfig(ISpoofaxLanguageSpecConfig config) {
        withGroupId(config.identifier().groupId);
        withId(config.identifier().id);
        withVersion(config.identifier().version);
        withName(config.name());
        withMetaborgVersion(config.metaborgVersion());
        return this;
    }


    public LangSpecGeneratorSettingsBuilder withDefaultGroupId(@Nullable String defaultGroupId) {
        this.defaultGroupId = defaultGroupId;
        return this;
    }

    public LangSpecGeneratorSettingsBuilder withDefaultVersion(@Nullable String defaultVersionString) {
        this.defaultVersion = (defaultVersionString != null && LanguageVersion.valid(defaultVersionString))
            ? LanguageVersion.parse(defaultVersionString) : null;
        return this;
    }

    public LangSpecGeneratorSettingsBuilder withDefaultAnalysisType(@Nullable AnalysisType defaultAnalysisType) {
        this.defaultAnalysisType = defaultAnalysisType;
        return this;
    }

    public LangSpecGeneratorSettingsBuilder withDefaultSyntaxType(@Nullable SyntaxType defaultSyntaxType) {
        this.defaultSyntaxType = defaultSyntaxType;
        return this;
    }

    public LangSpecGeneratorSettingsBuilder withDefaultMetaborgVersion(@Nullable String defaultMetaborgVersion) {
        this.defaultMetaborgVersion = defaultMetaborgVersion;
        return this;
    }


    public LangSpecGeneratorSettingsBuilder configureFromPrompt(Prompter prompter) {
        while(groupId == null || groupId.isEmpty()) {
            groupId = prompter.readString("Group ID [" + defaultGroupId + "]").trim();
            if(groupId.isEmpty()) {
                groupId = defaultGroupId;
            }
            if(!LanguageIdentifier.validId(groupId)) {
                System.err.println("Please enter a valid id");
                groupId = null;
            }
        }

        while(name == null || name.isEmpty()) {
            name = prompter.readString("Name").trim();
            if(!LanguageIdentifier.validId(name)) {
                System.err.println("Please enter a valid name");
                name = null;
            }
        }

        final String defaultId = name.toLowerCase();
        while(id == null || id.isEmpty()) {
            id = prompter.readString("Id [" + defaultId + "]").trim();
            if(id.isEmpty()) {
                id = defaultId;
            }
            id = id.trim();
            if(!LanguageIdentifier.validId(id)) {
                System.err.println("Please enter a valid id");
                id = null;
            }
        }

        while(version == null) {
            final String versionString = prompter
                .readString(
                    "Version" + (defaultVersion != null ? " [" + defaultVersion + "]" : " (e.g. '1.0.0-SNAPSHOT')"))
                .trim();
            if(versionString.isEmpty()) {
                version = defaultVersion;
            } else {
                if(versionString.trim().isEmpty() || !LanguageVersion.valid(versionString.trim())) {
                    System.err.println("Please enter a valid version");
                    version = null;
                } else {
                    version = LanguageVersion.parse(versionString.trim());
                }
            }
        }

        final String defaultExt = name.toLowerCase().substring(0, Math.min(name.length(), 3));
        while(extensions == null) {
            final String extensionString =
                prompter.readString("File extensions (space separated) [" + defaultExt + "]").trim();
            if(extensionString.isEmpty()) {
                extensions = Lists.newArrayList(defaultExt);
            } else {
                extensions = Lists.newArrayList(extensionString.split("\\s+"));
                if(extensions.isEmpty()) {
                    System.err.println("Please enter valid file extensions.");
                    extensions = null;
                } else {
                    for(String ext : extensions) {
                        if(!NameUtil.isValidFileExtension(ext)) {
                            System.err.println("Please enter valid file extensions. Invalid: " + ext);
                            extensions = null;
                        }
                    }
                }
            }
        }

        while(syntaxType == null) {
            final String syntaxTypeString =
                prompter.readString("Type of syntax (one of " + Joiner.on(", ").join(SyntaxType.values()) + ")"
                    + (defaultSyntaxType != null ? " [" + defaultSyntaxType + "]" : "")).trim();
            if(syntaxTypeString.isEmpty()) {
                syntaxType = defaultSyntaxType;
            } else {
                try {
                    syntaxType = SyntaxType.valueOf(syntaxTypeString.trim());
                } catch(IllegalArgumentException e) {
                    System.err.println("Please enter a valid syntax type");
                    syntaxType = null;
                }
            }
        }

        while(analysisType == null) {
            final String analysisTypeString =
                prompter.readString("Type of analysis (one of " + Joiner.on(", ").join(AnalysisType.values()) + ")"
                    + (defaultAnalysisType != null ? " [" + defaultAnalysisType + "]" : "")).trim();
            if(analysisTypeString.isEmpty()) {
                analysisType = defaultAnalysisType;
            } else {
                try {
                    analysisType = AnalysisType.valueOf(analysisTypeString.trim());
                } catch(IllegalArgumentException e) {
                    System.err.println("Please enter a valid analysis type");
                    analysisType = null;
                }
            }
        }

        while(metaborgVersion == null || metaborgVersion.isEmpty()) {
            metaborgVersion =
                prompter.readString("Version for MetaBorg artifacts [" + defaultMetaborgVersion + "]").trim();
            if(metaborgVersion.isEmpty()) {
                metaborgVersion = defaultMetaborgVersion;
            }
        }

        return this;
    }


    public LangSpecGeneratorSettings build(FileObject projectLocation,
        final ISpoofaxLanguageSpecConfigBuilder languageSpecConfigBuilder) throws ProjectException {
        if(!canBuild()) {
            throw new ProjectException(
                "Cannot build incomplete configuration, missing " + Joiner.on(", ").join(stillMissing()));
        }

        if(groupId == null) {
            groupId = defaultGroupId;
        }
        if(version == null) {
            version = defaultVersion;
        }
        if(syntaxType == null) {
            syntaxType = defaultSyntaxType;
        }
        if(analysisType == null) {
            analysisType = defaultAnalysisType;
        }
        if(metaborgVersion == null) {
            metaborgVersion = defaultMetaborgVersion;
        }

        final LanguageIdentifier identifier = new LanguageIdentifier(groupId, id, version);

        final ISpoofaxLanguageSpecConfig config =
            languageSpecConfigBuilder.withIdentifier(identifier).withName(name).build(projectLocation);
        final GeneratorSettings generatorSettings = new GeneratorSettings(projectLocation, config, analysisType);
        generatorSettings.setMetaborgVersion(metaborgVersion);

        return new LangSpecGeneratorSettings(generatorSettings, extensions, syntaxType);
    }

    public boolean canBuild() {
        return (groupId != null || defaultGroupId != null) && id != null && (version != null || defaultVersion != null)
            && name != null && extensions != null && (syntaxType != null || defaultSyntaxType != null)
            && (analysisType != null || defaultAnalysisType != null)
            && (metaborgVersion != null || defaultMetaborgVersion != null);
    }

    public Iterable<String> stillMissing() {
        List<String> missing = Lists.newArrayList();
        if(groupId == null && defaultGroupId == null) {
            missing.add("groupId");
        }
        if(id == null) {
            missing.add("id");
        }
        if(version == null && defaultVersion == null) {
            missing.add("version");
        }
        if(name == null) {
            missing.add("name");
        }
        if(extensions == null) {
            missing.add("extensions");
        }
        if(syntaxType == null && defaultSyntaxType == null) {
            missing.add("syntax type");
        }
        if(analysisType == null && defaultAnalysisType == null) {
            missing.add("analysis type");
        }
        if(metaborgVersion == null && defaultMetaborgVersion == null) {
            missing.add("metaborg version");
        }
        return missing;
    }

    public boolean isComplete() {
        return groupId != null && id != null && version != null && name != null && extensions != null
            && syntaxType != null && analysisType != null && metaborgVersion != null;
    }

    public Iterable<String> stillRequired() {
        List<String> missing = Lists.newArrayList();
        if(groupId == null) {
            missing.add("groupId");
        }
        if(id == null) {
            missing.add("id");
        }
        if(version == null) {
            missing.add("version");
        }
        if(name == null) {
            missing.add("name");
        }
        if(extensions == null) {
            missing.add("extensions");
        }
        if(syntaxType == null) {
            missing.add("syntax type");
        }
        if(analysisType == null) {
            missing.add("analysis type");
        }
        if(metaborgVersion == null) {
            missing.add("metaborg version");
        }
        return missing;
    }
}
