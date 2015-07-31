package org.metaborg.spoofax.generator.project;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.project.NameUtil;
import org.metaborg.core.project.ProjectException;
import org.metaborg.core.project.settings.IProjectSettings;
import org.metaborg.spoofax.core.SpoofaxProjectConstants;
import org.metaborg.spoofax.core.project.settings.Format;
import org.metaborg.spoofax.core.project.settings.SpoofaxProjectSettings;

public class GeneratorProjectSettings {
    private final SpoofaxProjectSettings settings;


    public GeneratorProjectSettings(SpoofaxProjectSettings settings) throws ProjectException {
        final IProjectSettings metaborgSettings = settings.settings();
        if(!NameUtil.isValidLanguageIdentifier(metaborgSettings.identifier())) {
            throw new ProjectException("Invalid language identifier: " + metaborgSettings.identifier());
        }
        if(!NameUtil.isValidName(metaborgSettings.name())) {
            throw new ProjectException("Invalid name: " + name());
        }
        for(LanguageIdentifier compileIdentifier : metaborgSettings.compileDependencies()) {
            if(!NameUtil.isValidLanguageIdentifier(compileIdentifier)) {
                throw new ProjectException("Invalid compile dependency identifier: " + compileIdentifier);
            }
        }
        for(LanguageIdentifier runtimeIdentifier : metaborgSettings.runtimeDependencies()) {
            if(!NameUtil.isValidLanguageIdentifier(runtimeIdentifier)) {
                throw new ProjectException("Invalid runtime dependency identifier: " + runtimeIdentifier);
            }
        }
        for(LanguageContributionIdentifier contributionIdentifier : metaborgSettings.languageContributions()) {
            if(!NameUtil.isValidLanguageIdentifier(contributionIdentifier.identifier)) {
                throw new ProjectException("Invalid language contribution identifier: "
                    + contributionIdentifier.identifier);
            }
            if(!NameUtil.isValidName(contributionIdentifier.name)) {
                throw new ProjectException("Invalid language contribution name: " + metaborgSettings.name());
            }
        }
        this.settings = settings;
    }


    private @Nullable String metaborgVersion;

    public void setMetaborgVersion(String metaborgVersion) throws ProjectException {
        if(metaborgVersion != null && !NameUtil.isValidVersion(metaborgVersion)) {
            throw new ProjectException("Invalid metaborg version: " + metaborgVersion);
        }
        this.metaborgVersion = metaborgVersion;
    }

    public String metaborgVersion() {
        return metaborgVersion != null && !metaborgVersion.isEmpty() ? metaborgVersion
            : SpoofaxProjectConstants.METABORG_VERSION;
    }

    public String eclipseMetaborgVersion() {
        return metaborgVersion().replace("-SNAPSHOT", ".qualifier");
    }


    public String groupId() {
        return settings.settings().identifier().groupId;
    }

    public boolean generateGroupId() {
        return !groupId().equals(SpoofaxProjectConstants.METABORG_GROUP_ID);
    }

    public String id() {
        return settings.settings().identifier().id;
    }

    public String version() {
        return settings.settings().identifier().version.toString();
    }

    public boolean generateVersion() {
        return !version().equals(metaborgVersion());
    }

    public String eclipseVersion() {
        return version().replace("-SNAPSHOT", ".qualifier");
    }

    public String name() {
        return settings.settings().name();
    }

    public FileObject location() {
        return settings.location();
    }


    public Format format() {
        final Format format = settings.format();
        return format != null ? format : Format.ctree;
    }


    public String strategoName() {
        return settings.strategoName();
    }

    public String javaName() {
        return settings.javaName();
    }

    public String packageName() {
        return settings.packageName();
    }

    public String packagePath() {
        return settings.packagePath();
    }
}
