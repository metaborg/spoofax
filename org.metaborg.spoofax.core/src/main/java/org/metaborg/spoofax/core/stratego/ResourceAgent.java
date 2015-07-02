package org.metaborg.spoofax.core.stratego;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.util.log.LoggingOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.library.PrintStreamWriter;

import com.google.common.collect.Maps;

public class ResourceAgent extends IOAgent {
    private static class ResourceHandle {
        public final FileObject resource;

        public Reader reader;
        public Writer writer;
        public InputStream inputStream;
        public OutputStream outputStream;


        ResourceHandle(FileObject resource) {
            this.resource = resource;
        }
    }


    private final IResourceService resourceService;
    private final FileObject tempDir;

    private final Map<Integer, ResourceHandle> openFiles = Maps.newHashMap();

    private final Logger stdoutLogger = LoggerFactory.getLogger("stdout");
    private final OutputStream stdout = new LoggingOutputStream(stdoutLogger, false);
    private final Writer stdoutWriter = new PrintStreamWriter(new PrintStream(stdout));

    private final Logger stderrLogger = LoggerFactory.getLogger("stderr");
    private final OutputStream stderr = new LoggingOutputStream(stderrLogger, true);
    private final Writer stderrWriter = new PrintStreamWriter(new PrintStream(stderr));

    private FileObject workingDir;
    private FileObject definitionDir;
    private boolean acceptDirChanges = false;


    public ResourceAgent(IResourceService resourceService) {
        this(resourceService, resourceService.resolve(System.getProperty("java.io.tmpdir")), resourceService
            .resolve(System.getProperty("user.dir")), resourceService.resolve(System.getProperty("user.dir")));
    }

    public ResourceAgent(IResourceService resourceService, FileObject tempDir, FileObject workingDir,
        FileObject definitionDir) {
        super();
        this.acceptDirChanges = true; // Start accepting dir changes after IOAgent constructor call.

        this.resourceService = resourceService;
        this.tempDir = tempDir;
        this.workingDir = workingDir;
        this.definitionDir = definitionDir;
    }


    @Override public String getWorkingDir() {
        return workingDir.getName().getURI();
    }

    public FileObject getWorkingDirResource() {
        return workingDir;
    }

    @Override public String getDefinitionDir() {
        return definitionDir.getName().getURI();
    }

    public FileObject getDefinitionDirResource() {
        return definitionDir;
    }

    @Override public String getTempDir() {
        return tempDir.getName().getURI();
    }

    public FileObject getTempDirResource() {
        return tempDir;
    }

    @Override public void setWorkingDir(String newWorkingDir) throws FileNotFoundException, IOException {
        if(!acceptDirChanges)
            return;

        workingDir = resolve(workingDir, newWorkingDir);
    }

    public void setAbsoluteWorkingDir(FileObject dir) {
        workingDir = dir;
    }

    @Override public void setDefinitionDir(String newDefinitionDir) throws FileNotFoundException {
        if(!acceptDirChanges)
            return;

        try {
            definitionDir = resolve(definitionDir, newDefinitionDir);
        } catch(FileSystemException e) {
            throw new FileNotFoundException(newDefinitionDir);
        }
    }

    public void setAbsoluteDefinitionDir(FileObject dir) {
        definitionDir = dir;
    }


    @Override public Writer getWriter(int fd) {
        if(fd == CONST_STDOUT) {
            return stdoutWriter;
        } else if(fd == CONST_STDERR) {
            return stderrWriter;
        } else {
            final ResourceHandle handle = openFiles.get(fd);
            if(handle.writer == null) {
                assert handle.outputStream == null;
                try {
                    handle.writer =
                        new BufferedWriter(new OutputStreamWriter(internalGetOutputStream(fd), FILE_ENCODING));
                } catch(UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
            return handle.writer;
        }
    }


    @Override public OutputStream internalGetOutputStream(int fd) {
        if(fd == CONST_STDOUT) {
            return stdout;
        } else if(fd == CONST_STDERR) {
            return stderr;
        } else {
            final ResourceHandle handle = openFiles.get(fd);
            if(handle.outputStream == null) {
                assert handle.writer == null;
                try {
                    handle.outputStream = handle.resource.getContent().getOutputStream();
                } catch(FileSystemException e) {
                    throw new RuntimeException("Could not get output stream for resource", e);
                }
            }
            return handle.outputStream;
        }
    }

    @Override public void writeChar(int fd, int c) throws IOException {
        if(fd == CONST_STDOUT || fd == CONST_STDERR) {
            getWriter(fd).append((char) c);
        } else {
            getWriter(fd).append((char) c);
        }
    }

    @Override public boolean closeRandomAccessFile(int fd) throws InterpreterException {
        if(fd == CONST_STDOUT || fd == CONST_STDERR || fd == CONST_STDIN) {
            return true;
        }

        final ResourceHandle handle = openFiles.remove(fd);
        if(handle == null)
            return true; // already closed: be forgiving

        try {
            if(handle.writer != null)
                handle.writer.close();
            if(handle.outputStream != null)
                handle.outputStream.close();
            handle.resource.getContent().close();
        } catch(IOException e) {
            throw new RuntimeException("Could not close resource", e);
        }
        return true;
    }

    @Override public void closeAllFiles() {
        for(ResourceHandle handle : openFiles.values()) {
            try {
                if(handle.writer != null)
                    handle.writer.close();
                if(handle.outputStream != null)
                    handle.outputStream.close();
                handle.resource.getContent().close();
            } catch(IOException e) {
                throw new RuntimeException("Could not close resource", e);
            }
        }
        openFiles.clear();
    }


    @Override public int openRandomAccessFile(String fn, String mode) throws FileNotFoundException, IOException {
        boolean appendMode = mode.indexOf('a') >= 0;
        boolean writeMode = appendMode || mode.indexOf('w') >= 0;
        boolean clearFile = false;

        final FileObject resource = resolve(workingDir, fn);

        if(writeMode) {
            if(!resource.exists()) {
                resource.createFile();
            } else if(!appendMode) {
                clearFile = true;
            }
        }

        if(clearFile) {
            resource.delete();
            resource.createFile();
        }

        openFiles.put(fileCounter, new ResourceHandle(resource));

        return fileCounter++;
    }

    @Override public InputStream internalGetInputStream(int fd) {
        if(fd == CONST_STDIN) {
            return stdin;
        }
        final ResourceHandle handle = openFiles.get(fd);
        if(handle.inputStream == null) {
            try {
                handle.inputStream = handle.resource.getContent().getInputStream();
            } catch(FileSystemException e) {
                throw new RuntimeException("Could not get input stream for resource", e);
            }
        }
        return handle.inputStream;
    }

    @Override public Reader getReader(int fd) {
        if(fd == CONST_STDIN) {
            return stdinReader;
        }
        final ResourceHandle handle = openFiles.get(fd);
        try {
            if(handle.reader == null)
                handle.reader = new BufferedReader(new InputStreamReader(internalGetInputStream(fd), FILE_ENCODING));
        } catch(UnsupportedEncodingException e) {
            throw new RuntimeException("Could not get reader for resource", e);
        }
        return handle.reader;
    }

    @Override public String readString(int fd) throws IOException {
        char[] buffer = new char[2048];
        final StringBuilder result = new StringBuilder();
        final Reader reader = getReader(fd);
        for(int read = 0; read != -1; read = reader.read(buffer)) {
            result.append(buffer, 0, read);
        }
        return result.toString();
    }


    @Override public void printError(String error) {
        try {
            getWriter(CONST_STDERR).write(error + "\n");
        } catch(IOException e) {
            // Like System.err.println, we swallow excpetions
        }
    }

    @Override public InputStream openInputStream(String fn, boolean isDefinitionFile) throws FileNotFoundException {
        final FileObject dir = isDefinitionFile ? definitionDir : workingDir;
        try {
            final FileObject file = resolve(dir, fn);
            return file.getContent().getInputStream();
        } catch(FileSystemException e) {
            throw new RuntimeException("Could not get input stream for resource", e);
        }
    }


    @Override public OutputStream openFileOutputStream(String fn) throws FileNotFoundException {
        try {
            return resolve(workingDir, fn).getContent().getOutputStream();
        } catch(FileSystemException e) {
            throw new RuntimeException("Could not get output stream for resource", e);
        }
    }

    @Override public File openFile(String fn) {
        // GTODO: does not work for files that do not reside on the local file system
        try {
            final FileObject resource = resolve(workingDir, fn);
            File localResource = resourceService.localPath(resource);
            if(localResource == null) {
                final File localWorkingDir = resourceService.localPath(workingDir);
                if(localWorkingDir == null) {
                    // Local working directory does not reside on the local file system, just return a File.
                    return new File(fn);
                }
                // Could not get a local File using the FileObject interface, fall back to composing Files.
                return new File(getAbsolutePath(localWorkingDir.getPath(), fn));
            }
            return localResource;
        } catch(FileSystemException e) {
            throw new RuntimeException("Could not open file", e);
        }
    }

    @Override public String createTempFile(String prefix) throws IOException {
        // GTODO: should use FileObject interface
        final File tempFile = File.createTempFile(prefix, null);
        tempFile.deleteOnExit();
        return tempFile.getPath();
    }

    @Override public String createTempDir(String prefix) throws IOException {
        // GTODO: should use FileObject interface
        File result;
        do {
            result = File.createTempFile(prefix, null);
            result.delete();
        } while(!result.mkdir());
        result.deleteOnExit();
        return result.getPath();
    }

    @Override public boolean mkdir(String dn) {
        try {
            final FileObject resource = resolve(workingDir, dn);
            final boolean created = !resource.exists();
            resource.createFolder();
            return created;
        } catch(FileSystemException e) {
            throw new RuntimeException("Could not create directories", e);
        }
    }

    @Override @Deprecated public boolean mkDirs(String dn) {
        return mkdir(dn);
    }

    @Override public boolean rmdir(String dn) {
        try {
            final FileObject resource = resolve(workingDir, dn);
            return resource.delete(new AllFileSelector()) > 0;
        } catch(FileSystemException e) {
            throw new RuntimeException("Could not delete directory", e);
        }
    }


    /**
     * Tries to resolve {@code path} as an absolute path first, if that fails, resolves {@code path} relative to
     * {@code parent}.
     * 
     * @param parent
     *            Parent file object to resolve relatively to, if {@code path} is a relative path.
     * @param path
     *            Path to resolve
     * @return Resolved file object
     * @throws FileSystemException
     *             when absolute and relative resolution fails.
     */
    private FileObject resolve(FileObject parent, String path) throws FileSystemException {
        final File file = new File(path);

        try {
            final URI uri = new URI(path);
            if(uri.isAbsolute()) {
                return resourceService.resolve(path);
            }
        } catch(URISyntaxException e) {
            // Ignore
        }

        if(file.isAbsolute()) {
            return resourceService.resolve("file://" + path);
        }
        if(parent != null) {
            return parent.resolveFile(path);
        }
        throw new RuntimeException("Cannot resolve relative path if base path is null");
    }
}
