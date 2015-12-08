package org.metaborg.spoofax.core.project.settings;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.metaborg.core.project.NameUtil;
import org.metaborg.core.project.settings.ConfigurationBasedLanguageSpecConfig;
import org.metaborg.spoofax.core.project.configuration.Format;
import org.metaborg.spoofax.core.project.configuration.ISpoofaxLanguageSpecConfig;

import javax.annotation.Nullable;

/**
 * An implementation of the {@link ISpoofaxLanguageSpecConfig} interface
 * that is backed by an {@link ImmutableConfiguration} object.
 */
public class ConfigurationBasedSpoofaxLanguageSpecConfig extends ConfigurationBasedLanguageSpecConfig implements ISpoofaxLanguageSpecConfig {

    private static final String PROP_FORMAT = "format";
    private static final String PROP_EXTERNAL_DEF = "externalDef";
    private static final String PROP_EXTERNAL_JAR = "externalJar.name";
    private static final String PROP_EXTERNAL_JAR_FLAGS = "externalJar.flags";
    private static final String PROP_SDF_ARGS = "language.sdf.args";
    private static final String PROP_STRATEGO_ARGS = "language.stratego.args";

    /**
     * Initializes a new instance of the {@link ConfigurationBasedSpoofaxLanguageSpecConfig} class.
     *
     * @param configuration The configuration that provides the properties.
     */
    public ConfigurationBasedSpoofaxLanguageSpecConfig(final HierarchicalConfiguration<ImmutableNode> configuration) {
        super(configuration);
    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override public LanguageIdentifier identifier() {
//        return this.config.get(LanguageIdentifier.class, PROP_IDENTIFIER);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override public String name() {
//        return this.config.getString(PROP_NAME);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public Iterable<LanguageIdentifier> compileDependencies() {
//        return this.config.getList(LanguageIdentifier.class, PROP_COMPILE_DEPENDENCIES);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override public Iterable<LanguageIdentifier> runtimeDependencies() {
//        return this.config.getList(LanguageIdentifier.class, PROP_RUNTIME_DEPENDENCIES);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public Iterable<String> pardonedLanguages() {
//        return this.config.getList(String.class, PROP_PARDONED_LANGUAGES);
//    }

    /**
     * {@inheritDoc}
     */
    public Format format() {
        return Format.valueOf(this.config.getString(PROP_FORMAT));
    }

//    public void setFormat(Format format) {
//        this.format = format;
//    }


    /**
     * {@inheritDoc}
     */
    public Iterable<String> sdfArgs() {
        return this.config.getList(String.class, PROP_SDF_ARGS);
    }

//    public void setSdfArgs(Collection<String> sdfArgs) {
//        this.sdfArgs = sdfArgs;
//    }

    /**
     * {@inheritDoc}
     */
    public @Nullable
    String externalDef() {
        return this.config.getString(PROP_EXTERNAL_DEF);
    }

//    public void setExternalDef(String externalDef) {
//        this.externalDef = externalDef;
//    }


    /**
     * {@inheritDoc}
     */
    public Iterable<String> strategoArgs() {
        return this.config.getList(String.class, PROP_STRATEGO_ARGS);
    }

//    public void setStrategoArgs(Collection<String> strategoArgs) {
//        this.strategoArgs = strategoArgs;
//    }

    /**
     * {@inheritDoc}
     */
    public @Nullable String externalJar() {
        return this.config.getString(PROP_EXTERNAL_JAR);
    }

//    public void setExternalJar(String externalJar) {
//        this.externalJar = externalJar;
//    }

    /**
     * {@inheritDoc}
     */
    public @Nullable String externalJarFlags() {
        return this.config.getString(PROP_EXTERNAL_JAR_FLAGS);
    }

//    public void setExternalJarFlags(String externalJarFlags) {
//        this.externalJarFlags = externalJarFlags;
//    }


    /**
     * {@inheritDoc}
     */
    public String strategoName() {
        return NameUtil.toJavaId(this.name().toLowerCase());
    }

    /**
     * {@inheritDoc}
     */
    public String javaName() {
        return NameUtil.toJavaId(this.name());
    }

    /**
     * {@inheritDoc}
     */
    public String packageName() {
        return NameUtil.toJavaId(this.identifier().id);
    }

    /**
     * {@inheritDoc}
     */
    public String packagePath() {
        return packageName().replace('.', '/');
    }


//    /**
//     * {@inheritDoc}
//     */
//    public FileObject getGeneratedSourceDirectory() {
//        return resolve(DIR_SRCGEN);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    public FileObject getOutputDirectory() {
//        return resolve(DIR_INCLUDE);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    public FileObject getIconsDirectory() {
//        return resolve(DIR_ICONS);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    public FileObject getLibDirectory() {
//        return resolve(DIR_LIB);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    public FileObject getSyntaxDirectory() {
//        return resolve(DIR_SYNTAX);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    public FileObject getEditorDirectory() {
//        return resolve(DIR_EDITOR);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    public FileObject getJavaDirectory() {
//        return resolve(DIR_JAVA);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    public FileObject getJavaTransDirectory() {
//        return resolve(DIR_JAVA_TRANS);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    public FileObject getGeneratedSyntaxDirectory() {
//        return resolve(DIR_SRCGEN_SYNTAX);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    public FileObject getTransDirectory() {
//        return resolve(DIR_TRANS);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    public FileObject getCacheDirectory() {
//        return resolve(DIR_CACHE);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    public FileObject getMainESVFile() {
//        return resolve(DIR_EDITOR + "/" + this.name() + ".main.esv");
//    }


//    private FileObject resolve(String directory) {
//        try {
//            return location.resolveFile(directory);
//        } catch(FileSystemException e) {
//            throw new MetaborgRuntimeException(e);
//        }
//    }

}
