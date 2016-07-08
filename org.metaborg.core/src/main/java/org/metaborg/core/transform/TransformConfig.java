package org.metaborg.core.transform;

public class TransformConfig implements ITransformConfig {

    private final boolean dry;

    /**
     * Default constructor.
     * 
     * Sets all configuration settings to their defaults.
     */
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
