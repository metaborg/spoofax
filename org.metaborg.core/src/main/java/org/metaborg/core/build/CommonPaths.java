package org.metaborg.core.build;

import java.util.Collection;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.project.NameUtil;

import com.google.common.collect.Lists;

public class CommonPaths {
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
        return resolve(srcGenDir(), "metaborg.component.yaml");
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
        return resolve(transDir(), NameUtil.toJavaId(languageName.toLowerCase()) + ".str");
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

    public FileObject strTypesmartFile() {
        return resolve(targetMetaborgDir(), "stratego.typesmart");
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
     * Gets all the Java source root folders.
     * 
     * @return A list of Java source root folders.
     */
    public Collection<FileObject> javaSrcDirs() {
        return Lists.newArrayList(strJavaStratDir(), dsManualJavaDir(), dsSrcGenJavaDir());
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
