package org.metaborg.core.testing;

import com.google.inject.Inject;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.time.StopWatch;
import org.metaborg.util.log.ILogger;

import javax.annotation.Nullable;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Test reporter that produces TeamCity test service messages.
 *
 * @see <a href="https://confluence.jetbrains.com/display/TCD10/Build+Script+Interaction+with+TeamCity">
 *     Build Script Interaction with TeamCity</a>
 */
public class TeamCityTestReporterService extends TestReporterServiceBase {

    private final TeamCityWriter writer;
    private final TeamCityLogger logger;

    /**
     * Stopwatch used to measure the time each test takes.
     */
    private StopWatch stopwatch = new StopWatch();

    private ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    private ByteArrayOutputStream errStream = new ByteArrayOutputStream();
    /**
     * The output stream.
     */
    private PrintStream out = new PrintStream(this.outStream);
    /**
     * The error stream.
     */
    private PrintStream err = new PrintStream(this.errStream);

    @Inject
    public TeamCityTestReporterService(TeamCityWriter writer, TeamCityLogger logger) {
        this.writer = writer;
        this.logger = logger;
    }

    @Override
    protected void onSessionStarted() {
        // Nothing to do.
    }

    @Override
    protected void onSessionFinished() {
        // Nothing to do.
    }

    @Override
    protected void onTestSuiteStarted(String name) {
        this.writer.send("testSuiteStarted",
                new TeamCityWriter.Attribute("name", name.trim())
        );
    }

    @Override
    protected void onTestSuiteFinished(String name) {
        this.writer.send("testSuiteFinished",
                new TeamCityWriter.Attribute("name", name.trim())
        );
    }

    @Override
    protected void onTestStarted(String name) {
        this.writer.send("testStarted",
                new TeamCityWriter.Attribute("name", name.trim()),
                new TeamCityWriter.Attribute("captureStandardOutput", false)
        );
        startStopwatch();
    }

    @Override
    protected void onTestFailed(String name, @Nullable String reason, @Nullable String details) {
        long duration = stopStopwatch();  // in ms

        printOutputs(name);
        this.writer.send("testFailed",
                new TeamCityWriter.Attribute("name", name.trim()),
                new TeamCityWriter.Attribute("message", reason),
                new TeamCityWriter.Attribute("details", details)
        );
        finishTest(name, duration);
    }

    @Override
    protected void onTestPassed(String name) {
        long duration = stopStopwatch();  // in ms

        printOutputs(name);
        finishTest(name, duration);
    }

    @Override
    protected void onTestIgnored(String name, @Nullable String reason) {
        long duration = stopStopwatch();  // in ms

        printOutputs(name);
        this.writer.send("testIgnored",
                new TeamCityWriter.Attribute("name", name.trim()),
                new TeamCityWriter.Attribute("message", reason)
        );
        finishTest(name, duration);
    }

    @Override
    public ILogger getLogger() {
        return this.logger;
    }

    @Override
    public PrintStream getOut() {
        return this.out;
    }

    @Override
    public PrintStream getErr() {
        return this.err;
    }

    /**
     * Starts the stopwatch, for measuring the time each test takes.
     */
    private void startStopwatch() {
        this.stopwatch.reset();
        this.stopwatch.start();
    }

    /**
     * Stops the stopwatch, and returns the measured time in milliseconds.
     *
     * @return The measured time in milliseconds.
     */
    private long stopStopwatch() {
        this.stopwatch.stop();
        return this.stopwatch.getTime();  // in ms
    }

    /**
     * Prints the standard output and standard error streams, if any.
     *
     * @param name The name of the test case.
     */
    private void printOutputs(String name) {
        String outData = this.outStream.toString(StandardCharsets.UTF_8);
        String errData = this.errStream.toString(StandardCharsets.UTF_8);
        this.outStream.reset();
        this.errStream.reset();

        if (!outData.isEmpty()) {
            this.writer.send("testStdOut",
                    new TeamCityWriter.Attribute("name", name.trim()),
                    new TeamCityWriter.Attribute("out", out)
            );
        }

        if (!errData.isEmpty()) {
            this.writer.send("testStdErr",
                    new TeamCityWriter.Attribute("name", name.trim()),
                    new TeamCityWriter.Attribute("out", err)
            );
        }
    }

    /**
     * Indicates that a test has finished (after passing, failing, or being ignored).
     *
     * @param name The name of the test case.
     * @param duration The duration of the test, in milliseconds.
     */
    private void finishTest(String name, long duration) {
        this.writer.send("testFinished",
                new TeamCityWriter.Attribute("name", name.trim()),
                new TeamCityWriter.Attribute("duration", duration)
        );
    }
}
