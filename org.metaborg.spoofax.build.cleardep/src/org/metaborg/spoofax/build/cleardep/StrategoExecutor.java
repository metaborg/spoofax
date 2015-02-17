package org.metaborg.spoofax.build.cleardep;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.metatooling.stratego.SDFBundleCommand;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.StrategoExit;
import org.strategoxt.lang.Strategy;
import org.strategoxt.stratego_lib.dr_scope_all_end_0_0;
import org.strategoxt.stratego_lib.dr_scope_all_start_0_0;
import org.sugarj.common.ATermCommands;
import org.sugarj.common.Log;

public class StrategoExecutor {

	public static class ExecutionResult {
		public final boolean success;
		public final String outLog;
		public final String errLog;
		public final IStrategoTerm result;

		public ExecutionResult(boolean success, String outLog, String errLog) {
			this.success = success;
			this.outLog = outLog;
			this.errLog = errLog;
			this.result = null;
		}
		
		public ExecutionResult(IStrategoTerm result, String outLog, String errLog) {
			this.success = result != null;
			this.outLog = outLog;
			this.errLog = errLog;
			this.result = result;
		}
	}

	
	public static ExecutionResult runStrategoCLI(Context strategoContext, Strategy strat, String desc, LoggingFilteringIOAgent agent, Object... args) {
		List<String> sargs = new ArrayList<>(args.length);
		for (int i = 0; i < args.length; i++) {
			String str = args[i].toString();
			for (String s : str.split("[ \t\r\n]"))
				if (!s.isEmpty())
					sargs.add(s);
		}

		try {
			Log.log.beginTask("Execute " + desc, Log.CORE);
			strategoContext.setIOAgent(agent);
			dr_scope_all_start_0_0.instance.invoke(strategoContext, strategoContext.getFactory().makeTuple());
			strategoContext.invokeStrategyCLI(strat, desc, sargs.toArray(new String[sargs.size()]));
			return new ExecutionResult(true, agent.getOutLog(), agent.getErrLog());
		} catch (StrategoExit e) {
			if (e.getValue() == 0)
				return new ExecutionResult(true, agent.getOutLog(), agent.getErrLog());
			return new ExecutionResult(false, agent.getOutLog(), agent.getErrLog());
		} finally {
			dr_scope_all_end_0_0.instance.invoke(strategoContext, strategoContext.getFactory().makeTuple());
			Log.log.endTask();
		}
	}
	
	public static ExecutionResult runStratego(Context strategoContext, Strategy strat, String desc, LoggingFilteringIOAgent agent, IStrategoTerm current) {
		try {
			Log.log.beginTask("Execute " + desc, Log.CORE);
			strategoContext.setIOAgent(agent);
			dr_scope_all_start_0_0.instance.invoke(strategoContext, current);
			IStrategoTerm result  = strat.invoke(strategoContext, current);
			return new ExecutionResult(result, agent.getOutLog(), agent.getErrLog());
		} catch (StrategoExit e) {
			return new ExecutionResult(false, agent.getOutLog(), agent.getErrLog());
		} finally {
			dr_scope_all_end_0_0.instance.invoke(strategoContext, current);
			Log.log.endTask();
		}
	}
	
	public static ExecutionResult runSdf2TableCLI(Context xtcContext, Object... args) throws IOException {
		List<IStrategoTerm> tArgs = new ArrayList<>(args.length);
		for (int i = 0; i < args.length; i++) {
			String str = args[i].toString();
			for (String s : str.split("[ \t\r\n]"))
				if (!s.isEmpty())
					tArgs.add(ATermCommands.makeString(s));
		}

		LoggingFilteringIOAgent agent = new LoggingFilteringIOAgent(Pattern.quote("Invoking native tool") + ".*");
		try {
			Log.log.beginTask("Execute sdf2table", Log.CORE);
			xtcContext.setIOAgent(agent);
			SDFBundleCommand.getInstance().init();
			SDFBundleCommand.getInstance().invoke(xtcContext, "sdf2table", tArgs.toArray(new IStrategoTerm[tArgs.size()]));
			return new ExecutionResult(true, agent.getOutLog(), agent.getErrLog());
		} catch (StrategoExit e) {
			if (e.getValue() == 0)
				return new ExecutionResult(true, agent.getOutLog(), agent.getErrLog());
			return new ExecutionResult(false, agent.getOutLog(), agent.getErrLog());
		} finally {
			Log.log.endTask();
		}
	}
}
