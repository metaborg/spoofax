package org.metaborg.spoofax.meta.core.pluto;

import java.util.ArrayList;
import java.util.List;

import org.metaborg.spoofax.meta.core.pluto.util.ResourceAgentTracker;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.StrategoExit;
import org.strategoxt.lang.Strategy;
import org.strategoxt.stratego_lib.dr_scope_all_end_0_0;
import org.strategoxt.stratego_lib.dr_scope_all_start_0_0;
import org.strategoxt.stratego_sdf.stratego_sdf;

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


    private static final ILogger log = LoggerUtils.logger("Build log");


    public static ExecutionResult runStrategoCLI(Context context, Strategy strat, String desc,
        ResourceAgentTracker tracker, Object... args) {
        return runStrategoCLI(false, context, strat, desc, tracker, args);
    }

    public static ExecutionResult runStrategoCLI(boolean silent, Context context, Strategy strat, String desc,
        ResourceAgentTracker tracker, Object... args) {
        List<String> sargs = new ArrayList<>(args.length);
        for(int i = 0; i < args.length; i++) {
            String str = args[i].toString();
            // XXX handle quoted paths to support spaces inside paths
            for(String s : str.split("[ \t\r\n]")) {
                if(!s.isEmpty()) {
                    sargs.add(s);
                }
            }
        }

        try {
            if(!silent) {
                log.info("Execute {}", desc);
            }
            context.setIOAgent(tracker.agent());
            dr_scope_all_start_0_0.instance.invoke(context, context.getFactory().makeTuple());
            context.invokeStrategyCLI(strat, desc, sargs.toArray(new String[sargs.size()]));
            return new ExecutionResult(true, tracker.stdout(), tracker.stderr());
        } catch(StrategoExit e) {
            if(e.getValue() == 0) {
                return new ExecutionResult(true, tracker.stdout(), tracker.stderr());
            }
            return new ExecutionResult(false, tracker.stdout(), tracker.stderr());
        } finally {
            dr_scope_all_end_0_0.instance.invoke(context, context.getFactory().makeTuple());
        }
    }

    public static ExecutionResult runStratego(Context context, Strategy strat, String desc,
        ResourceAgentTracker tracker, IStrategoTerm current) {
        return runStratego(false, context, strat, desc, tracker, current);
    }

    public static ExecutionResult runStratego(boolean silent, Context context, Strategy strat, String desc,
        ResourceAgentTracker tracker, IStrategoTerm current) {
        try {
            if(!silent) {
                log.info("Execute {}", desc);
            }
            context.setIOAgent(tracker.agent());
            dr_scope_all_start_0_0.instance.invoke(context, current);
            IStrategoTerm result = strat.invoke(context, current);
            return new ExecutionResult(result, tracker.stdout(), tracker.stderr());
        } catch(StrategoExit e) {
            return new ExecutionResult(false, tracker.stdout(), tracker.stderr());
        } finally {
            dr_scope_all_end_0_0.instance.invoke(context, current);
        }
    }


    private static Context strategoSdfContext;
    private static Context permissiveGrammarsContext;
    private static Context toolsContext;

    public static synchronized Context strategoSdfcontext() {
        if(strategoSdfContext == null)
            strategoSdfContext = stratego_sdf.init();
        return strategoSdfContext;
    }

    public static synchronized Context permissiveGrammarsContext() {
        if(permissiveGrammarsContext != null)
            return permissiveGrammarsContext;
        permissiveGrammarsContext = org.strategoxt.permissivegrammars.permissivegrammars.init();
        return permissiveGrammarsContext;
    }

    public static Context strjContext() {
        // strj requires a fresh context each time.
        return org.strategoxt.strj.strj.init();
    }

    public static synchronized Context toolsContext() {
        if(toolsContext != null)
            return toolsContext;
        toolsContext = org.strategoxt.tools.tools.init();
        return toolsContext;
    }
}
