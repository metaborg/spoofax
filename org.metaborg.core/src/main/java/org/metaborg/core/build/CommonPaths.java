package org.metaborg.core.build;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgConstants;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

public class CommonPaths {
    static final ILogger logger = LoggerUtils.logger(CommonPaths.class);

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

    /**
     * @return Transformations directory. Contains the Stratego definition, NaBL definition, TS definition, and DynSem
     *         definition.
     */
    public FileObject transDir() {
        return resolve(root(), "trans");
    }

    /**
     * @return Generated sources directory. All generated source code (Stratego, SDF, config files, etc.) go into this
     *         directory.
     */
    public FileObject srcGenDir() {
        return resolve(root(), "src-gen");
    }

    /**
     * @return Target output directory. All compiled outputs go into this directory.
     */
    public FileObject targetDir() {
        return resolve(root(), "target");
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
     * @return Metaborg component configuration file.
     */
    public FileObject mbComponentConfigFile() {
        return resolve(srcGenDir(), MetaborgConstants.FILE_COMPONENT_CONFIG);
    }

    @Nullable protected FileObject find(Iterable<FileObject> dirs, String path) {
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