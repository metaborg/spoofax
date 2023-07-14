package org.metaborg.spoofax.meta.core.build;

import java.util.Collection;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.build.SpoofaxCommonPaths;

public class SpoofaxLangSpecCommonPaths extends SpoofaxCommonPaths {
    public SpoofaxLangSpecCommonPaths(FileObject root) {
        super(root);
    }


    /* Shared output directories */

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



    /* Stratego */

    /**
     * @return Generated Stratego Java directory, generated from Stratego definition.
     */
    public FileObject strSrcGenJavaTransDir(String languageId) {
        final String pkg = strJavaTransPkg(languageId);
        final String pkgPath = pkg.replace('.', '/');
        return resolve(strSrcGenJavaDir(), pkgPath);
    }

    /**
     * @return Target output directory for compiled Stratego Java classes.
     */
    public FileObject strTargetClassesTransDir(String languageId) {
        final String pkg = strJavaTransPkg(languageId);
        final String pkgPath = pkg.replace('.', '/');
        return resolve(targetClassesDir(), pkgPath);
    }

    /**
     * @param languageId
     *            Identifier of the language.
     * @return Target output directory for compiled Stratego Java strategy classes.
     */
    public FileObject strTargetClassesJavaStratDir(String languageId) {
        return resolve(targetClassesDir(), strJavaStratPkgPath(languageId));
    }

    /**
     * @return Stratego 2 library replication directory for unpacked class files.
     */
    public FileObject str2libsDir() {
        return resolve(replicateDir(), "str2libs");
    }


    /* DynSem */

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
}
