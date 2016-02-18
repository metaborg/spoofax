package org.metaborg.spoofax.meta.core.project;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgConstants;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.language.LanguageVersion;
import org.metaborg.core.project.ProjectException;
import org.metaborg.spoofax.core.project.settings.StrategoFormat;
import org.metaborg.spoofax.generator.IGeneratorSettings;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfig;

/**
 * Provides the values that can be used in a generator template, e.g. a Mustache template.
 */
public class GeneratorSettings implements IGeneratorSettings {
    private final FileObject location;
    private final ISpoofaxLanguageSpecConfig config;
    private final ISpoofaxLanguageSpecPaths paths;


    public GeneratorSettings(ISpoofaxLanguageSpecConfig config, ISpoofaxLanguageSpecPaths paths)
        throws ProjectException {

        if(!config.identifier().valid()) {
            throw new ProjectException("Invalid language identifier: " + config.identifier());
        }
        if(!LanguageIdentifier.validId(config.name())) {
            throw new ProjectException("Invalid name: " + name());
        }
        for(LanguageIdentifier compileIdentifier : config.compileDeps()) {
            if(!compileIdentifier.valid()) {
                throw new ProjectException("Invalid compile dependency identifier: " + compileIdentifier);
            }
        }
        for(LanguageIdentifier runtimeIdentifier : config.sourceDeps()) {
            if(!runtimeIdentifier.valid()) {
                throw new ProjectException("Invalid runtime dependency identifier: " + runtimeIdentifier);
            }
        }
        for(LanguageContributionIdentifier contributionIdentifier : config.langContribs()) {
            if(!contributionIdentifier.id.valid()) {
                throw new ProjectException(
                    "Invalid language contribution identifier: " + contributionIdentifier.id);
            }
            if(!LanguageIdentifier.validId(contributionIdentifier.name)) {
                throw new ProjectException("Invalid language contribution name: " + config.name());
            }
        }

        this.paths = paths;
        this.location = this.paths.rootFolder();
        this.config = config;
    }


    private @Nullable String metaborgVersion;

    @Override public String metaborgVersion() {
        return metaborgVersion != null && !metaborgVersion.isEmpty() ? metaborgVersion
            : MetaborgConstants.METABORG_VERSION;
    }

    public void setMetaborgVersion(@Nullable String metaborgVersion) throws ProjectException {
        if(metaborgVersion != null && !LanguageVersion.valid(metaborgVersion)) {
            throw new ProjectException("Invalid metaborg version: " + metaborgVersion);
        }
        this.metaborgVersion = metaborgVersion;
    }

    @Override public String eclipseMetaborgVersion() {
        return metaborgVersion().replace("-SNAPSHOT", ".qualifier");
    }


    @Override public String groupId() {
        return this.config.identifier().groupId;
    }

    @Override public boolean generateGroupId() {
        return !groupId().equals(MetaborgConstants.METABORG_GROUP_ID);
    }

    @Override public String id() {
        return this.config.identifier().id;
    }

    @Override public String version() {
        return this.config.identifier().version.toString();
    }

    @Override public boolean generateVersion() {
        return !version().equals(metaborgVersion());
    }

    @Override public String eclipseVersion() {
        return version().replace("-SNAPSHOT", ".qualifier");
    }

    @Override public String name() {
        return this.config.name();
    }

    @Override public FileObject location() {
        return this.location;
    }


    @Override public StrategoFormat format() {
        final StrategoFormat format = this.config.format();
        return format != null ? format : StrategoFormat.ctree;
    }


    @Override public String strategoName() {
        return this.config.strategoName();
    }

    @Override public String javaName() {
        return this.config.javaName();
    }

    @Override public String packageName() {
        return this.config.packageName();
    }

    @Override public String packagePath() {
        return this.paths.packagePath();
    }
}
