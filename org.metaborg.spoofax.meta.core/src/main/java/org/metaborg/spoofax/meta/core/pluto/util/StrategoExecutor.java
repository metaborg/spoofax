package org.metaborg.spoofax.meta.core.pluto.util;

import java.util.List;

import jakarta.annotation.Nullable;

import org.metaborg.util.cmd.Arguments;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.library.IOperatorRegistry;
import org.spoofax.interpreter.library.ssl.SSLLibrary;
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

    private static Context strategoSdfContext;
    private static Context permissiveGrammarsContext;
    private static Context toolsContext;

    private Context context;
    private Strategy strategy;
    private String strategyName;
    private ResourceAgentTracker tracker;
    private String name;
    private boolean silent;


    public StrategoExecutor withContext(Context context) {
        this.context = context;
        return this;
    }

    public StrategoExecutor withSdfContext() {
        if(strategoSdfContext == null) {
            strategoSdfContext = stratego_sdf.init();
        }
        withContext(strategoSdfContext);
        return this;
    }

    public StrategoExecutor withPermissiveGrammarsContext() {
        if(permissiveGrammarsContext == null) {
            permissiveGrammarsContext = org.strategoxt.permissivegrammars.permissivegrammars.init();
        }
        withContext(permissiveGrammarsContext);
        return this;
    }

    public StrategoExecutor withToolsContext() {
        if(toolsContext == null) {
            toolsContext = org.strategoxt.tools.tools.init();
        }
        withContext(toolsContext);
        return this;
    }

    public StrategoExecutor withStrjContext() {
        // strj requires a fresh context each time.
        withContext(org.strategoxt.strj.strj.init());
        return this;
    }

    public StrategoExecutor withStrategy(Strategy strategy) {
        this.strategy = strategy;
        return this;
    }

    public StrategoExecutor withStrategyName(String strategyName) {
        this.strategyName = strategyName;
        return this;
    }

    public StrategoExecutor withTracker(ResourceAgentTracker tracker) {
        this.tracker = tracker;
        return this;
    }

    public StrategoExecutor withName(String name) {
        this.name = name;
        return this;
    }

    public StrategoExecutor setSilent(boolean silent) {
        this.silent = silent;
        return this;
    }


    public ExecutionResult executeCLI(Arguments arguments) {
        prepare();

        try {
            if(!silent) {
                log.info("Execute {} {}", name, arguments);
            }
            context.setIOAgent(tracker.agent());
            final String[] args = getArgumentStrings(arguments);
            if(strategy != null) {
                context.invokeStrategyCLI(strategy, name, args);
            } else {
                context.invokeStrategyCLI(strategyName, name, args);
            }
            return new ExecutionResult(true, tracker.stdout(), tracker.stderr());
        } catch(StrategoExit e) {
            if(e.getValue() == 0) {
                return new ExecutionResult(true, tracker.stdout(), tracker.stderr());
            }
            if(!silent) {
                log.error("Executing {} failed: {}", name, e);
            }
            return new ExecutionResult(false, tracker.stdout(), tracker.stderr());
        } finally {
            final @Nullable IOperatorRegistry registry = context.getOperatorRegistry(SSLLibrary.REGISTRY_NAME);
            if(registry != null) {
                final SSLLibrary sslLibrary = (SSLLibrary) registry;
                sslLibrary.getDynamicRuleTable().clear();
                sslLibrary.getTableTable().clear();
            }
        }
    }


    private void prepare() {
        if(context == null) {
            throw new RuntimeException("Cannot execute Stratego strategy; context was not set");
        }
        if(strategy == null && strategyName == null) {
            throw new RuntimeException("Cannot execute Stratego strategy; strategy or strategy name was not set");
        }
        if(tracker == null) {
            throw new RuntimeException("Cannot execute Stratego strategy; tracker was not set");
        }
        if(name == null) {
            name = strategy.getName();
        }
    }

    private String[] getArgumentStrings(Arguments arguments) {
        List<String> strings = arguments.asStrings(null);
        return strings.toArray(new String[strings.size()]);
    }
}
