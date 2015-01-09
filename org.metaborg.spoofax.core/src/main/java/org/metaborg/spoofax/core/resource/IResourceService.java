package org.metaborg.spoofax.core.resource;

import java.io.File;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;

/**
 * Interface for access to the virtual file system.
 */
public interface IResourceService {
    /**
     * Returns the root file system object.
     * 
     * @return The root file system object.
     * @throws RuntimeException
     *             if an error occurs.
     */
    public FileObject root();

    /**
     * Returns a file system object for given absolute or relative to the root URI. See <a
     * href="http://commons.apache.org/proper/commons-vfs/filesystems.html">FVS file systems</a> for examples
     * of URIs.
     * 
     * @param uri
     *            relative URI to resolve.
     * @return File system object for given URI.
     * @throws RuntimeException
     *             if file at given URI could not be located.
     */
    public FileObject resolve(String uri);

    /**
     * Returns a local file system object for given Java file system object.
     * 
     * @param file
     *            Java file system object to resolve.
     * @return File system object for given Java file system object.
     * @throws RuntimeException
     *             if given file could not be located.
     */
    public FileObject resolve(File file);

    /**
     * Returns file system objects for given absolute or relative to the root URIs. See <a
     * href="http://commons.apache.org/proper/commons-vfs/filesystems.html">FVS file systems</a> for examples
     * of URIs.
     * 
     * @param uris
     *            URIs to resolve.
     * @return File system objects for given URIs.
     * @throws RuntimeException
     *             if any of the files for given URIs could not be located.
     */
    public Iterable<FileObject> resolveAll(Iterable<String> uris);

    /**
     * Returns a file system name for given absolute or relative to the root URI. See <a
     * href="http://commons.apache.org/proper/commons-vfs/filesystems.html">FVS file systems</a> for examples
     * of URIs.
     * 
     * @param uri
     *            URI to resolve.
     * @return File system name for given URI.
     */
    public FileName resolveURI(String uri);

    /**
     * Returns a file system object that points to a directory where user-specific data can be stored.
     */
    public FileObject userStorage();

    /**
     * Temporary hack to get the internal file system manager.
     */
    public FileSystemManager manager();
}
