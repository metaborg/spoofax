package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.strategoxt.imp.metatooling.building.AntForceRefreshScheduler;
import org.sugarj.cleardep.CompilationUnit;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.SimpleMode;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.build.BuilderFactory;
import org.sugarj.cleardep.buildjava.JavaJar;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.FileCommands;
import org.sugarj.common.Log;
import org.sugarj.common.path.AbsolutePath;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class SpoofaxDefaultCtree extends Builder<SpoofaxBuildContext, Void, SimpleCompilationUnit> {

	public static BuilderFactory<SpoofaxBuildContext, Void, SimpleCompilationUnit, SpoofaxDefaultCtree> factory = new BuilderFactory<SpoofaxBuildContext, Void, SimpleCompilationUnit, SpoofaxDefaultCtree>() {
		@Override
		public SpoofaxDefaultCtree makeBuilder(SpoofaxBuildContext context) { return new SpoofaxDefaultCtree(context); }
	};
	
	public SpoofaxDefaultCtree(SpoofaxBuildContext context) {
		super(context);
	}
	
	@Override
	protected String taskDescription(Void input) {
		return null;
	}

	@Override
	protected Path persistentPath(Void input) {
		return context.depPath("spoofaxDefault.dep");
	}
	
	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result, Void input) throws IOException {
		String sdfmodule = context.props.getOrFail("sdfmodule");
		String strmodule = context.props.getOrFail("strmodule");
		String esvmodule = context.props.getOrFail("esvmodule");
		String metasdfmodule = context.props.getOrFail("metasdfmodule");
		String buildSdfImports = context.props.getOrElse("build.sdf.imports", "");
		Path externaldef = context.props.isDefined("externaldef") ? new AbsolutePath(context.props.get("externaldef")) : null;
		Path externaljar = context.props.isDefined("externaljar") ? new AbsolutePath(context.props.get("externaljar")) : null;
		String externaljarflags = context.props.getOrElse("externaljarflags", "");

		try {
			checkClassPath();
			
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
	
			// This dependency was discovered by cleardep, due to an implicit dependency on 'org.strategoxt.imp.editors.template/lib/editor-common.generated.str'.
			RequirableCompilationUnit sdf2Imp = context.sdf2ImpEclipse.requireLater(new Sdf2ImpEclipse.Input(esvmodule, sdfmodule, buildSdfImports), new SimpleMode());
			// This dependency was discovered by cleardep, due to an implicit dependency on 'org.strategoxt.imp.editors.template/include/TemplateLang-parenthesize.str'.
			RequirableCompilationUnit sdf2Parenthesize = context.sdf2Parenthesize.requireLater(new Sdf2Parenthesize.Input(sdfmodule, buildSdfImports, externaldef), new SimpleMode());
	
			CompilationUnit strategoCtree = context.strategoCtree.require(
					new StrategoCtree.Input(
							sdfmodule, 
							buildSdfImports, 
							strmodule, 
							externaljar, 
							externaljarflags, 
							externaldef,
							new RequirableCompilationUnit[] {sdf2Imp, sdf2Parenthesize}),
					new SimpleMode());
			result.addModuleDependency(strategoCtree);
			
			compileJavaCode(result);
	
			javaJar(result, strmodule);
		} finally {
			forceWorkspaceRefresh();
		}
	}


	private void compileJavaCode(SimpleCompilationUnit result) throws IOException {
		CompilationUnit copyUtils = context.copyUtils.require(null, new SimpleMode());
		result.addModuleDependency(copyUtils);
		
		Path targetDir = context.basePath("${build}");
		boolean debug = true;
		String sourceVersion = "1.7";
		String targetVersion = "1.7";
		
		String srcDirs = context.props.getOrElse("src-dirs", context.props.get("src-gen"));
		List<String> sourcePath = new ArrayList<>();
		for (String dir : srcDirs.split("[\\s]+"))
			sourcePath.add(dir);
		
		List<String> classPath = new ArrayList<>();
		classPath.add(context.props.getOrFail("eclipse.spoofaximp.strategominjar"));
		classPath.add(context.basePath("${src-gen}").getAbsolutePath());
		if (context.props.isDefined("externaljar"))
			classPath.add(context.props.get("externaljar"));
		if (context.props.isDefined("externaljarx"))
			classPath.add(context.props.get("externaljarx"));
		if (context.isJavaJarEnabled(result))
			classPath.add(context.basePath("${include}/${strmodule}-java.jar").getAbsolutePath());

		// TODO call JavaBuilder.require(..)
	}

	private void javaJar(SimpleCompilationUnit result, String strmodule) throws IOException {
		if (!context.isJavaJarEnabled(result))
			return;
		
		Path jarPath = context.basePath("${include}/" + strmodule + "-java.jar");
		JavaJar.Mode jarMode = FileCommands.exists(jarPath) ? JavaJar.Mode.Update : JavaJar.Mode.Create;
		CompilationUnit javaJar = JavaJar.factory.makeBuilder(context).require(
				new JavaJar.Input(
						jarMode,
						jarPath, 
						null,
						context.basePath("${build}"), 
						context.props.getOrElse("javajar-includes", "org/strategoxt/imp/editors/template/strategies/").split("[\\s]+"), 
						null), 
						new SimpleMode());
		result.addModuleDependency(javaJar);
	}
	
	private void checkClassPath() {
		@SuppressWarnings("unused")
		org.strategoxt.imp.generator.sdf2imp c;
	}

	protected void forceWorkspaceRefresh() {
		try {
			AntForceRefreshScheduler.main(new String[] {context.baseDir.getAbsolutePath()});
		} catch (Exception e) {
			Log.log.logErr(e.getMessage(), Log.CORE);
		}
	}
}
