package org.metaborg.spoofax.meta.core.pluto.stamp;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;

import build.pluto.stamp.Stamp;
import build.pluto.stamp.Stamper;
import build.pluto.stamp.ValueStamp;

public class DirectoryModifiedStamper implements Stamper {
    private static final long serialVersionUID = 6052649976972107130L;

    private final @Nullable IOFileFilter filter;
    private final @Nullable Stamper fileStamper;


    public DirectoryModifiedStamper(@Nullable IOFileFilter filter, @Nullable Stamper fileStamper) {
        this.filter = filter;
        this.fileStamper = fileStamper;
    }

    public DirectoryModifiedStamper() {
        this(null, null);
    }


    @Override public Stamp stampOf(File directory) {
        if(!directory.exists()) {
            return new ValueStamp<>(this, -1L);
        } else if(!directory.isDirectory()) {
            throw new RuntimeException("Directory stamper cannot stamp " + directory + ", it is not a directory");
        } else if(filter != null && fileStamper != null) {
            final Map<File, Stamp> stamps = new HashMap<>();
            final Collection<File> files = FileUtils.listFiles(directory, filter, FalseFileFilter.INSTANCE);
            for(File file : files) {
                stamps.put(file, fileStamper.stampOf(file));
            }
            return new ValueStamp<>(this, stamps);
        } else {
            return new ValueStamp<>(this, directory.lastModified());
        }
    }
}
