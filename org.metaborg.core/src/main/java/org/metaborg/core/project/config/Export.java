package org.metaborg.core.project.config;

import javax.annotation.Nullable;

public class Export {
    public final @Nullable String languageName;
    public final @Nullable String directory;
    public final @Nullable String file;
    public final @Nullable Iterable<String> includes;
    public final @Nullable Iterable<String> excludes;


    public Export(@Nullable String languageName, @Nullable String directory, @Nullable String file,
        @Nullable Iterable<String> includes, @Nullable Iterable<String> excludes) {
        this.languageName = languageName;
        this.directory = directory;
        this.file = file;
        this.includes = includes;
        this.excludes = excludes;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((languageName == null) ? 0 : languageName.hashCode());
        result = prime * result + ((directory == null) ? 0 : directory.hashCode());
        result = prime * result + ((file == null) ? 0 : file.hashCode());
        result = prime * result + ((includes == null) ? 0 : includes.hashCode());
        result = prime * result + ((excludes == null) ? 0 : excludes.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final Export other = (Export) obj;
        if(languageName == null) {
            if(other.languageName != null)
                return false;
        } else if(!languageName.equals(other.languageName))
            return false;
        if(directory == null) {
            if(other.directory != null)
                return false;
        } else if(!directory.equals(other.directory))
            return false;
        if(file == null) {
            if(other.file != null)
                return false;
        } else if(!file.equals(other.file))
            return false;
        if(includes == null) {
            if(other.includes != null)
                return false;
        } else if(!includes.equals(other.includes))
            return false;
        if(excludes == null) {
            if(other.excludes != null)
                return false;
        } else if(!excludes.equals(other.excludes))
            return false;
        return true;
    }
}
