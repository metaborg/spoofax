package org.metaborg.core.build;

import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgConstants;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.project.NameUtil;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.common.collect.Lists;

public class CommonPaths {
    private static final ILogger logger = LoggerUtils.logger(CommonPaths.class);

    protected final FileObject root;


    public CommonPaths(FileObject root) {
        this.root = root;
    }


    /**
     * @return Root directory.
     */
    public FileObject root() {
        return root;
    }


    /* Shared input directories */

    /**
     * @return Transformations directory. Contains the Stratego definition, NaBL definition, TS definition, and DynSem
     *         definition.
     */
    public FileObject transDir() {
        return resolve(root, "trans");
    }

    /**
     * @return Icons directory.
     */
    public FileObject iconsDir() {
        return resolve(root, "icons");
    }

    /**
     * @return Java sources directory.
     */
    public FileObject srcMainDir() {
        return resolve(root, "src", "main");
    }


    /* Shared generated sources directories */

    /**
     * @return Generated sources directory. All generated source code (Stratego, SDF, config files, etc.) go into this
     *         directory.
     */
    public FileObject srcGenDir() {
        return resolve(root, "src-gen");
    }


    /* Shared output directories */

    /**
     * @return Target output directory. All compiled outputs go into this directory.
     */
    public FileObject targetDir() {
        return resolve(root, "target");
    }

    /**
     * @return Target output directory for compiled MetaBorg artifacts (Stratego JAR, parse table, etc.). All compiled
     *         artifacts that should be included with the language go into this directory.
     */
    public FileObject targetMetaborgDir() {
        return resolve(targetDir(), "metaborg");
    }


    /**
     * @return Target output directory for replicated resources.
     */
    public FileObject replicateDir() {
        return resolve(targetDir(), "replicate");
    }


    /* Metaborg */

    /**
     * @return Metaborg component configuration file.
     */
    public FileObject mbComponentConfigFile() {
        return resolve(srcGenDir(), MetaborgConstants.FILE_COMPONENT_CONFIG);
    }


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


    protected @Nullable FileObject find(Iterable<FileObject> dirs, String path) {
        FileObject file = null;
        for(FileObject dir : dirs) {
            try {
                FileObject candidate = dir.resolveFile(path);
                if(candidate.exists()) {
                    if(file != null) {
                        throw new MetaborgRuntimeException("Found multiple candidates for " + path);
                    } else {
                        file = candidate;
                    }
                }
            } catch(FileSystemException e) {
                logger.warn("Error when trying to resolve {} in {}", e, path, dir);
            }
        }
        return file;
    }

    protected FileObject resolve(FileObject dir, String path) {
        try {
            return dir.resolveFile(path);
        } catch(FileSystemException e) {
            throw new MetaborgRuntimeException(e);
        }
    }
    protected FileObject resolve(FileObject dir, String... paths) {
        FileObject file = dir;
        for(String path : paths) {
            file = resolve(file, path);
        }
        return file;
    }
}
