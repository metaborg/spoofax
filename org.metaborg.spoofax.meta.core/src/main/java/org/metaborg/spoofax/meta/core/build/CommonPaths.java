package org.metaborg.spoofax.meta.core.build;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgRuntimeException;

public class CommonPaths {
    private final FileObject root;


    public CommonPaths(FileObject root) {
        this.root = root;
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
     * @return Target output directory for compiled Java classes.
     */
    public FileObject targetClassesDir() {
        return resolve(targetDir(), "classes");
    }

    /**
     * @return Target output directory for compiled Java classes.
     */
    public FileObject targetTestClassesDir() {
        return resolve(targetDir(), "test-classes");
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

    /**
     * @return Old cache directory.
     * @deprecated Use subdirectory for {@link #targetDir()} instead.
     */
    @Deprecated public FileObject cacheDir() {
        return resolve(root, ".cache");
    }

    /**
     * @return Old output directory for compiled MetaBorg artifacts.
     * @deprecated Use {@link #targetMetaborgDir()} instead.
     */
    @Deprecated public FileObject includeDir() {
        return resolve(root, "include");
    }


    /* ESV */

    /**
     * @return ESV directory. Contains all ESV files.
     */
    public FileObject esvDir() {
        return resolve(root, "editor");
    }

    /**
     * @return Main ESV file.
     */
    public FileObject esvMainFile() {
        return resolve(esvDir(), "Main.esv");
    }

    /**
     * @param languageName
     *            Name of the language.
     * @return Old main ESV file.
     * @deprecated Use {@link #esvMainFile()} instead.
     */
    @Deprecated public FileObject esvOldMainFile(String languageName) {
        return resolve(esvDir(), languageName + ".main.esv");
    }


    /* SDF2 and SDF3 */

    /**
     * @return Syntax directory. Contains the SDF2 or SDF3 definition.
     */
    public FileObject syntaxDir() {
        return resolve(root, "syntax");
    }

    /**
     * @param languageName
     *            Name of the language.
     * @return Main SDF2 file.
     */
    public FileObject syntaxMainFile(String languageName) {
        return resolve(syntaxDir(), languageName + ".sdf");
    }

    /**
     * @return Generated SDF2 syntax directory, generated from SDF3 definition.
     */
    public FileObject syntaxSrcGenDir() {
        return resolve(srcGenDir(), "syntax");
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
    public FileObject strMainFile(String languageName) {
        return resolve(transDir(), languageName + ".str");
    }

    /**
     * @return Generated Stratego Java directory, generated from Stratego definition.
     */
    public FileObject strSrcGenJavaTransDir() {
        return resolve(srcGenDir(), "stratego-java", "trans");
    }

    /**
     * @return Target output directory for compiled Stratego Java classes.
     */
    public FileObject strTargetClassesTransDir() {
        return resolve(targetClassesDir(), "trans");
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
        final String pkg = languageId + ".strategies";
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
     * @param languageId
     *            Identifier of the language.
     * @return Target output directory for compiled Stratego Java strategy classes.
     */
    public FileObject strTargetClassesJavaStratDir(String languageId) {
        return resolve(targetClassesDir(), strJavaStratPkgPath(languageId));
    }


    /* DynSem */

    /**
     * @param languageName
     *            Name of the language.
     * @return Main DynSem file.
     */
    public FileObject dsMainFile(String languageName) {
        return resolve(transDir(), languageName + ".ds");
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
     * @return Target output directory for compiled manual DynSem interpreter classes.
     */
    public FileObject dsTargetClassesManualDir() {
        return resolve(targetClassesDir(), "ds", "manual", "interpreter");
    }

    /**
     * @return Target output directory for compiled generated DynSem interpreter classes.
     */
    public FileObject dsTargetClassesGenerateDir() {
        return resolve(targetClassesDir(), "ds", "generated", "interpreter");
    }


    /* Pluto */

    /**
     * @return Target output directory for pluto build information.
     */
    public FileObject plutoBuildInfoDir() {
        return resolve(targetDir(), "pluto");
    }


    private FileObject resolve(FileObject dir, String path) {
        try {
            return dir.resolveFile(path);
        } catch(FileSystemException e) {
            throw new MetaborgRuntimeException(e);
        }
    }

    private FileObject resolve(FileObject dir, String... paths) {
        FileObject file = dir;
        for(String path : paths) {
            file = resolve(file, path);
        }
        return file;
    }
}
