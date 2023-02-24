package org.metaborg.spoofax.core.unit;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.unit.IUnit;
import org.metaborg.core.unit.IUnitContrib;

public class Unit implements IUnit {
    private final @Nullable FileObject source;
    private final Map<String, IUnitContrib> contribs;


    public Unit(@Nullable FileObject source, Map<String, IUnitContrib> contribs) {
        this.contribs = contribs;
        this.source = source;
    }

    public Unit(@Nullable FileObject source) {
        this(source, new HashMap<String, IUnitContrib>());
    }

    public Unit() {
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
