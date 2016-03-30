package org.metaborg.core.build;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.analysis.IAnalyzeUnit;
import org.metaborg.core.analysis.IAnalyzeUnitUpdate;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.syntax.IParseUnit;
import org.metaborg.core.transform.ITransformUnit;

public interface IBuildOutputInternal<P extends IParseUnit, A extends IAnalyzeUnit, AU extends IAnalyzeUnitUpdate, T extends ITransformUnit<?>>
    extends IBuildOutput<P, A, AU, T> {
    void setState(BuildState state);

    void add(boolean success, Iterable<FileName> removedResources, Iterable<FileName> includedResources,
        Iterable<FileObject> changedResources, Iterable<P> parseResults, Iterable<A> analysisResults,
        Iterable<AU> analysisUpdates, Iterable<T> transformResults, Iterable<IMessage> extraMessages);
}
