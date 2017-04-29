package org.metaborg.core.config;

public interface ISourceConfig {

    void accept(ISourceVisitor visitor);

}