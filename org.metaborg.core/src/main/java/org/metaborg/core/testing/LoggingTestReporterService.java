package org.metaborg.core.testing;

import org.apache.commons.lang3.time.StopWatch;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import javax.annotation.Nullable;
import java.io.PrintStream;

/**
 * Test reporter that logs to the logger, standard out and standard error.
 *
 * If you want to change the default output of a test runner (e.g. SPT), this is the place to be.
 */
public final class LoggingTestReporterService extends TestReporterServiceBase {

    private static final ILogger logger = LoggerUtils.logger(LoggingTestReporterService.class);

    private int testPassedCounter = 0;
    private int testFailedCounter = 0;
    private int testIgnoredCounter = 0;
    private int totalTestPassedCounter = 0;
    private int totalTestFailedCounter = 0;
    private int totalTestIgnoredCounter = 0;

    @Override
    protected void onSessionStarted() {
        logger.info("running tests");
        this.totalTestPassedCounter = 0;
        this.totalTestFailedCounter = 0;
        this.totalTestIgnoredCounter = 0;
    }

    @Override
    protected void onSessionFinished() {
        TestResult result = this.totalTestFailedCounter > 0 ? TestResult.FAILED : TestResult.OK;
        logger.info("test result: {}. {} passed; {} failed; {} ignored",
                result, this.totalTestPassedCounter, this.totalTestFailedCounter, this.totalTestIgnoredCounter);
    }

    @Override
    protected void onTestSuiteStarted(String name) {
        logger.info("running test suite {}", name.trim());
        this.testPassedCounter = 0;
        this.testFailedCounter = 0;
        this.testIgnoredCounter = 0;
    }

    @Override
    protected void onTestSuiteFinished(String name) {
        TestResult result = this.testFailedCounter > 0 ? TestResult.FAILED : TestResult.OK;
        logger.info("test suite result: {}. {} passed; {} failed; {} ignored",
                result, this.testPassedCounter, this.testFailedCounter, this.testIgnoredCounter);
    }

    @Override
    protected void onTestStarted(String name) {
        logger.debug("test {} ...", name.trim());
    }

    @Override
    protected void onTestFailed(String name, @Nullable String reason, @Nullable String details) {
        this.testFailedCounter += 1;
        this.totalTestFailedCounter += 1;
        logger.info("test {} ... {}", name.trim(), TestResult.FAILED.toString());
    }

    @Override
    protected void onTestPassed(String name) {
        this.testPassedCounter += 1;
        this.totalTestPassedCounter += 1;
        logger.info("test {} ... {}", name.trim(), TestResult.OK.toString());
    }

    @Override
    protected void onTestIgnored(String name, @Nullable String reason) {
        this.testIgnoredCounter += 1;
        this.totalTestIgnoredCounter += 1;
        logger.info("test {} ... {}", name.trim(), TestResult.IGNORED.toString());
    }

    @Override
    public ILogger getLogger() {
        return logger;
    }

    @Override
    public PrintStream getOut() {
        return System.out;
    }

    @Override
    public PrintStream getErr() {
        return System.err;
    }

    /**
     * Specifies the test result.
     */
    private enum TestResult {
        OK,
        FAILED,
        IGNORED;


        @Override
        public String toString() {
            switch(this) {
                case OK: return "ok";
                case FAILED: return "FAILED";
                case IGNORED: return "ignored";
                default: throw new IllegalArgumentException();
            }
        }
    }
}
