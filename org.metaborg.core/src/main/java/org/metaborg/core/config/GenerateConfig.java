package org.metaborg.core.config;

public class GenerateConfig implements IGenerateConfig {
    public final String language;
    public final String directory;


    public GenerateConfig(String languageName, String directory) {
        this.language = languageName;
        this.directory = directory;
    }


    @Override public String languageName() {
        return language;
    }

    @Override public String directory() {
        return directory;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + language.hashCode();
        result = prime * result + directory.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final GenerateConfig other = (GenerateConfig) obj;
        if(!language.equals(other.language))
            return false;
        if(!directory.equals(other.directory))
            return false;
        return true;
    }

    @Override public String toString() {
        return language + " generates into " + directory;
    }
}
