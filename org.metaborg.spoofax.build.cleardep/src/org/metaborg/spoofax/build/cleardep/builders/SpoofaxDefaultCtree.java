package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.strategoxt.imp.metatooling.building.AntForceRefreshScheduler;
import org.sugarj.cleardep.CompilationUnit;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.SimpleMode;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.build.BuilderFactory;
import org.sugarj.cleardep.build.EmptyBuildInput;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.Log;
import org.sugarj.common.path.AbsolutePath;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class SpoofaxDefaultCtree extends Builder<SpoofaxBuildContext, EmptyBuildInput, SimpleCompilationUnit> {

	public static BuilderFactory<SpoofaxBuildContext, EmptyBuildInput, SimpleCompilationUnit, SpoofaxDefaultCtree> factory = new BuilderFactory<SpoofaxBuildContext, EmptyBuildInput, SimpleCompilationUnit, SpoofaxDefaultCtree>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 9103600927076616924L;

		@Override
		public SpoofaxDefaultCtree makeBuilder(SpoofaxBuildContext context) { return new SpoofaxDefaultCtree(context); }
	};
	
	private SpoofaxDefaultCtree(SpoofaxBuildContext context) {
		super(context, factory);
	}
	
	@Override
	protected String taskDescription(EmptyBuildInput input) {
		return null;
	}

	@Override
	protected Path persistentPath(EmptyBuildInput input) {
		return context.depPath("spoofaxDefault.dep");
	}
	
	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result, EmptyBuildInput input) throws IOException {
		checkClassPath();
		
		String sdfmodule = context.props.getOrFail("sdfmodule");
		String strmodule = context.props.getOrFail("strmodule");
		String esvmodule = context.props.getOrFail("esvmodule");
		String metasdfmodule = context.props.getOrFail("metasdfmodule");
		String buildSdfImports = context.props.getOrElse("build.sdf.imports", "");
		Path externaldef = context.props.isDefined("externaldef") ? new AbsolutePath(context.props.get("externaldef")) : null;
		Path externaljar = context.props.isDefined("externaljar") ? new AbsolutePath(context.props.get("externaljar")) : null;
		String externaljarflags = context.props.getOrElse("externaljarflags", "");
		
		CompilationUnit sdf2Table = context.sdf2Table.require(new Sdf2Table.Input(sdfmodule, buildSdfImports, externaldef), new SimpleMode());
		result.addModuleDependency(sdf2Table);
		
		CompilationUnit metaSdf2Table = context.metaSdf2Table.require(new MetaSdf2Table.Input(metasdfmodule, buildSdfImports, externaldef), new SimpleMode());
		result.addModuleDependency(metaSdf2Table);
		
		CompilationUnit ppGen = context.ppGen.require(null, new SimpleMode());
		result.addModuleDependency(ppGen);
		
		RelativePath ppPackInputPath = context.basePath("${syntax}/${sdfmodule}.pp");
		RelativePath ppPackOutputPath = context.basePath("${include}/${sdfmodule}.pp.af");
		CompilationUnit ppPack = context.ppPack.require(new PPPack.Input(ppPackInputPath, ppPackOutputPath, true), new SimpleMode());
		result.addModuleDependency(ppPack);
		
		CompilationUnit strategoAster = context.strategoAster.require(new StrategoAster.Input(strmodule), new SimpleMode());
		result.addModuleDependency(strategoAster);

		RequirableCompilationUnit forceOnSave = context.forceOnSave.requireLater(null, new SimpleMode());
		RequirableCompilationUnit sdf2Imp = context.sdf2ImpEclipse.requireLater(new Sdf2ImpEclipse.Input(esvmodule, sdfmodule, buildSdfImports), new SimpleMode());
		RequirableCompilationUnit sdf2Parenthesize = context.sdf2Parenthesize.requireLater(new Sdf2Parenthesize.Input(sdfmodule, buildSdfImports, externaldef), new SimpleMode());

		CompilationUnit strategoCtree = context.strategoCtree.require(
				new StrategoCtree.Input(
						sdfmodule, 
						buildSdfImports, 
						strmodule, 
						externaljar, 
						externaljarflags, 
						externaldef,
						new RequirableCompilationUnit[] {forceOnSave, sdf2Imp, sdf2Parenthesize}),
				new SimpleMode());
		result.addModuleDependency(strategoCtree);
		
		forceWorkspaceRefresh();
	}

	private void checkClassPath() {
		@SuppressWarnings("unused")
		org.strategoxt.imp.generator.sdf2imp c;
	}

	protected void forceWorkspaceRefresh() {
		try {
			AntForceRefreshScheduler.main(new String[] {context.basePath("${include}").getAbsolutePath()});
		} catch (Exception e) {
			Log.log.logErr(e.getMessage(), Log.CORE);
		}
	}
}
