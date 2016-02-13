package org.metaborg.spoofax.meta.core.pluto.stamp;

import java.io.File;

import build.pluto.stamp.Stamper;
import build.pluto.stamp.ValueStamp;

public class DirectoryLastModifiedStamper implements Stamper {
    private static final long serialVersionUID = 6052649976972107130L;


    @Override public ValueStamp<Long> stampOf(File directory) {
        if(!(directory.exists())) {
            return new ValueStamp<>(this, -1L);
        }

        if(!directory.isDirectory()) {
            throw new RuntimeException("Directory stamper cannot stamp " + directory + ", it is not a directory");
        }

        return new ValueStamp<>(this, directory.lastModified());
    }
}
