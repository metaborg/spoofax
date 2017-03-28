package org.metaborg.spoofax.core.analysis.constraint;

import java.time.Duration;

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
    
    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Analysis time : "); sb.append(Duration.ofNanos(totalTime).toMillis() / 1000.0); sb.append("s\n");
        sb.append(" * collect    : "); sb.append(Duration.ofNanos(collectionTime).toMillis() / 1000.0); sb.append("s\n");
        sb.append(" * solve      : "); sb.append(Duration.ofNanos(solverTime).toMillis() / 1000.0); sb.append("s\n");
        sb.append(" * finalize   : "); sb.append(Duration.ofNanos(finalizeTime).toMillis() / 1000.0); sb.append("s\n");
        return sb.toString();
    }
    
}
