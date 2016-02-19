package org.metaborg.spoofax.meta.core.config;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nullable;

import org.metaborg.meta.core.config.LegacyLanguageSpecConfig;
import org.metaborg.spoofax.core.project.settings.LegacySpoofaxProjectSettings;
import org.metaborg.spoofax.core.project.settings.StrategoFormat;
import org.metaborg.util.cmd.Arguments;

import com.google.common.collect.Lists;

/**
 * This class is only used for the configuration system migration.
 */
@Deprecated
@SuppressWarnings("deprecation")
public class LegacySpoofaxLanguageSpecConfig extends LegacyLanguageSpecConfig implements ISpoofaxLanguageSpecConfig {
    private static final long serialVersionUID = -5913973186313150350L;

    final LegacySpoofaxProjectSettings spoofaxSettings;


    public LegacySpoofaxLanguageSpecConfig(LegacySpoofaxProjectSettings settings) {
        super(settings.settings());
        this.spoofaxSettings = settings;
    }


    @Override public Collection<String> pardonedLanguages() {
        return Lists.newArrayList(spoofaxSettings.pardonedLanguages());
    }

    @Override public StrategoFormat strFormat() {
        return spoofaxSettings.format();
    }

    @Override public String sdfName() {
        return spoofaxSettings.sdfName();
    }

    @Override public String metaSdfName() {
        return spoofaxSettings.metaSdfName();
    }

    @Override public Arguments sdfArgs() {
        return spoofaxSettings.sdfArgs();
    }

    @Override public Arguments strArgs() {
        return spoofaxSettings.strategoArgs();
    }

    @Nullable @Override public String sdfExternalDef() {
        return spoofaxSettings.externalDef();
    }

    @Nullable @Override public String strExternalJar() {
        return spoofaxSettings.externalJar();
    }

    @Nullable @Override public String strExternalJarFlags() {
        return spoofaxSettings.externalJarFlags();
    }

    @Override public String strategoName() {
        return spoofaxSettings.strategoName();
    }

    @Override public String javaName() {
        return spoofaxSettings.javaName();
    }

    @Override public String packageName() {
        return spoofaxSettings.packageName();
    }

    @Override public String strategiesPackageName() {
        return spoofaxSettings.strategiesPackageName();
    }

    @Override public String esvName() {
        return spoofaxSettings.esvName();
    }

    @Override public Collection<IBuildStepConfig> buildSteps() {
        return Collections.emptyList();
    }
}
