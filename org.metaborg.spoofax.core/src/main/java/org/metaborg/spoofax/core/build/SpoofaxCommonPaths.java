package org.metaborg.spoofax.core.build;

import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.build.CommonPaths;
import org.metaborg.core.project.NameUtil;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.common.collect.Lists;

public class SpoofaxCommonPaths extends CommonPaths {
    static final ILogger logger = LoggerUtils.logger(SpoofaxCommonPaths.class);

    public SpoofaxCommonPaths(FileObject root) {
        super(root);
    }

    /* Shared input directories */

    /**
     * @return Icons directory.
     */
    public FileObject iconsDir() {
        return resolve(root(), "icons");
    }

    /**
     * @return Java sources directory.
     */
    public FileObject srcMainDir() {
        return resolve(root(), "src", "main");
    }


    /* Shared generated sources directories */



    /* Shared output directories */



    /* Metaborg */



    /* Spoofax */

    /**
     * @param languageId
     *            Identifier of the language.
     * @return Archived Spoofax language file.
     */
    public FileObject spxArchiveFile(String languageId) {
        return resolve(targetDir(), languageId + ".spoofax-language");
    }



    /* ESV */

    /**
     * @return Main ESV file.
     */
    public @Nullable FileObject findEsvMainFile(Iterable<FileObject> sources) {
        return find(sources, "Main.esv");
    }


    /* SDF2 and SDF3 */

    /**
     * @param languageName
     *            Name of the language.
     * @return Main SDF2 file.
     */
    public @Nullable FileObject findSyntaxMainFile(Iterable<FileObject> sources, String languageName) {
        return find(sources, languageName + ".sdf");
    }

    /**
     * @param languageName
     *            Name of the language.
     * @return Main SDF2 completion file.
     */
    public FileObject syntaxCompletionMainFile(String languageName) {
        return resolve(syntaxCompletionSrcGenDir(), languageName + ".sdf");
    }

    /**
     * @param languageName
     *            Name of the language.
     * @return Main normalized aterm completion file.
     */
    public FileObject syntaxCompletionMainFileNormalized(String languageName) {
        return resolve(syntaxNormalizedCompletionSrcGenDir(), languageName + "-norm.aterm");
    }

    /**
     * @return Normalized syntax directory. Contains the SDF3 normalized files.
     */
    public FileObject syntaxNormDir() {
        return resolve(syntaxSrcGenDir(), "normalized");
    }

    /**
     * @return Generated SDF2 syntax directory, generated from SDF3 definition.
     */
    public FileObject syntaxSrcGenDir() {
        return resolve(srcGenDir(), "syntax");
    }

    /**
     * 
     * @return Generated SDF2 completion syntax directory, generated from SDF3 definition.
     */
    public FileObject syntaxCompletionSrcGenDir() {
        return resolve(syntaxSrcGenDir(), "completion");
    }

    /**
     * 
     * @return Generated normalized completion syntax directory, generated from SDF3 definition.
     */
    private FileObject syntaxNormalizedCompletionSrcGenDir() {
        return resolve(syntaxNormDir(), "completion");
    }


    /**
     * @param languageName
     *            Name of the language.
     * @return Main generated SDF2 file, generated from main SDF3 file.
     */
    public FileObject syntaxSrcGenMainFile(String languageName) {
        return resolve(syntaxSrcGenDir(), languageName + ".sdf");
    }

    /**
     * @param languageName
     *            Name of the language.
     * @return Main generated SDF2 file, generated from main SDF3 file.
     */
    public FileObject syntaxSrcGenMainNormFile(String languageName) {
        return resolve(syntaxNormDir(), languageName + "-norm.aterm");
    }

    /**
     * @return Generated Stratego signatures directory, generated from SDF3 definition.
     */
    public FileObject syntaxSrcGenSignatureDir() {
        return resolve(srcGenDir(), "signatures");
    }

    /**
     * @return Generated Stratego/Box pretty printer directory, generated from SDF3 definition.
     */
    public FileObject syntaxSrcGenPpDir() {
        return resolve(srcGenDir(), "pp");
    }


    /* Stratego */

    /**
     * @param languageName
     *            Name of the language.
     * @return Main Stratego file.
     */
    public @Nullable FileObject findStrMainFile(Iterable<FileObject> sources, String languageName) {
        return find(sources, NameUtil.toJavaId(languageName.toLowerCase()) + ".str");
    }

    /**
     * @param languageId
     *            Identifier of the language.
     * @return Stratego Java transformation package.
     */
    public String strJavaTransPkg(String languageId) {
        return NameUtil.toJavaId(languageId) + ".trans";
    }

    /**
     * @return Stratego Java strategies directory.
     */
    public FileObject strJavaStratDir() {
        return resolve(srcMainDir(), "strategies");
    }

    /**
     * @param languageId
     *            Identifier of the language.
     * @return Stratego Java strategies package.
     */
    public String strJavaStratPkg(String languageId) {
        final String pkg = NameUtil.toJavaId(languageId) + ".strategies";
        return pkg;
    }

    /**
     * @param languageId
     *            Identifier of the language.
     * @return Stratego Java strategies package path.
     */
    public String strJavaStratPkgPath(String languageId) {
        final String pkg = strJavaStratPkg(languageId);
        final String pkgPath = pkg.replace('.', '/');
        return pkgPath;
    }

    /**
     * @param languageId
     *            Identifier of the language.
     * @return Main Stratego Java strategies file.
     */
    public FileObject strMainJavaStratFile(String languageId) {
        final String path = strJavaStratPkgPath(languageId);
        return resolve(strJavaStratDir(), path, "Main.java");
    }

    /**
     * @return Stratego parse cache directory.
     */
    public FileObject strCacheDir() {
        return resolve(targetDir(), "stratego-cache");
    }

    public FileObject strTypesmartExportedFile() {
        return resolve(targetMetaborgDir(), "typesmart.context");
    }

    /* DynSem */

    /**
     * @param languageName
     *            Name of the language.
     * @return Main DynSem file.
     */
    public @Nullable FileObject findDsMainFile(Iterable<FileObject> sources, String languageName) {
        return find(sources, languageName + ".ds");
    }

    /**
     * @return DynSem manual Java interpreter directory.
     */
    public FileObject dsManualJavaDir() {
        return resolve(srcMainDir(), "ds");
    }

    /**
     * @return DynSem generated Java interpreter directory.
     */
    public FileObject dsSrcGenJavaDir() {
        return resolve(srcGenDir(), "ds-java");
    }

    /**
     * Gets all the Java source root folders.
     * 
     * @param languageId
     *            Identifier of the language.
     * @return A list of Java source root folders.
     */
    public Collection<FileObject> javaSrcDirs(String languageId) {
        return Lists.newArrayList(strJavaStratDir(), dsManualJavaDir(), dsSrcGenJavaDir());
    }
}
