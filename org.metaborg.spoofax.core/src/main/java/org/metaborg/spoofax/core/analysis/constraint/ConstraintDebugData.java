package org.metaborg.spoofax.core.analysis.constraint;

public class ConstraintDebugData {
    public final long totalTime;
    public final long collectionTime;
    public final long solverTime;
    public final long finalizeTime;


    public ConstraintDebugData(long totalTime, long collectionTime, long solverTime, long finalizeTime) {
        this.totalTime = totalTime;
        this.collectionTime = collectionTime;
        this.solverTime = solverTime;
        this.finalizeTime = finalizeTime;
    }
}
