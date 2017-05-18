package org.metaborg.core.testing;

import org.metaborg.util.log.ILogger;

import javax.annotation.Nullable;
import java.io.PrintStream;

/**
 * Used to report the status of tests.
 *
 * Derive from {@link TestReporterServiceBase} instead of this interface.
 * The base class performs some additional checks to ensure it is used correctly.
 *
 * Test frameworks, such as SPT, can report the status of the testing through a test reporter.
 * Different test reporters handle these status messages differently. For example, a test reporter
 * might simply write the test status to the standard output, whereas another test reporter
 * would report the test results in the IDE UI.
 */
public interface ITestReporterService {

    /**
     * A test session has started.
     *
     * After this method only calls to {@link #sessionFinished()},
     * {@link #testStarted(String)} and {@link #testSuiteStarted(String)} are allowed.
     */
    void sessionStarted();

    /**
     * The test session has finished.
     *
     * After this method no calls to the reporter are allowed.
     */
    void sessionFinished();

    /**
     * A test suite has started.
     *
     * After this method only calls to {@link #testSuiteStarted(String)},
     * {@link #testSuiteFinished(String)} and {@link #testStarted(String)}
     * are allowed. In other words, test suites can be nested.
     *
     * @param name The name of the test suite.
     */
    void testSuiteStarted(String name);

    /**
     * The test suite has finished.
     *
     * After this method only calls to {@link #sessionFinished()},
     * {@link #testStarted(String)} and {@link #testSuiteStarted(String)} are allowed.
     *
     * @param name The name of the test suite.
     */
    void testSuiteFinished(String name);

    /**
     * A test has started.
     *
     * After this method only calls to {@link #testPassed(String)},
     * {@link #testFailed(String, String, String)} and {@link #testIgnored(String, String)}
     * are allowed.
     *
     * @param name The name of the test.
     */
    void testStarted(String name);

    /**
     * The test has finished and failed.
     *
     * After this method only calls to {@link #sessionFinished()},
     * {@link #testStarted(String)} and {@link #testSuiteStarted(String)} are allowed.
     *
     * @param name The name of the test.
     * @param reason The reason the test failed; or null.
     * @param details The details about the failed test, such as additional messages and the stack trace; or null.
     */
    void testFailed(String name, @Nullable String reason, @Nullable String details);

    /**
     * The test has finished and passed.
     *
     * After this method only calls to {@link #sessionFinished()},
     * {@link #testStarted(String)} and {@link #testSuiteStarted(String)} are allowed.
     *
     * @param name The name of the test.
     */
    void testPassed(String name);

    /**
     * The test has finished and was ignored.
     *
     * After this method only calls to {@link #sessionFinished()},
     * {@link #testStarted(String)} and {@link #testSuiteStarted(String)} are allowed.
     *
     * @param name The name of the test.
     * @param reason The reason the test was ignored; or null.
     */
    void testIgnored(String name, @Nullable String reason);

    /**
     * Gets a logger to which the test output may be logged.
     *
     * @return A logger.
     */
    ILogger getLogger();

    /**
     * Gets a stream to which test output may be written.
     *
     * @return A stream.
     */
    PrintStream getOut();

    /**
     * Gets a stream to which test errors may be written.
     *
     * @return A stream.
     */
    PrintStream getErr();
}
