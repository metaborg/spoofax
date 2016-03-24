package org.metaborg.spoofax.meta.core.generator.language;

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

public class GeneratorSettingsBuilder {

    private @Nullable String groupId;
    private @Nullable String id;
    private @Nullable LanguageVersion version;
    private @Nullable String name;
    private @Nullable AnalysisType analysisType;
    private @Nullable String[] extensions;
    private @Nullable String metaborgVersion;

    private @Nullable LanguageVersion defaultVersion;
    private @Nullable AnalysisType defaultAnalysisType;

    public GeneratorSettingsBuilder withGroupId(final @Nullable String groupId) {
        this.groupId = groupId;
        return this;
    }

    public GeneratorSettingsBuilder withId(final @Nullable String id) {
        this.id = id;
        return this;
    }

    public GeneratorSettingsBuilder withVersion(final @Nullable LanguageVersion version) {
        this.version = version;
        return this;
    }

    public GeneratorSettingsBuilder withName(final @Nullable String name) {
        this.name = name;
        return this;
    }

    public GeneratorSettingsBuilder withExtensions(final @Nullable String[] extensions) {
        this.extensions = extensions;
        return this;
    }

    public GeneratorSettingsBuilder withAnalysisType(final @Nullable AnalysisType analysisType) {
        this.analysisType = analysisType;
        return this;
    }

    public GeneratorSettingsBuilder withMetaborgVersion(final @Nullable String metaborgVersion) {
        this.metaborgVersion = metaborgVersion;
        return this;
    }

    public GeneratorSettingsBuilder withDefaultVersion(final @Nullable String defaultVersionString) {
        this.defaultVersion = (defaultVersionString != null && LanguageVersion.valid(defaultVersionString))
            ? LanguageVersion.parse(defaultVersionString) : null;
        return this;
    }

    public GeneratorSettingsBuilder withDefaultAnalysisType(final @Nullable AnalysisType defaultAnalysisType) {
        this.defaultAnalysisType = defaultAnalysisType;
        return this;
    }


    public GeneratorSettingsBuilder configureFromPrompt(final Prompter prompter) {
        while(groupId == null || groupId.isEmpty()) {
            groupId = prompter.readString("Group ID (e.g. 'org.metaborg')").trim();
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
            id = prompter.readString("Id [" + defaultId + "]");
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
            final String versionString = prompter.readString(
                "Version" + (defaultVersion != null ? " [" + defaultVersion + "]" : " (e.g. '1.0.0-SNAPSHOT')"));
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

        String defaultExt = name.toLowerCase().substring(0, Math.min(name.length(), 3));
        while(extensions == null) {
            final String extensionString =
                prompter.readString("File extensions (space separated) [" + defaultExt + "]");
            if(extensionString.isEmpty()) {
                extensions = new String[] { defaultExt };
            } else {
                extensions = extensionString.split("\\s+");
                if(extensions.length == 0) {
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

        while(analysisType == null) {
            final String analysisTypeString =
                prompter.readString("Type of Analysis (one of " + Joiner.on(", ").join(AnalysisType.values()) + ")"
                    + (defaultAnalysisType != null ? " [" + defaultAnalysisType + "]" : ""));
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
                prompter.readString("Version for MetaBorg artifacts [" + MetaborgConstants.METABORG_VERSION + "]");
            if(metaborgVersion.isEmpty()) {
                metaborgVersion = MetaborgConstants.METABORG_VERSION;
            }
            metaborgVersion = metaborgVersion.trim();
        }

        return this;
    }


    public FullGeneratorSettings build(final FileObject projectLocation,
        final ISpoofaxLanguageSpecConfigBuilder languageSpecConfigBuilder) throws ProjectException {
        if(!canBuild()) {
            throw new ProjectException("Cannot build incomplete configuration.");
        }

        final LanguageIdentifier identifier = new LanguageIdentifier(groupId, id, version);

        final ISpoofaxLanguageSpecConfig config =
            languageSpecConfigBuilder.withIdentifier(identifier).withName(name).build(projectLocation);
        final GeneratorSettings generatorSettings = new GeneratorSettings(projectLocation, config);
        generatorSettings.setMetaborgVersion(metaborgVersion);

        return new FullGeneratorSettings(generatorSettings, extensions, analysisType);
    }

    public boolean canBuild() {
        return groupId != null && id != null && version != null && name != null && extensions != null
            && analysisType != null;
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
        if(analysisType == null) {
            missing.add("analysisType");
        }
        return missing;
    }

    public boolean isComplete() {
        return groupId != null && id != null && version != null && name != null && extensions != null
            && analysisType != null && metaborgVersion != null;
    }

    public static class FullGeneratorSettings {
        public final GeneratorSettings generatorSettings;
        public final String[] extensions;
        public final AnalysisType analysisType;

        public FullGeneratorSettings(GeneratorSettings generatorSettings, String[] extensions,
            AnalysisType analysisType) {
            this.generatorSettings = generatorSettings;
            this.extensions = extensions;
            this.analysisType = analysisType;
        }
    }

}
