package org.metaborg.core.project.config;

import com.google.common.base.Joiner;

public class Generate {
    public final String languageName;
    public final Iterable<String> directories;


    public Generate(String languageName, Iterable<String> directories) {
        this.languageName = languageName;
        this.directories = directories;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + directories.hashCode();
        result = prime * result + languageName.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final Generate other = (Generate) obj;
        if(!directories.equals(other.directories))
            return false;
        if(!languageName.equals(other.languageName))
            return false;
        return true;
    }

    @Override public String toString() {
        return languageName + " generates into " + Joiner.on(", ").join(directories);
    }
}
