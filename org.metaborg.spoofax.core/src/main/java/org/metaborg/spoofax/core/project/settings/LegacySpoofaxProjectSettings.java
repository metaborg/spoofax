package org.metaborg.spoofax.core.project.settings;

import java.io.Serializable;
import java.util.Collection;

import javax.annotation.Nullable;

import org.metaborg.core.project.NameUtil;
import org.metaborg.core.project.settings.ILegacyProjectSettings;
import org.metaborg.util.cmd.Arguments;

import com.google.common.collect.Lists;

@SuppressWarnings("deprecation")
@Deprecated
public class LegacySpoofaxProjectSettings implements Serializable {
    private static final long serialVersionUID = 7439146986768086591L;

    private final ILegacyProjectSettings settings;

    private Collection<String> pardonedLanguages = Lists.newLinkedList();
    private Format format = Format.ctree;
    private Arguments sdfArgs = new Arguments();
    private @Nullable String externalDef;
    private Arguments strategoArgs = new Arguments();
    private @Nullable String externalJar;
    private @Nullable String externalJarFlags;


    public LegacySpoofaxProjectSettings(ILegacyProjectSettings settings) {
        this.settings = settings;
    }

    public ILegacyProjectSettings settings() {
        return settings;
    }

    public Iterable<String> pardonedLanguages() {
        return pardonedLanguages;
    }

    public void setPardonedLanguages(Collection<String> pardonedLanguages) {
        this.pardonedLanguages = pardonedLanguages;
    }


    public Format format() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }


    public Arguments sdfArgs() {
        return sdfArgs;
    }

    public void setSdfArgs(Arguments sdfArgs) {
        this.sdfArgs = sdfArgs;
    }

    public @Nullable String externalDef() {
        return externalDef;
    }

    public void setExternalDef(@Nullable String externalDef) {
        this.externalDef = externalDef;
    }


    public Arguments strategoArgs() {
        return strategoArgs;
    }

    public void setStrategoArgs(Arguments strategoArgs) {
        this.strategoArgs = strategoArgs;
    }

    public @Nullable String externalJar() {
        return externalJar;
    }

    public void setExternalJar(@Nullable String externalJar) {
        this.externalJar = externalJar;
    }

    public @Nullable String externalJarFlags() {
        return externalJarFlags;
    }

    public void setExternalJarFlags(@Nullable String externalJarFlags) {
        this.externalJarFlags = externalJarFlags;
    }


    public String sdfName() {
        return settings.name();
    }

    public String metaSdfName() {
        return sdfName() + "-Statego";
    }

    public String esvName() {
        return settings.name();
    }

    public String strategoName() {
        return NameUtil.toJavaId(settings.name().toLowerCase());
    }

    public String javaName() {
        return NameUtil.toJavaId(settings.name());
    }

    public String packageName() {
        return NameUtil.toJavaId(settings.identifier().id);
    }

    public String strategiesPackageName() {
        return packageName() + ".strategies";
    }
}
