package org.metaborg.spoofax.build.cleardep;

import java.util.ArrayList;
import java.util.List;

import org.strategoxt.lang.Context;
import org.strategoxt.lang.StrategoExit;
import org.strategoxt.lang.Strategy;

public class StrategoExecutor {

	public static class ExecutionResult {
		public final boolean success;
		public final String outLog;
		public final String errLog;

		public ExecutionResult(boolean success, String outLog, String errLog) {
			this.success = success;
			this.outLog = outLog;
			this.errLog = errLog;
		}
	}

	
	public static <S extends Strategy> ExecutionResult runStrategoCLI(Context strategoContext, S strat, String desc, LoggingFilteringIOAgent agent, Object... args) {
		List<String> sargs = new ArrayList<>(args.length);
		for (int i = 0; i < args.length; i++) {
			String str = args[i].toString();
			for (String s : str.split("[ \t\r\n]"))
				if (!s.isEmpty())
					sargs.add(s);
		}

		try {
			strategoContext.setIOAgent(agent);
			strategoContext.invokeStrategyCLI(strat, desc, sargs.toArray(new String[sargs.size()]));
			return new ExecutionResult(true, agent.getOutLog(), agent.getErrLog());
		} catch (StrategoExit e) {
			if (e.getValue() == 0)
				return new ExecutionResult(true, agent.getOutLog(), agent.getErrLog());
			return new ExecutionResult(false, agent.getOutLog(), agent.getErrLog());
		}
	}
}
