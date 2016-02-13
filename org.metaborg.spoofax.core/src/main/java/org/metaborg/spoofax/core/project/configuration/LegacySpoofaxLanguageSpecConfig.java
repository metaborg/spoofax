package org.metaborg.spoofax.core.project.configuration;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import org.metaborg.core.project.configuration.LegacyLanguageSpecConfig;
import org.metaborg.spoofax.core.project.settings.Format;
import org.metaborg.spoofax.core.project.settings.LegacySpoofaxProjectSettings;
import org.metaborg.util.cmd.Arguments;

import java.util.Collection;

/**
 * This class is only used for the configuration system migration.
 */
@Deprecated
@SuppressWarnings("deprecation")
public class LegacySpoofaxLanguageSpecConfig extends LegacyLanguageSpecConfig implements ISpoofaxLanguageSpecConfig {

    private static final long serialVersionUID = -5913973186313150350L;
    public final LegacySpoofaxProjectSettings settings;

    public LegacySpoofaxLanguageSpecConfig(final LegacySpoofaxProjectSettings settings) {
        super(settings.settings());
        this.settings = settings;
    }

    @Override
    public Collection<String> pardonedLanguages() {
        return Lists.newArrayList(this.settings.pardonedLanguages());
    }

    @Override
    public Format format() {
        return this.settings.format();
    }

    @Override
    public String sdfName() { return this.settings.sdfName(); }

    @Override
    public String metaSdfName() { return this.settings.metaSdfName(); }

    @Override
    public Arguments sdfArgs() {
        return this.settings.sdfArgs();
    }

    @Override
    public Arguments strategoArgs() {
        return this.settings.strategoArgs();
    }

    @Nullable
    @Override
    public String externalDef() {
        return this.settings.externalDef();
    }

    @Nullable
    @Override
    public String externalJar() {
        return this.settings.externalJar();
    }

    @Nullable
    @Override
    public String externalJarFlags() {
        return this.settings.externalJarFlags();
    }

    @Override
    public String strategoName() {
        return this.settings.strategoName();
    }

    @Override
    public String javaName() {
        return this.settings.javaName();
    }

    @Override
    public String packageName() {
        return this.settings.packageName();
    }

    @Override
    public String strategiesPackageName() { return this.settings.strategiesPackageName(); }

    @Override
    public String esvName() { return this.settings.esvName(); }

}
