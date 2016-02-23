package org.metaborg.meta.core.project;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.Project;
import org.metaborg.meta.core.config.ILanguageSpecConfig;

public class LanguageSpec extends Project implements ILanguageSpec {
    private final ILanguageSpecConfig config;
    private final ILanguageSpecPaths paths;


    public LanguageSpec(FileObject location, ILanguageSpecConfig config, ILanguageSpecPaths paths) {
        super(location, config);
        this.config = config;
        this.paths = paths;
    }


    @Override public ILanguageSpecConfig config() {
        return config;
    }

    @Override public ILanguageSpecPaths paths() {
        return paths;
    }
}
