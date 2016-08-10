package org.metaborg.core.transform;

public class TransformConfig implements ITransformConfig {
    private final boolean dry;


    public TransformConfig() {
        this.dry = false;
    }

    public TransformConfig(boolean dry) {
        this.dry = dry;
    }


    @Override public boolean dryRun() {
        return dry;
    }
}
