package org.metaborg.meta.core.project;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.Project;
import org.metaborg.meta.core.config.ILanguageSpecConfig;

public class LanguageSpec extends Project implements ILanguageSpec {
    private final ILanguageSpecConfig config;


    public LanguageSpec(FileObject location, ILanguageSpecConfig config) {
        super(location, config);
        this.config = config;
    }


    @Override public ILanguageSpecConfig config() {
        return config;
    }
}
