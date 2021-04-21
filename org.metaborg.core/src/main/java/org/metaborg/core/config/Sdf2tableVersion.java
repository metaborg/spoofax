package org.metaborg.core.config;

public enum Sdf2tableVersion {
    c(false), java(true), dynamic(true), incremental(true);

    public final boolean javaBased;

    Sdf2tableVersion(boolean javaBased) {
        this.javaBased = javaBased;
    }
}
