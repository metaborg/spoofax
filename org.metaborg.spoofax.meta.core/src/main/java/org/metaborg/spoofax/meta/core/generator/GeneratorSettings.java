package org.metaborg.spoofax.meta.core.generator;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgConstants;
import org.metaborg.core.build.CommonPaths;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.language.LanguageVersion;
import org.metaborg.core.project.ProjectException;
import org.metaborg.spoofax.meta.core.build.LangSpecCommonPaths;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfig;
import org.metaborg.spoofax.meta.core.config.StrategoFormat;

/**
 * Provides the values that can be used in a generator template, e.g. a Mustache template.
 */
public class GeneratorSettings {
    private final FileObject location;
    private final ISpoofaxLanguageSpecConfig config;
    private final CommonPaths paths;


    public GeneratorSettings(FileObject location, ISpoofaxLanguageSpecConfig config) throws ProjectException {
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
                throw new ProjectException("Invalid language contribution identifier: " + contributionIdentifier.id);
            }
            if(!LanguageIdentifier.validId(contributionIdentifier.name)) {
                throw new ProjectException("Invalid language contribution name: " + config.name());
            }
        }

        this.location = location;
        this.config = config;
        this.paths = new LangSpecCommonPaths(location);
    }


    private @Nullable String metaborgVersion;

    public String metaborgVersion() {
        return metaborgVersion != null && !metaborgVersion.isEmpty() ? metaborgVersion
            : MetaborgConstants.METABORG_VERSION;
    }

    public void setMetaborgVersion(@Nullable String metaborgVersion) throws ProjectException {
        if(metaborgVersion != null && !LanguageVersion.valid(metaborgVersion)) {
            throw new ProjectException("Invalid metaborg version: " + metaborgVersion);
        }
        this.metaborgVersion = metaborgVersion;
    }

    public String eclipseMetaborgVersion() {
        return metaborgVersion().replace("-SNAPSHOT", ".qualifier");
    }


    public String groupId() {
        return config.identifier().groupId;
    }

    public boolean generateGroupId() {
        return !groupId().equals(MetaborgConstants.METABORG_GROUP_ID);
    }

    public String id() {
        return config.identifier().id;
    }

    public String version() {
        return config.identifier().version.toString();
    }

    public boolean generateVersion() {
        return !version().equals(metaborgVersion());
    }

    public String eclipseVersion() {
        return version().replace("-SNAPSHOT", ".qualifier");
    }

    public String fullIdentifier() {
        return config.identifier().toString();
    }

    public String name() {
        return config.name();
    }

    public String ppName() {
        return config.prettyPrintLanguage();
    }

    public FileObject location() {
        return location;
    }


    public StrategoFormat format() {
        final StrategoFormat format = this.config.strFormat();
        return format != null ? format : StrategoFormat.ctree;
    }


    public String strategoName() {
        return config.strategoName();
    }

    public String javaName() {
        return config.javaName();
    }

    public String packageName() {
        return config.packageName();
    }

    public String strategiesPackagePath() {
        return paths.strJavaStratPkgPath(config.identifier().id);
    }
}
