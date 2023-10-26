package org.metaborg.core.transform;

import jakarta.annotation.Nullable;

import org.metaborg.core.source.ISourceRegion;

public class TransformConfig implements ITransformConfig {
    private final @Nullable ISourceRegion selection;
    private final boolean dry;


    public TransformConfig(@Nullable ISourceRegion selection, boolean dry) {
        this.selection = selection;
        this.dry = dry;
    }

    public TransformConfig(boolean dry) {
        this.selection = null;
        this.dry = dry;
    }

    public TransformConfig(@Nullable ISourceRegion selection) {
        this.selection = selection;
        this.dry = false;
    }

    public TransformConfig() {
        this.selection = null;
        this.dry = false;
    }


    @Override public @Nullable ISourceRegion selection() {
        return selection;
    }

    @Override public boolean dryRun() {
        return dry;
    }
}
