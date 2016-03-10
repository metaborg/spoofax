package org.metaborg.spoofax.core.unit;

import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.unit.IUnit;
import org.metaborg.core.unit.IUnitContrib;

import com.google.common.collect.Maps;

public class SpoofaxUnit implements IUnit {
    private final @Nullable FileObject source;
    private final Map<String, IUnitContrib> contribs;


    public SpoofaxUnit(@Nullable FileObject source, Map<String, IUnitContrib> contribs) {
        this.contribs = contribs;
        this.source = source;
    }

    public SpoofaxUnit(@Nullable FileObject source) {
        this(source, Maps.<String, IUnitContrib>newHashMap());
    }

    public SpoofaxUnit() {
        this(null);
    }


    @Override public boolean detached() {
        return source == null;
    }

    @Override public @Nullable FileObject source() {
        return source;
    }

    @Override public @Nullable IUnitContrib unitContrib(String id) {
        return contribs.get(id);
    }

    @Override public Iterable<IUnitContrib> unitContribs() {
        return contribs.values();
    }

    public void addUnitContrib(IUnitContrib contrib) {
        contribs.put(contrib.id(), contrib);
    }
}
