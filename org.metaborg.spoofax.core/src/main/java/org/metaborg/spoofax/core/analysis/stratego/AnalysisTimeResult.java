package org.metaborg.spoofax.core.analysis.stratego;

import java.io.Serializable;

public class AnalysisTimeResult implements Serializable {
    private static final long serialVersionUID = 79478894383818113L;

    public final long parse;
    public final long preTrans;
    public final long collect;
    public final long taskEval;
    public final long postTrans;
    public final long indexPersist;
    public final long taskPersist;


    public AnalysisTimeResult(long parse, long preTrans, long collect, long taskEval, long postTrans,
        long indexPersist, long taskPersist) {
        this.parse = parse;
        this.preTrans = preTrans;
        this.collect = collect;
        this.taskEval = taskEval;
        this.postTrans = postTrans;
        this.indexPersist = indexPersist;
        this.taskPersist = taskPersist;
    }

    public AnalysisTimeResult() {
        this(0, 0, 0, 0, 0, 0, 0);
    }
}
