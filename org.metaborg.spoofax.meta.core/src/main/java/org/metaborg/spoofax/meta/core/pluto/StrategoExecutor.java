package org.metaborg.spoofax.meta.core.pluto;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.metaborg.spoofax.meta.core.pluto.util.ResourceAgentTracker;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.StrategoExit;
import org.strategoxt.lang.Strategy;
import org.strategoxt.stratego_lib.dr_scope_all_end_0_0;
import org.strategoxt.stratego_lib.dr_scope_all_start_0_0;
import org.strategoxt.stratego_sdf.stratego_sdf;
import org.sugarj.common.Exec;
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

        boolean success = false;
        try {
            if(!silent) {
                Log.log.beginTask("Execute " + desc, Log.CORE);
            }
            context.setIOAgent(tracker.agent());
            dr_scope_all_start_0_0.instance.invoke(context, context.getFactory().makeTuple());
            context.invokeStrategyCLI(strat, desc, sargs.toArray(new String[sargs.size()]));
            success = true;
            return new ExecutionResult(true, tracker.stdout(), tracker.stderr());
        } catch(StrategoExit e) {
            if(e.getValue() == 0) {
                success = true;
                return new ExecutionResult(true, tracker.stdout(), tracker.stderr());
            }
            return new ExecutionResult(false, tracker.stdout(), tracker.stderr());
        } finally {
            dr_scope_all_end_0_0.instance.invoke(context, context.getFactory().makeTuple());
            if(!silent) {
                Log.log.endTask(success);
            }
        }
    }

    public static ExecutionResult runStratego(Context context, Strategy strat, String desc,
        ResourceAgentTracker tracker, IStrategoTerm current) {
        return runStratego(false, context, strat, desc, tracker, current);
    }

    public static ExecutionResult runStratego(boolean silent, Context context, Strategy strat, String desc,
        ResourceAgentTracker tracker, IStrategoTerm current) {
        boolean success = false;
        try {
            if(!silent) {
                Log.log.beginTask("Execute " + desc, Log.CORE);
            }
            context.setIOAgent(tracker.agent());
            dr_scope_all_start_0_0.instance.invoke(context, current);
            IStrategoTerm result = strat.invoke(context, current);
            success = true;
            return new ExecutionResult(result, tracker.stdout(), tracker.stderr());
        } catch(StrategoExit e) {
            return new ExecutionResult(false, tracker.stdout(), tracker.stderr());
        } finally {
            dr_scope_all_end_0_0.instance.invoke(context, current);
            if(!silent) {
                Log.log.endTask(success);
            }
        }
    }


    public static ExecutionResult runSdf2TableCLI(File sdf2tableExecutable, Object... args) throws IOException {
        File sdf2tableDir = sdf2tableExecutable.getParentFile();
        String sdf2tableFile = sdf2tableExecutable.getName();
        String[] sargs = new String[args.length + 1];
        sargs[0] = sdf2tableFile;
        for(int i = 0; i < args.length; i++) {
            sargs[i + 1] = args[i].toString();
        }

        boolean success = false;
        try {
            Log.log.beginTask("Execute sdf2table", Log.CORE);
            Exec.ExecutionResult result = Exec.run(sdf2tableDir, sargs);
            success = true;
            return new ExecutionResult(true, StringUtils.join(result.outMsgs, '\n'), StringUtils.join(result.errMsgs,
                '\n'));
        } finally {
            Log.log.endTask(success);
        }
    }


    private static Context strategoSdfContext;
    private static Context permissiveGrammarsContext;
    private static Context toolsContext;
    private static Context xtcContext;

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

    public static synchronized Context xtcContext() {
        if(xtcContext != null)
            return xtcContext;
        xtcContext = org.strategoxt.stratego_xtc.stratego_xtc.init();
        return xtcContext;
    }
}
