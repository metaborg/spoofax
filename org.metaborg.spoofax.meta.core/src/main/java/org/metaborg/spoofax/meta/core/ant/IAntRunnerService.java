package org.metaborg.spoofax.meta.core.ant;

import java.net.URL;
import java.util.Map;

import jakarta.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.tools.ant.BuildListener;

public interface IAntRunnerService {
    IAntRunner get(FileObject antFile, FileObject baseDir, Map<String, String> properties, @Nullable URL[] classpaths,
        @Nullable BuildListener listener);
}