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
import java.util.Map;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.util.log.Level;
import org.metaborg.util.log.LoggerUtils;
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

    private final OutputStream stdout;
    private final Writer stdoutWriter;

    private final OutputStream stderr;
    private final Writer stderrWriter;

    private FileObject workingDir;
    private FileObject definitionDir;
    private boolean acceptDirChanges = false;


    public static OutputStream defaultStdout(String... excludePatterns) {
        return LoggerUtils.stream(LoggerUtils.logger("stdout"), Level.Info, excludePatterns);
    }

    public static OutputStream defaultStderr(String... excludePatterns) {
        return LoggerUtils.stream(LoggerUtils.logger("stderr"), Level.Info, excludePatterns);
    }


    public ResourceAgent(IResourceService resourceService) {
        this(resourceService, resourceService.resolve(System.getProperty("user.dir")));
    }

    public ResourceAgent(IResourceService resourceService, FileObject initialDir) {
        this(resourceService, initialDir, defaultStdout());
    }

    public ResourceAgent(IResourceService resourceService, FileObject initialDir, OutputStream stdout) {
        this(resourceService, initialDir, stdout, defaultStderr());
    }

    public ResourceAgent(IResourceService resourceService, FileObject initialDir, OutputStream stdout,
        OutputStream stderr) {
        super();
        this.acceptDirChanges = true; // Start accepting dir changes after IOAgent constructor call.

        this.resourceService = resourceService;
        this.tempDir = resourceService.resolve(System.getProperty("java.io.tmpdir"));
        this.workingDir = initialDir;
        this.definitionDir = initialDir;

        this.stdout = stdout;
        this.stdoutWriter = new PrintStreamWriter(new PrintStream(stdout));

        this.stderr = stderr;
        this.stderrWriter = new PrintStreamWriter(new PrintStream(stderr));
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

    @Override public void setWorkingDir(String newWorkingDir) throws IOException {
        if(!acceptDirChanges)
            return;

        workingDir = resourceService.resolve(workingDir, newWorkingDir);
    }

    public void setAbsoluteWorkingDir(FileObject dir) {
        workingDir = dir;
    }

    @Override public void setDefinitionDir(String newDefinitionDir) {
        if(!acceptDirChanges)
            return;

        definitionDir = resourceService.resolve(definitionDir, newDefinitionDir);
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


    @Override public int openRandomAccessFile(String fn, String mode) throws IOException {
        boolean appendMode = mode.indexOf('a') >= 0;
        boolean writeMode = appendMode || mode.indexOf('w') >= 0;
        boolean clearFile = false;

        final FileObject resource = resourceService.resolve(workingDir, fn);

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

    @Override public String[] readdir(String fn) {
        try {
            final FileObject resource = resourceService.resolve(workingDir, fn);
            if(!resource.exists() || resource.getType() == FileType.FILE) {
                return new String[0];
            }
            final FileName name = resource.getName();
            final FileObject[] children = resource.getChildren();
            final String[] strings = new String[children.length];
            for(int i = 0; i < children.length; ++i) {
                final FileName absName = children[i].getName();
                strings[i] = name.getRelativeName(absName);
            }
            return strings;
        } catch(FileSystemException e) {
            throw new RuntimeException("Could not list contents of directory " + fn, e);
        }
    }


    @Override public void printError(String error) {
        try {
            getWriter(CONST_STDERR).write(error + "\n");
        } catch(IOException e) {
            // Like System.err.println, we swallow exceptions
        }
    }

    @Override public InputStream openInputStream(String fn, boolean isDefinitionFile) throws FileNotFoundException {
        final FileObject dir = isDefinitionFile ? definitionDir : workingDir;
        try {
            final FileObject file = resourceService.resolve(dir, fn);
            return file.getContent().getInputStream();
        } catch(FileSystemException e) {
            throw new RuntimeException("Could not get input stream for resource", e);
        }
    }


    @Override public OutputStream openFileOutputStream(String fn) throws FileNotFoundException {
        try {
            return resourceService.resolve(workingDir, fn).getContent().getOutputStream();
        } catch(FileSystemException e) {
            throw new RuntimeException("Could not get output stream for resource", e);
        }
    }

    @Override public File openFile(String fn) {
        final FileObject resource = resourceService.resolve(workingDir, fn);
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
            final FileObject resource = resourceService.resolve(workingDir, dn);
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
            final FileObject resource = resourceService.resolve(workingDir, dn);
            return resource.delete(new AllFileSelector()) > 0;
        } catch(FileSystemException e) {
            throw new RuntimeException("Could not delete directory " + dn, e);
        }
    }

    @Override public boolean exists(String fn) {
        try {
            final FileObject resource = resourceService.resolve(workingDir, fn);
            return resource.exists();
        } catch(FileSystemException e) {
            throw new RuntimeException("Could not check if file " + fn + " exists", e);
        }
    }

    @Override public boolean readable(String fn) {
        try {
            final FileObject resource = resourceService.resolve(workingDir, fn);
            return resource.isReadable();
        } catch(FileSystemException e) {
            throw new RuntimeException("Could not check if file " + fn + " is readable", e);
        }
    }

    @Override public boolean writable(String fn) {
        try {
            final FileObject resource = resourceService.resolve(workingDir, fn);
            return resource.isWriteable();
        } catch(FileSystemException e) {
            throw new RuntimeException("Could not check if file " + fn + " is writeable", e);
        }
    }

    @Override public boolean isDirectory(String fn) {
        try {
            final FileObject resource = resourceService.resolve(workingDir, fn);
            final FileType type = resource.getType();
            return type == FileType.FOLDER || type == FileType.FILE_OR_FOLDER;
        } catch(FileSystemException e) {
            throw new RuntimeException("Could not check if file " + fn + " is a directory", e);
        }
    }
}
