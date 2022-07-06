package org.metaborg.spoofax.meta.core.config;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.build.SpoofaxCommonPaths;

public enum StrategoVersion {
    v1,
    v2;

    /**
     * @param languageName
     *            Name of the language.
     * @return Main Stratego file.
     */
    public @Nullable FileObject findStrMainFile(SpoofaxCommonPaths paths, Iterable<FileObject> sources,
        String languageName) {
        switch(this) {
            case v1:
                return paths.findStrMainFile(sources, languageName);
            case v2:
                return paths.findStr2MainFile(sources, languageName);
            default:
                throw new IllegalStateException(
                    "StrategoBuildSetting in unknown state that isn't batch or incremental");
        }
    }
}
