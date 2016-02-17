package org.metaborg.spoofax.generator;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.project.settings.StrategoFormat;

public interface IGeneratorSettings {
    String metaborgVersion();

    String eclipseMetaborgVersion();

    String groupId();

    boolean generateGroupId();

    String id();

    String version();

    boolean generateVersion();

    String eclipseVersion();

    String name();

    FileObject location();

    StrategoFormat format();

    String strategoName();

    String javaName();

    String packageName();

    String packagePath();
}