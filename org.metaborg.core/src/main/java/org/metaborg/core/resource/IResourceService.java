package org.metaborg.core.resource;

import java.io.File;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgRuntimeException;

/**
 * Interface for access to the virtual file system.
 */
public interface IResourceService {
    /**
     * Returns the root file system object.
     * 
     * @return The root file system object.
     * @throws MetaborgRuntimeException
     *             if an error occurs.
     */
    public FileObject root();

    /**
     * Returns a file system object for given absolute or relative to the root URI. See <a
     * href="http://commons.apache.org/proper/commons-vfs/filesystems.html">FVS file systems</a> for examples of URIs.
     * 
     * @param uri
     *            relative URI to resolve.
     * @return File system object for given URI.
     * @throws MetaborgRuntimeException
     *             when uri is invalid.
     */
    public FileObject resolve(String uri);

    /**
     * Returns a local file system object for given Java file system object.
     * 
     * @param file
     *            Java file system object to resolve.
     * @return File system object for given Java file system object.
     * @throws MetaborgRuntimeException
     *             when file is invalid.
     */
    public FileObject resolve(File file);

    /**
     * Tries to resolve {@code path} as an absolute path first, if that fails, resolves {@code path} relative to
     * {@code parent}. If {@code path} is absolute but does not have a scheme, it is assumed to be on the local file
     * system.
     * 
     * @param parent
     *            Parent file object to resolve relatively to, if {@code path} is a relative path.
     * @param path
     *            Path to resolve
     * @return File system object for given path.
     * @throws MetaborgRuntimeException
     *             When absolute or relative resolution fails.
     */
    public FileObject resolve(FileObject parent, String path);

    /**
     * Returns file system objects for given absolute or relative to the root URIs. See <a
     * href="http://commons.apache.org/proper/commons-vfs/filesystems.html">FVS file systems</a> for examples of URIs.
     * 
     * @param uris
     *            URIs to resolve.
     * @return File system objects for given URIs.
     * @throws MetaborgRuntimeException
     *             when any uri is invalid.
     */
    public Iterable<FileObject> resolveAll(Iterable<String> uris);

    /**
     * Returns a file system name for given absolute or relative to the root URI. See <a
     * href="http://commons.apache.org/proper/commons-vfs/filesystems.html">FVS file systems</a> for examples of URIs.
     * 
     * @param uri
     *            URI to resolve.
     * @return File system name for given URI.
     * @throws MetaborgRuntimeException
     *             when uri is invalid.
     */
    public FileName resolveURI(String uri);

    /**
     * Attempts to get a local file for given resource, or copies the resource to the local file system if it does not
     * reside on the local file system.
     * 
     * @param resource
     *            Resource to get a local file for.
     * @return Local file.
     * @throws MetaborgRuntimeException
     *             When given resource does not exist.
     */
    public File localFile(FileObject resource);

    /**
     * Attempts to get a local file handle for given resource.
     * 
     * @param resource
     *            Resource to get a local file handle for.
     * @return Local file handle, or null if given resource does not reside on the local file system.
     */
    public @Nullable File localPath(FileObject resource);

    /**
     * Returns a file system object that points to a directory where user-specific data can be stored.
     * 
     * @throws MetaborgRuntimeException
     *             when an internal error occurs.
     * 
     * @deprecated Wrong abstraction, will be removed in the future.
     */
    @Deprecated public FileObject userStorage();
}
