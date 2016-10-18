package org.metaborg.core.resource;

import java.io.File;
import java.net.URI;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgRuntimeException;

/**
 * Interface for access to the virtual file system.
 */
public interface IResourceService extends AutoCloseable {
    /**
     * Returns the root file system object.
     * 
     * @return The root file system object.
     * @throws MetaborgRuntimeException
     *             if an error occurs.
     */
    FileObject root();

    /**
     * Returns a file system object for given (absolute or relative to the root) URI. The given URI will be encoded (\,
     * /, and : symbols will not be encoded) in its entirely. If your URI is already encoded, convert it to an
     * {@link URI} and call {@link #resolve(URI)} instead.
     * 
     * See <a href="http://commons.apache.org/proper/commons-vfs/filesystems.html">FVS file systems</a> for examples of
     * URIs.
     * 
     * @param uri
     *            Absolute or relative to the root URI to resolve.
     * @return File system object for given URI.
     * @throws MetaborgRuntimeException
     *             When <code>uri</code> is invalid.
     */
    FileObject resolve(String uri);

    /**
     * Returns a local file system object for given Java file system object.
     * 
     * @param file
     *            Java file system object to resolve.
     * @return File system object for given Java file system object.
     * @throws MetaborgRuntimeException
     *             When file is invalid.
     */
    FileObject resolve(File file);

    /**
     * Returns a file system object for given Java URI object.
     * 
     * See <a href="http://commons.apache.org/proper/commons-vfs/filesystems.html">FVS file systems</a> for examples of
     * URIs.
     * 
     * @param uri
     *            Java URI object to resolve.
     * @return File system object for given Java URI object.
     * @throws MetaborgRuntimeException
     *             When <code>uri</code> is invalid.
     */
    FileObject resolve(URI uri);

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
    FileObject resolve(FileObject parent, String path);


    /**
     * Returns a file name for given URI. The given URI will be encoded (\, /, and : symbols will not be encoded) in its
     * entirely. If your URI is already encoded, convert it to an {@link URI} and call {@link #resolveToName(URI)}
     * instead.
     * 
     * See <a href="http://commons.apache.org/proper/commons-vfs/filesystems.html">FVS file systems</a> for examples of
     * URIs.
     * 
     * @param uri
     *            URI to resolve to a name.
     * @return File name for given URI.
     * @throws MetaborgRuntimeException
     *             When <code>uri</code> is invalid.
     */
    FileName resolveToName(String uri);

    /**
     * Returns a file name for given Java URI object.
     * 
     * See <a href="http://commons.apache.org/proper/commons-vfs/filesystems.html">FVS file systems</a> for examples of
     * URIs.
     * 
     * @param uri
     *            Java URI object to resolve to a name.
     * @return File name for given Java URI object.
     * @throws MetaborgRuntimeException
     *             When <code>uri</code> is invalid.
     */
    FileName resolveToName(URI uri);


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
    File localFile(FileObject resource);

    /**
     * Attempts to get a local file for given resource, or copies the resource to the local file system at given
     * directory if it does not reside on the local file system.
     * 
     * @param resource
     *            Resource to get a local file for.
     * @param dir
     *            Directory to copy the resources to if they are not on a local filesystem. Must be on the local
     *            filesystem.
     * @return Local file.
     * @throws MetaborgRuntimeException
     *             When given resource does not exist.
     */
    File localFile(FileObject resource, FileObject dir);

    /**
     * Attempts to get a local file handle for given resource.
     * 
     * @param resource
     *            Resource to get a local file handle for.
     * @return Local file handle, or null if given resource does not reside on the local file system.
     */
    @Nullable File localPath(FileObject resource);
}
