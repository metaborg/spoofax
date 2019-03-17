package org.metaborg.spoofax.core.language;

import org.metaborg.core.action.ITransformGoal;
import org.metaborg.core.action.TransformActionFlags;
import org.metaborg.spoofax.core.action.JavaGeneratedTransformAction;
import org.metaborg.spoofax.core.action.JavaTransformAction;
import org.metaborg.spoofax.core.action.StrategoTransformAction;
import org.metaborg.spoofax.core.analysis.JavaAnalysisFacet;
import org.metaborg.spoofax.core.analysis.JavaGeneratedAnalysisFacet;
import org.metaborg.spoofax.core.analysis.StrategoAnalysisFacet;
import org.metaborg.spoofax.core.outline.JavaGeneratedOutlineFacet;
import org.metaborg.spoofax.core.outline.JavaOutlineFacet;
import org.metaborg.spoofax.core.outline.StrategoOutlineFacet;
import org.metaborg.spoofax.core.tracing.JavaGeneratedHoverFacet;
import org.metaborg.spoofax.core.tracing.JavaGeneratedResolverFacet;
import org.metaborg.spoofax.core.tracing.JavaHoverFacet;
import org.metaborg.spoofax.core.tracing.JavaResolverFacet;
import org.metaborg.spoofax.core.tracing.StrategoHoverFacet;
import org.metaborg.spoofax.core.tracing.StrategoResolverFacet;

import com.google.inject.assistedinject.Assisted;

public interface IFacetFactory {
    JavaGeneratedResolverFacet javaGeneratedResolverFacet();
    JavaResolverFacet javaResolverFacet(String javaClassName);
    StrategoResolverFacet strategoResolverFacet(String strategyName);

    JavaGeneratedHoverFacet javaGeneratedHoverFacet();
    JavaHoverFacet javaHoverFacet(String javaClassName);
    StrategoHoverFacet strategoHoverFacet(String strategyName);

    JavaGeneratedOutlineFacet javaGeneratedOutlineFacet(int expandTo);
    JavaOutlineFacet javaOutlineFacet(String javaClassName, int expandTo);
    StrategoOutlineFacet strategoOutlineFacet(String strategyName, int expandTo);

    JavaGeneratedTransformAction javaGeneratedTransformAction(ITransformGoal goal, TransformActionFlags flags);
    JavaTransformAction javaTransformAction(@Assisted("name") String name, ITransformGoal goal,
        TransformActionFlags flags, @Assisted("className") String className);
    StrategoTransformAction strategoTransformAction(@Assisted("name") String name, ITransformGoal goal,
        TransformActionFlags flags, @Assisted("strategy") String strategy);

    JavaGeneratedAnalysisFacet javaGeneratedAnalysisFacet();
    JavaAnalysisFacet javaAnalysisFacet(String name);
    StrategoAnalysisFacet strategoAnalysisFacet(String name);
}
